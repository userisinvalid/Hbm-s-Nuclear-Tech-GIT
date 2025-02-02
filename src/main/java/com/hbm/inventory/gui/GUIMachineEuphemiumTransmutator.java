package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hbm.inventory.FluidTank;
import com.hbm.inventory.container.ContainerMachineEuphemiumTransmutator;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineEuphemiumTransmutator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIMachineEuphemiumTransmutator extends GuiInfoContainer {
	
	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/gui_etd.png");
	private TileEntityMachineEuphemiumTransmutator diFurnace;

	public GUIMachineEuphemiumTransmutator(InventoryPlayer invPlayer, TileEntityMachineEuphemiumTransmutator tedf) {
		super(new ContainerMachineEuphemiumTransmutator(invPlayer, tedf));
		diFurnace = tedf;
		
		this.xSize = 176;
		this.ySize = 222;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		
		diFurnace.tank.renderTankInfo(this, mouseX, mouseY, guiLeft + 152, guiTop + 106 - 88, 16, 88);
		
		this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 8, guiTop + 106 - 88, 16, 88, diFurnace.power, diFurnace.maxPower);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.diFurnace.hasCustomInventoryName() ? this.diFurnace.getInventoryName() : I18n.format(this.diFurnace.getInventoryName());
		
		this.fontRendererObj.drawString(name, this.xSize / 2 - this.fontRendererObj.getStringWidth(name) / 2, 6, 4210752);
		this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(diFurnace.getPower() > 0) {
			int i = (int)diFurnace.getPowerScaled(88);
			drawTexturedModalRect(guiLeft + 8, guiTop + 106 - i, 176, 88 - i, 16, i);
		}
		
		if(diFurnace.isProcessing())
		{
			int j1 = diFurnace.getProgressScaled(54);
			drawTexturedModalRect(guiLeft + 61, guiTop + 43, 176, 88, j1, 54);
		}
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(diFurnace.tank.getSheet());
		diFurnace.tank.renderTank(this, guiLeft + 152, guiTop + 106, diFurnace.tank.getTankType().textureX() * FluidTank.x, diFurnace.tank.getTankType().textureY() * FluidTank.y, 16, 88);	
	}
}
