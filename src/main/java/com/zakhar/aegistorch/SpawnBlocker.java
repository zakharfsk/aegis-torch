package com.zakhar.aegistorch;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.monster.IMob;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;

/**
 * Denies natural spawns inside a Aegis Torch area.
 *
 * In Forge 1.7.10 LivingSpawnEvent.CheckSpawn is fired by the natural spawner
 * (SpawnerAnimals) only. Vanilla monster spawners (cages) do not fire it,
 * so mob farms built on spawners keep working inside the protected area.
 * Result.DENY makes the game skip the spawn attempt.
 */
public class SpawnBlocker {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        World world = event.world;
        if (world == null || world.isRemote) {
            return;
        }
        if (!AegisTorchConfig.blockPassiveMobs && !(event.entityLiving instanceof IMob)) {
            return;
        }
        if (TorchRegistry.isProtected(world, event.x, event.y, event.z)) {
            event.setResult(Event.Result.DENY);
        }
    }
}
