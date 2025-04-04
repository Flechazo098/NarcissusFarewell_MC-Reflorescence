package xin.vanilla.narcissus.network;

import lombok.Data;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Random;

@Data
public abstract class SplitPacket {
    /**
     * 分包ID
     */
    private String id;
    /**
     * 总包数
     */
    private int total;
    /**
     * 当前包序号
     */
    private int sort;

    protected SplitPacket() {
        this.id = String.format("%d.%d", System.currentTimeMillis(), new Random().nextInt(999999999));
    }

    protected SplitPacket(FriendlyByteBuf buf) {
        this.id = buf.readUtf();
        this.total = buf.readInt();
        this.sort = buf.readInt();
    }

    protected void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeInt(total);
        buf.writeInt(sort);
    }

    public abstract int getChunkSize();
}