package com.github.stefvanschie.inventoryframework.pane.component

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.*
import com.github.stefvanschie.inventoryframework.pane.component.PercentageBar
import com.github.stefvanschie.inventoryframework.pane.component.util.VariableBar
import com.github.stefvanschie.inventoryframework.pane.util.Slot
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import kotlin.math.min

/**
 * A percentage bar for a graphical interface into what amount of a whole is set.
 *
 * @since 0.5.0
 */
class PercentageBar : VariableBar {
    /**
     * Creates a new percentage bar
     *
     * @param slot the slot of the bar
     * @param length the length of the bar
     * @param height the height of the bar
     * @param priority the priority of the bar
     * @param plugin the plugin that will be the owner for this percentage bar's items
     * @since 0.10.8
     */
    constructor(
        slot: Slot, length: Int, height: Int, priority: Priority,
        plugin: Plugin
    ) : super(slot, length, height, priority, plugin)

    /**
     * Creates a new percentage bar
     *
     * @param x the x coordinate of the bar
     * @param y the y coordinate of the bar
     * @param length the length of the bar
     * @param height the height of the bar
     * @param priority the priority of the bar
     * @param plugin the plugin that will be the owner for this percentage bar's items
     * @since 0.10.8
     */
    constructor(
        x: Int,
        y: Int,
        length: Int,
        height: Int,
        priority: Priority,
        plugin: Plugin
    ) : super(x, y, length, height, priority, plugin)

    /**
     * Creates a new percentage bar
     *
     * @param slot the slot of the bar
     * @param length the length of the bar
     * @param height the height of the bar
     * @param plugin the plugin that will be the owner for this percentage bar's items
     * @since 0.10.8
     */
    constructor(slot: Slot, length: Int, height: Int, plugin: Plugin) : super(
        slot,
        length,
        height,
        plugin
    )

    /**
     * Creates a new percentage bar
     *
     * @param x the x coordinate of the bar
     * @param y the y coordinate of the bar
     * @param length the length of the bar
     * @param height the height of the bar
     * @param plugin the plugin that will be the owner for this percentage bar's items
     * @since 0.10.8
     */
    constructor(x: Int, y: Int, length: Int, height: Int, plugin: Plugin) : super(
        x,
        y,
        length,
        height,
        plugin
    )

    /**
     * Creates a new percentage bar
     *
     * @param length the length of the bar
     * @param height the height of the bar
     * @param plugin the plugin that will be the owner for this percentage bar's items
     * @since 0.10.8
     */
    constructor(length: Int, height: Int, plugin: Plugin) : super(length, height, plugin)

    /**
     * Creates a new percentage bar
     *
     * @param slot the slot of the bar
     * @param length the length of the bar
     * @param height the height of the bar
     * @param priority the priority of the bar
     * @since 0.10.8
     */
    constructor(slot: Slot, length: Int, height: Int, priority: Priority) : super(
        slot,
        length,
        height,
        priority
    )

    constructor(x: Int, y: Int, length: Int, height: Int, priority: Priority) : super(
        x,
        y,
        length,
        height,
        priority
    )

    /**
     * Creates a new percentage bar
     *
     * @param slot the slot of the bar
     * @param length the length of the bar
     * @param height the height of the bar
     * @since 0.10.8
     */
    constructor(slot: Slot, length: Int, height: Int) : super(slot, length, height)

    constructor(x: Int, y: Int, length: Int, height: Int) : super(x, y, length, height)

    constructor(length: Int, height: Int) : super(length, height)

    override fun click(
        gui: Gui, inventoryComponent: InventoryComponent,
        event: InventoryClickEvent, slot: Int, paneOffsetX: Int, paneOffsetY: Int, maxLength: Int,
        maxHeight: Int
    ): Boolean {
        val length: Int =
            min(length.toDouble(), maxLength.toDouble()).toInt()
        val height: Int =
            min(height.toDouble(), maxHeight.toDouble()).toInt()

        val paneSlot: Slot = getSlot()

        val xPosition: Int = paneSlot.getX(maxLength)
        val yPosition: Int = paneSlot.getY(maxLength)

        val totalLength: Int = inventoryComponent.length

        val adjustedSlot: Int =
            slot - (xPosition + paneOffsetX) - totalLength * (yPosition + paneOffsetY)

        val x: Int = adjustedSlot % totalLength
        val y: Int = adjustedSlot / totalLength

        if (x < 0 || x >= length || y < 0 || y >= height) {
            return false
        }

        callOnClick(event)

        event.setCancelled(true)

        val newPaneOffsetX: Int = paneOffsetX + xPosition
        val newPaneOffsetY: Int = paneOffsetY + yPosition


        return fillPane.click(
            gui, inventoryComponent, event, slot, newPaneOffsetX, newPaneOffsetY, length, height
        ) || backgroundPane.click(
            gui, inventoryComponent, event, slot, newPaneOffsetX, newPaneOffsetY, length, height
        )
    }

    @Contract(pure = true)
    override fun copy(): PercentageBar {
        val percentageBar: PercentageBar = PercentageBar(getSlot(), length, height, getPriority())

        applyContents(percentageBar)

        return percentageBar
    }

    var percentage: Float
        /**
         * Gets the percentage as a float in between (0,1) this bar is currently set at.
         *
         * @return the percentage
         * @since 0.5.0
         */
        get() {
            return value
        }
        /**
         * Sets the percentage of this bar. The percentage has to be in (0,1). If not, this method will throw an
         * [IllegalArgumentException].
         *
         * @param percentage the new percentage.
         * @throws IllegalArgumentException when the percentage is out of range
         * @since 0.5.0
         * @see VariableBar.setValue
         */
        set(percentage) {
            super.setValue(percentage)
        }

    companion object {
        /**
         * Loads a percentage bar from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @param plugin the plugin that will be the owner of the underlying items
         * @return the percentage bar
         * @since 0.10.8
         */
        @JvmStatic
        @Contract(pure = true)
        fun load(instance: Any, element: Element, plugin: Plugin): PercentageBar {
            val length: Int
            val height: Int

            try {
                length = element.getAttribute("length").toInt()
                height = element.getAttribute("height").toInt()
            } catch (exception: NumberFormatException) {
                throw XMLLoadException(exception)
            }

            val percentageBar: PercentageBar = PercentageBar(length, height, plugin)

            Pane.Companion.load(percentageBar, instance, element)
            Orientable.Companion.load(percentageBar, element)
            Flippable.Companion.load(percentageBar, element)

            if (element.hasAttribute("populate")) {
                return percentageBar
            }

            if (element.hasAttribute("percentage")) {
                try {
                    percentageBar.percentage = element.getAttribute("percentage").toFloat()
                } catch (exception: IllegalArgumentException) {
                    throw XMLLoadException(exception)
                }
            }

            return percentageBar
        }

        /**
         * Loads a percentage bar from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @return the percentage bar
         */
        @Contract(pure = true)
        @Deprecated(
            """this method is no longer used internally and has been superseded by
                  {@link #load(Object, Element, Plugin)}"""
        )
        fun load(instance: Any, element: Element): PercentageBar {
            return load(
                instance, element, JavaPlugin.getProvidingPlugin(
                    PercentageBar::class.java
                )
            )
        }
    }
}
