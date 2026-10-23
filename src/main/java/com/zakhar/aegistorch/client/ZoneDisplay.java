package com.zakhar.aegistorch.client;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.World;

/**
 * Client-only: which torches currently have their zone shown.
 * Purely visual, not saved. The toggles belong to one client world;
 * when the player changes world/dimension they are dropped.
 */
public final class ZoneDisplay {

    private static final Set<Long> SHOWN = new HashSet<Long>();
    private static WeakReference<World> owner = new WeakReference<World>(null);

    private ZoneDisplay() {}

    static long key(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (long) (y & 0xFFF);
    }

    static int keyX(long k) { return (int) (k >> 38); }
    static int keyZ(long k) { return (int) (k << 26 >> 38); }
    static int keyY(long k) { return (int) (k & 0xFFF); }

    /** Drops toggles that belong to another (old) world. */
    static void syncWorld(World world) {
        if (owner.get() != world) {
            SHOWN.clear();
            owner = new WeakReference<World>(world);
        }
    }

    public static boolean isShown(World world, int x, int y, int z) {
        return owner.get() == world && SHOWN.contains(key(x, y, z));
    }

    /** Flips the display for this torch and returns the new state. */
    public static boolean toggle(World world, int x, int y, int z) {
        syncWorld(world);
        long k = key(x, y, z);
        if (SHOWN.remove(k)) {
            return false;
        }
        SHOWN.add(k);
        return true;
    }

    static Set<Long> all() {
        return SHOWN;
    }
}
