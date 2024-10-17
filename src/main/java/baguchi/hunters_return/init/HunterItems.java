package baguchi.hunters_return.init;

import baguchi.hunters_return.HuntersReturn;
import baguchi.hunters_return.item.BoomerangItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class HunterItems {
	public static final DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(BuiltInRegistries.ITEM, baguchi.hunters_return.HuntersReturn.MODID);

	public static final Supplier<DeferredSpawnEggItem> SPAWNEGG_HUNTER = ITEM_REGISTRY.register("spawnegg_hunter", () -> new DeferredSpawnEggItem(HunterEntityRegistry.HUNTERILLAGER, 9804699, 5777447, (new Item.Properties().setId(prefix("spawnegg_hunter")))));
	public static final Supplier<Item> BOOMERANG = ITEM_REGISTRY.register("boomerang", () -> new BoomerangItem((new Item.Properties().enchantable(2).setId(prefix("boomerang"))).durability(384)));

	private static ResourceKey<Item> prefix(String path) {
		return ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HuntersReturn.MODID, path));
	}
}
