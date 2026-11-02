package dev.vitalbars;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class VitalBarsClient implements ClientModInitializer {
    public static final String MOD_ID = "vitalbars";

    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of(MOD_ID, "bars"),
                BarRenderer::render);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> Commands.register(dispatcher));

        // Custom keybind: default M, can be changed in Options > Controls > Key Binds
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.vitalbars.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                KeyBinding.Category.create(Identifier.of(MOD_ID, "main"))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                Config.enabled = !Config.enabled;
                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal("VitalBars: " + (Config.enabled ? "ON" : "OFF")), true);
                }
            }
        });
    }
}
