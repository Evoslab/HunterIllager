package baguchi.hunters_return.data;

import baguchi.hunters_return.HuntersReturn;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends BlockTagsProvider {
    public BlockTagGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider, HuntersReturn.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

    }
}
