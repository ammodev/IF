package com.github.stefvanschie.inventoryframework.pane.component

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.*
import com.github.stefvanschie.inventoryframework.pane.component.Slider
import com.github.stefvanschie.inventoryframework.pane.component.util.VariableBar
import com.github.stefvanschie.inventoryframework.pane.util.Slot
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import kotlin.math.min

/**
 * A slider for a graphical interface into what amount of a whole is set.
 *
 * @since 0.5.0
 */
class Slider : VariableBar {
    /**
     * Creates a new slider
     *
     * @param slot the slot of the slider
     * @param length the length of the slider
     * @param height the height of the slider
     * @param priority the priority of the slider
     * @param plugin the plugin that will be the owner of the slider's items
     * @since 0.10.8
     */
    constructor(slot: Slot, length: Int, height: Int, priority: Priority, plugin: Plugin) : super(
        slot,
        length,
        height,
        priority,
        plugin
    )

    /**
     * Creates a new slider
     *
     * @param x the x coordinate of the slider
     * @param y the y coordinate of the slier
     * @param length the length of the slider
     * @param height the height of the slider
     * @param priority the priority of the slider
     * @param plugin the plugin that will be the owner of the slider's items
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
     * Creates a new slider
     *
     * @param slot the slot of the slider
     * @param length the length of the slider
     * @param height the height of the slider
     * @param plugin the plugin that will be the owner of the slider's items
     * @since 0.10.8
     */
    constructor(slot: Slot, length: Int, height: Int, plugin: Plugin) : super(
        slot,
        length,
        height,
        plugin
    )

    /**
     * Creates a new slider
     *
     * @param x the x coordinate of the slider
     * @param y the y coordinate of the slier
     * @param length the length of the slider
     * @param height the height of the slider
     * @param plugin the plugin that will be the owner of the slider's items
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
     * Creates a new slider
     *
     * @param length the length of the slider
     * @param height the height of the slider
     * @param plugin the plugin that will be the owner of the slider's items
     * @since 0.10.8
     */
    constructor(length: Int, height: Int, plugin: Plugin) : super(length, height, plugin)

    /**
     * Creates a new slider
     *
     * @param slot the slot of the slider
     * @param length the length of the slider
     * @param height the height of the slider
     * @param priority the priority of the slider
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
     * Creates a new slider
     *
     * @param slot the slot of the slider
     * @param length the length of the slider
     * @param height the height of the slider
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
        val length =
            min(length.toDouble(), maxLength.toDouble()).toInt()
        val height =
            min(height.toDouble(), maxHeight.toDouble()).toInt()

        val paneSlot = getSlot()

        val xPosition = paneSlot.getX(maxLength)
        val yPosition = paneSlot.getY(maxLength)

        val totalLength = inventoryComponent.length

        val adjustedSlot =
            slot - (xPosition + paneOffsetX) - totalLength * (yPosition + paneOffsetY)

        val x = adjustedSlot % totalLength
        val y = adjustedSlot / totalLength

        if (x < 0 || x >= length || y < 0 || y >= height) {
            return false
        }

        if (orientation == Orientable.Orientation.HORIZONTAL) {
            setValue((x + 1).toFloat() / length)
        } else if (orientation == Orientable.Orientation.VERTICAL) {
            setValue((y + 1).toFloat() / height)
        } else {
            throw UnsupportedOperationException("Unknown orientation")
        }

        callOnClick(event)

        val newPaneOffsetX = paneOffsetX + xPosition
        val newPaneOffsetY = paneOffsetY + yPosition

        val success = fillPane.click(
            gui, inventoryComponent, event, slot, newPaneOffsetX, newPaneOffsetY, length, height
        ) || backgroundPane.click(
            gui, inventoryComponent, event, slot, newPaneOffsetX, newPaneOffsetY, length, height
        )

        gui.update()

        return success
    }

    /**
     * Sets the value of this bar. The value has to be in (0,1). If not, this method will throw an
     * [IllegalArgumentException].
     *
     * @param value the new value.
     * @throws IllegalArgumentException when the value is out of range
     * @since 0.5.0
     * @see VariableBar.setValue
     */
    public override fun setValue(value: Float) {
        super.setValue(value)
    }

    @Contract(pure = true)
    override fun copy(): Slider {
        val slider = Slider(getSlot(), length, height, priority)

        applyContents(slider)

        return slider
    }

    /**
     * Gets the value as a float in between (0,1) this bar is currently set at.
     *
     * @return the value
     * @since 0.5.0
     */
    fun getValue(): Float {
        return value
    }

    companion object {
        /**
         * Loads a percentage bar from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @param plugin the plugin that will be the owner of the udnerlying items
         * @return the percentage bar
         * @since 0.10.8
         */
        @JvmStatic
        @Contract(pure = true)
        fun load(instance: Any, element: Element, plugin: Plugin): Slider {
            val length: Int
            val height: Int

            try {
                length = element.getAttribute("length").toInt()
                height = element.getAttribute("height").toInt()
            } catch (exception: NumberFormatException) {
                throw XMLLoadException(exception)
            }

            val slider = Slider(length, height, plugin)

            Pane.Companion.load(slider, instance, element)
            Orientable.Companion.load(slider, element)
            Flippable.Companion.load(slider, element)

            if (element.hasAttribute("populate")) {
                return slider
            }

            if (element.hasAttribute("value")) {
                try {
                    slider.setValue(element.getAttribute("value").toFloat())
                } catch (exception: IllegalArgumentException) {
                    throw XMLLoadException(exception)
                }
            }

            return slider
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
        fun load(instance: Any, element: Element): Slider {
            return load(
                instance, element, JavaPlugin.getProvidingPlugin(
                    Slider::class.java
                )
            )
        }
    }
}
