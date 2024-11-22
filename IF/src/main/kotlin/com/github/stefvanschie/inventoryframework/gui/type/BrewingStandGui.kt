package com.github.stefvanschie.inventoryframework.gui.type

import com.github.stefvanschie.inventoryframework.HumanEntityCache
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.BrewingStandGui
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
 * Represents a gui in the form of a brewing stand
 *
 * @since 0.8.0
 */
class BrewingStandGui : NamedGui, InventoryBased {
    /**
     * Gets the inventory component representing the first bottle
     *
     * @return the first bottle component
     * @since 0.8.0
     */
    /**
     * Represents the inventory component for the first bottle
     */
    @get:Contract(pure = true)
    var firstBottleComponent: InventoryComponent = InventoryComponent(1, 1)
        private set

    /**
     * Gets the inventory component representing the second bottle
     *
     * @return the second bottle component
     * @since 0.8.0
     */
    /**
     * Represents the inventory component for the second bottle
     */
    @get:Contract(pure = true)
    var secondBottleComponent: InventoryComponent = InventoryComponent(1, 1)
        private set

    /**
     * Gets the inventory component representing the third bottle
     *
     * @return the third bottle component
     * @since 0.8.0
     */
    /**
     * Represents the inventory component for the third bottle
     */
    @get:Contract(pure = true)
    var thirdBottleComponent: InventoryComponent = InventoryComponent(1, 1)
        private set

    /**
     * Gets the inventory component representing the potion ingredient
     *
     * @return the potion ingredient component
     * @since 0.8.0
     */
    /**
     * Represents the inventory component for the potion ingredient
     */
    @get:Contract(pure = true)
    var potionIngredientComponent: InventoryComponent = InventoryComponent(1, 1)
        private set

    /**
     * Gets the inventory component representing the blaze powder
     *
     * @return the blaze powder component
     * @since 0.8.0
     */
    /**
     * Represents the inventory component for the blaze powder
     */
    @get:Contract(pure = true)
    var blazePowderComponent: InventoryComponent = InventoryComponent(1, 1)
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
     * Constructs a new brewing stand gui for the given `plugin`.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .BrewingStandGui
     * @since 0.10.8
     */
    constructor(title: String, plugin: Plugin) : super(title, plugin)

    /**
     * Constructs a new brewing stand gui for the given `plugin`.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .BrewingStandGui
     * @since 0.10.8
     */
    constructor(title: TextHolder, plugin: Plugin) : super(title, plugin)

    override fun show(humanEntity: HumanEntity) {
        if (isDirty()) {
            this.inventory = createInventory()
            markChanges()
        }

        getInventory().clear()

        firstBottleComponent.display(getInventory(), 0)
        secondBottleComponent.display(getInventory(), 1)
        thirdBottleComponent.display(getInventory(), 2)
        potionIngredientComponent.display(getInventory(), 3)
        blazePowderComponent.display(getInventory(), 4)
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
    override fun copy(): BrewingStandGui {
        val gui: BrewingStandGui = BrewingStandGui(getTitleHolder(), super.plugin)

        gui.firstBottleComponent = firstBottleComponent.copy()
        gui.secondBottleComponent = secondBottleComponent.copy()
        gui.thirdBottleComponent = thirdBottleComponent.copy()
        gui.potionIngredientComponent = potionIngredientComponent.copy()
        gui.blazePowderComponent = blazePowderComponent.copy()
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

        if (rawSlot == 0) {
            firstBottleComponent.click(this, event, 0)
        } else if (rawSlot == 1) {
            secondBottleComponent.click(this, event, 0)
        } else if (rawSlot == 2) {
            thirdBottleComponent.click(this, event, 0)
        } else if (rawSlot == 3) {
            potionIngredientComponent.click(this, event, 0)
        } else if (rawSlot == 4) {
            blazePowderComponent.click(this, event, 0)
        } else {
            playerInventoryComponent.click(this, event, rawSlot - 5)
        }
    }

    @get:Contract(pure = true)
    override val isPlayerInventoryUsed: Boolean
        get() {
            return playerInventoryComponent.hasItem()
        }

    override fun getInventory(): Inventory {
        if (this.inventory == null) {
            this.inventory = createInventory()
        }

        return inventory!!
    }

    @Contract(pure = true)
    override fun createInventory(): Inventory {
        val inventory: Inventory = getTitleHolder().asInventoryTitle(this, InventoryType.BREWING)

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
         * Loads a brewing stand gui from an XML file.
         *
         * @param instance the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @param plugin the plugin that will be the owner of the created gui
         * @return the loaded brewing stand gui
         * @see .load
         * @since 0.10.8
         */
        @Contract(pure = true)
        fun load(
            instance: Any, inputStream: InputStream,
            plugin: Plugin
        ): BrewingStandGui? {
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
         * Loads a brewing stand gui from the specified element, applying code references to the provided instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element the element to load the gui from
         * @param plugin the plugin that will be the owner of the created gui
         * @return the loaded brewing stand gui
         * @see .load
         * @since 0.10.8
         */
        /**
         * Loads a brewing stand gui from the specified element, applying code references to the provided instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element the element to load the gui from
         * @return the loaded brewing stand gui
         * @since 0.8.0
         */
        @JvmOverloads
        fun load(
            instance: Any, element: Element, plugin: Plugin = JavaPlugin.getProvidingPlugin(
                BrewingStandGui::class.java
            )
        ): BrewingStandGui {
            if (!element.hasAttribute("title")) {
                throw XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set")
            }

            val brewingStandGui: BrewingStandGui = BrewingStandGui(element.getAttribute("title"))
            brewingStandGui.initializeOrThrow(instance, element)

            if (element.hasAttribute("populate")) {
                return brewingStandGui
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
                    "first-bottle" -> component =
                        brewingStandGui.firstBottleComponent

                    "second-bottle" -> component = brewingStandGui.secondBottleComponent
                    "third-bottle" -> component = brewingStandGui.thirdBottleComponent
                    "potion-ingredient" -> component = brewingStandGui.potionIngredientComponent
                    "blaze-powder" -> component = brewingStandGui.blazePowderComponent
                    "player-inventory" -> component = brewingStandGui.playerInventoryComponent
                    else -> throw XMLLoadException("Unknown component name")
                }

                component.load(instance, componentElement, plugin)
            }

            return brewingStandGui
        }

        /**
         * Loads a brewing stand gui from an XML file.
         *
         * @param instance the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @return the loaded brewing stand gui
         * @since 0.8.0
         */
        @Contract(pure = true)
        fun load(instance: Any, inputStream: InputStream): BrewingStandGui? {
            return load(
                instance, inputStream, JavaPlugin.getProvidingPlugin(
                    BrewingStandGui::class.java
                )
            )
        }
    }
}
