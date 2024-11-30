package baguchan.hunters_return.init;

import baguchan.hunters_return.item.BoomerangItem;
import baguchan.hunters_return.item.MiniCrossBowItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HunterItems {
	public static final DeferredRegister.Items ITEM_REGISTRY = DeferredRegister.createItems(baguchan.hunters_return.HuntersReturn.MODID);

	public static final DeferredItem<DeferredSpawnEggItem> SPAWNEGG_HUNTER = ITEM_REGISTRY.register("spawnegg_hunter", () -> new DeferredSpawnEggItem(HunterEntityRegistry.HUNTERILLAGER, 9804699, 5777447, (new Item.Properties())));
	public static final DeferredItem<Item> BOOMERANG = ITEM_REGISTRY.register("boomerang", () -> new BoomerangItem((new Item.Properties()).durability(384)));
	public static final DeferredItem<Item> MINI_CROSSBOW = ITEM_REGISTRY.register("mini_crossbow", () -> new MiniCrossBowItem((new Item.Properties().durability(412).component(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).stacksTo(1))));

}
