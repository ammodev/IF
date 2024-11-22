package com.github.stefvanschie.inventoryframework.inventoryview.abstraction

import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

/**
 * A wrapper for [InventoryView] methods that apply when [InventoryView] was an abstract class.
 *
 * @since 0.10.16
 */
interface AbstractInventoryViewUtil {

    /**
     * Behaves according to [InventoryView.getBottomInventory].
     *
     * @param view the [InventoryView] to invoke [InventoryView.getBottomInventory] on
     * @return the result of invoking [InventoryView.getBottomInventory]
     * @since 0.10.16
     */
    fun getBottomInventory(view: InventoryView): Inventory

    /**
     * Behaves according to [InventoryView.getCursor].
     *
     * @param view the [InventoryView] to invoke [InventoryView.getCursor] on
     * @return the result of invoking [InventoryView.getCursor]
     * @since 0.10.16
     */
    fun getCursor(view: InventoryView): ItemStack?

    /**
     * Behaves according to [InventoryView.setCursor].
     *
     * @param view the [InventoryView] to invoke [InventoryView.setCursor] on
     * @param item the [ItemStack] to apply when invoking [InventoryView.setCursor]
     * @since 0.10.16
     */
    fun setCursor(view: InventoryView, item: ItemStack?)

    /**
     * Behaves according to [InventoryView.getInventory].
     *
     * @param view the [InventoryView] to invoke [InventoryView.getInventory] on
     * @param slot the slot to apply when invoking [InventoryView.getInventory]
     * @return the result of invoking [InventoryView.getInventory]
     * @since 0.10.16
     */
    fun getInventory(view: InventoryView, slot: Int): Inventory?

    /**
     * Behaves according to [InventoryView.getSlotType].
     *
     * @param view the [InventoryView] to invoke [InventoryView.getSlotType] on
     * @param slot the slot to apply when invoking [InventoryView.getSlotType]
     * @return the result of invoking [InventoryView.getSlotType]
     * @since 0.10.16
     */
    fun getSlotType(view: InventoryView, slot: Int): InventoryType.SlotType

    /**
     * Behaves according to [InventoryView.getTitle].
     *
     * @param view the [InventoryView] to invoke [InventoryView.getTitle] on
     * @return the result of invoking [InventoryView.getTitle]
     * @since 0.10.16
     */
    fun getTitle(view: InventoryView): String

    /**
     * Behaves according to [InventoryView.getTopInventory].
     *
     * @param view the [InventoryView] to invoke [InventoryView.getTopInventory] on
     * @return the result of invoking [InventoryView.getTopInventory]
     * @since 0.10.16
     */
    fun getTopInventory(view: InventoryView): Inventory
}
