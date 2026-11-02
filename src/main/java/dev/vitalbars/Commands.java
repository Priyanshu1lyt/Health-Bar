package dev.vitalbars;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public final class Commands {
    private Commands() {}

    private static int say(FabricClientCommandSource src, String s) {
        src.sendFeedback(Text.literal("[VitalBars] " + s));
        return 1;
    }

    private static String onOff(boolean b) {
        return b ? "ON" : "OFF";
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> d) {
        d.register(ClientCommandManager.literal("vitalbars")
                .then(ClientCommandManager.literal("toggle").executes(c -> {
                    Config.enabled = !Config.enabled;
                    return say(c.getSource(), "Bars " + onOff(Config.enabled));
                }))
                .then(ClientCommandManager.literal("damaged").executes(c -> {
                    Config.onlyDamaged = !Config.onlyDamaged;
                    return say(c.getSource(), "Only damaged: " + onOff(Config.onlyDamaged));
                }))
                .then(ClientCommandManager.literal("names").executes(c -> {
                    Config.showNames = !Config.showNames;
                    return say(c.getSource(), "Names: " + onOff(Config.showNames));
                }))
                .then(ClientCommandManager.literal("players").executes(c -> {
                    Config.showPlayers = !Config.showPlayers;
                    return say(c.getSource(), "Players: " + onOff(Config.showPlayers));
                }))
                .then(ClientCommandManager.literal("range")
                        .then(ClientCommandManager.argument("blocks", IntegerArgumentType.integer(4, 64))
                                .executes(c -> {
                                    Config.range = IntegerArgumentType.getInteger(c, "blocks");
                                    return say(c.getSource(), "Range: " + Config.range + " blocks");
                                }))));
    }
}
