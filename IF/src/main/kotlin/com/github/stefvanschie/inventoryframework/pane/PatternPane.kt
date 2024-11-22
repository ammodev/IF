package com.github.stefvanschie.inventoryframework.pane

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.PatternPane
import com.github.stefvanschie.inventoryframework.pane.util.Pattern
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
import java.util.*
import kotlin.math.min

/**
 * A pattern pane allows you to specify a textual pattern and assign items to individual characters.
 *
 * @since 0.9.8
 */
class PatternPane(slot: Slot, length: Int, height: Int, priority: Priority, pattern: Pattern) :
    Pane(slot, length, height, priority), Flippable, Rotatable {
    /**
     * The pattern of this pane.
     */
    private var pattern: Pattern

    /**
     * The bindings between the characters in the pattern and the gui item. Not every character in the pattern has to be
     * present in this map and this map may contain characters that are not present in the pattern.
     */
    private val bindings: MutableMap<Int, GuiItem> = HashMap()

    /**
     * The amount of degrees this pane is rotated by. This will always be between [0,360) and a multiple of 90.
     */
    override var rotation: Int = 0

    /**
     * Whether this pane is flipped horizontally.
     */
    override var isFlippedHorizontally: Boolean = false
        private set

    /**
     * Whether this pane is flipped vertically.
     */
    override var isFlippedVertically: Boolean = false
        private set

    /**
     * Constructs a new pattern pane.
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     * @param pattern the pattern of the pane
     * @throws IllegalArgumentException when the pane and pattern dimensions don't match
     * @since 0.10.8
     */
    init {
        require(!(pattern.getLength() != length || pattern.getHeight() != height)) { "Dimensions of the provided pattern do not match the dimensions of the pane" }

        this.pattern = pattern
    }

    /**
     * Constructs a new pattern pane.
     *
     * @param x the upper left x coordinate of the pane
     * @param y the upper left y coordinate of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     * @param pattern the pattern of the pane
     * @throws IllegalArgumentException when the pane and pattern dimensions don't match
     * @since 0.9.8
     */
    constructor(
        x: Int,
        y: Int,
        length: Int,
        height: Int,
        priority: Priority,
        pattern: Pattern
    ) : this(
        Slot.Companion.fromXY(x, y), length, height, priority, pattern
    )

    /**
     * Constructs a new pattern pane, with no position.
     *
     * @param length the length of the pane
     * @param height the height of the pane
     * @param pattern the pattern of the pane
     * @throws IllegalArgumentException when the pane and pattern dimensions don't match
     * @since 0.9.8
     */
    constructor(length: Int, height: Int, pattern: Pattern) : this(0, 0, length, height, pattern)

    /**
     * Constructs a new pattern pane.
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param pattern the pattern of the pane
     * @throws IllegalArgumentException when the pane and pattern dimensions don't match
     * @since 0.10.8
     */
    constructor(slot: Slot, length: Int, height: Int, pattern: Pattern) : this(
        slot,
        length,
        height,
        Priority.NORMAL,
        pattern
    )

    /**
     * Constructs a new pattern pane.
     *
     * @param x the upper left x coordinate of the pane
     * @param y the upper left y coordinate of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param pattern the pattern of the pane
     * @throws IllegalArgumentException when the pane and pattern dimensions don't match
     * @since 0.9.8
     */
    constructor(x: Int, y: Int, length: Int, height: Int, pattern: Pattern) : this(
        x,
        y,
        length,
        height,
        Priority.NORMAL,
        pattern
    )

    override fun display(
        inventoryComponent: InventoryComponent, paneOffsetX: Int, paneOffsetY: Int, maxLength: Int,
        maxHeight: Int
    ) {
        val length: Int =
            min(length.toDouble(), maxLength.toDouble()).toInt()
        val height: Int =
            min(height.toDouble(), maxHeight.toDouble()).toInt()

        for (x in 0 until length) {
            for (y in 0 until height) {
                val item: GuiItem? = bindings.get(pattern.getCharacter(x, y))

                if (item == null || !item.isVisible()) {
                    continue
                }

                var newX: Int = x
                var newY: Int = y

                if (isFlippedHorizontally) {
                    newX = length - x - 1
                }

                if (isFlippedVertically) {
                    newY = height - y - 1
                }

                val coordinates: Map.Entry<Int, Int> = processClockwiseRotation(
                    newX, newY, length,
                    height, rotation
                )

                newX = coordinates.key
                newY = coordinates.value

                val slot: Slot = getSlot()

                val finalRow: Int = slot.getY(maxLength) + newY + paneOffsetY
                val finalColumn: Int = slot.getX(maxLength) + newX + paneOffsetX

                inventoryComponent.setItem(item, finalColumn, finalRow)
            }
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

        val clickedItem: GuiItem? = Pane.Companion.findMatchingItem<GuiItem>(items, itemStack)

        if (clickedItem == null) {
            return false
        }

        clickedItem.callAction(event)

        return true
    }

    @Contract(pure = true)
    override fun copy(): PatternPane {
        val patternPane: PatternPane =
            PatternPane(getSlot(), getLength(), getHeight(), getPriority(), getPattern())

        patternPane.setVisible(isVisible())
        patternPane.onClick = onClick

        patternPane.uuid = uuid

        patternPane.rotation = rotation
        patternPane.isFlippedHorizontally = isFlippedHorizontally
        patternPane.isFlippedVertically = isFlippedVertically

        return patternPane
    }

    override fun setRotation(rotation: Int) {
        require(getLength() == getHeight()) { "Rotations can only be applied to square panes" }

        require(!(rotation >= 0 && rotation % 90 != 0)) { "Rotation must be non-negative and be a multiple of 90" }

        this.rotation = rotation % 360
    }

    override val items: Collection<GuiItem>
        /**
         * {@inheritDoc}
         *
         * This only returns the items for which their binding also appears in the given pattern. An item bound to 'x',
         * where 'x' does not appear in the pattern will not be returned.
         *
         * @return the bounded and used items
         * @since 0.9.8
         */
        get() {
            val items: MutableSet<GuiItem> = HashSet()

            for (binding: Map.Entry<Int, GuiItem> in bindings.entries) {
                if (pattern.contains(binding.key)) {
                    items.add(binding.value)
                }
            }

            return Collections.unmodifiableCollection(items)
        }

    /**
     * Overrides the pattern set on this pane.
     *
     * @param pattern the new pattern to set
     * @throws IllegalArgumentException when the pane and pattern dimensions don't match
     * @since 0.9.8
     */
    fun setPattern(pattern: Pattern) {
        require(!(pattern.getLength() != getLength() || pattern.getHeight() != getHeight())) { "Dimensions of the provided pattern do not match the dimensions of the pane" }

        this.pattern = pattern
    }

    override var height: Int
        get() = super.height
        set(height) {
            super.setHeight(height)

            this.pattern = pattern.setHeight(height)
        }

    override var length: Int
        get() = super.length
        set(length) {
            super.setLength(length)

            this.pattern = pattern.setLength(length)
        }

    /**
     * Binds a character to a specific item or if the character was already bound, this overwrites the previously
     * binding with the provided one. To bind characters above the 16-bit range, see [.bindItem].
     *
     * @param character the character
     * @param item the item this represents
     * @since 0.9.8
     */
    fun bindItem(character: Char, item: GuiItem) {
        bindings.put(character.code, item)
    }

    /**
     * Binds a character to a specific item or if the character was already bound, this overwrites the previously
     * binding with the provided one.
     *
     * @param character the character
     * @param item the item this represents
     * @since 0.9.8
     * @see PatternPane.bindItem
     */
    fun bindItem(character: Int, item: GuiItem) {
        bindings.put(character, item)
    }

    override fun clear() {
        bindings.clear()
    }

    override fun flipHorizontally(flipHorizontally: Boolean) {
        this.isFlippedHorizontally = flipHorizontally
    }

    override fun flipVertically(flipVertically: Boolean) {
        this.isFlippedVertically = flipVertically
    }

    override val panes: Collection<Pane>
        get() {
            return emptySet()
        }

    /**
     * Gets the pattern.
     *
     * @return the pattern
     * @since 0.9.8
     */
    @Contract(pure = true)
    fun getPattern(): Pattern {
        return pattern
    }

    override fun getRotation(): Int {
        return this.rotation
    }

    companion object {
        /**
         * Loads a pattern pane from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @param plugin the plugin that will own the underlying items
         * @return the pattern pane
         * @since 0.10.8
         */
        @JvmStatic
        fun load(instance: Any, element: Element, plugin: Plugin): PatternPane {
            try {
                val childNodes: NodeList = element.getChildNodes()

                var pattern: Pattern? = null
                val bindings: MutableMap<Int, GuiItem?> = HashMap()

                for (i in 0 until childNodes.getLength()) {
                    val item: Node = childNodes.item(i)

                    if (item.getNodeType() != Node.ELEMENT_NODE) {
                        continue
                    }

                    val child: Element = item as Element
                    val name: String = item.getNodeName()

                    if (name == "pattern") {
                        pattern = Pattern.Companion.load(child)
                    } else if (name == "binding") {
                        val character: String = child.getAttribute("char")

                        if (character == null) {
                            throw XMLLoadException("Missing char attribute on binding")
                        }

                        if (character.codePointCount(0, character.length) != 1) {
                            throw XMLLoadException("Char attribute doesn't have one character")
                        }

                        val children: NodeList = child.getChildNodes()
                        var guiItem: GuiItem? = null

                        for (index in 0 until children.getLength()) {
                            val guiItemNode: Node = children.item(index)

                            if (guiItemNode.getNodeType() != Node.ELEMENT_NODE) {
                                continue
                            }

                            if (guiItem != null) {
                                throw XMLLoadException("Binding has multiple inner tags, one expected")
                            }

                            guiItem =
                                Pane.Companion.loadItem(instance, guiItemNode as Element, plugin)
                        }

                        //guaranteed to only be a single code point
                        bindings.put(character.codePoints().toArray().get(0), guiItem)
                    } else {
                        throw XMLLoadException("Unknown tag " + name + " in pattern pane")
                    }
                }

                if (pattern == null) {
                    throw XMLLoadException("Pattern pane doesn't have a pattern")
                }

                val patternPane: PatternPane = PatternPane(
                    element.getAttribute("length").toInt(),
                    element.getAttribute("height").toInt(),
                    pattern
                )

                Pane.Companion.load(patternPane, instance, element)
                Flippable.Companion.load(patternPane, element)
                Rotatable.Companion.load(patternPane, element)

                if (!element.hasAttribute("populate")) {
                    for (entry: Map.Entry<Int, GuiItem> in bindings.entries) {
                        patternPane.bindItem(entry.key, entry.value)
                    }
                }

                return patternPane
            } catch (exception: NumberFormatException) {
                throw XMLLoadException(exception)
            }
        }

        /**
         * Loads a pattern pane from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @return the pattern pane
         */
        @Deprecated(
            """this method is no longer used internally and has been superseded by
                  {@link #load(Object, Element, Plugin)}"""
        )
        fun load(instance: Any, element: Element): PatternPane {
            return load(
                instance, element, JavaPlugin.getProvidingPlugin(
                    PatternPane::class.java
                )
            )
        }
    }
}
