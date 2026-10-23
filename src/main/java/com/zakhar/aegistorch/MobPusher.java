package com.zakhar.aegistorch;

import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/**
 * Pushes hostile mobs out of the zone of every torch that has pushing turned on.
 *
 *            . . . zone edge . . .
 *         .                       .
 *       .    mob ---> push         .      direction = from torch center to mob (horizontal)
 *       .         \                .      speed     = pushStrength blocks/tick
 *       .        TORCH             .      + small hop if on ground, to get over 1-block steps
 *         .                       .
 *            . . . . . . . . . . .
 *
 * Runs on the server world tick every pushInterval ticks. Bosses are never pushed.
 */
public class MobPusher {

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !AegisTorchConfig.allowPushing) {
            return;
        }
        World world = event.world;
        if (world == null || world.isRemote) {
            return;
        }
        if (world.getTotalWorldTime() % AegisTorchConfig.pushInterval != 0) {
            return;
        }
        for (TileAegisTorch te : TorchRegistry.snapshot(world)) {
            if (te.isPushMobs()) {
                pushOut(world, te);
            }
        }
    }

    /** Package-private for tests. Returns how many mobs were pushed. */
    static int pushOut(World world, TileAegisTorch te) {
        int r = te.getRadius();
        double cx = te.xCoord + 0.5D;
        double cz = te.zCoord + 0.5D;
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                cx - r, te.yCoord - r, cz - r,
                cx + r, te.yCoord + r + 1, cz + r);

        @SuppressWarnings("unchecked")
        List<EntityLiving> mobs = world.getEntitiesWithinAABB(EntityLiving.class, box);
        int pushed = 0;
        for (EntityLiving mob : mobs) {
            if (!(mob instanceof IMob) || mob instanceof IBossDisplayData || mob.isDead) {
                continue;
            }
            if (!TorchRegistry.inZone(te, mob.posX, mob.posY, mob.posZ)) {
                continue; // inside the box corners but outside the cylinder
            }
            double dx = mob.posX - cx;
            double dz = mob.posZ - cz;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 0.01D) {        // standing exactly on the torch - pick any direction
                dx = 1.0D;
                dz = 0.0D;
                dist = 1.0D;
            }
            double s = AegisTorchConfig.pushStrength;
            mob.motionX = dx / dist * s;
            mob.motionZ = dz / dist * s;
            if (mob.onGround && mob.motionY < 0.3D) {
                mob.motionY = 0.3D;    // little hop over fences/steps
            }
            mob.velocityChanged = true; // sync the new motion to clients right away
            pushed++;
        }
        return pushed;
    }
}
