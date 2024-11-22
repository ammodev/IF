package com.github.stefvanschie.inventoryframework.gui.type.util

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.jetbrains.annotations.Contract

interface InventoryBased : InventoryHolder {
    /**
     * Creates a new inventory of the type of the implementing class.
     *
     * @return the new inventory
     * @since 0.10.0
     */
    @Contract(pure = true)
    fun createInventory(): Inventory
}
