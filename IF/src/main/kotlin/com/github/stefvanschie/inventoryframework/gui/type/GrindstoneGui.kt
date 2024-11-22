package com.github.stefvanschie.inventoryframework.gui.type

import com.github.stefvanschie.inventoryframework.HumanEntityCache
import com.github.stefvanschie.inventoryframework.abstraction.GrindstoneInventory
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.GrindstoneGui
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil.instance
import com.github.stefvanschie.inventoryframework.util.version.Version.Companion.version
import com.github.stefvanschie.inventoryframework.util.version.VersionMatcher.newGrindstoneInventory
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
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
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * Represents a gui in the form of a grindstone
 *
 * @since 0.8.0
 */
class GrindstoneGui : NamedGui, InventoryBased {
    /**
     * Gets the inventory component representing the items
     *
     * @return the items component
     * @since 0.8.0
     */
    /**
     * Represents the inventory component for the items
     */
    @get:Contract(pure = true)
    var itemsComponent: InventoryComponent = InventoryComponent(1, 2)
        private set

    /**
     * Gets the inventory component representing the result
     *
     * @return the result component
     * @since 0.8.0
     */
    /**
     * Represents the inventory component for the result
     */
    @get:Contract(pure = true)
    var resultComponent: InventoryComponent = InventoryComponent(1, 1)
        private set

    /**
     * Gets the inventory component representing the player inventory
     *
     * @return the player inventory component
     * @since 0.8.0
     */
    /**
     * Represents the inventory component for the player inventory
     */
    @get:Contract(pure = true)
    var playerInventoryComponent: InventoryComponent = InventoryComponent(9, 4)
        private set

    /**
     * An internal grindstone inventory
     */
    private val grindstoneInventory: GrindstoneInventory = newGrindstoneInventory(
        version,
        this
    )

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
     * Constructs a new grindstone gui for the given `plugin`.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .GrindstoneGui
     * @since 0.10.8
     */
    constructor(title: String, plugin: Plugin) : super(title, plugin)

    /**
     * Constructs a new grindstone gui for the given `plugin`.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .GrindstoneGui
     * @since 0.10.8
     */
    constructor(title: TextHolder, plugin: Plugin) : super(title, plugin)

    override fun show(humanEntity: HumanEntity) {
        require(humanEntity is Player) { "Grindstones can only be opened by players" }

        if (isDirty()) {
            this.inventory = createInventory()
            markChanges()
        }

        getInventory().clear()

        itemsComponent.display(getInventory(), 0)
        resultComponent.display(getInventory(), 2)
        playerInventoryComponent.display()

        if (playerInventoryComponent.hasItem()) {
            val humanEntityCache: HumanEntityCache = getHumanEntityCache()

            if (!humanEntityCache.contains(humanEntity)) {
                humanEntityCache.storeAndClear(humanEntity)
            }

            playerInventoryComponent.placeItems(humanEntity.getInventory(), 0)
        }

        val inventory: Inventory = grindstoneInventory.openInventory(
            humanEntity, getTitleHolder(),
            topItems
        )

        addInventory(inventory, this)
    }

    @Contract(pure = true)
    override fun copy(): GrindstoneGui {
        val gui: GrindstoneGui = GrindstoneGui(getTitleHolder(), super.plugin)

        gui.itemsComponent = itemsComponent.copy()
        gui.resultComponent = resultComponent.copy()
        gui.playerInventoryComponent = playerInventoryComponent.copy()

        gui.setOnTopClick(this.onTopClick)
        gui.setOnBottomClick(this.onBottomClick)
        gui.setOnGlobalClick(this.onGlobalClick)
        gui.setOnOutsideClick(this.onOutsideClick)
        gui.setOnClose(this.onClose)

        return gui
    }

    override fun click(event: InventoryClickEvent) {
        val rawSlot: Int = event.getRawSlot()

        if (rawSlot >= 0 && rawSlot <= 1) {
            itemsComponent.click(this, event, rawSlot)
        } else if (rawSlot == 2) {
            resultComponent.click(this, event, 0)
        } else {
            playerInventoryComponent.click(this, event, rawSlot - 3)
        }
    }

    override fun getInventory(): Inventory {
        if (this.inventory == null) {
            this.inventory = createInventory()
        }

        return inventory!!
    }

    @get:Contract(pure = true)
    override val isPlayerInventoryUsed: Boolean
        get() {
            return playerInventoryComponent.hasItem()
        }

    @Contract(pure = true)
    override fun createInventory(): Inventory {
        return getTitleHolder().asInventoryTitle(this, InventoryType.GRINDSTONE)
    }

    /**
     * Handles an incoming inventory click event
     *
     * @param event the event to handle
     * @since 0.8.0
     */
    @Deprecated("no longer used internally")
    fun handleClickEvent(event: InventoryClickEvent) {
        val slot: Int = event.getRawSlot()
        val player: Player = event.getWhoClicked() as Player

        if (slot >= 3 && slot <= 38) {
            grindstoneInventory.sendItems(player, topItems, event.getCurrentItem())
        } else if (slot >= 0 && slot <= 2) {
            grindstoneInventory.sendItems(player, topItems, event.getCurrentItem())

            if (event.isCancelled()) {
                grindstoneInventory.clearCursor(player)
            }
        }
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

    @get:Contract(pure = true)
    private val topItems: Array<ItemStack?>
        /**
         * Gets the top items
         *
         * @return the top items
         * @since 0.8.0
         */
        get() {
            return arrayOf(
                itemsComponent.getItem(0, 0),
                itemsComponent.getItem(0, 1),
                resultComponent.getItem(0, 0)
            )
        }

    companion object {
        /**
         * Loads a grindstone gui from an XML file.
         *
         * @param instance the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @param plugin the plugin that will be the owner of the created gui
         * @return the loaded furnace gui
         * @see .load
         * @since 0.10.8
         */
        @Contract(pure = true)
        fun load(
            instance: Any, inputStream: InputStream,
            plugin: Plugin
        ): GrindstoneGui? {
            try {
                val document: Document =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
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
         * Loads a grindstone gui from the specified element, applying code references to the provided instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element the element to load the gui from
         * @param plugin the plugin that will be the owner of the created gui
         * @return the loaded grindstone gui
         * @see .load
         * @since 0.10.8
         */
        /**
         * Loads a grindstone gui from the specified element, applying code references to the provided instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element the element to load the gui from
         * @return the loaded grindstone gui
         * @since 0.8.0
         */
        @JvmOverloads
        fun load(
            instance: Any, element: Element, plugin: Plugin = JavaPlugin.getProvidingPlugin(
                GrindstoneGui::class.java
            )
        ): GrindstoneGui {
            if (!element.hasAttribute("title")) {
                throw XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set")
            }

            val grindstoneGui: GrindstoneGui = GrindstoneGui(element.getAttribute("title"), plugin)
            grindstoneGui.initializeOrThrow(instance, element)

            if (element.hasAttribute("populate")) {
                return grindstoneGui
            }

            val childNodes: NodeList = element.getChildNodes()

            for (index in 0 until childNodes.getLength()) {
                val item: Node = childNodes.item(index)

                if (item.getNodeType() != Node.ELEMENT_NODE) {
                    continue
                }

                val componentElement: Element = item as Element

                if (!componentElement.getTagName().equals("component", ignoreCase = true)) {
                    throw XMLLoadException("Gui element contains non-component tags")
                }

                if (!componentElement.hasAttribute("name")) {
                    throw XMLLoadException("Component tag does not have a name specified")
                }

                var component: InventoryComponent

                when (componentElement.getAttribute("name")) {
                    "items" -> component =
                        grindstoneGui.itemsComponent

                    "result" -> component = grindstoneGui.resultComponent
                    "player-inventory" -> component = grindstoneGui.playerInventoryComponent
                    else -> throw XMLLoadException("Unknown component name")
                }

                component.load(instance, componentElement, plugin)
            }

            return grindstoneGui
        }

        /**
         * Loads a grindstone gui from an XML file.
         *
         * @param instance the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @return the loaded furnace gui
         * @since 0.8.0
         */
        @Contract(pure = true)
        fun load(instance: Any, inputStream: InputStream): GrindstoneGui? {
            return load(
                instance, inputStream, JavaPlugin.getProvidingPlugin(
                    GrindstoneGui::class.java
                )
            )
        }
    }
}
