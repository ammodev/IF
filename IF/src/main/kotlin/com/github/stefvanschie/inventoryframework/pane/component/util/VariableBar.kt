package com.github.stefvanschie.inventoryframework.pane.component.util

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.pane.*
import com.github.stefvanschie.inventoryframework.pane.component.util.VariableBar
import com.github.stefvanschie.inventoryframework.pane.util.Slot
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.math.min

/**
 * A variable bar for UI elements that require some sort of bar
 *
 * @since 0.5.0
 */
abstract class VariableBar protected constructor(
    slot: Slot, length: Int, height: Int, priority: Priority = Priority.NORMAL,
    plugin: Plugin = JavaPlugin.getProvidingPlugin(
        VariableBar::class.java
    )
) :
    Pane(slot, length, height), Orientable,
    Flippable {
    /**
     * The green and the red parts of the slider
     */
    protected val fillPane: OutlinePane
    protected val backgroundPane: OutlinePane

    /**
     * The value this slider is at. This is a value between 0 and 1 (both inclusive).
     */
    protected var value: Float = 0f

    /**
     * The orientation of the slider
     */
    override var orientation: Orientable.Orientation? = null

    /**
     * Whether the pane is flipped horizontally or vertically
     */
    override var isFlippedHorizontally: Boolean = false
        protected set
    override var isFlippedVertically: Boolean = false
        protected set

    /**
     * Creates a new variable bar
     *
     * @param length the length of the bar
     * @param height the height of the bar
     * @param plugin the plugin that will be the owner for this variable bar's items
     * @see .VariableBar
     * @since 0.10.8
     */
    protected constructor(length: Int, height: Int, plugin: Plugin) : this(
        0,
        0,
        length,
        height,
        plugin
    )

    /**
     * Creates a new variable bar
     *
     * @param slot the slot of the bar
     * @param length the length of the bar
     * @param height the height of the bar
     * @param priority the priority of the bar
     * @param plugin the plugin that will be the owner for this variable bar's items
     * @see .VariableBar
     * @since 0.10.8
     */
    /**
     * Creates a new variable bar
     *
     * @param slot the slot of the bar
     * @param length the length of the bar
     * @param height the height of the bar
     * @param priority the priority of the bar
     * @since 0.10.8
     */
    /**
     * Creates a new variable bar
     *
     * @param slot the slot of the bar
     * @param length the length of the bar
     * @param height the height of the bar
     * @since 0.10.8
     */
    init {
        this.value = 0f
        this.orientation = Orientable.Orientation.HORIZONTAL

        this.fillPane = OutlinePane(0, 0, length, height)
        this.backgroundPane = OutlinePane(0, 0, length, height)

        fillPane.addItem(
            GuiItem(
                ItemStack(Material.GREEN_STAINED_GLASS_PANE),
                { event: InventoryClickEvent ->
                    event.isCancelled =
                        true
                }, plugin
            )
        )
        backgroundPane.addItem(
            GuiItem(
                ItemStack(Material.RED_STAINED_GLASS_PANE),
                { event: InventoryClickEvent ->
                    event.isCancelled =
                        true
                }, plugin
            )
        )

        fillPane.setRepeat(true)
        backgroundPane.setRepeat(true)

        fillPane.isVisible = false

        setPriority(priority)
    }

    /**
     * Creates a new variable bar
     *
     * @param x the x coordinate of the bar
     * @param y the y coordinate of the bar
     * @param length the length of the bar
     * @param height the height of the bar
     * @param priority the priority of the bar
     * @param plugin the plugin that will be the owner for this variable bar's items
     * @see .VariableBar
     * @since 0.10.8
     */
    protected constructor(
        x: Int,
        y: Int,
        length: Int,
        height: Int,
        priority: Priority = Priority.NORMAL,
        plugin: Plugin = JavaPlugin.getProvidingPlugin(
            VariableBar::class.java
        )
    ) : this(Slot.Companion.fromXY(x, y), length, height, priority, plugin)

    /**
     * Creates a new variable bar
     *
     * @param slot the slot of the bar
     * @param length the length of the bar
     * @param height the height of the bar
     * @param plugin the plugin that will be the owner for this variable bar's items
     * @see .VariableBar
     * @since 0.10.8
     */
    protected constructor(slot: Slot, length: Int, height: Int, plugin: Plugin) : this(
        slot,
        length,
        height,
        Priority.NORMAL,
        plugin
    )

    /**
     * Creates a new variable bar
     *
     * @param x the x coordinate of the bar
     * @param y the y coordinate of the bar
     * @param length the length of the bar
     * @param height the height of the bar
     * @param plugin the plugin that will be the owner for this variable bar's items
     * @see .VariableBar
     * @since 0.10.8
     */
    protected constructor(x: Int, y: Int, length: Int, height: Int, plugin: Plugin) : this(
        x,
        y,
        length,
        height,
        Priority.NORMAL,
        plugin
    )

    protected constructor(length: Int, height: Int) : this(0, 0, length, height)

    /**
     * Sets the value of this bar. The value has to be in (0,1). If not, this method will throw an
     * [IllegalArgumentException].
     *
     * @param value the new value.
     * @throws IllegalArgumentException when the value is out of range
     * @since 0.9.5
     */
    protected open fun setValue(value: Float) {
        require(!(value < 0 || value > 1)) { "Value is out of range (0,1)" }

        this.value = value

        if (orientation == Orientable.Orientation.HORIZONTAL) {
            val length = Math.round(getLength() * value).toInt()
            val positiveLength = length != 0

            fillPane.isVisible = positiveLength

            if (positiveLength) {
                fillPane.length = length
            }

            if (isFlippedHorizontally) {
                fillPane.x = getLength() - fillPane.length
            }
        } else if (orientation == Orientable.Orientation.VERTICAL) {
            val height = Math.round(getHeight() * value).toInt()
            val positiveHeight = height != 0

            fillPane.isVisible = positiveHeight

            if (positiveHeight) {
                fillPane.height = height
            }

            if (isFlippedVertically) {
                fillPane.y = getHeight() - fillPane.height
            }
        } else {
            throw UnsupportedOperationException("Unknown orientation")
        }
    }

    override var length: Int
        get() = super.length
        set(length) {
            super.setLength(length)

            if (orientation == Orientable.Orientation.HORIZONTAL) {
                val fillLength = Math.round(length * value)
                val positiveLength = fillLength != 0

                fillPane.isVisible = positiveLength

                if (positiveLength) {
                    fillPane.length = fillLength
                }

                if (isFlippedHorizontally) {
                    fillPane.x = getLength() - fillPane.length
                }
            } else if (orientation == Orientable.Orientation.VERTICAL) {
                fillPane.length = length
            } else {
                throw UnsupportedOperationException("Unknown orientation")
            }

            backgroundPane.length = length
        }

    override var height: Int
        get() = super.height
        set(height) {
            super.setHeight(height)

            if (orientation == Orientable.Orientation.HORIZONTAL) {
                fillPane.height = height
            } else if (orientation == Orientable.Orientation.VERTICAL) {
                val fillHeight = Math.round(height * value)
                val positiveHeight = fillHeight != 0

                fillPane.isVisible = positiveHeight

                if (positiveHeight) {
                    fillPane.height = fillHeight
                }

                if (isFlippedVertically) {
                    fillPane.y = getHeight() - fillPane.height
                }
            } else {
                throw UnsupportedOperationException("Unknown orientation")
            }

            backgroundPane.height = height
        }

    /**
     * Applies the contents of this variable bar onto the provided copy of this variable bar. This variable bar will not
     * be modified.
     *
     * @param copy the copy of the variable bar
     * @since 0.6.2
     */
    protected fun applyContents(copy: VariableBar) {
        copy.x = x
        copy.y = y
        copy.slot = slot
        copy.length = length
        copy.height = height
        copy.priority = priority

        copy.isVisible = isVisible
        copy.onClick = onClick

        copy.setFillItem(fillPane.items[0].copy())
        copy.setBackgroundItem(backgroundPane.items[0].copy())

        copy.value = value
        copy.orientation = orientation

        copy.isFlippedHorizontally = isFlippedHorizontally
        copy.isFlippedVertically = isFlippedVertically

        copy.uuid = uuid
    }

    override fun setOrientation(orientation: Orientable.Orientation) {
        this.orientation = orientation

        if (orientation == Orientable.Orientation.HORIZONTAL) {
            val fillLength = Math.round(getLength() * value).toInt()
            val positiveLength = fillLength != 0

            fillPane.isVisible = fillLength != 0

            if (positiveLength) {
                fillPane.length = fillLength
            }

            fillPane.height = getHeight()
        } else if (orientation == Orientable.Orientation.VERTICAL) {
            val fillHeight = Math.round(getHeight() * value).toInt()
            val positiveHeight = fillHeight != 0

            fillPane.isVisible = fillHeight != 0
            fillPane.length = getLength()

            if (positiveHeight) {
                fillPane.height = fillHeight
            }
        } else {
            throw IllegalArgumentException("Unknown orientation")
        }
    }

    override fun display(
        inventoryComponent: InventoryComponent, paneOffsetX: Int, paneOffsetY: Int, maxLength: Int,
        maxHeight: Int
    ) {
        val slot = getSlot()

        val newPaneOffsetX = paneOffsetX + slot.getX(maxLength)
        val newPaneOffsetY = paneOffsetY + slot.getY(maxLength)
        val newMaxLength =
            min(maxLength.toDouble(), getLength().toDouble()).toInt()
        val newMaxHeight =
            min(maxHeight.toDouble(), getHeight().toDouble()).toInt()

        if (backgroundPane.isVisible) {
            backgroundPane.display(
                inventoryComponent,
                newPaneOffsetX,
                newPaneOffsetY,
                newMaxLength,
                newMaxHeight
            )
        }

        if (fillPane.isVisible) {
            fillPane.display(
                inventoryComponent,
                newPaneOffsetX,
                newPaneOffsetY,
                newMaxLength,
                newMaxHeight
            )
        }
    }

    /**
     * Sets the fill item (foreground)
     *
     * @param item the new item
     * @since 0.5.0
     */
    fun setFillItem(item: GuiItem) {
        fillPane.clear()

        fillPane.addItem(item)
    }

    /**
     * Sets the background item
     *
     * @param item the new item
     * @since 0.5.0
     */
    fun setBackgroundItem(item: GuiItem) {
        backgroundPane.clear()

        backgroundPane.addItem(item)
    }

    override val items: Collection<GuiItem>
        get() = panes.stream().flatMap { pane: Pane -> pane.items.stream() }
            .collect(Collectors.toSet())

    override val panes: Collection<Pane>
        get() = Stream.of(
            this.fillPane,
            backgroundPane
        )
            .collect(Collectors.toSet())

    override fun flipHorizontally(flipHorizontally: Boolean) {
        this.isFlippedHorizontally = flipHorizontally
    }

    override fun flipVertically(flipVertically: Boolean) {
        this.isFlippedVertically = flipVertically
    }

    override fun getOrientation(): Orientable.Orientation {
        return orientation!!
    }

    override fun clear() {}
}
