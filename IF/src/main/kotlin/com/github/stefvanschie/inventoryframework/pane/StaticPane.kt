package com.github.stefvanschie.inventoryframework.pane

import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder.Companion.deserialize
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import com.github.stefvanschie.inventoryframework.pane.util.Slot
import com.github.stefvanschie.inventoryframework.util.GeometryUtil.processClockwiseRotation
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.util.function.Consumer
import kotlin.math.min

/**
 * A pane for static items and stuff. All items will have to be specified a slot, or will be added in the next position.
 *
 *
 * This pane allows you to specify the positions of the items either in the form of an x and y coordinate pair or as an
 * index, in which case the indexing starts from the top left and continues to the right and bottom, with the horizontal
 * axis taking priority. There are nuances at play with regard to mixing these two types of positioning systems within
 * the same pane. It's recommended to only use one of these systems per pane and to not mix them.
 *
 */
class StaticPane @JvmOverloads constructor(
    slot: Slot,
    length: Int,
    height: Int,
    priority: Priority = Priority.NORMAL
) :
    Pane(slot, length, height, priority), Flippable, Rotatable {
    /**
     * A map of locations inside this pane and their item. The locations are stored in a way where the x coordinate is
     * the key and the y coordinate is the value.
     */
    override val items: MutableMap<Slot, GuiItem>

    /**
     * The clockwise rotation of this pane in degrees
     */
    override var rotation: Int = 0

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
     * Creates a new static pane.
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     * @since 0.10.8
     */
    /**
     * Creates a new static pane.
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @since 0.10.8
     */
    init {
        this.items = HashMap(length * height)
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

    /**
     * {@inheritDoc}
     *
     * If there are multiple items in the same position when displaying the items, either one of those items may be
     * shown. In particular, there is no guarantee that a specific item will be shown.
     *
     * @param inventoryComponent {@inheritDoc}
     * @param paneOffsetX {@inheritDoc}
     * @param paneOffsetY {@inheritDoc}
     * @param maxLength {@inheritDoc}
     * @param maxHeight {@inheritDoc}
     */
    override fun display(
        inventoryComponent: InventoryComponent, paneOffsetX: Int, paneOffsetY: Int, maxLength: Int,
        maxHeight: Int
    ) {
        val length: Int =
            min(length.toDouble(), maxLength.toDouble()).toInt()
        val height: Int =
            min(height.toDouble(), maxHeight.toDouble()).toInt()

        items.entries.stream().filter { entry: Map.Entry<Slot, GuiItem> -> entry.value.isVisible() }
            .forEach { entry: Map.Entry<Slot, GuiItem> ->
                val location: Slot = entry.key
                var x: Int = location.getX(getLength())
                var y: Int = location.getY(getLength())

                if (isFlippedHorizontally) x = length - x - 1

                if (isFlippedVertically) y = height - y - 1

                val coordinates: Map.Entry<Int, Int> = processClockwiseRotation(
                    x, y, length, height,
                    rotation
                )

                x = coordinates.key
                y = coordinates.value

                if (x < 0 || x >= length || y < 0 || y >= height) {
                    return@forEach
                }

                val item: GuiItem = entry.value

                val slot: Slot = getSlot()
                val finalRow: Int = slot.getY(maxLength) + y + paneOffsetY
                val finalColumn: Int = slot.getX(maxLength) + x + paneOffsetX
                inventoryComponent.setItem(item, finalColumn, finalRow)
            }
    }

    /**
     * Adds a gui item at the specific spot in the pane. If there is another item specified in terms of x and y
     * coordinates that are equal to the coordinates of this item, the old item will be overwritten by this item.
     *
     * @param item the item to set
     * @param x    the x coordinate of the position of the item
     * @param y    the y coordinate of the position of the item
     */
    fun addItem(item: GuiItem, x: Int, y: Int) {
        addItem(item, Slot.Companion.fromXY(x, y))
    }

    /**
     * Adds a gui item at the specific spot in the pane. If the slot is specified in terms of an x and y coordinate pair
     * and this pane contains another item whose position is specified as such and these positions are equal, the old
     * item will be overwritten by this item. If the slot is specified in terms of an index and this pane contains
     * another item whose position is specified as such and these positions are equal, the old item will be overwritten
     * by this item.
     *
     * @param item the item to set
     * @param slot the position of the item
     * @since 0.10.8
     */
    fun addItem(item: GuiItem, slot: Slot) {
        items.put(slot, item)
    }

    /**
     * Removes the specified item from the pane
     *
     * @param item the item to remove
     * @since 0.5.8
     */
    fun removeItem(item: GuiItem) {
        items.values.removeIf { guiItem: GuiItem -> guiItem == item }
    }

    /**
     * Removes the specified item from the pane. This will only remove items whose slot was specified in terms of an x
     * and y coordinate pair which matches the coordinate specified.
     *
     * @param x the x coordinate of the item to remove
     * @param y the y coordinate of the item to remove
     * @since 0.10.0
     */
    fun removeItem(x: Int, y: Int) {
        items.remove(Slot.Companion.fromXY(x, y))
    }

    /**
     * Removes the specified item from the pane. This will only remove items whose slot was specified in the same way as
     * the original slot and whose slot positions match.
     *
     * @param slot the slot of the item to remove
     * @since 0.10.8
     */
    fun removeItem(slot: Slot) {
        items.remove(slot)
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

        val clickedItem: GuiItem? =
            Pane.Companion.findMatchingItem<GuiItem>(items.values, itemStack)

        if (clickedItem == null) {
            return false
        }

        clickedItem.callAction(event)

        return true
    }

    @Contract(pure = true)
    override fun copy(): StaticPane {
        val staticPane: StaticPane = StaticPane(getSlot(), length, height, getPriority())

        for (entry: Map.Entry<Slot, GuiItem> in items.entries) {
            staticPane.addItem(entry.value.copy(), entry.key)
        }

        staticPane.setVisible(isVisible())
        staticPane.onClick = onClick

        staticPane.uuid = uuid

        staticPane.rotation = rotation
        staticPane.isFlippedHorizontally = isFlippedHorizontally
        staticPane.isFlippedVertically = isFlippedVertically

        return staticPane
    }

    override fun setRotation(rotation: Int) {
        if (length != height) {
            throw UnsupportedOperationException("length and height are different")
        }
        require(rotation % 90 == 0) { "rotation isn't divisible by 90" }

        this.rotation = rotation % 360
    }

    /**
     * Fills all empty space in the pane with the given `itemStack` and adds the given action
     *
     * @param itemStack The [ItemStack] to fill the empty space with
     * @param action The action called whenever an interaction with the item happens
     * @param plugin the plugin that will be the owner of the created items
     * @see .fillWith
     * @since 0.10.8
     */
    /**
     * Fills all empty space in the pane with the given `itemStack` and adds the given action
     *
     * @param itemStack The [ItemStack] to fill the empty space with
     * @param action The action called whenever an interaction with the item happens
     * @since 0.5.9
     */
    @JvmOverloads
    fun fillWith(
        itemStack: ItemStack, action: Consumer<InventoryClickEvent?>?,
        plugin: Plugin = JavaPlugin.getProvidingPlugin(
            StaticPane::class.java
        )
    ) {
        //The non empty spots
        val locations: Set<Slot> =
            items.keys

        for (y in 0 until this.getHeight()) {
            for (x in 0 until this.getLength()) {
                var found: Boolean = false

                for (location: Slot in locations) {
                    if (location.getX(getLength()) == x && location.getY(getLength()) == y) {
                        found = true
                        break
                    }
                }

                if (!found) {
                    this.addItem(GuiItem(itemStack, action, plugin), x, y)
                }
            }
        }
    }

    /**
     * Fills all empty space in the pane with the given `itemStack`
     *
     * @param itemStack The [ItemStack] to fill the empty space with
     * @since 0.2.4
     */
    @Contract("null -> fail")
    fun fillWith(itemStack: ItemStack) {
        this.fillWith(itemStack, null)
    }

    override fun getItems(): Collection<GuiItem> {
        return items.values
    }

    override fun clear() {
        items.clear()
    }

    @get:Contract(pure = true)
    override val panes: Collection<Pane>
        get() {
            return HashSet()
        }

    override fun flipHorizontally(flipHorizontally: Boolean) {
        this.isFlippedHorizontally = flipHorizontally
    }

    override fun flipVertically(flipVertically: Boolean) {
        this.isFlippedVertically = flipVertically
    }

    @Contract(pure = true)
    override fun getRotation(): Int {
        return rotation
    }

    companion object {
        /**
         * Loads an outline pane from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @param plugin the plugin that will be the owner of the udnerlying items
         * @return the outline pane
         * @since 0.10.8
         */
        @JvmStatic
        fun load(instance: Any, element: Element, plugin: Plugin): StaticPane {
            try {
                val staticPane: StaticPane = StaticPane(
                    element.getAttribute("length").toInt(),
                    element.getAttribute("height").toInt()
                )

                Pane.Companion.load(staticPane, instance, element)
                Flippable.Companion.load(staticPane, element)
                Rotatable.Companion.load(staticPane, element)

                if (element.hasAttribute("populate")) return staticPane

                val childNodes: NodeList = element.getChildNodes()

                for (i in 0 until childNodes.getLength()) {
                    val item: Node = childNodes.item(i)

                    if (item.getNodeType() != Node.ELEMENT_NODE) continue

                    val child: Element = item as Element

                    staticPane.addItem(
                        Pane.Companion.loadItem(instance, child, plugin),
                        Slot.Companion.deserialize(child)
                    )
                }

                return staticPane
            } catch (exception: NumberFormatException) {
                throw XMLLoadException(exception)
            }
        }

        /**
         * Loads an outline pane from a given element
         *
         * @param instance the instance class
         * @param element  the element
         * @return the outline pane
         */
        @Deprecated(
            """this method is no longer used internally and has been superseded by
                  {@link #load(Object, Element, Plugin)}"""
        )
        fun load(instance: Any, element: Element): StaticPane {
            return load(
                instance, element, JavaPlugin.getProvidingPlugin(
                    StaticPane::class.java
                )
            )
        }
    }
}
