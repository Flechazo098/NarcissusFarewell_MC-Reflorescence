package xin.vanilla.narcissus.network;

import net.minecraft.network.FriendlyByteBuf;

public interface Packet {
    void encode(FriendlyByteBuf buf);
}