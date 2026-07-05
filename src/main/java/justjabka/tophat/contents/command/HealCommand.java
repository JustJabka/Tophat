package justjabka.tophat.contents.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;


public class HealCommand {
    private static final SimpleCommandExceptionType ERROR_NOT_LIVING = new SimpleCommandExceptionType(Component.literal("Target don't have health"));

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("heal")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("target", EntityArgument.entity())
                        .executes(context -> heal(
                                context.getSource(),
                                EntityArgument.getEntity(context, "target"),
                                null
                        ))
                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                .executes(context -> heal(
                                        context.getSource(),
                                        EntityArgument.getEntity(context, "target"),
                                        FloatArgumentType.getFloat(context, "amount")
                                ))
                        )
                );
    }

    private static int heal(CommandSourceStack source, Entity entity, @Nullable Float amount) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity livingEntity)) throw ERROR_NOT_LIVING.create();

        float amountToHeal = amount != null ? amount : livingEntity.getMaxHealth();
        livingEntity.heal(amountToHeal);

        source.sendSuccess(() -> Component.literal("Applied %s health to %s".formatted(amountToHeal, livingEntity.getDisplayName().getString())), true);
        return Command.SINGLE_SUCCESS;
    }
}
