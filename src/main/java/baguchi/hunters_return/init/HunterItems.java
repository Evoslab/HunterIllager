package baguchi.hunters_return.init;

import baguchi.hunters_return.HuntersReturn;
import baguchi.hunters_return.item.BoomerangItem;
import baguchi.hunters_return.item.MiniCrossBowItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HunterItems {
	public static final DeferredRegister.Items ITEM_REGISTRY = DeferredRegister.createItems(baguchi.hunters_return.HuntersReturn.MODID);

	public static final DeferredItem<SpawnEggItem> SPAWNEGG_HUNTER = ITEM_REGISTRY.register("spawnegg_hunter", () -> new SpawnEggItem(HunterEntityRegistry.HUNTERILLAGER.get(), 9804699, 5777447, (new Item.Properties().setId(prefix("spawnegg_hunter")))));
	public static final DeferredItem<Item> BOOMERANG = ITEM_REGISTRY.register("boomerang", () -> new BoomerangItem((new Item.Properties().enchantable(2).setId(prefix("boomerang"))).durability(384)));
	public static final DeferredItem<Item> MINI_CROSSBOW = ITEM_REGISTRY.register("mini_crossbow", () -> new MiniCrossBowItem((new Item.Properties().setId(prefix("mini_crossbow"))).durability(412).rarity(Rarity.UNCOMMON).component(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).enchantable(2).stacksTo(1)));

	private static ResourceKey<Item> prefix(String path) {
		return ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HuntersReturn.MODID, path));
	}
}
