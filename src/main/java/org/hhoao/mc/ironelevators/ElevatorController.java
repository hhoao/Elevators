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



import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.Optional;


public class ElevatorController {
	public boolean isElevator(Level world, BlockPos targetPos) {
		return Config.getElevatorBlockMaxHeightMap().containsKey(world.getBlockState(targetPos).getBlock());
	}

	private boolean isAir(Level world, BlockPos targetPos) {
		return world.getBlockState(targetPos).isAir();
	}

	private boolean isSpaceFree(Level world, BlockPos targetPos) {
		return world.getBlockState(targetPos.above()).isAir() && world.getBlockState(targetPos.above().above()).isAir();
	}

	private Optional<BlockPos> nextUpElevator(Level world, BlockPos pos, int maxTeleportHeight) {
		BlockPos upPos = pos;
		for (int i = 0; i < maxTeleportHeight; i++) {
			upPos = upPos.above();
			if (upPos.getY() > world.getMaxBuildHeight()) {
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

	private Optional<BlockPos> nextDownElevator(Level world, BlockPos pos, int maxTeleportHeight) {
		BlockPos downPos = pos;
		for (int i = 0; i < maxTeleportHeight; i++) {
			downPos = downPos.below();
			if (downPos.getY() < world.getMinBuildHeight()) {
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

	public boolean tryTeleport(ServerPlayer player, boolean up) {
		if (!isElevator(player.level, player.getOnPos())) {
			return false;
		}
		int maxTeleportHeight = getMaxTeleportHeight(player.level.getBlockState(player.getOnPos()).getBlock());
		Optional<BlockPos> target = up ?
			nextUpElevator(player.level, player.getOnPos(), maxTeleportHeight) :
			nextDownElevator(player.level, player.getOnPos(), maxTeleportHeight);
		if (target.isEmpty()) {
			return false;
		}
		player.teleportTo(player.getX(), target.get().getY()+1, player.getZ());
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
