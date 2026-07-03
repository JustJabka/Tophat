package justjabka.datapack_utils.registries;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import justjabka.datapack_utils.DatapackUtils;
import justjabka.datapack_utils.contents.command.*;
import justjabka.datapack_utils.contents.command.sub.RaycastSubCommand;
import justjabka.datapack_utils.contents.command.sub.RealTimeSubCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;

public class DatapackUtilsCommands {
    public static void initialize() {
        DatapackUtils.LOGGER.info("Initializing Commands");
        registerCommands();
        registerSubCommands();
    }

    private static void registerCommands() {
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
        });
    }

    private static void registerSubCommands() {
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
