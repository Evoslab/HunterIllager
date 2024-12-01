package baguchan.hunters_return.init;

import net.minecraft.world.entity.raid.Raid;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public class RaiderEnumParams {
    @SuppressWarnings("unused")
    public static final EnumProxy<Raid.RaiderType> HUNTER = new EnumProxy<>(
            Raid.RaiderType.class, HunterEntityRegistry.HUNTERILLAGER, new int[]{0, 1, 1, 1, 2, 2, 2, 3}
    );
}