package com.github.stefvanschie.inventoryframework.pane

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.github.stefvanschie.inventoryframework.pane.util.Mask
import com.github.stefvanschie.inventoryframework.pane.util.Slot
import com.github.stefvanschie.inventoryframework.util.GeometryUtil.processClockwiseRotation
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.util.*
import kotlin.math.min

/**
 * A pane for items that should be outlined
 */
open class OutlinePane @JvmOverloads constructor(
    slot: Slot,
    length: Int,
    height: Int,
    priority: Priority = Priority.NORMAL
) :
    Pane(slot, length, height, priority), Flippable, Orientable,
    Rotatable {
    /**
     * A set of items inside this pane
     */
    override val items: MutableList<GuiItem>

    /**
     * The orientation of the items in this pane
     */
    override var orientation: Orientable.Orientation? = null

    /**
     * The clockwise rotation of this pane in degrees
     */
    override var rotation: Int = 0

    /**
     * The amount of empty spots in between each item
     */
    private var gap: Int = 0

    /**
     * Whether the items should be repeated to fill the entire pane
     */
    private var repeat: Boolean = false

    /**
     * Whether the items should be flipped horizontally and/or vertically
     */
    @get:Contract(pure = true)
    override var isFlippedHorizontally: Boolean = false
        private set

    @get:Contract(pure = true)
    override var isFlippedVertically: Boolean = false
        private set

    /**
     * Gets the alignment set on this pane.
     *
     * @return the alignment
     * @since 0.10.1
     */
    /**
     * The alignment of this pane
     */
    @get:Contract(pure = true)
    var alignment: Alignment = Alignment.BEGIN
        private set

    /**
     * The mask for this pane
     */
    private var mask: Mask? = null

    /**
     * Creates a new outline pane
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     * @since 0.10.8
     */
    /**
     * Creates a new outline pane
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @since 0.10.8
     */
    init {
        this.items = ArrayList(length * height)
        this.orientation = Orientable.Orientation.HORIZONTAL

        val mask: Array<String?> = arrayOfNulls(height)
        val maskString: StringBuilder = StringBuilder()

        for (i in 0 until length) {
            maskString.append('1')
        }

        Arrays.fill(mask, maskString.toString())

        this.mask = Mask(*mask)
    }

    @JvmOverloads
    constructor(
        x: Int,
        y: Int,
        length: Int,
        height: Int,
        priority: Priority = Priority.NORMAL
    ) : this(
        Slot.Companion.fromXY(x, y), length, height, priority
    )

    constructor(length: Int, height: Int) : this(0, 0, length, height)

    override fun display(
        inventoryComponent: InventoryComponent, paneOffsetX: Int, paneOffsetY: Int, maxLength: Int,
        maxHeight: Int
    ) {
        val length: Int =
            min(length.toDouble(), maxLength.toDouble()).toInt()
        val height: Int =
            min(height.toDouble(), maxHeight.toDouble()).toInt()

        var itemIndex: Int = 0
        var gapCount: Int = 0

        val size: Int

        if (getOrientation() == Orientable.Orientation.HORIZONTAL) {
            size = height
        } else if (getOrientation() == Orientable.Orientation.VERTICAL) {
            size = length
        } else {
            throw IllegalStateException("Unknown orientation '" + getOrientation() + "'")
        }

        var vectorIndex: Int = 0
        while (vectorIndex < size && getItems().size > itemIndex) {
            var maskLine: BooleanArray?

            if (getOrientation() == Orientable.Orientation.HORIZONTAL) {
                maskLine = mask!!.getRow(vectorIndex)
            } else if (getOrientation() == Orientable.Orientation.VERTICAL) {
                maskLine = mask!!.getColumn(vectorIndex)
            } else {
                throw IllegalStateException("Unknown orientation '" + getOrientation() + "'")
            }

            var enabled: Int = 0

            for (bool: Boolean in maskLine) {
                if (bool) {
                    enabled++
                }
            }

            var items: Array<GuiItem?>

            if (doesRepeat()) {
                items = arrayOfNulls(enabled)
            } else {
                val remainingPositions: Int =
                    gapCount + (getItems().size - itemIndex - 1) * (getGap() + 1) + 1

                items = arrayOfNulls(
                    min(
                        enabled.toDouble(),
                        remainingPositions.toDouble()
                    ).toInt()
                )
            }

            for (index in items.indices) {
                if (gapCount == 0) {
                    items.get(index) = getItems().get(itemIndex)

                    itemIndex++

                    if (doesRepeat() && itemIndex >= getItems().size) {
                        itemIndex = 0
                    }

                    gapCount = getGap()
                } else {
                    items.get(index) = null

                    gapCount--
                }
            }

            var index: Int

            if (alignment == Alignment.BEGIN) {
                index = 0
            } else if (alignment == Alignment.CENTER) {
                index = -((enabled - items.size) / 2)
            } else {
                throw IllegalStateException("Unknown alignment '" + alignment + "'")
            }

            for (opposingVectorIndex in maskLine.indices) {
                if (!maskLine.get(opposingVectorIndex)) {
                    continue
                }

                if (index >= 0 && index < items.size && items.get(index) != null) {
                    var x: Int
                    var y: Int

                    if (getOrientation() == Orientable.Orientation.HORIZONTAL) {
                        x = opposingVectorIndex
                        y = vectorIndex
                    } else if (getOrientation() == Orientable.Orientation.VERTICAL) {
                        x = vectorIndex
                        y = opposingVectorIndex
                    } else {
                        throw IllegalStateException("Unknown orientation '" + getOrientation() + "'")
                    }

                    if (isFlippedHorizontally) {
                        x = length - x - 1
                    }

                    if (isFlippedVertically) {
                        y = height - y - 1
                    }

                    val coordinates: Map.Entry<Int, Int> = processClockwiseRotation(
                        x, y,
                        length, height, rotation
                    )

                    x = coordinates.key
                    y = coordinates.value

                    if (x >= 0 && x < length && y >= 0 && y < height) {
                        val slot: Slot = getSlot()

                        val finalRow: Int = slot.getY(maxLength) + y + paneOffsetY
                        val finalColumn: Int = slot.getX(maxLength) + x + paneOffsetX

                        val item: GuiItem? = items.get(index)
                        if (item!!.isVisible()) {
                            inventoryComponent.setItem(item, finalColumn, finalRow)
                        }
                    }
                }

                index++
            }
            vectorIndex++
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

        callOnClick(event)

        val itemStack: ItemStack? = event.getCurrentItem()

        if (itemStack == null) {
            return false
        }

        val item: GuiItem? = Pane.Companion.findMatchingItem<GuiItem>(items, itemStack)

        if (item == null) {
            return false
        }

        item.callAction(event)

        return true
    }

    @Contract(pure = true)
    override fun copy(): OutlinePane {
        val outlinePane: OutlinePane = OutlinePane(getSlot(), length, height, getPriority())

        for (item: GuiItem in items) {
            outlinePane.addItem(item.copy())
        }

        outlinePane.setVisible(isVisible())
        outlinePane.onClick = onClick

        outlinePane.uuid = uuid

        outlinePane.orientation = orientation
        outlinePane.rotation = rotation
        outlinePane.gap = gap
        outlinePane.repeat = repeat
        outlinePane.isFlippedHorizontally = isFlippedHorizontally
        outlinePane.isFlippedVertically = isFlippedVertically
        outlinePane.mask = mask
        outlinePane.alignment = alignment

        return outlinePane
    }

    override fun setRotation(rotation: Int) {
        if (length != height) {
            throw UnsupportedOperationException("length and height are different")
        }

        require(rotation % 90 == 0) { "rotation isn't divisible by 90" }

        this.rotation = rotation % 360
    }

    /**
     * Adds a gui item in the specified index
     *
     * @param item the item to add
     * @param index the item's index
     */
    fun insertItem(item: GuiItem, index: Int) {
        items.add(index, item)
    }

    /**
     * Adds a gui item at the specific spot in the pane
     *
     * @param item the item to set
     */
    fun addItem(item: GuiItem) {
        items.add(item)
    }

    /**
     * Removes the specified item from the pane
     *
     * @param item the item to remove
     * @since 0.5.8
     */
    fun removeItem(item: GuiItem) {
        items.remove(item)
    }

    override fun clear() {
        items.clear()
    }

    /**
     * Applies a custom mask to this pane. This will throw an [IllegalArgumentException] when the mask's dimension
     * differs from this pane's dimension.
     *
     * @param mask the mask to apply to this pane
     * @throws IllegalArgumentException when the mask's dimension is incorrect
     * @since 0.5.16
     */
    fun applyMask(mask: Mask) {
        require(!(length != mask.getLength() || height != mask.getHeight())) { "Mask's dimension must be the same as the pane's dimension" }

        this.mask = mask
    }

    override var length: Int
        get() = super.length
        set(length) {
            super.setLength(length)

            applyMask(getMask().setLength(length))
        }

    override var height: Int
        get() = super.height
        set(height) {
            super.setHeight(height)

            applyMask(getMask().setHeight(height))
        }

    /**
     * Aligns the pane in the way specified by the provided alignment.
     *
     * @param alignment the new alignment
     * @since 0.10.1
     */
    fun align(alignment: Alignment) {
        this.alignment = alignment
    }

    override fun flipHorizontally(flipHorizontally: Boolean) {
        this.isFlippedHorizontally = flipHorizontally
    }

    override fun flipVertically(flipVertically: Boolean) {
        this.isFlippedVertically = flipVertically
    }

    /**
     * Sets the gap of the pane
     *
     * @param gap the new gap
     */
    fun setGap(gap: Int) {
        this.gap = gap
    }

    override fun setOrientation(orientation: Orientable.Orientation) {
        this.orientation = orientation
    }

    /**
     * Sets whether this pane should repeat itself
     *
     * @param repeat whether the pane should repeat
     */
    fun setRepeat(repeat: Boolean) {
        this.repeat = repeat
    }

    @get:Contract(pure = true)
    override val panes: Collection<Pane>
        get() {
            return HashSet()
        }

    /**
     * Gets whether this outline pane repeats itself
     *
     * @return true if this pane repeats, false otherwise
     */
    @Contract(pure = true)
    fun doesRepeat(): Boolean {
        return repeat
    }

    /**
     * Gets the gap of the pane
     *
     * @return the gap
     */
    @Contract(pure = true)
    fun getGap(): Int {
        return gap
    }

    override fun getItems(): List<GuiItem> {
        return items
    }

    /**
     * Gets the mask applied to this pane.
     *
     * @return the mask
     * @since 0.6.2
     */
    @Contract(pure = true)
    fun getMask(): Mask {
        return mask!!
    }

    /**
     * Gets the orientation of this outline pane
     *
     * @return the orientation
     */
    @Contract(pure = true)
    override fun getOrientation(): Orientable.Orientation {
        return orientation!!
    }

    @Contract(pure = true)
    override fun getRotation(): Int {
        return rotation
    }

    /**
     * An enum containing different alignments that can be used on the outline pane.
     *
     * @since 0.10.1
     */
    enum class Alignment {
        /**
         * Aligns the items at the start of the pane.
         *
         * @since 0.10.1
         */
        BEGIN,

        /**
         * Aligns the items in the center of the pane. If there is no exact center, this will preference the left (for a
         * horizontal orientation) or the top (for a vertical orientation).
         *
         * @since 0.10.1
         */
        CENTER
    }

    companion object {
        /**
         * Loads an outline pane from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @param plugin the plugin that will be the owner of the created items
         * @return the outline pane
         * @since 0.10.8
         */
        @JvmStatic
        fun load(instance: Any, element: Element, plugin: Plugin): OutlinePane {
            try {
                val outlinePane: OutlinePane = OutlinePane(
                    element.getAttribute("length").toInt(),
                    element.getAttribute("height").toInt()
                )

                if (element.hasAttribute("gap")) outlinePane.setGap(
                    element.getAttribute("gap").toInt()
                )

                if (element.hasAttribute("repeat")) outlinePane.setRepeat(
                    element.getAttribute("repeat").toBoolean()
                )

                if (element.hasAttribute("alignment")) {
                    outlinePane.align(
                        Alignment.valueOf(
                            element.getAttribute("alignment").uppercase(Locale.getDefault())
                        )
                    )
                }

                Pane.Companion.load(outlinePane, instance, element)
                Flippable.Companion.load(outlinePane, element)
                Orientable.Companion.load(outlinePane, element)
                Rotatable.Companion.load(outlinePane, element)

                if (element.hasAttribute("populate")) return outlinePane

                val childNodes: NodeList = element.getChildNodes()

                for (i in 0 until childNodes.getLength()) {
                    val item: Node = childNodes.item(i)

                    if (item.getNodeType() != Node.ELEMENT_NODE) continue

                    if (item.getNodeName() == "empty") outlinePane.addItem(
                        GuiItem(
                            ItemStack(
                                Material.AIR
                            ), plugin
                        )
                    )
                    else outlinePane.addItem(
                        Pane.Companion.loadItem(
                            instance,
                            item as Element,
                            plugin
                        )
                    )
                }

                return outlinePane
            } catch (exception: NumberFormatException) {
                throw XMLLoadException(exception)
            }
        }

        /**
         * Loads an outline pane from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @return the outline pane
         */
        @Deprecated(
            """this method is no longer used internally and has been superseded by
                  {@link #load(Object, Element, Plugin)}"""
        )
        fun load(instance: Any, element: Element): OutlinePane {
            return load(
                instance, element, JavaPlugin.getProvidingPlugin(
                    OutlinePane::class.java
                )
            )
        }
    }
}
