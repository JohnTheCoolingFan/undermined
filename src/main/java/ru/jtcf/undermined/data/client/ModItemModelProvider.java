package ru.jtcf.undermined.data.client;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import ru.jtcf.undermined.UnderMined;

public class ModItemModelProvider extends ItemModelProvider {
    private static String[] MODULES = {"admin_module", "blank_module", "crafting_module", "crafting_observer_module",
            "crafting_satellite_module", "energy_booster_module", "energy_extractor_module", "energy_provider_module",
            "extraction_amount_module", "extraction_speed_module", "fluid_provider_module", "fluid_receiver_module",
            "item_provider_module", "item_receiver_module", "network_map_module"};

    public ModItemModelProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen, UnderMined.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //withExistingParent("pipe_regular", modLoc("block/pipe_regular_frame"));
        //withExistingParent("pipe_smart", modLoc("block/pipe_smart_core"));
        //withExistingParent("network_interface", modLoc("block/network_interface"));

        ModelFile itemGenerated = getExistingFile(mcLoc("item/generated"));

        //for (String module_name : MODULES) {
            //generatedBuilder(itemGenerated, module_name);
        //}
    }

    private ItemModelBuilder generatedBuilder(ModelFile itemGenerated, String name) {
        return getBuilder(name).parent(itemGenerated).texture("layer0", "item/" + name);
    }
}
