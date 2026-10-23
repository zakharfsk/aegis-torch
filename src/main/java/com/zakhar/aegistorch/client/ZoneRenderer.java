package com.zakhar.aegistorch.client;

import java.util.Iterator;

import org.lwjgl.opengl.GL11;

import com.zakhar.aegistorch.TileAegisTorch;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

/**
 * Draws the protected zone (a cylinder) of every torch the player toggled on in the GUI.
 *
 * Two passes: a faint one with depth test off (visible through walls)
 * and a bright one with depth test on (crisp where nothing is in the way).
 */
public class ZoneRenderer {

    private static final float R = 0.25F, G = 0.85F, B = 1.0F;   // cyan
    private static final int VERTICAL_LINES = 16;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null || ZoneDisplay.all().isEmpty()) {
            return;
        }
        ZoneDisplay.syncWorld(world);     // new world / dimension - forget old toggles
        if (ZoneDisplay.all().isEmpty()) {
            return;
        }

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LINE_BIT | GL11.GL_CURRENT_BIT
                | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);

        Iterator<Long> it = ZoneDisplay.all().iterator();
        while (it.hasNext()) {
            long k = it.next();
            int x = ZoneDisplay.keyX(k), y = ZoneDisplay.keyY(k), z = ZoneDisplay.keyZ(k);
            TileEntity te = world.getTileEntity(x, y, z);
            if (!(te instanceof TileAegisTorch)) {
                it.remove();              // torch broken or chunk gone
                continue;
            }
            int r = ((TileAegisTorch) te).getRadius();

            double cx = x + 0.5D - RenderManager.renderPosX;
            double cz = z + 0.5D - RenderManager.renderPosZ;
            double bottom = Math.max(0, y - r) - RenderManager.renderPosY;
            double top = Math.min(256, y + r + 1) - RenderManager.renderPosY;
            double mid = y + 0.5D - RenderManager.renderPosY;

            // pass 1: through walls, faint
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glLineWidth(1.5F);
            GL11.glColor4f(R, G, B, 0.25F);
            drawCylinder(cx, cz, bottom, mid, top, r);

            // pass 2: normal depth, bright
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glLineWidth(2.5F);
            GL11.glColor4f(R, G, B, 0.9F);
            drawCylinder(cx, cz, bottom, mid, top, r);
        }

        GL11.glDepthMask(true);
        GL11.glPopAttrib();
    }

    private static void drawCylinder(double cx, double cz, double bottom, double mid, double top, int r) {
        int segments = Math.max(32, Math.min(128, r * 2));

        ring(cx, cz, bottom, r, segments);
        ring(cx, cz, top, r, segments);
        ring(cx, cz, mid, r, segments);

        GL11.glBegin(GL11.GL_LINES);
        for (int i = 0; i < VERTICAL_LINES; i++) {
            double a = 2 * Math.PI * i / VERTICAL_LINES;
            double px = cx + Math.cos(a) * r;
            double pz = cz + Math.sin(a) * r;
            GL11.glVertex3d(px, bottom, pz);
            GL11.glVertex3d(px, top, pz);
        }
        // center axis through the torch
        GL11.glVertex3d(cx, bottom, cz);
        GL11.glVertex3d(cx, top, cz);
        GL11.glEnd();
    }

    private static void ring(double cx, double cz, double y, int r, int segments) {
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i < segments; i++) {
            double a = 2 * Math.PI * i / segments;
            GL11.glVertex3d(cx + Math.cos(a) * r, y, cz + Math.sin(a) * r);
        }
        GL11.glEnd();
    }
}
