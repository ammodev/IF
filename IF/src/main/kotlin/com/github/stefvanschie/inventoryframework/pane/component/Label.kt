package com.github.stefvanschie.inventoryframework.pane.component

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.font.util.Font
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.*
import com.github.stefvanschie.inventoryframework.pane.util.Slot
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import java.util.function.BiFunction

/**
 * A label for displaying text.
 *
 * @since 0.5.0
 */
class Label @JvmOverloads constructor(
    slot: Slot, length: Int, height: Int, priority: Priority,
    /**
     * The character set used for displaying the characters in this label
     */
    @get:Contract(pure = true) val font: Font,
    plugin: Plugin = JavaPlugin.getProvidingPlugin(
        Label::class.java
    )
) :
    OutlinePane(slot, length, height) {
    /**
     * Gets the character set currently used for the text in this label
     *
     * @return the character set
     * @since 0.5.0
     */

    /**
     * The text to be displayed
     */
    private var text: String? = null

    /**
     * The plugin to be sed for creating items
     */
    private val plugin: Plugin

    /**
     * Creates a new label
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @param font the character set
     * @param plugin the plugin that will be the owner for this label's items
     * @see .Label
     * @since 0.10.8
     */
    /**
     * Creates a new label
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @param font the character set
     * @since 0.10.8
     */
    init {
        this.text = ""

        this.plugin = plugin

        setPriority(priority)
    }

    /**
     * Creates a new label
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @param font the character set
     * @param plugin the plugin that will be the owner for this label's items
     * @see .Label
     * @since 0.10.8
     */
    /**
     * Creates a new label
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param length the length
     * @param height the height
     * @param priority the priority
     * @param font the character set
     * @since 0.5.0
     */
    @JvmOverloads
    constructor(
        x: Int, y: Int, length: Int, height: Int, priority: Priority, font: Font,
        plugin: Plugin = JavaPlugin.getProvidingPlugin(
            Label::class.java
        )
    ) : this(Slot.Companion.fromXY(x, y), length, height, priority, font, plugin)

    /**
     * Creates a new label
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param font the character set
     * @param plugin the plugin that will be the owner for this label's items
     * @see .Label
     * @since 0.10.8
     */
    constructor(slot: Slot, length: Int, height: Int, font: Font, plugin: Plugin) : this(
        slot,
        length,
        height,
        Priority.NORMAL,
        font,
        plugin
    )

    /**
     * Creates a new label
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param length the length
     * @param height the height
     * @param font the character set
     * @param plugin the plugin that will be the owner for this label's items
     * @see .Label
     * @since 0.10.8
     */
    constructor(x: Int, y: Int, length: Int, height: Int, font: Font, plugin: Plugin) : this(
        x,
        y,
        length,
        height,
        Priority.NORMAL,
        font,
        plugin
    )

    /**
     * Creates a new label
     *
     * @param length the length
     * @param height the height
     * @param font the character set
     * @param plugin the plugin that will be the owner for this label's items
     * @see .Label
     * @since 0.10.8
     */
    constructor(length: Int, height: Int, font: Font, plugin: Plugin) : this(
        0,
        0,
        length,
        height,
        font,
        plugin
    )

    /**
     * Creates a new label
     *
     * @param slot the slot
     * @param length the length
     * @param height the height
     * @param font the character set
     * @since 0.10.8
     */
    constructor(slot: Slot, length: Int, height: Int, font: Font) : this(
        slot,
        length,
        height,
        Priority.NORMAL,
        font
    )

    /**
     * Creates a new label
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param length the length
     * @param height the height
     * @param font the character set
     * @since 0.5.0
     */
    constructor(x: Int, y: Int, length: Int, height: Int, font: Font) : this(
        x,
        y,
        length,
        height,
        Priority.NORMAL,
        font
    )

    /**
     * Creates a new label
     *
     * @param length the length
     * @param height the height
     * @param font the character set
     * @since 0.5.0
     */
    constructor(length: Int, height: Int, font: Font) : this(0, 0, length, height, font)

    /**
     * Sets the text to be displayed in this label. If this label already had text, this text will be overwritten. The
     * specified processor will be called for each character that is part of the specified text. The provided character
     * will be the original character that was attempted to be shown - it is not subject to any transformations that may
     * be applied for finding a valid item corresponding to this character, such as capitalization changes.
     *
     * @param text the new text
     * @param processor processes each character before using them
     * @since 0.10.4
     */
    fun setText(
        text: String,
        processor: BiFunction<in Char, in ItemStack, out GuiItem>
    ) {
        this.text = text

        clear()

        for (character in text.toCharArray()) {
            var item = font.toItem(character)

            if (item == null) {
                item = font.toItem(character.uppercaseChar())
            }

            if (item == null) {
                item = font.toItem(character.lowercaseChar())
            }

            if (item == null) {
                item = font.defaultItem
            }

            addItem(processor.apply(character, item!!.clone()))
        }
    }

    /**
     * Sets the text to be displayed in this label. If this label already had text, this text will be overwritten.
     *
     * @param text the new text
     * @see .setText
     * @since 0.5.0
     */
    fun setText(text: String) {
        setText(
            text
        ) { character: Char, item: ItemStack ->
            GuiItem(
                item,
                plugin
            )
        }
    }

    @Contract(pure = true)
    override fun copy(): Label {
        val label = Label(
            getSlot(), length, height,
            priority,
            font,
            this.plugin
        )

        for (item in items) {
            label.addItem(item.copy())
        }

        label.isVisible = isVisible
        label.onClick = onClick

        label.orientation = orientation
        label.rotation = rotation
        label.gap = gap
        label.setRepeat(doesRepeat())
        label.flipHorizontally(isFlippedHorizontally)
        label.flipVertically(isFlippedVertically)
        label.applyMask(mask)
        label.uuid = uuid

        label.text = text

        return label
    }

    override fun click(
        gui: Gui, inventoryComponent: InventoryComponent,
        event: InventoryClickEvent, slot: Int, paneOffsetX: Int, paneOffsetY: Int, maxLength: Int,
        maxHeight: Int
    ): Boolean {
        event.isCancelled = true

        return super.click(
            gui,
            inventoryComponent,
            event,
            slot,
            paneOffsetX,
            paneOffsetY,
            maxLength,
            maxHeight
        )
    }

    /**
     * Gets the text currently displayed in this label
     *
     * @return the text in this label
     * @since 0.5.0
     */
    @Contract(pure = true)
    fun getText(): String {
        return text!!
    }

    companion object {
        /**
         * Loads a label from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @param plugin the plugin that will be the owner of the underlying items
         * @return the percentage bar
         * @since 0.10.8
         */
        @JvmStatic
        @Contract(pure = true)
        fun load(instance: Any, element: Element, plugin: Plugin): Label {
            val length: Int
            val height: Int

            try {
                length = element.getAttribute("length").toInt()
                height = element.getAttribute("height").toInt()
            } catch (exception: NumberFormatException) {
                throw XMLLoadException(exception)
            }

            var font: Font? = null

            if (element.hasAttribute("font")) {
                font = Font.fromName(element.getAttribute("font"))
            }

            if (font == null) {
                throw XMLLoadException("Incorrect font specified for label")
            }

            val label = Label(length, height, font, plugin)

            Pane.Companion.load(label, instance, element)
            Orientable.Companion.load(label, element)
            Flippable.Companion.load(label, element)
            Rotatable.Companion.load(label, element)

            if (element.hasAttribute("populate")) {
                return label
            }

            if (element.hasAttribute("text")) {
                label.setText(element.getAttribute("text"))
            }

            return label
        }

        /**
         * Loads a label from a given element
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
        fun load(instance: Any, element: Element): Label {
            return load(
                instance, element, JavaPlugin.getProvidingPlugin(
                    Label::class.java
                )
            )
        }
    }
}
