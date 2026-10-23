package com.zakhar.aegistorch;

import com.zakhar.aegistorch.client.GuiAegisTorch;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null; // client-only screen, no container
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == AegisTorchMod.GUI_TORCH) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileAegisTorch) {
                return new GuiAegisTorch((TileAegisTorch) te, x, y, z);
            }
        }
        return null;
    }
}
