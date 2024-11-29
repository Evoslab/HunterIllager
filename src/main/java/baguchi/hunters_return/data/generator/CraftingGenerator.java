package baguchi.hunters_return.data.generator;

import baguchi.hunters_return.init.HunterItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

public class CraftingGenerator extends RecipeProvider {
    public CraftingGenerator(HolderLookup.Provider generator, RecipeOutput p_323846_) {
        super(generator, p_323846_);
    }

    @Override
    protected void buildRecipes() {
        HolderLookup<Item> lookup = this.registries.lookupOrThrow(Registries.ITEM);

        ShapedRecipeBuilder.shaped(lookup, RecipeCategory.COMBAT, HunterItems.BOOMERANG, 1)
                .pattern(" B")
                .pattern("BL")
                .pattern(" B")
                .define('B', ItemTags.PLANKS)
                .define('L', Items.LEATHER)
                .unlockedBy("has_item", has(Items.LEATHER)).save(this.output);
        ShapedRecipeBuilder.shaped(lookup, RecipeCategory.COMBAT, HunterItems.MINI_CROSSBOW, 1)
                .pattern("RIR")
                .pattern("SIS")
                .pattern(" R ")
                .define('I', Items.IRON_NUGGET)
                .define('R', Tags.Items.RODS_WOODEN)
                .define('S', Tags.Items.STRINGS)
                .unlockedBy("has_item", has(Items.IRON_NUGGET)).save(this.output);

    }
}
