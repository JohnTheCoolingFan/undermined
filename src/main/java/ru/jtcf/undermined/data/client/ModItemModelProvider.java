package ru.jtcf.undermined.data.client;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import ru.jtcf.undermined.UnderMined;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen, UnderMined.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent("bore_head", modLoc("block/bore_head"));
        withExistingParent("vein_scanner", modLoc("block/vein_scanner"));

        ModelFile itemGenerated = getExistingFile(mcLoc("item/generated"));
    }

    private ItemModelBuilder generatedBuilder(ModelFile itemGenerated, String name) {
        return getBuilder(name).parent(itemGenerated).texture("layer0", "item/" + name);
    }
}
