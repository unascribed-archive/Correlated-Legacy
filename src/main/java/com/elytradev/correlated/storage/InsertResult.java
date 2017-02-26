package com.elytradev.correlated.storage;

import net.minecraft.item.ItemStack;

public class InsertResult {

	public enum Result {
		/**
		 * The insert operation was successful. However, only some items may
		 * have been inserted. If none of the items could be inserted, use
		 * {@link #INSUFFICIENT_MEMORY} or {@link #INSUFFICIENT_STORAGE}
		 * instead.
		 */
		SUCCESS,
		/**
		 * The insert operation was successful, and the items have been voided.
		 * They cannot be retrieved.
		 */
		SUCCESS_VOIDED,
		/**
		 * There is not enough memory to store even one of the items that were
		 * offered.
		 */
		INSUFFICIENT_MEMORY,
		/**
		 * There is not enough storage to store even one of the items that were
		 * offered.
		 */
		INSUFFICIENT_STORAGE,
		/**
		 * The item has been refused as it is incompatible with the storage.
		 * Further attempts to insert the item will never succeed unless the
		 * storage is reconfigured by the user.
		 */
		ITEM_INCOMPATIBLE,
		/**
		 * The item has been refused for gameplay reasons, such as a network
		 * error. The failure is not permanent and is likely to be resolved by
		 * the user soon.
		 */
		REFUSED,
		/**
		 * An internal error has occurred in the code responsible for putting
		 * this item in storage.
		 */
		INTERNAL_ERROR,
		/**
		 * The storage is read-only and insertion attempts will never succeed.
		 */
		READ_ONLY
	}
	
	public final Result result;
	public final ItemStack stack;
	
	public InsertResult(Result result, ItemStack stack) {
		this.result = result;
		this.stack = stack;
	}
	
	public boolean wasSuccessful() {
		return result == Result.SUCCESS || result == Result.SUCCESS_VOIDED;
	}
	
	
	public static InsertResult success(ItemStack stack) {
		return new InsertResult(Result.SUCCESS, stack);
	}
	
	public static InsertResult successVoided(ItemStack stack) {
		return new InsertResult(Result.SUCCESS_VOIDED, stack);
	}
	
	public static InsertResult insufficientMemory(ItemStack stack) {
		return new InsertResult(Result.INSUFFICIENT_MEMORY, stack);
	}
	
	public static InsertResult insufficientStorage(ItemStack stack) {
		return new InsertResult(Result.INSUFFICIENT_STORAGE, stack);
	}
	
	public static InsertResult itemIncompatible(ItemStack stack) {
		return new InsertResult(Result.ITEM_INCOMPATIBLE, stack);
	}
	
	public static InsertResult refused(ItemStack stack) {
		return new InsertResult(Result.REFUSED, stack);
	}
	
	public static InsertResult internalError(ItemStack stack) {
		return new InsertResult(Result.INTERNAL_ERROR, stack);
	}
	
	public static InsertResult readOnly(ItemStack stack) {
		return new InsertResult(Result.READ_ONLY, stack);
	}
}
