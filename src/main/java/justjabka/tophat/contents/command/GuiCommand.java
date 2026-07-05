package justjabka.tophat.contents.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import justjabka.tophat.types.VirtualMenu;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class GuiCommand {
    private static final SimpleCommandExceptionType ERROR_NO_MENU_OPENED = new SimpleCommandExceptionType(Component.literal("Nothing changed. Target don't have opened menu"));
    private static final SimpleCommandExceptionType ERROR_MENU_OPENED = new SimpleCommandExceptionType(Component.literal("Can't open menu on top of another"));

    public static LiteralArgumentBuilder<CommandSourceStack> register(final CommandBuildContext buildContext) {
        return Commands.literal("gui")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("close")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> closeMenu(
                                        context.getSource(),
                                        EntityArgument.getPlayer(context, "target"),
                                        null
                                ))
                                .then(Commands.argument("menu", ResourceArgument.resource(buildContext, Registries.MENU))
                                        .executes(context -> closeMenu(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "target"),
                                                getMenuType(context)
                                        ))
                                )
                        )
                )
                .then(Commands.literal("open")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("menu", ResourceArgument.resource(buildContext, Registries.MENU))
                                        .executes(context -> openMenu(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "target"),
                                                getMenuType(context),
                                                Component.empty(),
                                                false
                                        ))
                                        .then(Commands.argument("name", ComponentArgument.textComponent(buildContext))
                                                .executes(context -> openMenu(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "target"),
                                                        getMenuType(context),
                                                        ComponentArgument.getResolvedComponent(context, "name"),
                                                        false
                                                ))
                                                .then(Commands.argument("force", BoolArgumentType.bool())
                                                        .executes(context -> openMenu(
                                                                context.getSource(),
                                                                EntityArgument.getPlayer(context, "target"),
                                                                getMenuType(context),
                                                                ComponentArgument.getResolvedComponent(context, "name"),
                                                                BoolArgumentType.getBool(context, "force")
                                                        ))
                                                )
                                        )
                                )
                        )
                );
    }

    private static MenuType<?> getMenuType(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return ResourceArgument.getResource(context, "menu", Registries.MENU).value();
    }

    private static int closeMenu(final CommandSourceStack source, ServerPlayer player, @Nullable MenuType<?> menu) throws CommandSyntaxException {
        AbstractContainerMenu containerMenu = player.containerMenu;

        if (menu == null) {
            player.closeContainer();
            return Command.SINGLE_SUCCESS;
        }

        try {
            MenuType<?> playerMenu = containerMenu.getType();
            if (!playerMenu.equals(menu)) return 0;

            player.closeContainer();
        } catch (UnsupportedOperationException e) {
            throw ERROR_NO_MENU_OPENED.create();
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int openMenu(final CommandSourceStack source, ServerPlayer player, @NotNull MenuType<?> menu, Component name, boolean force) throws CommandSyntaxException {
        AbstractContainerMenu containerMenu = player.containerMenu;

        if (containerMenu != player.inventoryMenu && !force) throw ERROR_MENU_OPENED.create();

        SimpleMenuProvider menuProvider = new SimpleMenuProvider(
                (containerId, inventory, plr) -> {
                    AbstractContainerMenu createdMenu = menu.create(containerId, inventory);
                    ((VirtualMenu) createdMenu).tophat$setVirtual(true);

                    return createdMenu;
                },
                name
        );

        player.openMenu(menuProvider);

        source.sendSuccess(() -> Component.literal("Successfully opened menu for %s".formatted(player.getDisplayName().getString())), true);
        return Command.SINGLE_SUCCESS;
    }
}