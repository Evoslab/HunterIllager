package baguchi.hunters_return.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record QuiverSelectPacket(int slotId, int selectedItemIndex) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, QuiverSelectPacket> STREAM_CODEC = CustomPacketPayload.codec(QuiverSelectPacket::write, QuiverSelectPacket::new);
    public static final CustomPacketPayload.Type<QuiverSelectPacket> TYPE = CustomPacketPayload.createType("hunters_return:quiver");

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.slotId);
        buf.writeVarInt(this.selectedItemIndex);
    }

    public QuiverSelectPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(QuiverSelectPacket message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                AbstractContainerMenu abstractContainerScreen = serverPlayer.containerMenu;
                if (message.slotId >= 0 && message.slotId < abstractContainerScreen.slots.size()) {
                    ItemStack itemstack = abstractContainerScreen.slots.get(message.slotId).getItem();
                    BundleItem.toggleSelectedItem(itemstack, message.selectedItemIndex());
                }
            }
        });
    }
}