package dev.vitalbars;

import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class BarRenderer {
    private static final Identifier CONTAINER = Identifier.ofVanilla("hud/heart/container");
    private static final Identifier FULL = Identifier.ofVanilla("hud/heart/full");
    private static final Identifier HALF = Identifier.ofVanilla("hud/heart/half");
    private static final Identifier ABS_FULL = Identifier.ofVanilla("hud/heart/absorbing_full");
    private static final Identifier ABS_HALF = Identifier.ofVanilla("hud/heart/absorbing_half");

    private BarRenderer() {}

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!Config.enabled || mc.player == null || mc.world == null) return;
        if (mc.options.hudHidden || mc.currentScreen != null) return;
        if (!mc.options.getPerspective().isFirstPerson()) return;

        float td = tickCounter.getTickProgress(false);
        ClientPlayerEntity p = mc.player;
        Vec3d cam = p.getCameraPosVec(td);

        double yaw = Math.toRadians(p.getYaw(td));
        double pitch = Math.toRadians(p.getPitch(td));
        double sy = Math.sin(yaw), cy = Math.cos(yaw);
        double sp = Math.sin(pitch), cp = Math.cos(pitch);
        Vec3d fwd = new Vec3d(-sy * cp, -sp, cy * cp);
        Vec3d right = new Vec3d(-cy, 0, -sy);
        Vec3d up = new Vec3d(-sy * sp, cp, cy * sp);

        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();
        double tanHalf = Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0);
        double aspect = (double) w / (double) h;
        double maxSq = (double) Config.range * Config.range;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || e == p || e instanceof ArmorStandEntity) continue;
            if (!le.isAlive() || le.isInvisible()) continue;
            if (le instanceof PlayerEntity && !Config.showPlayers) continue;
            if (p.squaredDistanceTo(le) > maxSq) continue;

            float hp = le.getHealth();
            float max = Math.max(1.0f, le.getMaxHealth());
            float abs = le.getAbsorptionAmount();
            if (Config.onlyDamaged && hp >= max && abs <= 0) continue;

            Vec3d pos = le.getLerpedPos(td).add(0, le.getHeight() + 0.45, 0);
            Vec3d d = pos.subtract(cam);
            double depth = d.dotProduct(fwd);
            if (depth < 0.2) continue;

            double nx = d.dotProduct(right) / depth / (tanHalf * aspect);
            double ny = d.dotProduct(up) / depth / tanHalf;
            int x = (int) Math.round(w / 2.0 + nx * w / 2.0);
            int y = (int) Math.round(h / 2.0 - ny * h / 2.0);
            if (x < -120 || x > w + 120 || y < -60 || y > h + 30) continue;

            if (!p.canSee(le)) continue;

            double dist = Math.sqrt(d.lengthSquared());
            int total = (int) Math.ceil(max / 2.0f);
            if (Config.hearts && total <= 30) {
                drawHearts(ctx, mc, le, x, y, hp, total, abs, dist);
            } else {
                drawBar(ctx, mc, le, x, y, hp, max, abs, dist);
            }
        }
    }

    private static void drawHearts(DrawContext ctx, MinecraftClient mc, LivingEntity le, int cx, int y,
                                   float hp, int total, float abs, double dist) {
        int s = (int) Math.max(5, Math.min(9, 10 - dist * 0.15));
        int step = s - 1;
        int cols = Math.min(10, total);
        int rows = (total + 9) / 10;
        int startX = cx - (cols * step + 1) / 2;
        int halves = Math.max(0, Math.round(hp));

        for (int i = 0; i < total; i++) {
            int px = startX + (i % 10) * step;
            int py = y - (i / 10) * step;
            ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, CONTAINER, px, py, s, s);
            if (halves >= 2 * (i + 1)) {
                ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, FULL, px, py, s, s);
            } else if (halves == 2 * i + 1) {
                ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HALF, px, py, s, s);
            }
        }

        int absHalves = Math.min(20, Math.round(abs));
        int absHearts = (absHalves + 1) / 2;
        for (int i = 0; i < absHearts; i++) {
            int px = startX + (i % 10) * step;
            int py = y - rows * step;
            Identifier id = (absHalves >= 2 * (i + 1)) ? ABS_FULL : ABS_HALF;
            ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, id, px, py, s, s);
        }

        if (Config.showNames) {
            int topRows = rows + (absHearts > 0 ? 1 : 0);
            int ny = y - topRows * step - 9;
            ctx.drawCenteredTextWithShadow(mc.textRenderer, le.getDisplayName(), cx, ny, 0xFFFFFFFF);
        }
    }

    private static void drawBar(DrawContext ctx, MinecraftClient mc, LivingEntity le, int cx, int y,
                                float hp, float max, float abs, double dist) {
        int bw = (int) Math.max(26, Math.min(64, 64 - dist * 1.1));
        int bh = 5;
        int x0 = cx - bw / 2;

        float frac = Math.max(0f, Math.min(1f, hp / max));
        int color = Color.HSBtoRGB(frac * 0.33f, 0.85f, 1.0f) | 0xFF000000;

        ctx.fill(x0 - 1, y - 1, x0 + bw + 1, y + bh + 1, 0xB0000000);
        ctx.fill(x0, y, x0 + bw, y + bh, 0xFF3A3A3A);
        ctx.fill(x0, y, x0 + (int) (bw * frac), y + bh, color);

        if (abs > 0) {
            int aw = (int) (bw * Math.min(1f, abs / max));
            ctx.fill(x0, y + bh - 2, x0 + aw, y + bh, 0xFFFFD700);
        }

        String nums = String.format("%.1f / %.0f", hp, max);
        if (abs > 0) nums = nums + String.format(" (+%.0f)", abs);
        ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(nums), cx, y + bh + 3, 0xFFFFFFFF);

        if (Config.showNames) {
            ctx.drawCenteredTextWithShadow(mc.textRenderer, le.getDisplayName(), cx, y - 11, 0xFFFFFFFF);
        }
    }
}
