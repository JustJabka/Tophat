package justjabka.tophat.registries;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import justjabka.tophat.Tophat;
import justjabka.tophat.contents.command.*;
import justjabka.tophat.contents.command.sub.RaycastSubCommand;
import justjabka.tophat.contents.command.sub.RealTimeSubCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;

public class TophatCommands {
    public static void initialize() {
        registerCommands();
        registerSubCommands();
    }

    private static void registerCommands() {
        Tophat.LOGGER.info("Initializing Commands");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(MotionCommand.register());
            dispatcher.register(GuiCommand.register(registryAccess));
            dispatcher.register(EvalCommand.register());
            dispatcher.register(HealCommand.register());
            dispatcher.register(IgniteCommand.register());
            dispatcher.register(FreezeCommand.register());
            dispatcher.register(AirCommand.register());
            dispatcher.register(FoodCommand.register());
            dispatcher.register(ProvokeCommand.register());
            dispatcher.register(FindBlockCommand.register(registryAccess));
        });
    }

    private static void registerSubCommands() {
        Tophat.LOGGER.info("Initializing Sub-Commands");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandNode<CommandSourceStack> executeNode = dispatcher.getRoot().getChild("execute");
            CommandNode<CommandSourceStack> timeQueryNode = dispatcher.getRoot().getChild("time").getChild("query");

            if (executeNode instanceof LiteralCommandNode<CommandSourceStack> vanillaExecute) {
                LiteralCommandNode<CommandSourceStack> raycastNode = RaycastSubCommand.register(vanillaExecute);
                vanillaExecute.addChild(raycastNode);
            }

            if (timeQueryNode instanceof LiteralCommandNode<CommandSourceStack> vanillaTimeQuery) {
                LiteralCommandNode<CommandSourceStack> realTimeNode = RealTimeSubCommand.register();
                vanillaTimeQuery.addChild(realTimeNode);
            }
        });
    }
}
