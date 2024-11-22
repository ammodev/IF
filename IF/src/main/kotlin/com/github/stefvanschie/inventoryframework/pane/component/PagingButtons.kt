package com.github.stefvanschie.inventoryframework.pane.component

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.component.PagingButtons
import com.github.stefvanschie.inventoryframework.pane.util.Slot
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import java.util.*
import kotlin.math.min

/**
 * An interface for interacting with [PaginatedPane]s. This gives two buttons for navigating backwards and
 * forwards through the pages of the [PaginatedPane]. The backward button will be displayed at (0, 0) of this pane
 * and the forward button will be displayed at (length - 1, 0) of this pane. If the paginated pane is at the first page
 * or the last page, the backwards respectively the forward button will not show. This does not display the
 * [PaginatedPane] itself, but is merely an interface for interacting with it.
 *
 * @since 0.10.14
 */
class PagingButtons @JvmOverloads constructor(
    slot: Slot,
    length: Int,
    priority: Priority,
    pages: PaginatedPane,
    plugin: Plugin = JavaPlugin.getProvidingPlugin(
        PagingButtons::class.java
    )
) : Pane(slot, length, 1, priority) {
    /**
     * The paginated pane.
     */
    private val pages: PaginatedPane

    /**
     * The backwards button.
     */
    private var backwardButton: GuiItem? = null

    /**
     * The forwards button.
     */
    private var forwardButton: GuiItem? = null

    /**
     * The plugin with which the items were created.
     */
    private val plugin: Plugin

    /**
     * Creates a new PagingButtons instance, which controls the provided [PaginatedPane]. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * [IllegalArgumentException].
     *
     * @param slot the position of this interface
     * @param length the length of this interface
     * @param priority the priority of this interface
     * @param pages the pages to interact with
     * @param plugin the plugin that will be the owner of this interface's items
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    /**
     * Creates a new PagingButtons instance, which controls the provided [PaginatedPane]. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * [IllegalArgumentException].
     *
     * @param slot the position of this interface
     * @param length the length of this interface
     * @param priority the priority of this interface
     * @param pages the pages to interact with
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    init {
        require(length >= 2) { "Length of paging buttons must be at least 2" }

        this.pages = pages
        this.plugin = plugin

        this.backwardButton = GuiItem(ItemStack(Material.ARROW), plugin)
        this.forwardButton = GuiItem(ItemStack(Material.ARROW), plugin)
    }

    /**
     * Creates a new PagingButtons instance, which controls the provided [PaginatedPane]. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * [IllegalArgumentException].
     *
     * @param slot the position of this interface
     * @param length the length of this interface
     * @param pages the pages to interact with
     * @param plugin the plugin that will be the owner of this interface's items
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    constructor(slot: Slot, length: Int, pages: PaginatedPane, plugin: Plugin) : this(
        slot,
        length,
        Priority.NORMAL,
        pages,
        plugin
    )

    /**
     * Creates a new PagingButtons instance, which controls the provided [PaginatedPane]. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * [IllegalArgumentException].
     *
     * @param slot the position of this interface
     * @param length the length of this interface
     * @param pages the pages to interact with
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    constructor(slot: Slot, length: Int, pages: PaginatedPane) : this(
        slot,
        length,
        Priority.NORMAL,
        pages
    )

    /**
     * Creates a new PagingButtons instance, which controls the provided [PaginatedPane]. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * [IllegalArgumentException].
     *
     * @param length the length of this interface
     * @param priority the priority of this interface
     * @param pages the pages to interact with
     * @param plugin the plugin that will be the owner of this interface's items
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    constructor(
        length: Int,
        priority: Priority,
        pages: PaginatedPane,
        plugin: Plugin
    ) : this(Slot.Companion.fromXY(0, 0), length, priority, pages, plugin)

    /**
     * Creates a new PagingButtons instance, which controls the provided [PaginatedPane]. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * [IllegalArgumentException].
     *
     * @param length the length of this interface
     * @param priority the priority of this interface
     * @param pages the pages to interact with
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    constructor(length: Int, priority: Priority, pages: PaginatedPane) : this(
        Slot.Companion.fromXY(0, 0), length, priority, pages, JavaPlugin.getProvidingPlugin(
            PagingButtons::class.java
        )
    )

    /**
     * Creates a new PagingButtons instance, which controls the provided [PaginatedPane]. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * [IllegalArgumentException].
     *
     * @param length the length of this interface
     * @param pages the pages to interact with
     * @param plugin the plugin that will be the owner of this interface's items
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    constructor(length: Int, pages: PaginatedPane, plugin: Plugin) : this(
        Slot.Companion.fromXY(
            0,
            0
        ), length, Priority.NORMAL, pages, plugin
    )

    /**
     * Creates a new PagingButtons instance, which controls the provided [PaginatedPane]. The backward and forward
     * item will be an arrow. If the length provided is less than 2, this will throw an
     * [IllegalArgumentException].
     *
     * @param length the length of this interface
     * @param pages the pages to interact with
     * @since 0.10.14
     * @throws IllegalArgumentException if the length is less than 2
     */
    constructor(length: Int, pages: PaginatedPane) : this(
        Slot.Companion.fromXY(0, 0),
        length,
        Priority.NORMAL,
        pages
    )

    override fun click(
        gui: Gui,
        inventoryComponent: InventoryComponent,
        event: InventoryClickEvent,
        slot: Int,
        paneOffsetX: Int,
        paneOffsetY: Int,
        maxLength: Int,
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

        if (Pane.Companion.matchesItem(backwardButton!!, itemStack)) {
            pages.setPage(pages.getPage() - 1)

            backwardButton!!.callAction(event)

            gui.update()

            return true
        }

        if (Pane.Companion.matchesItem(forwardButton!!, itemStack)) {
            pages.setPage(pages.getPage() + 1)

            forwardButton!!.callAction(event)

            gui.update()

            return true
        }

        return false
    }

    override fun display(
        inventoryComponent: InventoryComponent,
        paneOffsetX: Int,
        paneOffsetY: Int,
        maxLength: Int,
        maxHeight: Int
    ) {
        val length: Int = min(getLength().toDouble(), maxLength.toDouble()).toInt()

        val x: Int = super.slot.getX(length) + paneOffsetX
        val y: Int = super.slot.getY(length) + paneOffsetY

        if (pages.getPage() > 0) {
            inventoryComponent.setItem(backwardButton!!, x, y)
        }

        if (pages.getPage() < pages.getPages() - 1) {
            inventoryComponent.setItem(forwardButton!!, x + length - 1, y)
        }
    }

    /**
     * {@inheritDoc}
     *
     * This does not make a copy of the [PaginatedPane] that is being controlled by this interface.
     */
    @Contract(pure = true)
    override fun copy(): PagingButtons {
        val pagingButtons: PagingButtons = PagingButtons(
            getSlot(), getLength(), getPriority(),
            this.pages,
            this.plugin
        )

        pagingButtons.setVisible(isVisible())
        pagingButtons.onClick = super.onClick

        pagingButtons.uuid = super.uuid

        pagingButtons.backwardButton = backwardButton!!.copy()
        pagingButtons.forwardButton = forwardButton!!.copy()

        return pagingButtons
    }

    @get:Contract(pure = true)
    override val items: Collection<GuiItem>
        get() {
            val items: MutableCollection<GuiItem?> =
                HashSet()

            items.add(this.backwardButton)
            items.add(this.forwardButton)

            return Collections.unmodifiableCollection(items)
        }

    /**
     * Sets the item to be used for navigating backwards. If an event is attached to the item, this event will be called
     * after the page has been changed.
     *
     * @param item the new backward item
     * @since 0.10.14
     */
    fun setBackwardButton(item: GuiItem) {
        this.backwardButton = item
    }

    /**
     * Sets the item to be used for navigating forwards. If an event is attached to the item, this event will be called
     * after the page has been changed.
     *
     * @param item the new forward item
     * @since 0.10.14
     */
    fun setForwardButton(item: GuiItem) {
        this.forwardButton = item
    }

    @get:Contract(pure = true)
    override val panes: Collection<Pane>
        get() {
            return emptySet()
        }

    /**
     * This is a no-op.
     *
     * @since 0.10.14
     */
    override fun clear() {}

    companion object {
        /**
         * Loads a paging buttons pane from an XML element.
         *
         * @param instance the instance class
         * @param element the element
         * @param plugin the plugin that will be the owner of the underlying items
         * @return the paging buttons pane
         * @since 0.10.14
         */
        @JvmStatic
        @Contract(pure = true)
        fun load(instance: Any, element: Element, plugin: Plugin): PagingButtons {
            val length: Int

            try {
                length = element.getAttribute("length").toInt()
            } catch (exception: NumberFormatException) {
                throw XMLLoadException(exception)
            }

            if (!element.hasAttribute("pages")) {
                throw XMLLoadException("Paging buttons does not have pages attribute")
            }

            val paginatedPaneElement: Element? =
                element.getOwnerDocument().getElementById(element.getAttribute("pages"))

            if (paginatedPaneElement == null) {
                throw XMLLoadException("Paging buttons pages reference is invalid")
            }

            val paginatedPane: Any = paginatedPaneElement.getUserData("pane")

            if (paginatedPane !is PaginatedPane) {
                throw XMLLoadException("Retrieved data is not a paginated pane")
            }

            val pagingButtons: PagingButtons = PagingButtons(length, paginatedPane)

            Pane.Companion.load(pagingButtons, instance, element)

            return pagingButtons
        }
    }
}
