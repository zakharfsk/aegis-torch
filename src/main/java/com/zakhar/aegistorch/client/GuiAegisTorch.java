package com.zakhar.aegistorch.client;

import com.zakhar.aegistorch.AegisTorchConfig;
import com.zakhar.aegistorch.AegisTorchMod;
import com.zakhar.aegistorch.TileAegisTorch;
import com.zakhar.aegistorch.network.MessageTorchSettings;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

/**
 *              Aegis Torch
 *          Position: x, y, z
 *  [-10] [-1]  Radius: 64 blocks  [+1] [+10]
 *     Zone: circle 129 wide, 129 high
 *  [   Push hostile mobs out: ON          ]
 *  [   Show zone: OFF                     ]
 *  [               Done                   ]
 */
public class GuiAegisTorch extends GuiScreen {

    private static final int BTN_MINUS_10 = 0, BTN_MINUS_1 = 1, BTN_PLUS_1 = 2, BTN_PLUS_10 = 3;
    private static final int BTN_SHOW = 4, BTN_DONE = 5, BTN_PUSH = 6;

    private final TileAegisTorch torch;
    private final int x, y, z;
    private int radius;
    private boolean pushMobs;

    private GuiButton minus10, minus1, plus1, plus10, show, push;

    public GuiAegisTorch(TileAegisTorch torch, int x, int y, int z) {
        this.torch = torch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = torch.getRadius();
        this.pushMobs = torch.isPushMobs();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        buttonList.clear();
        int cx = width / 2;
        int cy = height / 2;

        minus10 = new GuiButton(BTN_MINUS_10, cx - 130, cy - 34, 34, 20, "-10");
        minus1 = new GuiButton(BTN_MINUS_1, cx - 92, cy - 34, 26, 20, "-1");
        plus1 = new GuiButton(BTN_PLUS_1, cx + 66, cy - 34, 26, 20, "+1");
        plus10 = new GuiButton(BTN_PLUS_10, cx + 96, cy - 34, 34, 20, "+10");
        push = new GuiButton(BTN_PUSH, cx - 110, cy + 6, 220, 20, pushLabel());
        show = new GuiButton(BTN_SHOW, cx - 110, cy + 30, 220, 20, showLabel());
        GuiButton done = new GuiButton(BTN_DONE, cx - 110, cy + 54, 220, 20,
                StatCollector.translateToLocal("gui.aegistorch.done"));

        buttonList.add(minus10);
        buttonList.add(minus1);
        buttonList.add(plus1);
        buttonList.add(plus10);
        if (AegisTorchConfig.allowPushing) {
            buttonList.add(push);
        }
        buttonList.add(show);
        buttonList.add(done);
        updateButtons();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case BTN_MINUS_10: changeRadius(-10); break;
            case BTN_MINUS_1:  changeRadius(-1);  break;
            case BTN_PLUS_1:   changeRadius(1);   break;
            case BTN_PLUS_10:  changeRadius(10);  break;
            case BTN_PUSH:
                pushMobs = !pushMobs;
                push.displayString = pushLabel();
                sendSettings();
                break;
            case BTN_SHOW:
                ZoneDisplay.toggle(mc.theWorld, x, y, z);
                show.displayString = showLabel();
                break;
            case BTN_DONE:
                mc.displayGuiScreen(null);
                break;
            default:
                break;
        }
    }

    private void changeRadius(int delta) {
        int newRadius = AegisTorchConfig.clampRadius(radius + delta);
        if (newRadius == radius) {
            return;
        }
        radius = newRadius;
        sendSettings();
        updateButtons();
    }

    private void sendSettings() {
        torch.setSettingsClient(radius, pushMobs);   // instant preview
        AegisTorchMod.network.sendToServer(new MessageTorchSettings(x, y, z, radius, pushMobs));
    }

    private void updateButtons() {
        minus10.enabled = radius > 1;
        minus1.enabled = radius > 1;
        plus1.enabled = radius < AegisTorchConfig.maxRadius;
        plus10.enabled = radius < AegisTorchConfig.maxRadius;
    }

    private String showLabel() {
        return StatCollector.translateToLocal(ZoneDisplay.isShown(mc.theWorld, x, y, z)
                ? "gui.aegistorch.show.on" : "gui.aegistorch.show.off");
    }

    private String pushLabel() {
        return StatCollector.translateToLocal(pushMobs ? "gui.aegistorch.push.on" : "gui.aegistorch.push.off");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int cx = width / 2;
        int cy = height / 2;

        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.aegistorch.title"),
                cx, cy - 76, 0xFFD966);
        drawCenteredString(fontRendererObj,
                StatCollector.translateToLocalFormatted("gui.aegistorch.pos", x, y, z),
                cx, cy - 62, 0xA0A0A0);
        drawCenteredString(fontRendererObj,
                StatCollector.translateToLocalFormatted("gui.aegistorch.radius", radius),
                cx, cy - 28, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
                StatCollector.translateToLocalFormatted("gui.aegistorch.zone", radius * 2 + 1, radius * 2 + 1),
                cx, cy - 8, 0x80D8FF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
