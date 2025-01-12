/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020 TechReborn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.hhoao.mc.ironelevators;



import net.minecraft.block.Block;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Optional;


public class ElevatorController {
	public boolean isElevator(World world, BlockPos targetPos) {
		return Config.getElevatorBlockMaxHeightMap().containsKey(world.getBlockState(targetPos).getBlock());
	}

	private boolean isAir(World world, BlockPos targetPos) {
		return world.getBlockState(targetPos).isAir();
	}

	private boolean isSpaceFree(World world, BlockPos targetPos) {
		return world.getBlockState(targetPos.up()).isAir() && world.getBlockState(targetPos.up().up()).isAir();
	}

	private Optional<BlockPos> nextUpElevator(World world, BlockPos pos, int maxTeleportHeight) {
		BlockPos upPos = pos;
		for (int i = 0; i < maxTeleportHeight; i++) {
			upPos = upPos.up();
			if (upPos.getY() > world.getHeight()) {
				return Optional.empty();
			}
			if (isElevator(world, upPos) && isSpaceFree(world, upPos)) {
				return Optional.of(upPos);
			} else if (!isAir(world, upPos) && !Config.isAllowElevatingThroughBlocks()){
				return Optional.empty();
			}
		}
		return Optional.empty();
	}

	private Optional<BlockPos> nextDownElevator(World world, BlockPos pos, int maxTeleportHeight) {
		BlockPos downPos = pos;
		for (int i = 0; i < maxTeleportHeight; i++) {
			downPos = downPos.down();
			if (downPos.getY() < 0) {
				return Optional.empty();
			}
			if (isElevator(world, downPos) && isSpaceFree(world, downPos)) {
				return Optional.of(downPos);
			} else if (!isAir(world, downPos) && !Config.isAllowElevatingThroughBlocks()){
				return Optional.empty();
			}
		}
		return Optional.empty();
	}

	public boolean tryTeleport(ServerPlayerEntity player, boolean up) {
		if (!isElevator(player.world, player.getPosition().down())) {
			return false;
		}
		int maxTeleportHeight = getMaxTeleportHeight(player.world.getBlockState(player.getPosition().down()).getBlock());
		Optional<BlockPos> target = up ?
			nextUpElevator(player.world, player.getPosition().down(), maxTeleportHeight) :
			nextDownElevator(player.world, player.getPosition().down(), maxTeleportHeight);
		if (!target.isPresent()) {
			return false;
		}
		player.teleportKeepLoaded(player.getPosX(), target.get().getY()+1, player.getPosZ());
		return true;
	}

	private int getMaxTeleportHeight(Block block) {
		Map<Block, Integer> elevatorBlocks = Config.getElevatorBlockMaxHeightMap();
		Integer i = elevatorBlocks.get(block);
		if (i != -1) {
			return i;
		} else {
			return Config.getDefaultMaxTeleportHeight();
		}
	}
}
