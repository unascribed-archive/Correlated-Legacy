package com.elytradev.correlated.entity.ai;

import com.elytradev.correlated.entity.EntityAutomaton;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;

public class EntityAIAutomatonOwnerHurtByTarget extends EntityAITarget {
	EntityAutomaton theDefendingTameable;
	EntityLivingBase theOwnerAttacker;
	private int timestamp;

	public EntityAIAutomatonOwnerHurtByTarget(EntityAutomaton theDefendingTameableIn) {
		super(theDefendingTameableIn, false);
		this.theDefendingTameable = theDefendingTameableIn;
		this.setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {
		if (!this.theDefendingTameable.isTamed()) {
			return false;
		} else {
			EntityLivingBase entitylivingbase = this.theDefendingTameable.getOwner();

			if (entitylivingbase == null) {
				return false;
			} else {
				this.theOwnerAttacker = entitylivingbase.getAITarget();
				int i = entitylivingbase.getRevengeTimer();
				return i != this.timestamp && this.isSuitableTarget(this.theOwnerAttacker, false);
			}
		}
	}

	@Override
	public void startExecuting() {
		this.taskOwner.setAttackTarget(this.theOwnerAttacker);
		EntityLivingBase entitylivingbase = this.theDefendingTameable.getOwner();

		if (entitylivingbase != null) {
			this.timestamp = entitylivingbase.getRevengeTimer();
		}

		super.startExecuting();
	}
}