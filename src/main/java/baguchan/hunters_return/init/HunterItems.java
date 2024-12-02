package baguchan.hunters_return.init;

import baguchan.hunters_return.item.BoomerangItem;
import baguchan.hunters_return.item.MiniCrossBowItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(modid = baguchan.hunters_return.HuntersReturn.MODID, bus = EventBusSubscriber.Bus.MOD)
public class HunterItems {
	public static final DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, baguchan.hunters_return.HuntersReturn.MODID);

	public static final RegistryObject<Item> SPAWNEGG_HUNTER = ITEM_REGISTRY.register("spawnegg_hunter", () -> new ForgeSpawnEggItem(HunterEntityRegistry.HUNTERILLAGER, 9804699, 5777447, (new Item.Properties())));
	public static final RegistryObject<Item> BOOMERANG = ITEM_REGISTRY.register("boomerang", () -> new BoomerangItem((new Item.Properties()).durability(384)));
	public static final RegistryObject<Item> MINI_CROSSBOW = ITEM_REGISTRY.register("mini_crossbow", () -> new MiniCrossBowItem((new Item.Properties()).durability(412)));

}
