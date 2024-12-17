package cn.elytra.mod.tc4h.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

import cn.elytra.mod.tc4h.TCResearchHelperManager;

public class HelperButton extends GuiButton {

    public HelperButton(int buttonId, int x, int y, int width, int height) {
        super(buttonId, x, y, width, height, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        enabled = TCResearchHelperManager.isIdle();
        displayString = TCResearchHelperManager.isIdle() ? I18n.format("tc4helper.button.idle")
            : I18n.format("tc4helper.button.running");

        super.drawButton(mc, mouseX, mouseY);
    }
}
