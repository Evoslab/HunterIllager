package baguchi.hunters_return.data;

import baguchi.hunters_return.HuntersReturn;
import baguchi.hunters_return.init.ModBiomeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class BiomeTagGenerator extends BiomeTagsProvider {
    public BiomeTagGenerator(PackOutput p_255941_, CompletableFuture<HolderLookup.Provider> p_256600_, ExistingFileHelper exFileHelper) {
        super(p_255941_, p_256600_, HuntersReturn.MODID, exFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ModBiomeTags.HAS_HUNTER_HOUSE).addTag(BiomeTags.IS_TAIGA).addTag(BiomeTags.IS_FOREST);
    }
}
