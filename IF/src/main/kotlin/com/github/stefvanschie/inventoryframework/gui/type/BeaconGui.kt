package com.github.stefvanschie.inventoryframework.gui.type

import com.github.stefvanschie.inventoryframework.HumanEntityCache
import com.github.stefvanschie.inventoryframework.abstraction.BeaconInventory
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil.instance
import com.github.stefvanschie.inventoryframework.util.version.Version.Companion.version
import com.github.stefvanschie.inventoryframework.util.version.VersionMatcher.newBeaconInventory
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
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
 * Represents a gui in the form of a beacon
 *
 * @since 0.8.0
 */
class BeaconGui
/**
 * Constructs a new beacon gui for the given `plugin`.
 *
 * @param plugin the owning plugin of this gui
 * @see .BeaconGui
 * @since 0.10.8
 */
/**
 * Constructs a new beacon gui.
 *
 * @since 0.8.0
 */
@JvmOverloads constructor(
    plugin: Plugin = JavaPlugin.getProvidingPlugin(
        BeaconGui::class.java
    )
) : Gui(plugin), InventoryBased {
    /**
     * Gets the inventory component representing the payment item
     *
     * @return the payment item component
     * @since 0.8.0
     */
    /**
     * Represents the payment item inventory component
     */
    @get:Contract(pure = true)
    var paymentItemComponent: InventoryComponent = InventoryComponent(1, 1)
        private set

    /**
     * Gets the inventory component representing the player inventory
     *
     * @return the player inventory component
     * @since 0.8.0
     */
    /**
     * Represents the player inventory component
     */
    @get:Contract(pure = true)
    var playerInventoryComponent: InventoryComponent = InventoryComponent(9, 4)
        private set

    /**
     * An internal beacon inventory
     */
    private val beaconInventory: BeaconInventory = newBeaconInventory(
        version,
        this
    )

    override fun show(humanEntity: HumanEntity) {
        require(humanEntity is Player) { "Beacons can only be opened by players" }

        getInventory().clear()

        paymentItemComponent.display(getInventory(), 0)
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

        beaconInventory.openInventory(humanEntity, paymentItemComponent.getItem(0, 0))
    }

    @Contract(pure = true)
    override fun copy(): BeaconGui {
        val gui: BeaconGui = BeaconGui(super.plugin)

        gui.paymentItemComponent = paymentItemComponent.copy()
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
            paymentItemComponent.click(this, event, 0)
        } else {
            playerInventoryComponent.click(this, event, rawSlot - 1)
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
        return Bukkit.createInventory(this, InventoryType.BEACON)
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

    /**
     * Handles an incoming inventory click event
     *
     * @param event the event to handle
     * @since 0.8.0
     */
    fun handleClickEvent(event: InventoryClickEvent) {
        val slot: Int = event.getRawSlot()
        val player: Player = event.getWhoClicked() as Player

        if (slot >= 1 && slot <= 36) {
            beaconInventory.sendItem(player, paymentItemComponent.getItem(0, 0))
        } else if (slot == 0 && event.isCancelled()) {
            beaconInventory.sendItem(player, paymentItemComponent.getItem(0, 0))

            beaconInventory.clearCursor(player)
        }
    }

    companion object {
        /**
         * Loads a beacon gui from an XML file.
         *
         * @param instance the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @param plugin the plugin that will be the owner of the created gui
         * @return the loaded beacon gui
         * @see .load
         * @since 0.10.8
         */
        @Contract(pure = true)
        fun load(instance: Any, inputStream: InputStream, plugin: Plugin): BeaconGui? {
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
         * Loads a beacon gui from the specified element, applying code references to the provided instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element the element to load the gui from
         * @param plugin the plugin that will be the owner of the created gui
         * @return the loaded beacon gui
         * @see .load
         * @since 0.10.8
         */
        /**
         * Loads a beacon gui from the specified element, applying code references to the provided instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element the element to load the gui from
         * @return the loaded beacon gui
         * @since 0.8.0
         */
        @JvmOverloads
        fun load(
            instance: Any, element: Element, plugin: Plugin = JavaPlugin.getProvidingPlugin(
                BeaconGui::class.java
            )
        ): BeaconGui {
            if (!element.hasAttribute("title")) {
                throw XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set")
            }

            val beaconGui: BeaconGui = BeaconGui(plugin)
            beaconGui.initializeOrThrow(instance, element)

            if (element.hasAttribute("populate")) {
                return beaconGui
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
                    "payment-item" -> component =
                        beaconGui.paymentItemComponent

                    "player-inventory" -> component = beaconGui.playerInventoryComponent
                    else -> throw XMLLoadException("Unknown component name")
                }

                component.load(instance, componentElement, plugin)
            }

            return beaconGui
        }

        /**
         * Loads a beacon gui from an XML file.
         *
         * @param instance the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @return the loaded beacon gui
         * @since 0.8.0
         */
        @Contract(pure = true)
        fun load(instance: Any, inputStream: InputStream): BeaconGui? {
            return load(
                instance, inputStream, JavaPlugin.getProvidingPlugin(
                    BeaconGui::class.java
                )
            )
        }
    }
}
