package ru.jtcf.undermined.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import ru.jtcf.undermined.UnderMined;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(DataGenerator dataGenerator, BlockTagsProvider blockTagsProvider,
                               ExistingFileHelper existingFileHelper) {
        super(dataGenerator, blockTagsProvider, UnderMined.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {

    }
}
