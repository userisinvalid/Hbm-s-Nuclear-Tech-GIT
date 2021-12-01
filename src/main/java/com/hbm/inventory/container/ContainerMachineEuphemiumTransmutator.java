package com.hbm.inventory.container;

import com.hbm.inventory.SlotMachineOutput;
import com.hbm.tileentity.machine.TileEntityMachineEuphemiumTransmutator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMachineEuphemiumTransmutator extends Container {

private TileEntityMachineEuphemiumTransmutator nukeBoy;
	
	public ContainerMachineEuphemiumTransmutator(InventoryPlayer invPlayer, TileEntityMachineEuphemiumTransmutator tedf) {
		
		nukeBoy = tedf;
		
		this.addSlotToContainer(new SlotMachineOutput(tedf, 0, 98, 63)); //output
		this.addSlotToContainer(new Slot(tedf, 1, 8, 108)); //battery
		
		this.addSlotToContainer(new Slot(tedf, 2, 26, 18)); //redcoil 1
		this.addSlotToContainer(new Slot(tedf, 3, 26, 36)); //redcoil 2
		this.addSlotToContainer(new Slot(tedf, 4, 26, 54)); //redcoil 3
		this.addSlotToContainer(new Slot(tedf, 5, 26, 72)); //redcoil 4
	
		this.addSlotToContainer(new Slot(tedf, 6, 98, 45)); //balefire 1
		this.addSlotToContainer(new Slot(tedf, 7, 80, 63)); //balefire 2
		this.addSlotToContainer(new Slot(tedf, 8, 116, 63)); //balefire 3
		this.addSlotToContainer(new Slot(tedf, 9, 98, 81)); //balefire 4
		
		this.addSlotToContainer(new Slot(tedf, 10, 98, 27)); //schrab input 1
		this.addSlotToContainer(new Slot(tedf, 11, 62, 63)); //schrab input 2
		this.addSlotToContainer(new Slot(tedf, 12, 134, 63)); //schrab input 3
		this.addSlotToContainer(new Slot(tedf, 13, 98, 99)); //schrab input 4
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 56));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + 56));
		}
	}
	
	@Override
    public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int par2)
    {
		ItemStack var3 = null;
		Slot var4 = (Slot) this.inventorySlots.get(par2);
		
		if (var4 != null && var4.getHasStack())
		{
			ItemStack var5 = var4.getStack();
			var3 = var5.copy();
			
            if (par2 <= 3) {
				if (!this.mergeItemStack(var5, 4, this.inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if (!this.mergeItemStack(var5, 0, 1, false))
			{
				if (!this.mergeItemStack(var5, 3, 4, false))
					if (!this.mergeItemStack(var5, 2, 3, false))
						return null;
			}
			
			if (var5.stackSize == 0)
			{
				var4.putStack((ItemStack) null);
			}
			else
			{
				var4.onSlotChanged();
			}
		}
		
		return var3;
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return nukeBoy.isUseableByPlayer(player);
	}
	
	@Override
	public void updateProgressBar(int i, int j) {
		if(i == 1)
		{
			nukeBoy.power = j;
		}
	}
}
