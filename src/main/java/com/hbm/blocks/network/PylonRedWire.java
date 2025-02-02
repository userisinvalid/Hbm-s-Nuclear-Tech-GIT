package com.hbm.blocks.network;

import com.hbm.tileentity.network.TileEntityPylon;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PylonRedWire extends PylonBase {

	public PylonRedWire(Material material) {
		super(material);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityPylon();
	}
}
