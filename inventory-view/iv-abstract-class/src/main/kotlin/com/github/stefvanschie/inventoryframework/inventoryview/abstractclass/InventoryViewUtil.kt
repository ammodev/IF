package com.github.stefvanschie.inventoryframework.inventoryview.abstractclass

import com.github.stefvanschie.inventoryframework.inventoryview.abstraction.AbstractInventoryViewUtil
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

/**
 * A wrapper for [InventoryView] methods that apply when [InventoryView] was an abstract class.
 *
 * @since 0.10.16
 */
object InventoryViewUtil : AbstractInventoryViewUtil {
    
    override fun getBottomInventory(view: InventoryView) = view.bottomInventory

    override fun getCursor(view: InventoryView) = view.cursor

    override fun setCursor(view: InventoryView, item: ItemStack?) {
        view.cursor = item
    }

    override fun getInventory(view: InventoryView, slot: Int) = view.getInventory(slot)

    override fun getSlotType(view: InventoryView, slot: Int) = view.getSlotType(slot)

    override fun getTitle(view: InventoryView) = view.title

    override fun getTopInventory(view: InventoryView) = view.topInventory
}
