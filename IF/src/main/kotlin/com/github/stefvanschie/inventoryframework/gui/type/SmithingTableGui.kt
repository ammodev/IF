package com.github.stefvanschie.inventoryframework.gui.type

import com.github.stefvanschie.inventoryframework.HumanEntityCache
import com.github.stefvanschie.inventoryframework.abstraction.SmithingTableInventory
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.SmithingTableGui
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil.instance
import com.github.stefvanschie.inventoryframework.util.version.Version.Companion.version
import com.github.stefvanschie.inventoryframework.util.version.VersionMatcher.newSmithingTableInventory
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
 * Represents a gui in the form of a smithing table. This is the modern variant with three input
 * slots, available in from Minecraft 1.19.4.
 *
 * @since 0.10.9
 */
class SmithingTableGui : NamedGui, InventoryBased {
    /**
     * Gets the inventory component representing the input items
     *
     * @return the input component
     * @since 0.10.9
     */
    /**
     * Represents the inventory component for the input
     */
    @get:Contract(pure = true)
    var inputComponent: InventoryComponent = InventoryComponent(3, 1)
        private set

    /**
     * Gets the inventory component representing the result
     *
     * @return the result component
     * @since 0.10.9
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
     * @since 0.10.9
     */
    /**
     * Represents the inventory component for the player inventory
     */
    @get:Contract(pure = true)
    var playerInventoryComponent: InventoryComponent = InventoryComponent(9, 4)
        private set

    /**
     * An internal smithing inventory
     */
    private val smithingTableInventory: SmithingTableInventory = newSmithingTableInventory(
        version, this
    )

    /**
     * The viewers of this gui
     */
    override val viewers: MutableCollection<HumanEntity> = HashSet()

    /**
     * Constructs a new GUI.
     *
     * @param title the title/name of this gui.
     * @since 0.10.9
     */
    constructor(title: String) : super(title)

    /**
     * Constructs a new GUI.
     *
     * @param title the title/name of this gui.
     * @since 0.10.9
     */
    constructor(title: TextHolder) : super(title)

    /**
     * Constructs a new smithing table gui for the given `plugin`.
     *
     * @param title  the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .SmithingTableGui
     * @since 0.10.9
     */
    constructor(title: String, plugin: Plugin) : super(title, plugin)

    /**
     * Constructs a new smithing table gui for the given `plugin`.
     *
     * @param title  the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .SmithingTableGui
     * @since 0.10.9
     */
    constructor(title: TextHolder, plugin: Plugin) : super(title, plugin)

    override fun show(humanEntity: HumanEntity) {
        require(humanEntity is Player) { "Smithing tables can only be opened by players" }

        if (isDirty()) {
            this.inventory = createInventory()
            markChanges()
        }

        getInventory().clear()

        inputComponent.display(getInventory(), 0)
        resultComponent.display(getInventory(), 3)
        playerInventoryComponent.display()

        if (playerInventoryComponent.hasItem()) {
            val humanEntityCache: HumanEntityCache = getHumanEntityCache()

            if (!humanEntityCache.contains(humanEntity)) {
                humanEntityCache.storeAndClear(humanEntity)
            }

            playerInventoryComponent.placeItems(humanEntity.getInventory(), 0)
        }

        val inventory: Inventory? = smithingTableInventory.openInventory(
            humanEntity,
            getTitleHolder(),
            topItems
        )

        addInventory(inventory!!, this)

        viewers.add(humanEntity)
    }

    @Contract(pure = true)
    override fun copy(): SmithingTableGui {
        val gui: SmithingTableGui = SmithingTableGui(getTitleHolder(), super.plugin)

        gui.inputComponent = inputComponent.copy()
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

        if (rawSlot >= 0 && rawSlot <= 2) {
            inputComponent.click(this, event, rawSlot)
        } else if (rawSlot == 3) {
            resultComponent.click(this, event, 0)
        } else {
            playerInventoryComponent.click(this, event, rawSlot - 4)
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
        return getTitleHolder().asInventoryTitle(this, InventoryType.SMITHING_NEW)
    }

    /**
     * Handles a human entity closing this gui.
     *
     * @param humanEntity the human entity closing the gui
     * @since 0.10.9
     */
    fun handleClose(humanEntity: HumanEntity) {
        viewers.remove(humanEntity)
    }

    @get:Contract(pure = true)
    override val viewerCount: Int
        get() {
            return viewers.size
        }

    @Contract(pure = true)
    override fun getViewers(): List<HumanEntity> {
        return ArrayList(this.viewers)
    }

    @get:Contract(pure = true)
    private val topItems: Array<ItemStack?>
        /**
         * Gets the top items
         *
         * @return the top items
         * @since 0.10.9
         */
        get() {
            return arrayOf(
                inputComponent.getItem(0, 0),
                inputComponent.getItem(1, 0),
                inputComponent.getItem(2, 0),
                resultComponent.getItem(0, 0)
            )
        }

    companion object {
        /**
         * Loads a smithing table gui from an XML file.
         *
         * @param instance    the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @param plugin      the plugin that will be the owner of the created gui
         * @return the loaded smithing table gui
         * @see .load
         * @since 0.10.9
         */
        @Contract(pure = true)
        fun load(
            instance: Any, inputStream: InputStream,
            plugin: Plugin
        ): SmithingTableGui? {
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
         * Loads a smithing table gui from the specified element, applying code references to the provided
         * instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element  the element to load the gui from
         * @param plugin   the plugin that will be the owner of the created gui
         * @return the loaded smithing table gui
         * @since 0.10.9
         */
        /**
         * Loads a smithing table gui from the specified element, applying code references to the provided
         * instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element  the element to load the gui from
         * @return the loaded smithing table gui
         * @since 0.10.9
         */
        @JvmOverloads
        fun load(
            instance: Any, element: Element,
            plugin: Plugin = JavaPlugin.getProvidingPlugin(
                SmithingTableGui::class.java
            )
        ): SmithingTableGui {
            if (!element.hasAttribute("title")) {
                throw XMLLoadException(
                    "Provided XML element's gui tag doesn't have the mandatory title attribute set"
                )
            }

            val smithingTableGui: SmithingTableGui =
                SmithingTableGui(element.getAttribute("title"), plugin)
            smithingTableGui.initializeOrThrow(instance, element)

            if (element.hasAttribute("populate")) {
                return smithingTableGui
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
                        smithingTableGui.inputComponent

                    "result" -> component = smithingTableGui.resultComponent
                    "player-inventory" -> component = smithingTableGui.playerInventoryComponent
                    else -> throw XMLLoadException("Unknown component name")
                }

                component.load(instance, componentElement, plugin)
            }

            return smithingTableGui
        }

        /**
         * Loads a smithing table gui from an XML file.
         *
         * @param instance    the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @return the loaded smithing table gui
         * @since 0.10.9
         */
        @Contract(pure = true)
        fun load(instance: Any, inputStream: InputStream): SmithingTableGui? {
            return load(
                instance, inputStream, JavaPlugin.getProvidingPlugin(
                    SmithingTableGui::class.java
                )
            )
        }
    }
}
