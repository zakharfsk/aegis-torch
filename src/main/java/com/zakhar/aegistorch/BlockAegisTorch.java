package com.zakhar.aegistorch;

import net.minecraft.block.BlockTorch;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Behaves exactly like a vanilla torch (placement on walls/floor, model, particles),
 * but gives full light level 15, carries a tile entity with its own radius and
 * opens a settings GUI on right click.
 */
public class BlockAegisTorch extends BlockTorch {

    public BlockAegisTorch() {
        super();
        setLightLevel(1.0F);          // 15 - like glowstone (vanilla torch is 14)
        setHardness(0.0F);            // breaks instantly, like a torch
        setStepSound(soundTypeWood);
        setBlockName(AegisTorchMod.MODID + ".aegis_torch");
        setBlockTextureName(AegisTorchMod.MODID + ":aegis_torch");
        setCreativeTab(CreativeTabs.tabDecorations);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            // GUI is client-only (no container); changes are sent to the server as a packet
            player.openGui(AegisTorchMod.instance, AegisTorchMod.GUI_TORCH, world, x, y, z);
        }
        return true;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileAegisTorch();
    }
}
