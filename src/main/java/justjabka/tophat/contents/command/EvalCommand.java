package justjabka.tophat.contents.command;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EvalCommand {
    private static final DynamicCommandExceptionType ERROR_INVALID_EXPRESSION = new DynamicCommandExceptionType(
            obj -> Component.literal("Invalid expression: " + obj)
    );
    private static final SimpleCommandExceptionType ERROR_ARGUMENT_NOT_COMPOUND = new SimpleCommandExceptionType(
            Component.literal("Expected compound tag")
    );

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        LiteralArgumentBuilder<CommandSourceStack> withNode = Commands.literal("with");

        for (DataCommands.DataProvider provider : DataCommands.SOURCE_PROVIDERS) {
            provider.wrap(withNode, builder -> builder
                    // All storage as argument
                    .executes(context -> {
                        CompoundTag compoundTag = provider.access(context).getData();
                        return calcExpressionWithArguments(context, compoundTag);
                    })
                    // Storage with path as argument
                    .then(Commands.argument("path", NbtPathArgument.nbtPath())
                            .executes(context -> {
                                CompoundTag compoundTag = getArgumentTag(NbtPathArgument.getPath(context, "path"), provider.access(context));
                                return calcExpressionWithArguments(context, compoundTag);
                            })
                    )
            );
        }

        return Commands.literal("eval")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("expression", StringArgumentType.string())
                        .executes(context -> calcExpression(
                                context.getSource(),
                                StringArgumentType.getString(context, "expression"),
                                1,
                                Map.of()
                        ))
                        .then(Commands.argument("scale", DoubleArgumentType.doubleArg())
                                .executes(context -> calcExpression(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "expression"),
                                        DoubleArgumentType.getDouble(context, "scale"),
                                        Map.of()
                                ))
                                .then(Commands.argument("arguments", CompoundTagArgument.compoundTag())
                                        .executes(context -> calcExpressionWithArguments(
                                                context,
                                                CompoundTagArgument.getCompoundTag(context, "arguments")
                                        ))
                                ).then(withNode)
                        )
                );
    }

    private static int calcExpression(CommandSourceStack source, String expressionString, double scale, Map<String, Double> arguments) throws CommandSyntaxException {
        Expression expression = new Expression(expressionString).withValues(arguments);
        return finallyCalcExpression(source, expression, scale);
    }

    private static int calcExpressionWithArguments(CommandContext<CommandSourceStack> context, CompoundTag compoundTag) throws CommandSyntaxException {
        String expression = StringArgumentType.getString(context, "expression");
        double scale = DoubleArgumentType.getDouble(context, "scale");
        Map<String, Double> arguments = convertCompoundToMap(compoundTag);

        return calcExpression(context.getSource(), expression, scale, arguments);
    }

    private static int finallyCalcExpression(CommandSourceStack source, Expression expression, double scale) throws CommandSyntaxException {
        try {
            double result = expression.evaluate().getNumberValue().doubleValue();
            double scaledResult = result * scale;
            int roundedResult = (int) Math.round(scaledResult);

            source.sendSuccess(() -> Component.literal("Expression result: %s".formatted(roundedResult)), true);
            return roundedResult;
        } catch (EvaluationException | ParseException e) {
            throw ERROR_INVALID_EXPRESSION.create(e.getMessage());
        }
    }

    private static CompoundTag getArgumentTag(NbtPathArgument.NbtPath path, DataAccessor accessor) throws CommandSyntaxException {
        List<Tag> tags = path.get(accessor.getData());
        if (tags.isEmpty()) {
            return new CompoundTag();
        }

        Tag tag = tags.getFirst();

        if (tag instanceof CompoundTag compoundTag) {
            return compoundTag;
        } else {
            throw ERROR_ARGUMENT_NOT_COMPOUND.create();
        }
    }

    private static Map<String, Double> convertCompoundToMap(CompoundTag compoundTag) {
        Map<String, Double> arguments = new HashMap<>();

        for (String key : compoundTag.keySet()) {
            Tag tag = compoundTag.get(key);
            if (tag == null) continue;

            Optional<Number> tagAsNumber = tag.asNumber();
            tagAsNumber.ifPresent(number -> arguments.put(key, number.doubleValue()));
        }

        return arguments;
    }
}
