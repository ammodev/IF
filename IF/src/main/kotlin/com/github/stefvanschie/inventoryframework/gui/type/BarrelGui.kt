package com.github.stefvanschie.inventoryframework.gui.type

import com.github.stefvanschie.inventoryframework.HumanEntityCache
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.BarrelGui
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased
import com.github.stefvanschie.inventoryframework.gui.type.util.MergedGui
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil.instance
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
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
 * Represents a gui in the form of a barrel.
 *
 * @since 0.8.0
 */
class BarrelGui : NamedGui, MergedGui, InventoryBased {
    /**
     * Represents the inventory component for the entire gui
     */
    @get:Contract(pure = true)
    override var inventoryComponent: InventoryComponent = InventoryComponent(9, 7)
        private set

    /**
     * Constructs a new GUI
     *
     * @param title the title/name of this gui.
     * @since 0.8.0
     */
    constructor(title: String) : super(title)

    /**
     * Constructs a new GUI
     *
     * @param title the title/name of this gui.
     * @since 0.10.0
     */
    constructor(title: TextHolder) : super(title)

    /**
     * Constructs a new barrel gui for the given `plugin`.
     *
     * @param title  the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .BarrelGui
     * @since 0.10.8
     */
    constructor(title: String, plugin: Plugin) : super(title, plugin)

    /**
     * Constructs a new barrel gui for the given `plugin`.
     *
     * @param title  the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .BarrelGui
     * @since 0.10.8
     */
    constructor(title: TextHolder, plugin: Plugin) : super(title, plugin)

    override fun show(humanEntity: HumanEntity) {
        if (isDirty()) {
            this.inventory = createInventory()
            markChanges()
        }

        getInventory().clear()

        val height: Int = inventoryComponent.getHeight()

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

            bottomComponent.placeItems(humanEntity.getInventory(), 0)
        }

        humanEntity.openInventory(getInventory())
    }

    @Contract(pure = true)
    override fun copy(): BarrelGui {
        val gui: BarrelGui = BarrelGui(getTitleHolder(), super.plugin)

        gui.inventoryComponent = inventoryComponent.copy()

        gui.setOnTopClick(this.onTopClick)
        gui.setOnBottomClick(this.onBottomClick)
        gui.setOnGlobalClick(this.onGlobalClick)
        gui.setOnOutsideClick(this.onOutsideClick)
        gui.setOnClose(this.onClose)

        return gui
    }

    override fun getInventory(): Inventory {
        if (this.inventory == null) {
            this.inventory = createInventory()
        }

        return inventory!!
    }

    override fun click(event: InventoryClickEvent) {
        inventoryComponent.click(this, event, event.getRawSlot())
    }

    @get:Contract(pure = true)
    override val isPlayerInventoryUsed: Boolean
        get() {
            return inventoryComponent.excludeRows(0, inventoryComponent.getHeight() - 5)
                .hasItem()
        }

    override fun addPane(pane: Pane) {
        inventoryComponent.addPane(pane)
    }

    @get:Contract(pure = true)
    override val panes: List<Pane?>
        get() {
            return inventoryComponent.getPanes()
        }

    @get:Contract(pure = true)
    override val items: Collection<GuiItem>
        get() {
            return panes.stream().flatMap<Any> { pane: Pane? -> pane!!.items.stream() }
                .collect(Collectors.toSet())
        }

    @Contract(pure = true)
    override fun createInventory(): Inventory {
        return getTitleHolder().asInventoryTitle(this, InventoryType.BARREL)
    }

    @get:Contract(pure = true)
    override val viewerCount: Int
        get() {
            return getInventory().getViewers().size
        }

    @get:Contract(pure = true)
    override val viewers: List<HumanEntity>
        get() {
            return ArrayList(getInventory().getViewers())
        }

    companion object {
        /**
         * Loads a barrel gui from an XML file.
         *
         * @param instance    the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @param plugin      the plugin that will be the owner of the created gui
         * @return the loaded barrel gui
         * @see .load
         * @since 0.10.8
         */
        @Contract(pure = true)
        fun load(
            instance: Any, inputStream: InputStream,
            plugin: Plugin
        ): BarrelGui? {
            try {
                val document: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(inputStream)
                val documentElement: Element = document.getDocumentElement()

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
         * Loads a barrel gui from the specified element, applying code references to the provided
         * instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element  the element to load the gui from
         * @param plugin   the plugin that will be the owner of the created gui
         * @return the loaded barrel gui
         * @see .load
         * @since 0.10.8
         */
        /**
         * Loads a barrel gui from the specified element, applying code references to the provided
         * instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element  the element to load the gui from
         * @return the loaded barrel gui
         * @since 0.8.0
         */
        @JvmOverloads
        fun load(
            instance: Any, element: Element,
            plugin: Plugin = JavaPlugin.getProvidingPlugin(
                BarrelGui::class.java
            )
        ): BarrelGui {
            if (!element.hasAttribute("title")) {
                throw XMLLoadException(
                    "Provided XML element's gui tag doesn't have the mandatory title attribute set"
                )
            }

            val barrelGui: BarrelGui = BarrelGui(element.getAttribute("title"), plugin)
            barrelGui.initializeOrThrow(instance, element)

            if (element.hasAttribute("populate")) {
                return barrelGui
            }

            val childNodes: NodeList = element.getChildNodes()

            for (index in 0 until childNodes.getLength()) {
                val item: Node = childNodes.item(index)

                if (item.getNodeType() != Node.ELEMENT_NODE) {
                    continue
                }

                val componentElement: Element = item as Element
                val inventoryComponent: InventoryComponent = barrelGui.inventoryComponent

                if (componentElement.getTagName().equals("component", ignoreCase = true)) {
                    inventoryComponent.load(instance, componentElement, plugin)
                } else {
                    inventoryComponent.load(instance, element, plugin)
                }

                break
            }

            return barrelGui
        }

        /**
         * Loads a barrel gui from an XML file.
         *
         * @param instance    the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @return the loaded barrel gui
         * @since 0.8.0
         */
        @Contract(pure = true)
        fun load(instance: Any, inputStream: InputStream): BarrelGui? {
            return load(
                instance, inputStream, JavaPlugin.getProvidingPlugin(
                    BarrelGui::class.java
                )
            )
        }
    }
}
