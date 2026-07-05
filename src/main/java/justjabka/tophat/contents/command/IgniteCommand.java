package justjabka.tophat.contents.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class IgniteCommand {
    private static final SimpleCommandExceptionType TARGET_IMMUNE_TO_FIRE = new SimpleCommandExceptionType(Component.literal("Target is immune to fire"));

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("ignite")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("target", EntityArgument.entity())
                        .executes(context -> ignite(
                                context.getSource(),
                                EntityArgument.getEntity(context, "target"),
                                20
                        ))
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(Short.MIN_VALUE, Short.MAX_VALUE))
                                .executes(context -> ignite(
                                        context.getSource(),
                                        EntityArgument.getEntity(context, "target"),
                                        IntegerArgumentType.getInteger(context, "ticks")
                                ))
                        )
                );
    }

    private static int ignite(CommandSourceStack source, Entity entity, int ticks) throws CommandSyntaxException {
        if (entity.fireImmune()) throw TARGET_IMMUNE_TO_FIRE.create();

        entity.setRemainingFireTicks(ticks);

        if (ticks > 0) {
            entity.clearFreeze();
        }

        source.sendSuccess(() -> Component.literal("Ignited %s for %s ticks".formatted(entity.getDisplayName().getString(), ticks)), true);
        return Command.SINGLE_SUCCESS;
    }
}
