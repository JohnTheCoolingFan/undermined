package ru.jtcf.undermined.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import ru.jtcf.undermined.UnderMined;
import ru.jtcf.undermined.setup.ModBlocks;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(DataGenerator pGenerator, ExistingFileHelper existingFileHelper) {
        super(pGenerator, UnderMined.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        //tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.PIPE_REGULAR.get());
        //tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.PIPE_SMART.get());
        //tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.NETWORK_INTERFACE.get());
    }
}
