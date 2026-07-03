package justjabka.datapack_utils.contents.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.function.Predicate;

public class FindBlockCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register(final CommandBuildContext buildContext) {
        LiteralArgumentBuilder<CommandSourceStack> storeNode = Commands.literal("store");

        for (DataCommands.DataProvider provider : DataCommands.TARGET_PROVIDERS) {
            provider.wrap(storeNode, builder -> builder
                    // Storage with path as argument
                    .then(Commands.argument("path", NbtPathArgument.nbtPath())
                            .executes(context -> {
                                NbtPathArgument.NbtPath path = NbtPathArgument.getPath(context, "path");
                                return storeAllBlocks(
                                        context.getSource(),
                                        BlockPosArgument.getLoadedBlockPos(context, "from"),
                                        BlockPosArgument.getLoadedBlockPos(context, "to"),
                                        BlockPredicateArgument.getBlockPredicate(context, "block"),
                                        path,
                                        provider.access(context)
                                );
                            })
                    )
            );
        }

        return Commands.literal("findblock")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("from", BlockPosArgument.blockPos())
                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                                .then(Commands.argument("block", BlockPredicateArgument.blockPredicate(buildContext))
                                        .executes(context -> findFirstBlock(
                                                context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "from"),
                                                BlockPosArgument.getLoadedBlockPos(context, "to"),
                                                BlockPredicateArgument.getBlockPredicate(context, "block")
                                        ))
                                        .then(storeNode)
                                )
                        )
                );
    }

    private static int findFirstBlock(CommandSourceStack source, BlockPos from, BlockPos to, Predicate<BlockInWorld> filter) {
        ServerLevel level = source.getLevel();

        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            if (!filter.test(new BlockInWorld(level, pos.immutable(), true))) continue;

            BlockState blockState = level.getBlockState(pos);

            source.sendSuccess(() -> Component.literal("Found %s at %s %s %s".formatted(
                    blockState.getBlock().getName().getString(),
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
            )), true);
            return Command.SINGLE_SUCCESS;
        }

        return 0;
    }

    private static int storeAllBlocks(CommandSourceStack source, BlockPos from, BlockPos to, Predicate<BlockInWorld> filter, NbtPathArgument.NbtPath path, DataAccessor accessor) throws CommandSyntaxException {
        ServerLevel level = source.getLevel();

        ListTag listTag = new ListTag();
        int count = 0;

        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            if (!filter.test(new BlockInWorld(level, pos.immutable(), true))) continue;

            BlockState blockState = level.getBlockState(pos);

            saveBlockData(pos, blockState, listTag);
            count++;
        }

        if (count == 0) return 0;

        // Update accessor
        CompoundTag rootTag = accessor.getData();
        path.set(rootTag, listTag);
        accessor.setData(rootTag);

        int finalCount = count;
        source.sendSuccess(() -> Component.literal("Found %s blocks(s) in the area".formatted(finalCount)), true);
        return finalCount;
    }

    private static void saveBlockData(BlockPos pos, BlockState blockState, ListTag listTag) {
        CompoundTag blockEntry = new CompoundTag();

        String blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();
        blockEntry.putString("id", blockId);

        blockEntry.putInt("x", pos.getX());
        blockEntry.putInt("y", pos.getY());
        blockEntry.putInt("z", pos.getZ());

        listTag.add(blockEntry);
    }
}
