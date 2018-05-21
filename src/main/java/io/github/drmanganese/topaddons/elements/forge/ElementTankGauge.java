package io.github.drmanganese.topaddons.elements.forge;

import io.github.drmanganese.topaddons.addons.forge.AddonForge;
import io.github.drmanganese.topaddons.util.ElementHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import io.netty.buffer.ByteBuf;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.network.NetworkTools;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ElementTankGauge implements IElement {

    private static final int STROKE_COLOR = 0xffaa0000;
    private static final int BG_COLOR = 0xaa550000;

    private final boolean extended;
    private final int amount, capacity, color;
    private final String tankName, fluidName;

    public ElementTankGauge(boolean extended, int amount, int capacity, String tankName, String fluidName, int color) {
        this.extended = extended;
        this.amount = amount;
        this.capacity = capacity;
        this.tankName = tankName;
        this.fluidName = fluidName;
        this.color = color;
    }

    public ElementTankGauge(ByteBuf buf) {
        this.extended = buf.readBoolean();
        this.amount = buf.readInt();
        this.capacity = buf.readInt();
        this.tankName = NetworkTools.readString(buf);
        this.fluidName = NetworkTools.readString(buf);
        this.color = buf.readInt();
    }

    @Override
    public void render(int x, int y) {
        //Background
        ElementHelper.drawBox(x, y, 100, extended ? 12 : 8, BG_COLOR, 1, STROKE_COLOR);

        //Fluid
        for (int i = 0; i < 98 * amount / capacity; i++) {
            Gui.drawRect(x + 1 + i, y + 1, x + 2 + i, y + (extended ? 11 : 7), i % 2 == 0 ? color : new Color(color).darker().hashCode());
        }

        //Gauges
        if (extended) {
            final int[] gaugeCoords = {13, 25, 37, 49, 61, 73, 85};
            final int[] gaugeLengths = {5, 6, 5, 10, 5, 6, 5};
            for (int i = 0; i < gaugeCoords.length; i++) {
                Gui.drawRect(x + gaugeCoords[i], y + 1, x + gaugeCoords[i] + 1, y + 1 + gaugeLengths[i], STROKE_COLOR);
            }
        }

        //Text
        final FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        if (extended) {
            font.drawStringWithShadow(amount + "/" + capacity + " mB", x + 2, y + 2, 0xffffffff);
            GL11.glPushMatrix();
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            font.drawStringWithShadow(tankName, x * 2, (y + 13) * 2, 0xffffffff);
            font.drawStringWithShadow(fluidName, (x + 100) * 2 - font.getStringWidth(fluidName), (y + 13) * 2, color);
            GL11.glPopMatrix();
        } else {
            GL11.glPushMatrix();
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            font.drawStringWithShadow(tankName, (x + 2) * 2, (y + 2) * 2, 0xffffffff);
            GL11.glPopMatrix();
        }
    }

    @Override
    public int getWidth() {
        return 100;
    }

    @Override
    public int getHeight() {
        return extended ? 18 : 8;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.extended);
        buf.writeInt(this.amount);
        buf.writeInt(this.capacity);
        NetworkTools.writeString(buf, this.tankName);
        NetworkTools.writeString(buf, this.fluidName);
        buf.writeInt(this.color);
    }

    @Override
    public int getID() {
        return AddonForge.TANK_ELEMENT;
    }
}
