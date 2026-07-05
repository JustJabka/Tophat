package justjabka.tophat.contents.command.sub;

import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class RealTimeSubCommand {
    private static final DateTimeFormatter TIME_FORMAT_MESSAGE = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);
    private static final DateTimeFormatter TIME_FORMAT_RETURN = DateTimeFormatter.ofPattern("HHmm", Locale.ROOT);

    public static LiteralCommandNode<CommandSourceStack> register() {
        return Commands
                .literal("realtime")
                .executes(context -> queryTime(context.getSource()))
                .then(Commands.literal("day")
                        .executes(context -> queryDay(context.getSource()))
                )
                .build();

    }

    private static int queryDay(CommandSourceStack source) {
        DayOfWeek day = getDateTime().getDayOfWeek();
        String dayFormatted = day.getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        source.sendSuccess(() -> Component.literal("The real day is %s".formatted(dayFormatted)), true);
        return day.getValue();
    }

    private static int queryTime(CommandSourceStack source) {
        int time = Integer.parseInt(TIME_FORMAT_RETURN.format(getDateTime()));
        String timeFormatted = TIME_FORMAT_MESSAGE.format(getDateTime());

        source.sendSuccess(() -> Component.literal("The real time is %s".formatted(timeFormatted)), true);

        return time;
    }

    private static ZonedDateTime getDateTime() {
        return ZonedDateTime.now();
    }
}
