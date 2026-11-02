package dev.vitalbars;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.util.Identifier;

public class VitalBarsClient implements ClientModInitializer {
    public static final String MOD_ID = "vitalbars";

    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of(MOD_ID, "bars"),
                BarRenderer::render);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> Commands.register(dispatcher));
    }
}
