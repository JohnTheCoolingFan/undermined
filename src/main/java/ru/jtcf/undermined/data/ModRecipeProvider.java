package ru.jtcf.undermined.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import ru.jtcf.undermined.UnderMined;
import ru.jtcf.undermined.setup.ModBlocks;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(DataGenerator pGenerator) {
        super(pGenerator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(ModBlocks.BORE_HEAD.get())
                .define('B', Items.IRON_BLOCK)
                .define('i', Items.IRON_INGOT)
                .define('d', Items.DIAMOND)
                .define('n', Items.NETHERITE_INGOT)
                .pattern("iBi")
                .pattern("did")
                .pattern("ndn")
                .unlockedBy("has_item", has(Items.NETHERITE_INGOT))
                .save(consumer);
    }

    private ResourceLocation modId(String path) {
        return new ResourceLocation(UnderMined.MODID, path);
    }
}
