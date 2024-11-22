package com.github.stefvanschie.inventoryframework.pane.component

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.component.ToggleButton
import com.github.stefvanschie.inventoryframework.pane.util.Slot
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.math.min

/**
 * A button that toggles between an enabled and disabled state.
 *
 * @since 0.5.0
 */
class ToggleButton @JvmOverloads constructor(
    slot: Slot, length: Int, height: Int, priority: Priority, enabled: Boolean = false,
    plugin: Plugin = JavaPlugin.getProvidingPlugin(
        ToggleButton::class.java
    )
) :
    Pane(slot, length, height, priority) {
    /**
     * The panes used for showing the enabled and disabled states
     */
    private val enabledPane: OutlinePane
    private val disabledPane: OutlinePane

    /**
     * Gets whether this toggle button is currently enabled or disabled.
     *
     * @return whether the button is enabled or disabled
     * @since 0.9.6
     */
    /**
     * Whether the button is enabled or disabled
     */
    @get:Contract(pure = true)
    var isEnabled: Boolean = false
        private set

    /**
     * Whether this button can be toggled by a player
     */
    private var allowToggle: Boolean = true

    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @param enabled whether the button should start in its enabled or disabled state
     * @param plugin the plugin that will be the owner of this button's items
     * @see .ToggleButton
     * @since 0.10.8
     */
    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @param enabled whether the button should start in its enabled or disabled state
     * @since 0.10.8
     */
    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @since 0.10.8
     */
    init {
        this.isEnabled = enabled

        this.enabledPane = OutlinePane(length, height)
        enabledPane.addItem(GuiItem(ItemStack(Material.GREEN_STAINED_GLASS_PANE), plugin))
        enabledPane.setRepeat(true)

        this.disabledPane = OutlinePane(length, height)
        disabledPane.addItem(GuiItem(ItemStack(Material.RED_STAINED_GLASS_PANE), plugin))
        disabledPane.setRepeat(true)
    }

    /**
     * Creates a new toggle button
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @param enabled whether the button should start in its enabled or disabled state
     * @param plugin the plugin that will be the owner of this button's items
     * @see .ToggleButton
     * @since 0.10.8
     */
    @JvmOverloads
    constructor(
        x: Int, y: Int, length: Int, height: Int, priority: Priority, enabled: Boolean = false,
        plugin: Plugin = JavaPlugin.getProvidingPlugin(
            ToggleButton::class.java
        )
    ) : this(Slot.Companion.fromXY(x, y), length, height, priority, enabled, plugin)

    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @param plugin the plugin that will be the owner of this button's items
     * @see .ToggleButton
     * @since 0.10.8
     */
    constructor(
        slot: Slot, length: Int, height: Int, priority: Priority,
        plugin: Plugin
    ) : this(slot, length, height, priority, false, plugin)

    /**
     * Creates a new toggle button
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @param plugin the plugin that will be the owner of this button's items
     * @see .ToggleButton
     * @since 0.10.8
     */
    constructor(
        x: Int,
        y: Int,
        length: Int,
        height: Int,
        priority: Priority,
        plugin: Plugin
    ) : this(x, y, length, height, priority, false, plugin)

    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param enabled whether the button should start in its enabled or disabled state
     * @param plugin the plugin that will be the owner of this button's items
     * @see .ToggleButton
     * @since 0.10.8
     */
    constructor(slot: Slot, length: Int, height: Int, enabled: Boolean, plugin: Plugin) : this(
        slot,
        length,
        height,
        Priority.NORMAL,
        enabled,
        plugin
    )

    /**
     * Creates a new toggle button
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param length the length
     * @param height the height
     * @param enabled whether the button should start in its enabled or disabled state
     * @param plugin the plugin that will be the owner of this button's items
     * @see .ToggleButton
     * @since 0.10.8
     */
    constructor(
        x: Int,
        y: Int,
        length: Int,
        height: Int,
        enabled: Boolean,
        plugin: Plugin
    ) : this(x, y, length, height, Priority.NORMAL, enabled, plugin)

    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param plugin the plugin that will be the owner of this button's items
     * @see .ToggleButton
     * @since 0.10.8
     */
    constructor(slot: Slot, length: Int, height: Int, plugin: Plugin) : this(
        slot,
        length,
        height,
        false,
        plugin
    )

    /**
     * Creates a new toggle button
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param length the length
     * @param height the height
     * @param plugin the plugin that will be the owner of this button's items
     * @see .ToggleButton
     * @since 0.10.8
     */
    constructor(x: Int, y: Int, length: Int, height: Int, plugin: Plugin) : this(
        x,
        y,
        length,
        height,
        false,
        plugin
    )

    /**
     * Creates a new toggle button
     *
     * @param length the length
     * @param height the height
     * @param enabled whether the button should start in its enabled or disabled state
     * @param plugin the plugin that will be the owner of this button's items
     * @see .ToggleButton
     * @since 0.10.8
     */
    constructor(length: Int, height: Int, enabled: Boolean, plugin: Plugin) : this(
        0,
        0,
        length,
        height,
        enabled
    )

    /**
     * Creates a new toggle button
     *
     * @param length the length
     * @param height the height
     * @param plugin the plugin that will be the owner of this button's items
     * @see .ToggleButton
     * @since 0.10.8
     */
    constructor(length: Int, height: Int, plugin: Plugin) : this(length, height, false)

    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param enabled whether the button should start in its enabled or disabled state
     * @since 0.10.8
     */
    /**
     * Creates a new toggle button
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @since 0.10.8
     */
    @JvmOverloads
    constructor(slot: Slot, length: Int, height: Int, enabled: Boolean = false) : this(
        slot,
        length,
        height,
        Priority.NORMAL,
        enabled
    )

    @JvmOverloads
    constructor(x: Int, y: Int, length: Int, height: Int, enabled: Boolean = false) : this(
        x,
        y,
        length,
        height,
        Priority.NORMAL,
        enabled
    )

    @JvmOverloads
    constructor(length: Int, height: Int, enabled: Boolean = false) : this(
        0,
        0,
        length,
        height,
        enabled
    )

    override fun display(
        inventoryComponent: InventoryComponent, paneOffsetX: Int, paneOffsetY: Int, maxLength: Int,
        maxHeight: Int
    ) {
        val newMaxLength: Int = min(maxLength.toDouble(), length.toDouble()).toInt()
        val newMaxHeight: Int = min(maxHeight.toDouble(), height.toDouble()).toInt()

        val newPaneOffsetX: Int = slot.getX(newMaxLength) + paneOffsetX
        val newPaneOffsetY: Int = slot.getY(newMaxHeight) + paneOffsetY

        if (isEnabled) {
            enabledPane.display(
                inventoryComponent,
                newPaneOffsetX,
                newPaneOffsetY,
                newMaxLength,
                newMaxHeight
            )
        } else {
            disabledPane.display(
                inventoryComponent,
                newPaneOffsetX,
                newPaneOffsetY,
                newMaxLength,
                newMaxHeight
            )
        }
    }

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

        //this isn't our item
        if (x < 0 || x >= length || y < 0 || y >= height) {
            return false
        }

        if (this.allowToggle) {
            toggle()
        }

        callOnClick(event)

        val newX: Int = paneOffsetX + xPosition
        val newY: Int = paneOffsetY + yPosition

        /*
        Since we've toggled before, the click for the panes should be swapped around. If we haven't toggled due to
        allowToggle being false, then we should click the pane corresponding to the current state. An XOR achieves this.
         */
        if (isEnabled == this.allowToggle) {
            disabledPane.click(gui, inventoryComponent, event, slot, newX, newY, length, height)
        } else {
            enabledPane.click(gui, inventoryComponent, event, slot, newX, newY, length, height)
        }

        gui.update()

        return true
    }

    @Contract(pure = true)
    override fun copy(): ToggleButton {
        val toggleButton: ToggleButton = ToggleButton(
            getSlot(), length, height, priority,
            isEnabled
        )

        toggleButton.allowToggle = this.allowToggle

        toggleButton.isVisible = isVisible
        toggleButton.onClick = onClick

        toggleButton.uuid = uuid

        toggleButton.setEnabledItem(enabledPane.items.get(0).copy())
        toggleButton.setDisabledItem(disabledPane.items.get(0).copy())

        return toggleButton
    }

    override var length: Int
        get() = super.length
        set(length) {
            super.setLength(length)

            disabledPane.length = length
            enabledPane.length = length
        }

    override var height: Int
        get() = super.height
        set(height) {
            super.setHeight(height)

            disabledPane.height = height
            enabledPane.height = height
        }

    /**
     * Sets the item to use when the button is set to disabled
     *
     * @param item the disabled item
     * @since 0.5.0
     */
    fun setDisabledItem(item: GuiItem) {
        disabledPane.clear()

        disabledPane.addItem(item)
    }

    /**
     * Sets the item to use when the button is set to enabled
     *
     * @param item the enabled item
     * @since 0.5.0
     */
    fun setEnabledItem(item: GuiItem) {
        enabledPane.clear()

        enabledPane.addItem(item)
    }

    override val items: Collection<GuiItem>
        get() = panes.stream().flatMap { pane: Pane -> pane.items.stream() }
            .collect(Collectors.toSet())

    override val panes: Collection<Pane>
        get() {
            return Stream.of(enabledPane, disabledPane)
                .collect(Collectors.toSet())
        }

    /**
     * Sets whether this toggle button can be toggled. This only prevents players from toggling the button and does not
     * prevent toggling the button programmatically with methods such as [.toggle].
     *
     * @param allowToggle whether this button can be toggled
     * @since 0.10.8
     */
    fun allowToggle(allowToggle: Boolean) {
        this.allowToggle = allowToggle
    }

    /**
     * Toggles between the enabled and disabled states
     *
     * @since 0.5.0
     */
    fun toggle() {
        isEnabled = !isEnabled
    }

    override fun clear() {}

    companion object {
        /**
         * Loads a toggle button from an XML element
         *
         * @param instance the instance class
         * @param element the element
         * @param plugin the plugin that will be the owner of the underlying items
         * @return the toggle button
         * @since 0.10.8
         */
        @JvmStatic
        @Contract(pure = true)
        fun load(instance: Any, element: Element, plugin: Plugin): ToggleButton {
            val length: Int
            val height: Int

            try {
                length = element.getAttribute("length").toInt()
                height = element.getAttribute("height").toInt()
            } catch (exception: NumberFormatException) {
                throw XMLLoadException(exception)
            }

            val enabled: Boolean =
                element.hasAttribute("enabled") && element.getAttribute("enabled").toBoolean()
            val toggleButton: ToggleButton = ToggleButton(length, height, enabled, plugin)

            Pane.Companion.load(toggleButton, instance, element)

            return toggleButton
        }

        /**
         * Loads a toggle button from an XML element
         *
         * @param instance the instance class
         * @param element the element
         * @return the toggle button
         * @since 0.5.0
         */
        @Contract(pure = true)
        @Deprecated(
            """this method is no longer used internally and has been superseded by
                  {@link #load(Object, Element, Plugin)}"""
        )
        fun load(instance: Any, element: Element): ToggleButton {
            return load(
                instance, element, JavaPlugin.getProvidingPlugin(
                    ToggleButton::class.java
                )
            )
        }
    }
}
