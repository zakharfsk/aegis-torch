package com.zakhar.aegistorch;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/**
 * Non-ticking tile entity holding this torch's settings (radius, push on/off).
 * - Registers itself in {@link TorchRegistry} while loaded on the server.
 * - Saves settings to NBT (survives restarts).
 * - Syncs settings to clients via the vanilla description packet,
 *   so the GUI and the zone preview show the real values.
 */
public class TileAegisTorch extends TileEntity {

    private static final String NBT_RADIUS = "AegisRadius";
    private static final String NBT_PUSH = "AegisPushMobs";

    private int radius = AegisTorchConfig.defaultRadius;
    private boolean pushMobs = AegisTorchConfig.pushByDefault;

    public int getRadius() {
        return radius;
    }

    public boolean isPushMobs() {
        return pushMobs;
    }

    /** Server side: change settings, save and resync to all watching clients. */
    public void setSettingsAndSync(int newRadius, boolean newPush) {
        radius = AegisTorchConfig.clampRadius(newRadius);
        pushMobs = newPush;
        markDirty();
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    /** Client side: show new values immediately while the packet travels. */
    public void setSettingsClient(int newRadius, boolean newPush) {
        radius = AegisTorchConfig.clampRadius(newRadius);
        pushMobs = newPush;
    }

    @Override
    public boolean canUpdate() {
        return false; // no ticking - pushing is done by MobPusher only for registered torches
    }

    private void writeSettings(NBTTagCompound tag) {
        tag.setInteger(NBT_RADIUS, radius);
        tag.setBoolean(NBT_PUSH, pushMobs);
    }

    private void readSettings(NBTTagCompound tag) {
        if (tag.hasKey(NBT_RADIUS)) {
            radius = AegisTorchConfig.clampRadius(tag.getInteger(NBT_RADIUS));
        }
        if (tag.hasKey(NBT_PUSH)) {
            pushMobs = tag.getBoolean(NBT_PUSH);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeSettings(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readSettings(tag);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeSettings(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        NBTTagCompound tag = pkt.func_148857_g(); // getNbtCompound (unnamed in MCP stable 12)
        if (tag != null) {
            readSettings(tag);
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (worldObj != null && !worldObj.isRemote) {
            TorchRegistry.add(worldObj, this);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (worldObj != null && !worldObj.isRemote) {
            TorchRegistry.remove(worldObj, this);
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (worldObj != null && !worldObj.isRemote) {
            TorchRegistry.remove(worldObj, this);
        }
    }
}
