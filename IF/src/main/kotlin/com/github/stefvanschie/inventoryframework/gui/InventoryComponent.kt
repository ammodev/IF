package com.github.stefvanschie.inventoryframework.gui

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased
import com.github.stefvanschie.inventoryframework.pane.Pane
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.util.*

/**
 * Represents a component within an inventory that can hold items. This is always in the shape of a
 * rectangular grid.
 *
 * @since 0.8.0
 */
class InventoryComponent(length: Int, height: Int) {
    /**
     * Gets a list of panes this inventory component contains. The returned list is modifiable. If
     * this inventory component currently does not have any panes, an empty list is returned. This
     * list is guaranteed to be sorted according to the panes' priorities.
     *
     * @return the panes this component has
     * @since 0.8.0
     */
    /**
     * A set of all panes in this inventory. This is guaranteed to be sorted in order of the pane's
     * priorities, from the lowest priority to the highest priority. The order of panes with the same
     * priority is unspecified.
     */
    @JvmField
    @get:Contract(pure = true)
    val panes: MutableList<Pane> = ArrayList()

    /**
     * The items this inventory component has, stored in row-major order. Slots that are empty are
     * represented as null.
     */
    private val items: Array<Array<ItemStack?>>

    /**
     * Gets the length of this inventory component.
     *
     * @return the length
     * @since 0.8.0
     */
    /**
     * The length and height of this inventory component
     */
    @JvmField
    @get:Contract(pure = true)
    val length: Int

    /**
     * Gets the height of this inventory component.
     *
     * @return the height
     * @since 0.8.0
     */
    @JvmField
    @get:Contract(pure = true)
    val height: Int

    /**
     * Creates a new inventory component with the specified length and width. If either the length or
     * the width is less than zero, an [IllegalArgumentException] will be thrown.
     *
     * @param length the length of the component
     * @param height the height of the component
     * @since 0.8.0
     */
    init {
        require(!(length < 0 || height < 0)) { "Sizes must be greater or equal to zero" }

        this.length = length
        this.height = height

        this.items = Array(length) { arrayOfNulls(height) }
    }

    /**
     * Adds a pane to the current collection of panes.
     *
     * @param pane the pane to add
     * @since 0.8.0
     */
    fun addPane(pane: Pane) {
        val size: Int = panes.size

        if (size == 0) {
            panes.add(pane)

            return
        }

        val priority: Pane.Priority = pane.priority

        var left: Int = 0
        var right: Int = size - 1

        while (left <= right) {
            val middle: Int = (left + right) / 2

            val middlePriority: Pane.Priority = getPane(middle).priority

            if (middlePriority === priority) {
                panes.add(middle, pane)

                return
            }

            if (middlePriority.isLessThan(priority)) {
                left = middle + 1
            } else if (middlePriority.isGreaterThan(priority)) {
                right = middle - 1
            }
        }

        panes.add(right + 1, pane)
    }

    /**
     * This will make each pane in this component render their items in this inventory component. The
     * panes are displayed according to their priority, with the lowest priority rendering first and
     * the highest priority (note: highest priority, not [Pane.Priority.HIGHEST] priority)
     * rendering last. The items displayed in this inventory component will be put into the specified
     * inventory. The slots will start at the given offset up to this component's size + the offset
     * specified.
     *
     * @param inventory the inventory to place the items in
     * @param offset    the offset from which to start counting the slots
     * @see .display
     * @since 0.8.0
     */
    fun display(inventory: Inventory, offset: Int) {
        display()

        placeItems(inventory, offset)
    }

    /**
     * This will make each pane in this component render their items in this inventory component. The
     * panes are displayed according to their priority, with the lowest priority rendering first and
     * the highest priority (note: highest priority, not [Pane.Priority.HIGHEST] priority)
     * rendering last. The items displayed in this inventory component will be put into the inventory
     * found in [InventoryBased.getInventory]. The slots will be placed from the top-right to
     * the bottom-left, continuing from left-to-right, top-to-bottom plus the specified offset. This
     * ordering is different from the normal ordering of the indices of a [PlayerInventory]. See
     * for the normal ordering of a [PlayerInventory]'s slots its documentation.
     *
     * @param inventory the inventory to place the items in
     * @param offset    the offset from which to start counting the slots
     * @see .display
     * @since 0.8.0
     */
    fun display(inventory: PlayerInventory, offset: Int) {
        display()

        placeItems(inventory, offset)
    }

    /**
     * This places the items currently existing in this inventory component into the specified player
     * inventory. The slots will be placed from the top-right to the bottom-left, continuing from
     * left-to-right, top-to-bottom plus the specified offset. This ordering is different from the
     * normal ordering of the indices of a [PlayerInventory]. See for the normal ordering of a
     * [PlayerInventory]'s slots its documentation. In contrast to
     * [.display] this does not render the panes of this component.
     *
     * @param inventory the inventory to place the items in
     * @param offset    the offset from which to start counting the slots
     * @see .placeItems
     * @since 0.8.0
     */
    fun placeItems(inventory: PlayerInventory, offset: Int) {
        for (x in 0 until length) {
            for (y in 0 until height) {
                var slot: Int

                if (y == height - 1) {
                    slot = x + offset
                } else {
                    slot = (y + 1) * length + x + offset
                }

                inventory.setItem(slot, getItem(x, y))
            }
        }
    }

    /**
     * This places the items currently existing in this inventory component into the specified
     * inventory. The slots will start at the given offset up to this component's size + the offset
     * specified. In contrast to [.display] this does not render the panes of
     * this component.
     *
     * @param inventory the inventory to place the items in
     * @param offset    the offset from which to start counting the slots
     * @see .placeItems
     * @since 0.8.0
     */
    fun placeItems(inventory: Inventory, offset: Int) {
        for (x in 0 until length) {
            for (y in 0 until height) {
                inventory.setItem(y * length + x + offset, getItem(x, y))
            }
        }
    }

    /**
     * Delegates the handling of the specified click event to the panes of this component. This will
     * call [Pane.click]
     * on each pane until the right item has been found.
     *
     * @param gui   the gui this inventory component belongs to
     * @param event the event to delegate
     * @param slot  the slot that was clicked
     * @since 0.8.0
     */
    fun click(gui: Gui, event: InventoryClickEvent, slot: Int) {
        val panes: List<Pane> = ArrayList(panes)

        //loop panes in reverse, because the highest priority pane (last in list) is most likely to have the right item
        for (i in panes.indices.reversed()) {
            if (panes.get(i).click(
                    gui, this, event, slot, 0, 0, length, height
                )
            ) {
                break
            }
        }
    }

    /**
     * Creates a deep copy of this inventory component. This means that all internal items will be
     * cloned and all panes will be copied as per their own [ItemStack.clone] and
     * [Pane.copy] methods. The returned inventory component is guaranteed to not reference
     * equals this inventory component.
     *
     * @return the new inventory component
     * @since 0.8.0
     */
    @Contract(pure = true)
    fun copy(): InventoryComponent {
        val inventoryComponent: InventoryComponent = InventoryComponent(length, height)

        for (x in 0 until length) {
            for (y in 0 until height) {
                val item: ItemStack? = getItem(x, y)

                if (item == null) {
                    continue
                }

                inventoryComponent.setItem(item.clone(), x, y)
            }
        }

        for (pane: Pane in panes) {
            inventoryComponent.addPane(pane.copy())
        }

        return inventoryComponent
    }

    /**
     * Returns a new inventory component, excluding the range of specified rows. The new inventory
     * component will have its size shrunk so only the included rows are present and any items in the
     * excluded rows are discarded. All panes will stay present. Note that while this does make a new
     * inventory component, it does not make a copy. For example, the panes in the new inventory
     * component will be the exact same panes as in this one and will not be copied. This is also true
     * for any retained items. The specified range is 0-indexed: the first row starts at index 0 and
     * the last row ends at height - 1. The range is inclusive on both ends, the row specified at
     * either parameter will also be excluded. When the range specified is invalid - that is, part of
     * the range contains rows that are not included in this inventory component, and
     * [IllegalArgumentException] will be thrown.
     *
     * @param from the starting index of the range
     * @param end  the ending index of the range
     * @return the new, shrunk inventory component
     * @since 0.8.0
     */
    @Contract(pure = true)
    fun excludeRows(from: Int, end: Int): InventoryComponent {
        require(!(from < 0 || end >= height)) { "Specified range includes non-existent rows" }

        val newHeight: Int = height - (end - from + 1)

        val newInventoryComponent: InventoryComponent = InventoryComponent(length, newHeight)

        for (pane: Pane in panes) {
            newInventoryComponent.addPane(pane)
        }

        for (x in 0 until length) {
            var newY: Int = 0

            for (y in 0 until height) {
                val item: ItemStack? = getItem(x, y)

                if (y >= from && y <= end) {
                    continue
                }

                if (item != null) {
                    newInventoryComponent.setItem(item, x, newY)
                }

                newY++
            }
        }

        return newInventoryComponent
    }

    /**
     * Loads the provided element's child panes onto this component. If the element contains any child
     * panes, this will mutate this component.
     *
     * @param instance the instance to apply field and method references on
     * @param element  the element to load
     * @since 0.8.0
     */
    @Deprecated("superseded by {@link #load(Object, Element, Plugin)}")
    fun load(instance: Any, element: Element) {
        load(
            instance, element, JavaPlugin.getProvidingPlugin(
                InventoryComponent::class.java
            )
        )
    }

    /**
     * Loads the provided element's child panes onto this component. If the element contains any child
     * panes, this will mutate this component.
     *
     * @param instance the instance to apply field and method references on
     * @param element  the element to load
     * @param plugin   the plugin to load the panes with
     * @since 0.10.12
     */
    fun load(instance: Any, element: Element, plugin: Plugin) {
        val childNodes: NodeList = element.getChildNodes()

        for (innerIndex in 0 until childNodes.getLength()) {
            val innerItem: Node = childNodes.item(innerIndex)

            if (innerItem.getNodeType() != Node.ELEMENT_NODE) {
                continue
            }

            addPane(Gui.Companion.loadPane(instance, innerItem, plugin))
        }
    }

    /**
     * Checks whether this component has at least one item. If it does, true is returned; false
     * otherwise.
     *
     * @return true if this has an item, false otherwise
     * @since 0.8.0
     */
    @Contract(pure = true)
    fun hasItem(): Boolean {
        for (x in 0 until length) {
            for (y in 0 until height) {
                if (getItem(x, y) != null) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * This will make each pane in this component render their items in this inventory component. The
     * panes are displayed according to their priority, with the lowest priority rendering first and
     * the highest priority (note: highest priority, not [Pane.Priority.HIGHEST] priority)
     * rendering last.
     *
     * @see .display
     * @since 0.8.0
     */
    fun display() {
        clearItems()

        for (pane: Pane in panes) {
            if (!pane.isVisible) {
                continue
            }

            pane.display(this, 0, 0, length, height)
        }
    }

    /**
     * Checks whether the item at the specified coordinates exists. If the specified coordinates are
     * not within this inventory component, an [IllegalArgumentException] will be thrown.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if an item exists at the given coordinates, false otherwise
     * @throws IllegalArgumentException when the coordinates are out of bounds
     * @since 0.8.0
     */
    @Contract(pure = true)
    fun hasItem(x: Int, y: Int): Boolean {
        return getItem(x, y) != null
    }

    /**
     * Gets the item at the specified coordinates, or null if this cell is empty. If the specified
     * coordinates are not within this inventory component, an [IllegalArgumentException] will
     * be thrown.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the item or null
     * @throws IllegalArgumentException when the coordinates are out of bounds
     * @since 0.8.0
     */
    @Contract(pure = true)
    fun getItem(x: Int, y: Int): ItemStack? {
        require(isInBounds(x, y)) {
            "Coordinates must be in-bounds: x = " + x + ", y = " + y +
                    "; should be below " + length + " and " + height
        }

        return items.get(x).get(y)
    }

    /**
     * Adds the specified item in the slot at the specified positions. This will override an already
     * set item if it resides in the same position as specified. If the position specified is outside
     * of the boundaries set by this component, an [IllegalArgumentException] will be thrown.
     *
     * @param guiItem the item to place in this inventory component
     * @param x       the x coordinate of the item
     * @param y       the y coordinate of the item
     * @since 0.9.3
     */
    fun setItem(guiItem: GuiItem, x: Int, y: Int) {
        require(isInBounds(x, y)) {
            "Coordinates must be in-bounds: x = " + x + ", y = " + y +
                    "; should be below " + length + " and " + height
        }

        val copy: GuiItem = guiItem.copy()
        copy.applyUUID()

        items.get(x).get(y) = copy.getItem()
    }

    /**
     * Adds the specified item in the slot at the specified positions. This will override an already
     * set item if it resides in the same position as specified. If the position specified is outside
     * of the boundaries set by this component, an [IllegalArgumentException] will be thrown.
     *
     * @param item the item to place in this inventory component
     * @param x    the x coordinate of the item
     * @param y    the y coordinate of the item
     * @since 0.8.0
     */
    @Deprecated(
        """usage of {@link #setItem(GuiItem, int, int)} is preferred so gui item's item meta
    can be freely edited without losing important internal data"""
    )
    fun setItem(item: ItemStack, x: Int, y: Int) {
        require(isInBounds(x, y)) {
            "Coordinates must be in-bounds: x = " + x + ", y = " + y +
                    "; should be below " + length + " and " + height
        }

        items.get(x).get(y) = item
    }

    @get:Contract(pure = true)
    val size: Int
        /**
         * Gets the total size of this inventory component.
         *
         * @return the size
         * @since 0.8.0
         */
        get() {
            return length * height
        }

    /**
     * Clears the items of this inventory component.
     *
     * @since 0.9.2
     */
    private fun clearItems() {
        for (items: Array<ItemStack?> in this.items) {
            Arrays.fill(items, null)
        }
    }

    /**
     * Returns whether the specified coordinates are inside the boundary of this inventory component
     * or outside of this inventory component; true is returned for the former case and false for the
     * latter case.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return true if the coordinates are in bounds, false otherwise
     * @since 0.8.0
     */
    @Contract(pure = true)
    private fun isInBounds(x: Int, y: Int): Boolean {
        val xBounds: Boolean = isInBounds(0, length - 1, x)
        val yBounds: Boolean = isInBounds(0, height - 1, y)

        return xBounds && yBounds
    }

    /**
     * Gets the pane at the specified index.
     *
     * @param index the index of the pane
     * @return the pane
     * @since 0.8.0
     */
    @Contract(pure = true)
    private fun getPane(index: Int): Pane {
        require(isInBounds(0, panes.size - 1, index)) { "Index not in pane list" }

        return panes.get(index)
    }

    /**
     * Checks whether a number is within the specified number bound (inclusive on both ends).
     *
     * @param lowerBound the lower bound of the range
     * @param upperBound the upper bound of the range
     * @param value      the value to check
     * @return true if the value is within the bounds, false otherwise
     * @since 0.8.0
     */
    @Contract(pure = true)
    private fun isInBounds(lowerBound: Int, upperBound: Int, value: Int): Boolean {
        return lowerBound <= value && value <= upperBound
    }
}
