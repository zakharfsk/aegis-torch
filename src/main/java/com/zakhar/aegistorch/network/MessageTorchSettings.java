package com.zakhar.aegistorch.network;

import com.zakhar.aegistorch.TileAegisTorch;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** Client -> server: "apply these settings to the Aegis Torch at x,y,z". */
public class MessageTorchSettings implements IMessage {

    /** Player must be this close to the torch (squared distance) for the change to apply. */
    private static final double MAX_DIST_SQ = 12.0D * 12.0D;

    private int x, y, z, radius;
    private boolean pushMobs;

    public MessageTorchSettings() {}

    public MessageTorchSettings(int x, int y, int z, int radius, boolean pushMobs) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.pushMobs = pushMobs;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        radius = buf.readInt();
        pushMobs = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(radius);
        buf.writeBoolean(pushMobs);
    }

    public static class Handler implements IMessageHandler<MessageTorchSettings, IMessage> {

        @Override
        public IMessage onMessage(final MessageTorchSettings msg, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            // In 1.7.10 this runs on the Netty thread - hand the work to the server thread.
            ServerTaskQueue.enqueue(new Runnable() {
                @Override
                public void run() {
                    apply(player, msg);
                }
            });
            return null;
        }
    }

    static void apply(EntityPlayerMP player, MessageTorchSettings msg) {
        World world = player.worldObj;
        if (world == null) {
            return;
        }
        if (player.getDistanceSq(msg.x + 0.5D, msg.y + 0.5D, msg.z + 0.5D) > MAX_DIST_SQ) {
            return; // ignore packets for torches the player is not standing at
        }
        TileEntity te = world.getTileEntity(msg.x, msg.y, msg.z);
        if (te instanceof TileAegisTorch) {
            ((TileAegisTorch) te).setSettingsAndSync(msg.radius, msg.pushMobs);
        }
    }
}
