package com.elytradev.correlated.entity.ai;

import com.elytradev.correlated.entity.EntityAutomaton;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;

public class EntityAIAutomatonOwnerHurtTarget extends EntityAITarget {
	EntityAutomaton theEntityTameable;
	EntityLivingBase theTarget;
	private int timestamp;

	public EntityAIAutomatonOwnerHurtTarget(EntityAutomaton theEntityTameableIn) {
		super(theEntityTameableIn, false);
		this.theEntityTameable = theEntityTameableIn;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		if (!this.theEntityTameable.isTamed()) {
			return false;
		} else {
			EntityLivingBase entitylivingbase = this.theEntityTameable.getOwner();

			if (entitylivingbase == null) {
				return false;
			} else {
				this.theTarget = entitylivingbase.getLastAttacker();
				int i = entitylivingbase.getLastAttackerTime();
				return i != this.timestamp && this.isSuitableTarget(this.theTarget, false);
			}
		}
	}

	@Override
	public void startExecuting() {
		this.taskOwner.setAttackTarget(this.theTarget);
		EntityLivingBase entitylivingbase = this.theEntityTameable.getOwner();

		if (entitylivingbase != null) {
			this.timestamp = entitylivingbase.getLastAttackerTime();
		}

		super.startExecuting();
	}
}