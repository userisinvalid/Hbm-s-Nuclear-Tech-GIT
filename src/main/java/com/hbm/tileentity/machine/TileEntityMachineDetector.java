package com.hbm.tileentity.machine;

import api.hbm.energy.IEnergyUser;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMachineDetector extends TileEntity implements IEnergyUser {
	
	long power;

	@Override
    public void updateEntity() {
		
		if(!worldObj.isRemote) {
			
			this.updateConnections();
			
			int meta = this.getBlockMetadata();
			int state = 0;
			
			if(power > 0) {
				state = 1;
				power--;
			}
			
			if(meta != state) {
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, state, 3);
				this.markDirty();
			}
		}
	}
	
	private void updateConnections() {
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			this.trySubscribe(worldObj, xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
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
		return 20;
	}

}
