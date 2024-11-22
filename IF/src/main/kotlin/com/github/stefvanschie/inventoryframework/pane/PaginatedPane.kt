package com.github.stefvanschie.inventoryframework.pane

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.util.Slot
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * A pane for panes that should be spread out over multiple pages
 */
class PaginatedPane : Pane {
    /**
     * A set of panes for the different pages
     */
    override var panes: MutableMap<Int, MutableList<Pane>?> = HashMap()

    /**
     * The current page
     */
    private var page: Int = 0

    /**
     * Creates a new paginated pane
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     * @since 0.10.8
     */
    /**
     * Creates a new paginated pane
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @since 0.10.8
     */
    @JvmOverloads
    constructor(slot: Slot, length: Int, height: Int, priority: Priority = Priority.NORMAL) : super(
        slot,
        length,
        height,
        priority
    )

    constructor(
        x: Int,
        y: Int,
        length: Int,
        height: Int,
        priority: Priority
    ) : this(Slot.Companion.fromXY(x, y), length, height, priority)

    constructor(x: Int, y: Int, length: Int, height: Int) : super(x, y, length, height)

    constructor(length: Int, height: Int) : super(length, height)

    /**
     * Returns the current page
     *
     * @return the current page
     */
    fun getPage(): Int {
        return page
    }

    val pages: Int
        /**
         * Returns the amount of pages
         *
         * @return the amount of pages
         */
        get() {
            return panes.size
        }

    /**
     * Adds the specified pane to a new page. The new page will be at the index one after the highest indexed page
     * currently in this paginated pane. If the highest index pane is `Integer.MAX_VALUE`, this method will throw
     * an [ArithmeticException]. If this paginated pane has no pages, the index of the newly created page will
     * be zero.
     *
     * @param pane the pane to add to a new page
     * @since 0.10.8
     * @throws ArithmeticException if the highest indexed page is the maximum value
     */
    fun addPage(pane: Pane) {
        val list: MutableList<Pane> = ArrayList(1)

        list.add(pane)

        if (panes.isEmpty()) {
            panes.put(0, list)

            return
        }

        var highest: Int = Int.MIN_VALUE

        for (page: Int in panes.keys) {
            if (page > highest) {
                highest = page
            }
        }

        if (highest == Int.MAX_VALUE) {
            throw ArithmeticException("Can't increment page index beyond its maximum value")
        }

        panes.put(highest + 1, list)
    }

    /**
     * Assigns a pane to a selected page
     *
     * @param page the page to assign the pane to
     * @param pane the new pane
     */
    fun addPane(page: Int, pane: Pane) {
        if (!panes.containsKey(page)) panes.put(page, ArrayList())

        panes.get(page)!!.add(pane)

        panes.get(page)!!.sort(
            Comparator.comparing(
                Function { obj: Pane -> obj.getPriority() })
        )
    }

    /**
     * Sets the current displayed page
     *
     * @param page the page
     */
    fun setPage(page: Int) {
        if (!panes.containsKey(page)) throw ArrayIndexOutOfBoundsException("page outside range")
        this.page = page
    }

    /**
     * Populates the PaginatedPane based on the provided list by adding new pages until all items can fit.
     * This can be helpful when dealing with lists of unknown size.
     *
     * @param items The list to populate the pane with
     * @param plugin the plugin that will be the owner of the items created
     * @see .populateWithItemStacks
     * @since 0.10.8
     */
    fun populateWithItemStacks(items: List<ItemStack>, plugin: Plugin) {
        //Don't do anything if the list is empty
        if (items.isEmpty()) {
            return
        }

        val itemsPerPage: Int = this.height * this.length
        val pagesNeeded: Int = max(ceil(items.size / itemsPerPage.toDouble()), 1.0) as Int

        for (i in 0 until pagesNeeded) {
            val page: OutlinePane = OutlinePane(0, 0, this.length, this.height)

            for (j in 0 until itemsPerPage) {
                //Check if the loop reached the end of the list
                val index: Int = i * itemsPerPage + j

                if (index >= items.size) {
                    break
                }

                page.addItem(GuiItem(items.get(index), plugin))
            }

            this.addPane(i, page)
        }
    }

    /**
     * Populates the PaginatedPane based on the provided list by adding new pages until all items can fit.
     * This can be helpful when dealing with lists of unknown size.
     *
     * @param items The list to populate the pane with
     */
    fun populateWithItemStacks(items: List<ItemStack>) {
        populateWithItemStacks(
            items, JavaPlugin.getProvidingPlugin(
                PaginatedPane::class.java
            )
        )
    }

    /**
     * Populates the PaginatedPane based on the provided list by adding new pages until all items can fit.
     * This can be helpful when dealing with lists of unknown size.
     *
     * @param items The list to populate the pane with
     */
    @Contract("null -> fail")
    fun populateWithGuiItems(items: List<GuiItem>) {
        //Don't do anything if the list is empty
        if (items.isEmpty()) {
            return
        }

        val itemsPerPage: Int = this.height * this.length
        val pagesNeeded: Int = max(ceil(items.size / itemsPerPage.toDouble()), 1.0) as Int

        for (i in 0 until pagesNeeded) {
            val page: OutlinePane = OutlinePane(0, 0, this.length, this.height)

            for (j in 0 until itemsPerPage) {
                val index: Int = i * itemsPerPage + j

                //Check if the loop reached the end of the list
                if (index >= items.size) {
                    break
                }

                page.addItem(items.get(index))
            }

            this.addPane(i, page)
        }
    }

    /**
     * This method creates a list of ItemStacks all with the given `material` and the display names.
     * After that it calls [.populateWithItemStacks]
     * This method also translates the color char `&` for all names.
     *
     * @param displayNames The display names for all the items
     * @param material The material to use for the [org.bukkit.inventory.ItemStack]s
     * @param plugin the plugin that will be the owner of the created items
     * @see .populateWithNames
     * @since 0.10.8
     */
    /**
     * This method creates a list of ItemStacks all with the given `material` and the display names.
     * After that it calls [.populateWithItemStacks]
     * This method also translates the color char `&` for all names.
     *
     * @param displayNames The display names for all the items
     * @param material The material to use for the [org.bukkit.inventory.ItemStack]s
     */
    @JvmOverloads
    fun populateWithNames(
        displayNames: List<String?>, material: Material?,
        plugin: Plugin = JavaPlugin.getProvidingPlugin(
            PaginatedPane::class.java
        )
    ) {
        if (material == null || material == Material.AIR) return

        populateWithItemStacks(displayNames.stream().map { name: String? ->
            val itemStack: ItemStack = ItemStack(material)
            val itemMeta: ItemMeta? = itemStack.getItemMeta()
            itemMeta!!.setDisplayName(ChatColor.translateAlternateColorCodes('&', name!!))
            itemStack.setItemMeta(itemMeta)
            itemStack
        }.collect(Collectors.toList()), plugin)
    }

    override fun display(
        inventoryComponent: InventoryComponent, paneOffsetX: Int, paneOffsetY: Int, maxLength: Int,
        maxHeight: Int
    ) {
        val panes: List<Pane>? = panes.get(page)

        if (panes == null) {
            return
        }

        for (pane: Pane in panes) {
            if (!pane.isVisible()) {
                continue
            }

            val slot: Slot = getSlot()

            val newPaneOffsetX: Int = paneOffsetX + slot.getX(maxLength)
            val newPaneOffsetY: Int = paneOffsetY + slot.getY(maxLength)
            val newMaxLength: Int = min(length.toDouble(), maxLength.toDouble()).toInt()
            val newMaxHeight: Int = min(height.toDouble(), maxHeight.toDouble()).toInt()

            pane.display(
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

        callOnClick(event)

        var success: Boolean = false

        for (pane: Pane in ArrayList(panes.getOrDefault(page, emptyList()))) {
            if (!pane.isVisible()) {
                continue
            }

            success = success || pane.click(
                gui, inventoryComponent, event, slot, paneOffsetX + xPosition,
                paneOffsetY + yPosition, length, height
            )
        }

        return success
    }

    @Contract(pure = true)
    override fun copy(): PaginatedPane {
        val paginatedPane: PaginatedPane = PaginatedPane(getSlot(), length, height, getPriority())

        for (entry: Map.Entry<Int, List<Pane>?> in panes.entries) {
            for (pane: Pane in entry.value!!) {
                paginatedPane.addPane(entry.key, pane.copy())
            }
        }

        paginatedPane.setVisible(isVisible())
        paginatedPane.onClick = onClick

        paginatedPane.uuid = uuid

        paginatedPane.page = page

        return paginatedPane
    }

    /**
     * Deletes a page and all its associated panes from this paginated pane. It also decrements the indexes of all pages
     * beyond the specified page by one. For example, given a sequence of pages 0, 1, 2, 3, 4, upon removing page 2, the
     * new sequence of pages will be 0, 1, 2, 3. If the specified page does not exist, then this method will silently do
     * nothing.
     *
     * @param page the page to delete
     * @since 0.10.5
     */
    fun deletePage(page: Int) {
        if (panes.remove(page) == null) {
            return
        }

        val newPanes: MutableMap<Int, MutableList<Pane>?> = HashMap()

        for (entry: Map.Entry<Int, MutableList<Pane>?> in panes.entries) {
            val index: Int = entry.key
            val panes: MutableList<Pane>? = entry.value

            if (index > page) {
                newPanes.put(index - 1, panes)
            } else {
                newPanes.put(index, panes)
            }
        }

        this.panes = newPanes
    }

    @Contract(pure = true)
    override fun getPanes(): Collection<Pane> {
        val panes: MutableCollection<Pane> = HashSet()

        this.panes.forEach { (integer: Int?, p: MutableList<Pane?>?) ->
            p.forEach(Consumer { pane: Pane -> panes.addAll(pane.getPanes()) })
            panes.addAll(p)
        }

        return panes
    }

    /**
     * Gets all the panes from inside the specified page of this pane. If the specified page is not existent, this
     * method will throw an [IllegalArgumentException]. If the specified page is existent, but doesn't
     * have any panes, the returned collection will be empty. The returned collection is unmodifiable. The returned
     * collection is not synchronized and no guarantees should be made as to the safety of concurrently accessing the
     * returned collection. If synchronized behaviour should be allowed, the returned collection must be synchronized
     * externally.
     *
     * @param page the panes of this page will be returned
     * @return a collection of panes belonging to the specified page
     * @since 0.5.13
     * @throws IllegalArgumentException if the page does not exist
     */
    @Contract(pure = true)
    fun getPanes(page: Int): Collection<Pane> {
        val panes: Collection<Pane>? = panes.get(page)

        requireNotNull(panes) { "Invalid page" }

        return Collections.unmodifiableCollection(panes)
    }

    @get:Contract(pure = true)
    override val items: Collection<GuiItem>
        get() {
            return getPanes().stream().flatMap { pane: Pane -> pane.getItems().stream() }.collect(
                Collectors.toList()
            )
        }

    override fun clear() {
        panes.clear()
    }

    companion object {
        /**
         * Loads a paginated pane from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @param plugin the plugin that will be used to create the items
         * @return the paginated pane
         * @since 0.10.8
         */
        @JvmStatic
        fun load(instance: Any, element: Element, plugin: Plugin): PaginatedPane {
            try {
                val paginatedPane: PaginatedPane = PaginatedPane(
                    element.getAttribute("length").toInt(),
                    element.getAttribute("height").toInt()
                )

                Pane.Companion.load(paginatedPane, instance, element)

                if (element.hasAttribute("populate")) return paginatedPane

                if (element.hasAttribute("id")) {
                    element.setIdAttribute("id", true)
                    element.setUserData("pane", paginatedPane, null)
                }

                var pageCount: Int = 0

                val childNodes: NodeList = element.getChildNodes()
                for (i in 0 until childNodes.getLength()) {
                    val item: Node = childNodes.item(i)
                    if (item.getNodeType() != Node.ELEMENT_NODE) continue

                    if (item.getNodeName() != "page") throw XMLLoadException("Panes have to be inside page tag")

                    val innerNodes: NodeList = item.getChildNodes()

                    for (j in 0 until innerNodes.getLength()) {
                        val pane: Node = innerNodes.item(j)

                        if (pane.getNodeType() != Node.ELEMENT_NODE) {
                            continue
                        }

                        paginatedPane.addPane(pageCount, Gui.loadPane(instance, pane, plugin))
                    }

                    pageCount++
                }

                return paginatedPane
            } catch (exception: NumberFormatException) {
                throw XMLLoadException(exception)
            }
        }

        /**
         * Loads a paginated pane from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @return the paginated pane
         */
        @Deprecated(
            """this method is no longer used internally and has been superseded by
                  {@link #load(Object, Element, Plugin)}"""
        )
        fun load(instance: Any, element: Element): PaginatedPane {
            return load(
                instance, element, JavaPlugin.getProvidingPlugin(
                    PaginatedPane::class.java
                )
            )
        }
    }
}
