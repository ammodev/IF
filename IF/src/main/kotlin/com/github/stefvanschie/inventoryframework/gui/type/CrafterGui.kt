package com.github.stefvanschie.inventoryframework.gui.type

import com.github.stefvanschie.inventoryframework.HumanEntityCache
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.CrafterGui
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui
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
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * Represents a gui in the form of a crafter.
 *
 * @since 0.10.13
 */
class CrafterGui : NamedGui, InventoryBased {
    /**
     * Gets the inventory component representing the input.
     *
     * @return the input component
     * @since 0.10.13
     */
    /**
     * Represents the inventory component for the input
     */
    @get:Contract(pure = true)
    var inputComponent: InventoryComponent = InventoryComponent(3, 3)
        private set

    /**
     * Gets the inventory component representing the player inventory.
     *
     * @return the player inventory component
     * @since 0.10.13
     */
    /**
     * Represents the inventory component for the player inventory
     */
    @get:Contract(pure = true)
    var playerInventoryComponent: InventoryComponent = InventoryComponent(9, 4)
        private set

    /**
     * Constructs a new crafter gui.
     *
     * @param title the title/name of this gui
     * @since 0.10.13
     */
    constructor(title: String) : super(title)

    /**
     * Constructs a new crafter gui.
     *
     * @param title the title/name of this gui
     * @since 0.10.13
     */
    constructor(title: TextHolder) : super(title)

    /**
     * Constructs a new crafter gui for the given `plugin`.
     *
     * @param title the title/name of this gui
     * @param plugin the owning plugin of this gui
     * @see .CrafterGui
     * @since 0.10.13
     */
    constructor(title: String, plugin: Plugin) : super(title, plugin)

    /**
     * Constructs a new crafter gui for the given `plugin`.
     *
     * @param title the title/name of this gui
     * @param plugin the owning plugin of this gui
     * @see .CrafterGui
     * @since 0.10.13
     */
    constructor(title: TextHolder, plugin: Plugin) : super(title, plugin)

    override fun show(humanEntity: HumanEntity) {
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

        humanEntity.openInventory(getInventory())
    }

    @Contract(pure = true)
    override fun copy(): CrafterGui {
        val gui: CrafterGui = CrafterGui(getTitleHolder(), super.plugin)

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

        if (rawSlot >= 0 && rawSlot <= 8) {
            inputComponent.click(this, event, rawSlot)
        } else {
            playerInventoryComponent.click(this, event, rawSlot - 9)
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
        val inventory: Inventory = getTitleHolder().asInventoryTitle(this, InventoryType.CRAFTER)

        addInventory(inventory, this)

        return inventory
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
         * Loads a crafter gui from an XML file.
         *
         * @param instance the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @param plugin the plugin that will be the owner of the created gui
         * @return the loaded crafter gui
         * @see .load
         * @since 0.10.13
         */
        @Contract(pure = true)
        fun load(
            instance: Any, inputStream: InputStream,
            plugin: Plugin
        ): CrafterGui? {
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
         * Loads a crafter gui from the specified element, applying code references to the provided instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element the element to load the gui from
         * @param plugin the plugin that will be the owner of the created gui
         * @return the loaded crafter gui
         * @since 0.10.13
         */
        /**
         * Loads a crafter gui from the specified element, applying code references to the provided instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element the element to load the gui from
         * @return the loaded crafting table gui
         * @since 0.10.13
         */
        @JvmOverloads
        fun load(
            instance: Any, element: Element, plugin: Plugin = JavaPlugin.getProvidingPlugin(
                CrafterGui::class.java
            )
        ): CrafterGui {
            if (!element.hasAttribute("title")) {
                throw XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set")
            }

            val crafterGui: CrafterGui = CrafterGui(element.getAttribute("title"), plugin)
            crafterGui.initializeOrThrow(instance, element)

            if (element.hasAttribute("populate")) {
                return crafterGui
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
                        crafterGui.inputComponent

                    "player-inventory" -> component = crafterGui.playerInventoryComponent
                    else -> throw XMLLoadException("Unknown component name")
                }

                component.load(instance, componentElement, plugin)
            }

            return crafterGui
        }

        /**
         * Loads a crafter gui from an XML file.
         *
         * @param instance the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @return the loaded crafter gui
         * @since 0.10.13
         */
        @Contract(pure = true)
        fun load(instance: Any, inputStream: InputStream): CrafterGui? {
            return load(
                instance, inputStream, JavaPlugin.getProvidingPlugin(
                    CrafterGui::class.java
                )
            )
        }
    }
}
