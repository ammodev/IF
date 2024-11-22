package com.github.stefvanschie.inventoryframework.gui.type

import com.github.stefvanschie.inventoryframework.HumanEntityCache
import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder.Companion.of
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased
import com.github.stefvanschie.inventoryframework.gui.type.util.MergedGui
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil.instance
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import java.util.stream.Collectors
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * Represents a gui in the form of a chest. Unlike traditional chests, this may take on any amount
 * of rows between 1 and 6.
 *
 * @since 0.8.0
 */
class ChestGui @JvmOverloads constructor(
    rows: Int, title: TextHolder, plugin: Plugin = JavaPlugin.getProvidingPlugin(
        ChestGui::class.java
    )
) : NamedGui(title, plugin), MergedGui, InventoryBased {
    /**
     * Represents the inventory component for the entire gui
     */
    @get:Contract(pure = true)
    override var inventoryComponent: InventoryComponent
        private set

    /**
     * Whether the amount of rows are dirty i.e. has been changed
     */
    private var dirtyRows: Boolean = false

    /**
     * Constructs a new chest GUI
     *
     * @param rows  the amount of rows this gui should contain, in range 1..6.
     * @param title the title/name of this gui.
     * @since 0.8.0
     */
    constructor(rows: Int, title: String) : this(
        rows, of(title), JavaPlugin.getProvidingPlugin(
            ChestGui::class.java
        )
    )

    /**
     * Constructs a new chest gui for the given `plugin`.
     *
     * @param rows   the amount of rows this gui should contain, in range 1..6.
     * @param title  the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .ChestGui
     * @since 0.10.8
     */
    constructor(rows: Int, title: String, plugin: Plugin) : this(rows, of(title), plugin)

    /**
     * Constructs a new chest gui for the given `plugin`.
     *
     * @param rows   the amount of rows this gui should contain, in range 1..6.
     * @param title  the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .ChestGui
     * @since 0.10.8
     */
    /**
     * Constructs a new chest GUI
     *
     * @param rows  the amount of rows this gui should contain, in range 1..6.
     * @param title the title/name of this gui.
     * @since 0.10.0
     */
    init {
        require(rows >= 1 && rows <= 6) { "Rows should be between 1 and 6" }

        this.inventoryComponent = InventoryComponent(9, rows + 4)
    }

    override fun show(humanEntity: HumanEntity) {
        if (isDirty || dirtyRows) {
            this.inventory = createInventory()
            this.dirtyRows = false

            markChanges()
        }

        getInventory().clear()

        val height: Int = inventoryComponent.height

        inventoryComponent.display()

        val topComponent: InventoryComponent =
            inventoryComponent.excludeRows(height - 4, height - 1)
        val bottomComponent: InventoryComponent = inventoryComponent.excludeRows(0, height - 5)

        topComponent.placeItems(getInventory(), 0)

        if (bottomComponent.hasItem()) {
            val humanEntityCache: HumanEntityCache = getHumanEntityCache()

            if (!humanEntityCache.contains(humanEntity)) {
                humanEntityCache.storeAndClear(humanEntity)
            }

            bottomComponent.placeItems(humanEntity.inventory, 0)
        }

        humanEntity.openInventory(getInventory())
    }

    @Contract(pure = true)
    override fun copy(): ChestGui {
        val gui: ChestGui = ChestGui(rows, titleHolder, super.plugin)

        gui.inventoryComponent = inventoryComponent.copy()

        gui.setOnTopClick(this.onTopClick)
        gui.setOnBottomClick(this.onBottomClick)
        gui.setOnGlobalClick(this.onGlobalClick)
        gui.setOnOutsideClick(this.onOutsideClick)
        gui.setOnClose(this.onClose)

        return gui
    }

    override fun click(event: InventoryClickEvent) {
        inventoryComponent.click(this, event, event.rawSlot)
    }

    @get:Contract(pure = true)
    override val isPlayerInventoryUsed: Boolean
        get() = inventoryComponent.excludeRows(0, inventoryComponent.height - 5)
            .hasItem()

    override fun getInventory(): Inventory {
        if (this.inventory == null) {
            this.inventory = createInventory()
        }

        return inventory!!
    }

    override fun addPane(pane: Pane) {
        inventoryComponent.addPane(pane)
    }

    @get:Contract(pure = true)
    override val panes: List<Pane?>
        get() {
            return inventoryComponent.panes
        }

    @get:Contract(pure = true)
    override val items: Collection<GuiItem>
        get() {
            return panes.stream().flatMap<Any> { pane: Pane? -> pane!!.items.stream() }
                .collect(Collectors.toSet())
        }

    @Contract(pure = true)
    override fun createInventory(): Inventory {
        return titleHolder.asInventoryTitle(this, rows * 9)
    }

    @get:Contract(pure = true)
    var rows: Int
        /**
         * Returns the amount of rows this gui currently has
         *
         * @return the amount of rows
         * @since 0.8.0
         */
        get() {
            return inventoryComponent.height - 4
        }
        /**
         * Sets the amount of rows for this inventory. This will (unlike most other methods) directly
         * update itself in order to ensure all viewers will still be viewing the new inventory as well.
         *
         * @param rows the amount of rows in range 1..6.
         * @since 0.8.0
         */
        set(rows) {
            require(rows >= 1 && rows <= 6) { "Rows should be between 1 and 6" }

            val inventoryComponent: InventoryComponent = InventoryComponent(9, rows + 4)

            for (pane: Pane in this.inventoryComponent.panes) {
                inventoryComponent.addPane(pane)
            }

            this.inventoryComponent = inventoryComponent
            this.dirtyRows = true
        }

    @get:Contract(pure = true)
    override val viewerCount: Int
        get() {
            return getInventory().viewers.size
        }

    @get:Contract(pure = true)
    override val viewers: List<HumanEntity>
        get() {
            return ArrayList(getInventory().viewers)
        }

    companion object {
        /**
         * Loads a chest gui from an XML file.
         *
         * @param instance    the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @param plugin      the plugin that will be the owner of the created gui
         * @return the loaded chest gui
         * @see .load
         * @since 0.10.8
         */
        @Contract(pure = true)
        fun load(
            instance: Any, inputStream: InputStream,
            plugin: Plugin
        ): ChestGui? {
            try {
                val document: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(inputStream)
                val documentElement: Element = document.documentElement

                documentElement.normalize()

                return load(instance, documentElement, plugin)
            } catch (e: SAXException) {
                e.printStackTrace()
                return null
            } catch (e: ParserConfigurationException) {
                e.printStackTrace()
                return null
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        /**
         * Loads a chest gui from the specified element, applying code references to the provided
         * instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element  the element to load the gui from
         * @param plugin   the plugin that will be the owner of the created gui
         * @return the loaded chest gui
         * @see .load
         * @since 0.10.8
         */
        /**
         * Loads a chest gui from the specified element, applying code references to the provided
         * instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element  the element to load the gui from
         * @return the loaded chest gui
         * @since 0.8.0
         */
        @JvmOverloads
        fun load(
            instance: Any, element: Element,
            plugin: Plugin = JavaPlugin.getProvidingPlugin(
                ChestGui::class.java
            )
        ): ChestGui {
            if (!element.hasAttribute("title")) {
                throw XMLLoadException(
                    "Provided XML element's gui tag doesn't have the mandatory title attribute set"
                )
            }

            if (!element.hasAttribute("rows")) {
                throw XMLLoadException(
                    "Provided XML element's gui tag doesn't have the mandatory rows attribute set"
                )
            }

            val rows: Int

            try {
                rows = element.getAttribute("rows").toInt()
            } catch (exception: NumberFormatException) {
                throw XMLLoadException("Rows attribute is not an integer", exception)
            }

            val chestGui: ChestGui = ChestGui(rows, element.getAttribute("title"), plugin)
            chestGui.initializeOrThrow(instance, element)

            if (element.hasAttribute("populate")) {
                return chestGui
            }

            val childNodes: NodeList = element.childNodes

            for (index in 0 until childNodes.length) {
                val item: Node = childNodes.item(index)

                if (item.nodeType != Node.ELEMENT_NODE) {
                    continue
                }

                val componentElement: Element = item as Element
                val inventoryComponent: InventoryComponent = chestGui.inventoryComponent

                if (componentElement.tagName.equals("component", ignoreCase = true)) {
                    inventoryComponent.load(instance, componentElement, plugin)
                } else {
                    inventoryComponent.load(instance, element, plugin)
                }

                break
            }

            return chestGui
        }

        /**
         * Loads a chest gui from an XML file.
         *
         * @param instance    the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @return the loaded chest gui
         * @since 0.8.0
         */
        @Contract(pure = true)
        fun load(instance: Any, inputStream: InputStream): ChestGui? {
            return load(
                instance, inputStream, JavaPlugin.getProvidingPlugin(
                    ChestGui::class.java
                )
            )
        }
    }
}
