package baguchi.hunters_return;

import baguchi.hunters_return.init.HunterDataComponents;
import baguchi.hunters_return.init.HunterEntityRegistry;
import baguchi.hunters_return.init.HunterItems;
import baguchi.hunters_return.init.HunterSounds;
import baguchi.hunters_return.message.QuiverSelectPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(baguchi.hunters_return.HuntersReturn.MODID)
public class HuntersReturn {
	public static final String MODID = "hunters_return";
	public static final Logger LOGGER = LogManager.getLogger();

	public HuntersReturn(ModContainer modContainer, IEventBus modEventBus) {
		// Register the setup method for modloading
		modEventBus.addListener(this::setup);
		modEventBus.addListener(this::setupPackets);
		HunterEntityRegistry.ENTITIES_REGISTRY.register(modEventBus);
		HunterItems.ITEM_REGISTRY.register(modEventBus);
		HunterDataComponents.DATA_COMPONENTS.register(modEventBus);
		HunterSounds.SOUND_EVENTS.register(modEventBus);
		modContainer.registerConfig(ModConfig.Type.COMMON, HunterConfig.COMMON_SPEC);
		modContainer.registerConfig(ModConfig.Type.CLIENT, HunterConfig.CLIENT_SPEC);

		// Register ourselves for server and other game events we are interested in
		NeoForge.EVENT_BUS.addListener(this::serverStart);
	}

	private void setup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
		});
	}

	public void setupPackets(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0.0").optional();
		registrar.playBidirectional(QuiverSelectPacket.TYPE, QuiverSelectPacket.STREAM_CODEC, (handler, payload) -> handler.handle(handler, payload));
	}

	public static ResourceLocation locate(String path) {
		return ResourceLocation.fromNamespaceAndPath(baguchi.hunters_return.HuntersReturn.MODID, path);
	}


	private void serverStart(final ServerAboutToStartEvent event) {
	}
}
