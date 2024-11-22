package com.github.stefvanschie.inventoryframework.pane.component

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.component.CycleButton
import com.github.stefvanschie.inventoryframework.pane.util.Slot
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.util.stream.Collectors
import kotlin.math.min

/**
 * A button for cycling between different options
 *
 * @since 0.5.0
 */
class CycleButton : Pane {
    /**
     * The list of pane used for display
     */
    override val panes: MutableList<Pane> = ArrayList()

    /**
     * The current position of the cycle button
     */
    private var position = 0

    /**
     * Creates a new cycle button
     *
     * @param slot the slot of the button
     * @param length the length of the button
     * @param height the height of the button
     * @param priority the priority of the button
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
     * Creates a new cycle button
     *
     * @param slot the slot of the button
     * @param length the length of the button
     * @param height the height of the button
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

        //this isn't our item
        if (x < 0 || x >= length || y < 0 || y >= height) {
            return false
        }

        val previousPosition = position

        position++

        if (position == panes.size) {
            position = 0
        }

        callOnClick(event)

        //use the previous position, since that will have the pane we clicked on
        val pane = panes[previousPosition]
        pane.click(
            gui, inventoryComponent, event, slot, paneOffsetX + x, paneOffsetY + y,
            length, height
        )

        gui.update()

        return true
    }

    override fun display(
        inventoryComponent: InventoryComponent, paneOffsetX: Int, paneOffsetY: Int, maxLength: Int,
        maxHeight: Int
    ) {
        val slot = getSlot()

        val newX = paneOffsetX + slot.getX(maxLength)
        val newY = paneOffsetY + slot.getY(maxLength)

        val newMaxLength = min(maxLength.toDouble(), length.toDouble()).toInt()
        val newMaxHeight = min(maxHeight.toDouble(), height.toDouble()).toInt()

        panes[position].display(inventoryComponent, newX, newY, newMaxLength, newMaxHeight)
    }

    @Contract(pure = true)
    override fun copy(): CycleButton {
        val cycleButton = CycleButton(getSlot(), length, height, priority)

        for (pane in panes) {
            cycleButton.addPane(pane)
        }

        cycleButton.isVisible = isVisible
        cycleButton.onClick = onClick

        cycleButton.position = position

        cycleButton.uuid = uuid

        return cycleButton
    }

    override val items: Collection<GuiItem>
        get() = getPanes().stream().flatMap { pane: Pane -> pane.items.stream() }
            .collect(Collectors.toList())

    /**
     * Adds a pane to the current list of options
     *
     * @param index the index to insert the pane at
     * @param pane the pane to add
     * @since 0.5.0
     */
    fun addPane(index: Int, pane: Pane) {
        panes.add(index, pane)
    }

    /**
     * Adds a pane to the current list of options
     *
     * @param pane the pane to add
     * @since 0.5.0
     */
    fun addPane(pane: Pane) {
        panes.add(pane)
    }

    override fun clear() {
        panes.clear()
    }

    override fun getPanes(): Collection<Pane> {
        return panes
    }

    /**
     * Cycles through one option, making it go to the next one
     *
     * @since 0.5.0
     */
    fun cycle() {
        position++
    }

    companion object {
        /**
         * Loads a cycle button from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @param plugin the plugin that will be the owner of the underlying items
         * @return the cycle button
         * @since 0.10.8
         */
        @JvmStatic
        fun load(instance: Any, element: Element, plugin: Plugin): CycleButton {
            val length: Int
            val height: Int

            try {
                length = element.getAttribute("length").toInt()
                height = element.getAttribute("height").toInt()
            } catch (exception: NumberFormatException) {
                throw XMLLoadException(exception)
            }

            val cycleButton = CycleButton(length, height)

            Pane.Companion.load(cycleButton, instance, element)

            if (element.hasAttribute("populate")) {
                return cycleButton
            }

            val childNodes = element.childNodes

            for (j in 0 until childNodes.length) {
                val pane = childNodes.item(j)

                if (pane.nodeType != Node.ELEMENT_NODE) {
                    continue
                }

                cycleButton.addPane(Gui.loadPane(instance, pane, plugin))
            }

            return cycleButton
        }

        /**
         * Loads a cycle button from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @return the cycle button
         * @since 0.5.0
         */
        @Deprecated(
            """this method is no longer used internally and has been superseded by
                  {@link #load(Object, Element, Plugin)}"""
        )
        fun load(instance: Any, element: Element): CycleButton {
            return load(
                instance, element, JavaPlugin.getProvidingPlugin(
                    CycleButton::class.java
                )
            )
        }
    }
}
