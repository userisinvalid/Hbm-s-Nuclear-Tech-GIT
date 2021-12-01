package com.hbm.tileentity.machine;

import com.hbm.config.VersatileConfig;
import com.hbm.inventory.recipes.MachineRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemCapacitor;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energy.IBatteryItem;
import api.hbm.energy.IEnergyUser;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMachineEuphemiumTransmutator extends TileEntityMachineBase implements IEnergyUser {

	public long power = 0;
	public int process = 0;
	public static final long maxPower = 100000000;
	public static final int processSpeed = 600;
	
	private AudioWrapper audio;

	private static final int[] slots_top = new int[] { 0 };
	private static final int[] slots_bottom = new int[] { 1, 2 };
	private static final int[] slots_side = new int[] { 3, 2 };

	private static final int itemNum = 14;
	
	public TileEntityMachineEuphemiumTransmutator() {
		super(itemNum);
	}

	@Override
	public String getName() {
		return "container.machine_euphemium_transmutator";
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		switch (i) {
		case 1:
			if (stack.getItem() instanceof IBatteryItem)
				return true;
			break;
		case 2:
		case 3:
		case 4:
		case 5:
			if (stack.getItem() == ModItems.redcoil_capacitor)
				return true;
			break;
		case 6:
		case 7:
		case 8:
		case 9:
			if (stack.getItem() == ModItems.egg_balefire_shard)
				return true;
			break;
		case 10:
		case 11:
		case 12:
		case 13:
			if (MachineRecipes.mODE(stack, "ingotSchrabidium"))
				return true;
			break;
		}
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		power = nbt.getLong("power");
		process = nbt.getInteger("process");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setInteger("process", process);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
		return p_94128_1_ == 0 ? slots_bottom : (p_94128_1_ == 1 ? slots_top : slots_side);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack stack, int j) {
		
		if (i == 2 && stack.getItem() != null && stack.getItem() == ModItems.redcoil_capacitor && ItemCapacitor.getDura(stack) <= 0) {
			return true;
		}

		if (i == 1) {
			return true;
		}

		if (i == 3) {
			if (stack.getItem() instanceof IBatteryItem && ((IBatteryItem)stack.getItem()).getCharge(stack) == 0)
				return true;
		}

		return false;
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	public int getProgressScaled(int i) {
		return (process * i) / processSpeed;
	}
	
	
	
	public boolean canProcess() {
		
		// check all slots other than battery and output
		boolean isItemValid = true;
		for(int i = 2; i<itemNum; i++) {
			if(slots[i] == null) {
				isItemValid = false;
				break;
			}
		}
		
		// check if schrabidium is in
		boolean isSchrab = true;
		for(int i = 10; i<=13; i++) {
			if(slots[i] == null || MachineRecipes.mODE(slots[i], "ingotSchrabidium")) {
				isSchrab = false;
				break;
			}
		}
		
		// check if redcoils are in and charged
		boolean isRedcoil = true;
		for(int i = 2; i<=5; i++) {
			if(slots[i] == null || slots[i].getItem() != ModItems.redcoil_capacitor || ItemCapacitor.getDura(slots[i]) < 2) {
				isRedcoil = false;
				break;
			}
		}
		
		if (power >= maxPower 
				&& isItemValid
				&& isSchrab
				&& isRedcoil
				
				&& (slots[0] == null || (slots[4] != null && slots[0].getItem() == ModItems.ingot_euphemium
						&& slots[0].stackSize < slots[0].getMaxStackSize()))) {
			return true;
		}
		return false;
	}

	public boolean isProcessing() {
		return process > 0;
	}

	public void process() {
		process++;

		if (process >= processSpeed) {

			power = 0;
			process = 0;
			
			for(int i=6;i<itemNum;i++) {
				slots[i].stackSize--;
				if(slots[i].stackSize <= 0) {
					slots[i] = null;
				}
			}
			
			for(int i=2;i<=5;i++) {
				if (slots[i] != null) {
					ItemCapacitor.setDura(slots[i], ItemCapacitor.getDura(slots[i]) - 2);
				}
			}
			
			if (slots[0] == null) {
				slots[0] = new ItemStack(ModItems.ingot_euphemium);
			} else {
				slots[0].stackSize++;
			}

			this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "ambient.weather.thunder", 10000.0F,
					0.8F + this.worldObj.rand.nextFloat() * 0.2F);
		}
	}

	@Override
	public void updateEntity() {

		if (!worldObj.isRemote) {
			
			this.updateConnections();
			
			power = Library.chargeTEFromItems(slots, 1, power, maxPower);

			if(canProcess()) {
				process();
			} else {
				process = 0;
			}
			
			NBTTagCompound data = new NBTTagCompound();
			data.setLong("power", power);
			data.setInteger("progress", process);
			this.networkPack(data, 50);
			
		} else {

			if(process > 0) {
				
				if(audio == null) {
					audio = MainRegistry.proxy.getLoopedSound("hbm:weapon.tauChargeLoop", xCoord, yCoord, zCoord, 1.0F, 1.0F);
					audio.startSound();
				}
			} else {
				
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}
	
	private void updateConnections() {
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			this.trySubscribe(worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
	}
	
    public void onChunkUnload() {
    	
    	if(audio != null) {
			audio.stopSound();
			audio = null;
    	}
    }
	
    public void invalidate() {
    	
    	super.invalidate();
    	
    	if(audio != null) {
			audio.stopSound();
			audio = null;
    	}
    }
	
	@Override
	public void networkUnpack(NBTTagCompound data) {

		this.power = data.getLong("power");
		this.process = data.getInteger("progress");
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}
}
