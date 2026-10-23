package com.zakhar.aegistorch;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public final class AegisTorchConfig {

    /** Radius a newly placed torch starts with. */
    public static int defaultRadius = 64;
    /** Largest radius a player can set in the GUI. */
    public static int maxRadius = 128;
    /** If true, passive mobs (animals, squids, bats) are blocked from spawning too. */
    public static boolean blockPassiveMobs = false;
    /** If false, the crafting recipe is not registered (for packs that add their own). */
    public static boolean enableRecipe = true;

    /** Master switch for pushing hostile mobs out of the zone. */
    public static boolean allowPushing = true;
    /** Whether a newly placed torch has pushing turned on. */
    public static boolean pushByDefault = true;
    /** Outward speed given to a mob on each push (blocks per tick). */
    public static float pushStrength = 0.6F;
    /** Push every N ticks (20 ticks = 1 second). */
    public static int pushInterval = 2;

    private AegisTorchConfig() {}

    public static void load(File file) {
        Configuration cfg = new Configuration(file);
        cfg.load();
        String g = Configuration.CATEGORY_GENERAL;

        maxRadius = cfg.getInt("maxRadius", g, 128, 1, 512,
                "Largest radius a player can set on a Aegis Torch in its GUI.");
        defaultRadius = cfg.getInt("defaultRadius", g, 64, 1, 512,
                "Radius a newly placed Aegis Torch starts with. The zone is a cylinder: this radius horizontally and the same distance up and down.");
        if (defaultRadius > maxRadius) {
            defaultRadius = maxRadius;
        }
        blockPassiveMobs = cfg.getBoolean("blockPassiveMobs", g, false,
                "If true, the torch also blocks passive mobs (animals, bats, squids) from spawning.");
        enableRecipe = cfg.getBoolean("enableRecipe", g, true,
                "Register the default crafting recipe.");

        String p = "pushing";
        allowPushing = cfg.getBoolean("allowPushing", p, true,
                "Master switch. If false, no torch pushes mobs and the GUI button is hidden.");
        pushByDefault = cfg.getBoolean("pushByDefault", p, true,
                "Whether a newly placed Aegis Torch pushes hostile mobs out of its zone.");
        pushStrength = cfg.getFloat("pushStrength", p, 0.6F, 0.1F, 3.0F,
                "Outward speed (blocks per tick) given to a hostile mob each time it is pushed.");
        pushInterval = cfg.getInt("pushInterval", p, 2, 1, 40,
                "Push mobs every N ticks. Lower = stronger force field, slightly more CPU.");

        if (cfg.hasChanged()) {
            cfg.save();
        }
    }

    public static int clampRadius(int r) {
        return r < 1 ? 1 : (r > maxRadius ? maxRadius : r);
    }
}
