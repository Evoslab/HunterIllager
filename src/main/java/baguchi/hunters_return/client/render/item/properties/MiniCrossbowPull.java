package baguchi.hunters_return.client.render.item.properties;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.UseDuration;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class MiniCrossbowPull implements RangeSelectItemModelProperty {
    public static final MapCodec<MiniCrossbowPull> MAP_CODEC = MapCodec.unit(new MiniCrossbowPull());

    @Override
    public float get(ItemStack p_387470_, @Nullable ClientLevel p_387947_, @Nullable LivingEntity p_388564_, int p_388371_) {
        if (p_388564_ == null) {
            return 0.0F;
        } else if (CrossbowItem.isCharged(p_387470_)) {
            return 0.0F;
        } else {
            int i = CrossbowItem.getChargeDuration(p_387470_, p_388564_);
            return (float) UseDuration.useDuration(p_387470_, p_388564_) / (float) i;
        }
    }

    @Override
    public MapCodec<MiniCrossbowPull> type() {
        return MAP_CODEC;
    }
}
