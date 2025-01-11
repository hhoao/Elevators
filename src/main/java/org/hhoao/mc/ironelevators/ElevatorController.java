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

import java.util.Optional;


public class ElevatorController {
	private boolean isElevator(Level world, BlockPos targetPos) {
		return world.getBlockState(targetPos).getBlock() == Config.getElevatorBlock();
	}

	private boolean isAir(Level world, BlockPos targetPos) {
		return world.getBlockState(targetPos).isAir();
	}

	private boolean isSpaceFree(Level world, BlockPos targetPos) {
		return world.getBlockState(targetPos.above()).isAir() && world.getBlockState(targetPos.above().above()).isAir();
	}

	private Optional<BlockPos> nextUpElevator(Level world, BlockPos pos) {
		BlockPos upPos = pos;
		for (int i = 0; i < Config.getMaxTeleportHeight(); i++) {
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

	private Optional<BlockPos> nextDownElevator(Level world, BlockPos pos) {
		BlockPos downPos = pos;
		for (int i = 0; i < Config.getMaxTeleportHeight(); i++) {
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

	public boolean tryTeleportUp(ServerPlayer player) {
		if (!isElevator(player.level(), player.getOnPos())) {
			return false;
		}
		Optional<BlockPos> upTarget = nextUpElevator(player.level(), player.getOnPos());
		if (upTarget.isEmpty()) {
			return false;
		}
		BlockPos blockPos = upTarget.get();
		player.teleportTo(player.getX(), blockPos.getY()+1, player.getZ());
		player.setJumping(false);
		return true;
	}

	public boolean tryTeleportDown(ServerPlayer player) {
		if (!isElevator(player.level(), player.getOnPos())) {
			return false;
		}
		Optional<BlockPos> downTarget = nextDownElevator(player.level(), player.getOnPos());
		if (downTarget.isEmpty()) {
			return false;
		}
		BlockPos blockPos = downTarget.get();
		player.teleportTo(player.getX(), blockPos.getY()+1, player.getZ());
		return true;
	}
}
