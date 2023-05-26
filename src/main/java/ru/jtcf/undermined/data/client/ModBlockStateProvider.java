package ru.jtcf.undermined.data.client;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import ru.jtcf.undermined.UnderMined;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen, UnderMined.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
    }
}
