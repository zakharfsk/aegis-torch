package com.zakhar.aegistorch;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.world.World;

/**
 * Server-side index of loaded Aegis Torches, per world.
 * A world is held weakly, so unloaded dimensions are garbage collected automatically.
 * The set of torches is tiny (usually a handful), so a linear scan per spawn check is cheap.
 */
public final class TorchRegistry {

    private static final Map<World, Set<TileAegisTorch>> TORCHES = new WeakHashMap<World, Set<TileAegisTorch>>();

    private TorchRegistry() {}

    public static synchronized void add(World world, TileAegisTorch te) {
        Set<TileAegisTorch> set = TORCHES.get(world);
        if (set == null) {
            set = new LinkedHashSet<TileAegisTorch>();
            TORCHES.put(world, set);
        }
        set.add(te);
    }

    public static synchronized void remove(World world, TileAegisTorch te) {
        Set<TileAegisTorch> set = TORCHES.get(world);
        if (set != null) {
            set.remove(te);
            if (set.isEmpty()) {
                TORCHES.remove(world);
            }
        }
    }

    /** Copy of this world's torches, safe to iterate while torches are added/removed. */
    public static synchronized TileAegisTorch[] snapshot(World world) {
        Set<TileAegisTorch> set = TORCHES.get(world);
        return set == null ? new TileAegisTorch[0] : set.toArray(new TileAegisTorch[set.size()]);
    }

    /** True if (x,y,z) is inside this torch's cylinder. */
    public static boolean inZone(TileAegisTorch te, double x, double y, double z) {
        double r = te.getRadius();
        double dx = x - (te.xCoord + 0.5D);
        double dz = z - (te.zCoord + 0.5D);
        double dy = y - te.yCoord;
        return dx * dx + dz * dz <= r * r && Math.abs(dy) <= r;
    }

    /**
     * True if (x,y,z) lies inside the protected zone of any Aegis Torch in this world.
     * Zone = cylinder with the torch's radius horizontally and the same distance up and down.
     */
    public static synchronized boolean isProtected(World world, double x, double y, double z) {
        Set<TileAegisTorch> set = TORCHES.get(world);
        if (set == null || set.isEmpty()) {
            return false;
        }
        for (TileAegisTorch te : set) {
            if (inZone(te, x, y, z)) {
                return true;
            }
        }
        return false;
    }
}
