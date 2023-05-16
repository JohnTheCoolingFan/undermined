package ru.jtcf.undermined.data.client;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import ru.jtcf.undermined.UnderMined;

import java.util.Map;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen, UnderMined.MODID, existingFileHelper);
    }

    private static class RotationPair {
        int x;
        int y;

        public RotationPair(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static final Map<Direction, RotationPair> DIRECTION_ROTATIONS =
            ImmutableMap.<Direction, RotationPair>builder()
                    .put(Direction.NORTH, new RotationPair(0, 0))
                    .put(Direction.SOUTH, new RotationPair(0, 180))
                    .put(Direction.EAST, new RotationPair(0, 90))
                    .put(Direction.WEST, new RotationPair(0, 270))
                    .put(Direction.DOWN, new RotationPair(90, 0))
                    .put(Direction.UP, new RotationPair(270, 0))
                    .build();

    /*
    public void registerPipeRegular() {
        MultiPartBlockStateBuilder builder =
                getMultipartBuilder(ModBlocks.PIPE_REGULAR.get()).part().modelFile(models().getExistingFile(modLoc(
                        "block/pipe_regular_frame"))).addModel().end();
        for (var dir : Direction.values()) {
            builder = builder
                    .part()
                    .modelFile(models().getExistingFile(modLoc("block/pipe_regular_frame_cover")))
                    .rotationX(DIRECTION_ROTATIONS.get(dir).x)
                    .rotationY(DIRECTION_ROTATIONS.get(dir).y)
                    .addModel()
                    .condition(EnumProperty.create(dir.getName(), ConnectionType.class), ConnectionType.DISCONNECTED)
                    .end()
                    .part()
                    .modelFile(models().getExistingFile(modLoc("block/pipe_regular_junction")))
                    .rotationX(DIRECTION_ROTATIONS.get(dir).x)
                    .rotationY(DIRECTION_ROTATIONS.get(dir).y)
                    .addModel()
                    .condition(EnumProperty.create(dir.getName(), ConnectionType.class), ConnectionType.CONNECTED)
                    .end();
        }

    }
    */

    /*
    public void registerPipeSmart() {
        MultiPartBlockStateBuilder builder =
                getMultipartBuilder(ModBlocks.PIPE_SMART.get()).part().modelFile(models().getExistingFile(modLoc(
                        "block/pipe_smart_core"))).addModel().end();
        for (var dir : Direction.values()) {
            builder = builder
                    .part()
                    .modelFile(models().getExistingFile(modLoc("block/pipe_smart_frame_cover")))
                    .rotationX(DIRECTION_ROTATIONS.get(dir).x)
                    .rotationY(DIRECTION_ROTATIONS.get(dir).y)
                    .addModel()
                    .condition(EnumProperty.create(dir.getName(), ConnectionType.class), ConnectionType.DISCONNECTED)
                    .end()
                    .part()
                    .modelFile(models().getExistingFile(modLoc("block/pipe_regular_junction")))
                    .rotationX(DIRECTION_ROTATIONS.get(dir).x)
                    .rotationY(DIRECTION_ROTATIONS.get(dir).y)
                    .addModel()
                    .condition(EnumProperty.create(dir.getName(), ConnectionType.class), ConnectionType.CONNECTED)
                    .end()
                    .part()
                    .modelFile(models().getExistingFile(modLoc("block/pipe_smart_junction")))
                    .rotationX(DIRECTION_ROTATIONS.get(dir).x)
                    .rotationY(DIRECTION_ROTATIONS.get(dir).y)
                    .addModel()
                    .condition(EnumProperty.create(dir.getName(), ConnectionType.class), ConnectionType.CONNECTED_SMART)
                    .end();
        }
    }
    */

    @Override
    protected void registerStatesAndModels() {
        //registerPipeRegular();
        //registerPipeSmart();
        //horizontalBlock(ModBlocks.NETWORK_INTERFACE.get(), models().getExistingFile(modLoc("block/network_interface")));
    }
}
