package justjabka.tophat.contents.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public class FreezeCommand {
    private static final SimpleCommandExceptionType TARGET_IMMUNE_TO_FREEZING = new SimpleCommandExceptionType(Component.literal("Target is immune to freezing"));

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("freeze")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("target", EntityArgument.entity())
                        .executes(context -> freeze(
                                context.getSource(),
                                EntityArgument.getEntity(context, "target"),
                                null,
                                false
                        ))
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(Short.MIN_VALUE, Short.MAX_VALUE))
                                .executes(context -> freeze(
                                        context.getSource(),
                                        EntityArgument.getEntity(context, "target"),
                                        IntegerArgumentType.getInteger(context, "ticks"),
                                        false
                                ))
                                .then(Commands.argument("bypass_immunity", BoolArgumentType.bool())
                                        .executes(context -> freeze(
                                                context.getSource(),
                                                EntityArgument.getEntity(context, "target"),
                                                IntegerArgumentType.getInteger(context, "ticks"),
                                                BoolArgumentType.getBool(context, "bypass_immunity")
                                        ))
                                )
                        )
                );
    }

    private static int freeze(CommandSourceStack source, Entity entity, @Nullable Integer ticks, boolean bypassImmunity) throws CommandSyntaxException {
        if (!entity.canFreeze() && !bypassImmunity) throw TARGET_IMMUNE_TO_FREEZING.create();

        int tickToFreeze = ticks != null ? ticks : entity.getTicksRequiredToFreeze();
        entity.setTicksFrozen(tickToFreeze);

        if (tickToFreeze > 0) {
            entity.clearFire();
        }

        source.sendSuccess(() -> Component.literal("Froze %s for %s ticks".formatted(entity.getDisplayName().getString(), tickToFreeze)), true);
        return Command.SINGLE_SUCCESS;
    }
}
