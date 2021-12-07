package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.config.VersatileConfig;
import com.hbm.handler.FluidTypeHandler.FluidType;
import com.hbm.interfaces.IFluidAcceptor;
import com.hbm.interfaces.IFluidSource;
import com.hbm.inventory.FluidTank;
import com.hbm.inventory.OreDictManager;
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

public class TileEntityMachineEuphemiumTransmutator extends TileEntityMachineBase implements IEnergyUser, IFluidAcceptor {

	public long power = 0;
	public int process = 0;
	public static final long maxPower = 100000000;
	public static final int processSpeed = 600;
	
	private AudioWrapper audio;

	private static final int[] slots_top = new int[] { 0 };
	private static final int[] slots_bottom = new int[] { 1, 2 };
	private static final int[] slots_side = new int[] { 3, 2 };

	public FluidTank tank = new FluidTank(FluidType.BALEFIRE, 16000, 0);
	public List<IFluidAcceptor> list = new ArrayList();
	
	public int age = 0;
	
	private static final int itemNum = 12;
	private static final int consumption = 1000;
	
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
		case 4:
		case 5:
		case 6:
		case 7:
			if (stack.getItem() == ModItems.redcoil_capacitor)
				return true;
			break;
		case 8:
		case 9:
		case 10:
		case 11:
			if (MachineRecipes.mODE(stack, "nuggetSchrabidium")) //(stack.getItem() == ModItems.egg_balefire_shard)
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
		tank.readFromNBT(nbt, "tank");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		nbt.setLong("power", power);
		nbt.setInteger("process", process);
		tank.writeToNBT(nbt, "tank");
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
		for(int i = 4; i<itemNum; i++) {
			if(slots[i] == null) {
				isItemValid = false;
				break;
			}
		}
		
		// check if schrabidium is in
		// i could probably write this in a less shitty way
		// oh well lol
		boolean isSchrab = true;
		for(int i = 8; i<=11; i++) {
			if(slots[i] == null || !MachineRecipes.mODE(slots[i], OreDictManager.SA326.nugget())) {
				isSchrab = false;
				break;
			}
		}
		
		// check if redcoils are in and charged
		boolean isRedcoil = true;
		for(int i = 4; i<=7; i++) {
			if(slots[i] == null 
					|| slots[i].getItem() != ModItems.redcoil_capacitor 
					|| ItemCapacitor.getDura(slots[i]) < 2) {
				isRedcoil = false;
				break;
			}
		}
		
		if (power >= maxPower 
				&& isItemValid
				&& isSchrab
				&& isRedcoil
				
				&& tank.getFill() >= consumption
				
				&& (
					slots[0] == null || (
						slots[0].getItem() == ModItems.nugget_euphemium
						&& slots[0].stackSize < slots[0].getMaxStackSize()))
				) {
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
			
			for(int i=8;i<itemNum;i++) {
				slots[i].stackSize--;
				if(slots[i].stackSize <= 0) {
					slots[i] = null;
				}
			}
			
			for(int i=4;i<=7;i++) {
				if (slots[i] != null) {
					ItemCapacitor.setDura(slots[i], ItemCapacitor.getDura(slots[i]) - 2);
				}
			}
			
			if (slots[0] == null) {
				slots[0] = new ItemStack(ModItems.nugget_euphemium);
			} else {
				slots[0].stackSize++;
			}
			
			tank.setFill(tank.getFill() - consumption);
			
			this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "ambient.weather.thunder", 10000.0F,
					0.8F + this.worldObj.rand.nextFloat() * 0.2F);
			
			this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "random.explode", 10.0F,
					0.8F + this.worldObj.rand.nextFloat() * 0.2F);
		}
	}

	
	@Override
	public void updateEntity() {

		if (!worldObj.isRemote) {
			
			age++;
			if(age >= 20)
			{
				age = 0;
			}
			
			tank.loadTank(2, 3, slots);
			tank.updateTank(xCoord, yCoord, zCoord, worldObj.provider.dimensionId);
			
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

	// below is a bunch of garbage that is probably wrong
	// i dont understand most of it
	// i just copy pasted from the boiler, turbofan and barrel's tileentity
	// bob please dont get mad at me
	
	// "it just works" ~ todd howard
	
	@Override
	public void setFillstate(int fill, int index) {
		tank.setFill(fill);
	}

	@Override
	public void setType(FluidType type, int index) {
		tank.setTankType(type);
	}
	
	@Override
	public int getFluidFill(FluidType type) {
		return type.name().equals(this.tank.getTankType().name()) ? tank.getFill() : 0;
	}

	@Override
	public void setFluidFill(int i, FluidType type) {
		if(type.name().equals(tank.getTankType().name()))
			tank.setFill(i);
	}

	@Override
	public List<FluidTank> getTanks() {
		List<FluidTank> list = new ArrayList();
		list.add(tank);
		return list;
	}
	
	@Override
	public int getMaxFluidFill(FluidType type) {
		return type.name().equals(this.tank.getTankType().name()) ? tank.getMaxFill() : 0;
	}
}
