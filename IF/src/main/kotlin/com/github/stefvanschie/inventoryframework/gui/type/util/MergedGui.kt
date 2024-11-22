package com.github.stefvanschie.inventoryframework.gui.type.util

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.pane.Pane
import org.jetbrains.annotations.Contract

/**
 * Represents a chest-like gui in which the top and bottom inventories are merged together and only exist of one
 * inventory component.
 *
 * @since 0.8.1
 */
interface MergedGui {
    /**
     * Adds a pane to this gui
     *
     * @param pane the pane to add
     * @since 0.8.1
     */
    fun addPane(pane: Pane)

    @get:Contract(pure = true)
    val panes: List<Pane?>

    @get:Contract(pure = true)
    val items: Collection<GuiItem>

    @get:Contract(pure = true)
    val inventoryComponent: InventoryComponent
}
