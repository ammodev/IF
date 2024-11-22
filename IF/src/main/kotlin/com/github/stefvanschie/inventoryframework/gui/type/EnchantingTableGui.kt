package com.github.stefvanschie.inventoryframework.gui.type

import com.github.stefvanschie.inventoryframework.HumanEntityCache
import com.github.stefvanschie.inventoryframework.abstraction.EnchantingTableInventory
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.EnchantingTableGui
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil.instance
import com.github.stefvanschie.inventoryframework.util.version.Version.Companion.version
import com.github.stefvanschie.inventoryframework.util.version.VersionMatcher.newEnchantingTableInventory
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
 * Represents a gui in the form of an enchanting table
 *
 * @since 0.8.0
 */
class EnchantingTableGui : NamedGui, InventoryBased {
    /**
     * Gets the inventory component representing the input
     *
     * @return the input component
     * @since 0.8.0
     */
    /**
     * Represents the inventory component for the input
     */
    @get:Contract(pure = true)
    var inputComponent: InventoryComponent = InventoryComponent(2, 1)
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
     * An internal enchanting table inventory
     */
    private val enchantingTableInventory: EnchantingTableInventory = newEnchantingTableInventory(
        version, this
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
     * Constructs a new enchanting table gui for the given `plugin`.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .EnchantingTableGui
     * @since 0.10.8
     */
    constructor(title: String, plugin: Plugin) : super(title, plugin)

    /**
     * Constructs a new enchanting table gui for the given `plugin`.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .EnchantingTableGui
     * @since 0.10.8
     */
    constructor(title: TextHolder, plugin: Plugin) : super(title, plugin)

    override fun show(humanEntity: HumanEntity) {
        require(humanEntity is Player) { "Enchanting tables can only be opened by players" }

        if (isDirty()) {
            this.inventory = createInventory()
            markChanges()
        }

        getInventory().clear()

        inputComponent.display(getInventory(), 0)
        playerInventoryComponent.display()

        if (playerInventoryComponent.hasItem()) {
            val humanEntityCache: HumanEntityCache = getHumanEntityCache()

            if (!humanEntityCache.contains(humanEntity)) {
                humanEntityCache.storeAndClear(humanEntity)
            }

            playerInventoryComponent.placeItems(humanEntity.getInventory(), 0)
        }

        //also let Bukkit know that we opened an inventory
        humanEntity.openInventory(getInventory())

        enchantingTableInventory.openInventory(
            humanEntity, getTitleHolder(),
            topItems
        )
    }

    @Contract(pure = true)
    override fun copy(): EnchantingTableGui {
        val gui: EnchantingTableGui = EnchantingTableGui(getTitleHolder(), super.plugin)

        gui.inputComponent = inputComponent.copy()
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
            inputComponent.click(this, event, rawSlot)
        } else {
            playerInventoryComponent.click(this, event, rawSlot - 2)
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
        return getTitleHolder().asInventoryTitle(this, InventoryType.ENCHANTING)
    }

    /**
     * Handles an incoming inventory click event
     *
     * @param event the event to handle
     * @since 0.8.0
     */
    fun handleClickEvent(event: InventoryClickEvent) {
        val slot: Int = event.getRawSlot()
        val player: Player = event.getWhoClicked() as Player

        if (slot >= 2 && slot <= 37) {
            enchantingTableInventory.sendItems(player, topItems)
        } else if ((slot == 0 || slot == 1) && event.isCancelled()) {
            enchantingTableInventory.sendItems(player, topItems)

            enchantingTableInventory.clearCursor(player)
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
                inputComponent.getItem(0, 0),
                inputComponent.getItem(1, 0)
            )
        }

    companion object {
        /**
         * Loads an enchanting table gui from an XML file.
         *
         * @param instance the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @param plugin the plugin that will be the owner of the created gui
         * @return the loaded enchanting table gui
         * @see .load
         * @since 0.10.8
         */
        @Contract(pure = true)
        fun load(
            instance: Any, inputStream: InputStream,
            plugin: Plugin
        ): EnchantingTableGui? {
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
         * Loads an enchanting table gui from the specified element, applying code references to the provided instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element the element to load the gui from
         * @param plugin the plugin that will be the owner of the created gui
         * @return the loaded enchanting table gui
         * @see .load
         * @since 0.10.8
         */
        /**
         * Loads an enchanting table gui from the specified element, applying code references to the provided instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element the element to load the gui from
         * @return the loaded enchanting table gui
         * @since 0.8.0
         */
        @JvmOverloads
        fun load(
            instance: Any, element: Element, plugin: Plugin = JavaPlugin.getProvidingPlugin(
                EnchantingTableGui::class.java
            )
        ): EnchantingTableGui {
            if (!element.hasAttribute("title")) {
                throw XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set")
            }

            val enchantingTableGui: EnchantingTableGui =
                EnchantingTableGui(element.getAttribute("title"), plugin)
            enchantingTableGui.initializeOrThrow(instance, element)

            if (element.hasAttribute("populate")) {
                return enchantingTableGui
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
                    "input" -> component =
                        enchantingTableGui.inputComponent

                    "player-inventory" -> component = enchantingTableGui.playerInventoryComponent
                    else -> throw XMLLoadException("Unknown component name")
                }

                component.load(instance, componentElement, plugin)
            }

            return enchantingTableGui
        }

        /**
         * Loads an enchanting table gui from an XML file.
         *
         * @param instance the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @return the loaded enchanting table gui
         * @since 0.8.0
         */
        @Contract(pure = true)
        fun load(instance: Any, inputStream: InputStream): EnchantingTableGui? {
            return load(
                instance, inputStream, JavaPlugin.getProvidingPlugin(
                    EnchantingTableGui::class.java
                )
            )
        }
    }
}
