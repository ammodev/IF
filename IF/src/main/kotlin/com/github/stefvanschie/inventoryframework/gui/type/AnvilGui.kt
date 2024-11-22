package com.github.stefvanschie.inventoryframework.gui.type

import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.gui.type.util.InventoryBased
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil.instance
import com.github.stefvanschie.inventoryframework.util.version.Version.Companion.version
import com.github.stefvanschie.inventoryframework.util.version.VersionMatcher.newAnvilInventory
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import java.util.function.Consumer
import java.util.logging.Level
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * Represents a gui in the form of an anvil
 *
 * @since 0.8.0
 */
class AnvilGui : NamedGui, InventoryBased {
    /**
     * Called whenever the name input is changed.
     */
    private var onNameInputChanged: Consumer<in String> =
        Consumer { name: String? -> }

    /**
     * Gets the inventory component representing the first item
     *
     * @return the first item component
     * @since 0.8.0
     */
    /**
     * Represents the inventory component for the first item
     */
    @get:Contract(pure = true)
    var firstItemComponent: InventoryComponent = InventoryComponent(1, 1)
        private set

    /**
     * Gets the inventory component representing the second item
     *
     * @return the second item component
     * @since 0.8.0
     */
    /**
     * Represents the inventory component for the second item
     */
    @get:Contract(pure = true)
    var secondItemComponent: InventoryComponent = InventoryComponent(1, 1)
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
     * An internal anvil inventory
     */
    private val anvilInventory = newAnvilInventory(
        version,
        this
    )

    /**
     * The viewers of this gui
     */
    override val viewers: MutableCollection<HumanEntity> = HashSet()

    /**
     * Constructs a new anvil gui
     *
     * @param title the title/name of this gui.
     * @since 0.8.0
     */
    constructor(title: String) : super(title) {
        anvilInventory.subscribeToNameInputChanges { newInput: String ->
            this.callOnRename(
                newInput
            )
        }
    }

    /**
     * Constructs a new anvil gui
     *
     * @param title the title/name of this gui.
     * @since 0.10.0
     */
    constructor(title: TextHolder) : super(title) {
        anvilInventory.subscribeToNameInputChanges { newInput: String ->
            this.callOnRename(
                newInput
            )
        }
    }

    /**
     * Constructs a new anvil gui for the given `plugin`.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .AnvilGui
     * @since 0.10.8
     */
    constructor(title: String, plugin: Plugin) : super(title, plugin) {
        anvilInventory.subscribeToNameInputChanges { newInput: String ->
            this.callOnRename(
                newInput
            )
        }
    }

    /**
     * Constructs a new anvil gui for the given `plugin`.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .AnvilGui
     * @since 0.10.8
     */
    constructor(title: TextHolder, plugin: Plugin) : super(title, plugin) {
        anvilInventory.subscribeToNameInputChanges { newInput: String ->
            this.callOnRename(
                newInput
            )
        }
    }

    override fun show(humanEntity: HumanEntity) {
        require(humanEntity is Player) { "Anvils can only be opened by players" }

        if (isDirty) {
            this.inventory = createInventory()
            markChanges()
        }

        getInventory().clear()

        firstItemComponent.display(getInventory(), 0)
        secondItemComponent.display(getInventory(), 1)
        resultComponent.display(getInventory(), 2)

        playerInventoryComponent.display()

        if (playerInventoryComponent.hasItem()) {
            val humanEntityCache = getHumanEntityCache()

            if (!humanEntityCache.contains(humanEntity)) {
                humanEntityCache.storeAndClear(humanEntity)
            }

            playerInventoryComponent.placeItems(humanEntity.getInventory(), 0)
        }

        val inventory = anvilInventory.openInventory(
            humanEntity,
            titleHolder,
            topItems
        )

        addInventory(inventory, this)

        viewers.add(humanEntity)
    }

    @Contract(pure = true)
    override fun copy(): AnvilGui {
        val gui = AnvilGui(titleHolder, super.plugin)

        gui.firstItemComponent = firstItemComponent.copy()
        gui.secondItemComponent = secondItemComponent.copy()
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
        val rawSlot = event.rawSlot

        if (rawSlot == 0) {
            firstItemComponent.click(this, event, 0)
        } else if (rawSlot == 1) {
            secondItemComponent.click(this, event, 0)
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

    /**
     * Sets the enchantment level cost for this anvil gui. Taking the item from the result slot will not actually remove
     * these levels. Having a cost specified does not impede a player's ability to take the item in the result item,
     * even if the player does not have the specified amount of levels. The cost must be a non-negative number.
     *
     * @param cost the cost
     * @since 0.10.8
     * @throws IllegalArgumentException when the cost is less than zero
     */
    fun setCost(cost: Short) {
        require(cost >= 0) { "Cost must be non-negative" }

        anvilInventory.setCost(cost)
    }

    @Contract(pure = true)
    override fun createInventory(): Inventory {
        return titleHolder.asInventoryTitle(this, InventoryType.ANVIL)
    }

    @get:Contract(pure = true)
    val renameText: String
        /**
         * Gets the rename text currently specified in the anvil.
         *
         * @return the rename text
         * @since 0.8.0
         * @see org.bukkit.inventory.AnvilInventory.getRenameText
         */
        get() = anvilInventory.renameText

    @get:Contract(pure = true)
    override val isPlayerInventoryUsed: Boolean
        get() = playerInventoryComponent.hasItem()

    @get:Contract(pure = true)
    override val viewerCount: Int
        get() = viewers.size

    @Contract(pure = true)
    override fun getViewers(): List<HumanEntity> {
        return ArrayList(this.viewers)
    }

    /**
     * Handles an incoming inventory click event
     *
     * @param event the event to handle
     * @since 0.8.0
     */
    @Deprecated("no longer used internally")
    fun handleClickEvent(event: InventoryClickEvent) {
        val slot = event.rawSlot
        val player = event.whoClicked as Player

        if (slot >= 3 && slot <= 38) {
            anvilInventory.sendItems(player, topItems)
        } else if (slot == 0 || slot == 1) {
            if (event.isCancelled) {
                if (slot == 0) {
                    anvilInventory.sendFirstItem(player, firstItemComponent.getItem(0, 0))
                } else {
                    anvilInventory.sendSecondItem(player, secondItemComponent.getItem(0, 0))
                }

                anvilInventory.clearCursor(player)
            }

            anvilInventory.sendResultItem(player, resultComponent.getItem(0, 0))
        } else if (slot == 2 && !event.isCancelled) {
            anvilInventory.clearResultItem(player)

            val resultItem = resultComponent.getItem(0, 0)

            if (resultItem != null) {
                anvilInventory.setCursor(player, resultItem)
            }
        }
    }

    /**
     * Handles a human entity closing this gui.
     *
     * @param humanEntity the human entity closing the gui
     * @since 0.10.1
     */
    fun handleClose(humanEntity: HumanEntity) {
        viewers.remove(humanEntity)
    }

    /**
     * Sets the consumer that should be called whenever the name input is changed. The argument is the new input. When
     * this consumer is invoked, the value as returned by [.getRenameText] will not have updated yet, hence
     * allowing you to see the old value via that.
     *
     * @param onNameInputChanged the consumer to call when the rename input is changed
     * @since 0.10.10
     */
    fun setOnNameInputChanged(onNameInputChanged: Consumer<in String>) {
        this.onNameInputChanged = onNameInputChanged
    }

    /**
     * Calls the consumer that was specified using [.setOnNameInputChanged], so the consumer that should
     * be called whenever the rename input is changed. Catches and logs all exceptions the consumer might throw.
     *
     * @param newInput the new rename input
     * @since 0.10.10
     */
    private fun callOnRename(newInput: String) {
        try {
            onNameInputChanged.accept(newInput)
        } catch (throwable: Throwable) {
            val message = "Exception while handling onRename, newInput='$newInput'"

            plugin.logger.log(Level.SEVERE, message, throwable)
        }
    }

    @get:Contract(pure = true)
    private val topItems: Array<ItemStack?>
        /**
         * Gets the top items
         *
         * @return the top items
         * @since 0.8.0
         */
        get() = arrayOf(
            firstItemComponent.getItem(0, 0),
            secondItemComponent.getItem(0, 0),
            resultComponent.getItem(0, 0)
        )

    companion object {
        /**
         * Loads an anvil gui from an XML file.
         *
         * @param instance the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @param plugin the plugin that will be the owner of the created gui
         * @return the loaded anvil gui
         * @see .load
         * @since 0.10.8
         */
        @Contract(pure = true)
        fun load(instance: Any, inputStream: InputStream, plugin: Plugin): AnvilGui? {
            try {
                val document =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
                val documentElement = document.documentElement

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
         * Loads an anvil gui from the specified element, applying code references to the provided instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element the element to load the gui from
         * @param plugin the plugin that will own the created gui
         * @return the loaded anvil gui
         * @see .load
         * @since 0.10.8
         */
        /**
         * Loads an anvil gui from the specified element, applying code references to the provided instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element the element to load the gui from
         * @return the loaded anvil gui
         * @since 0.8.0
         */
        @JvmOverloads
        fun load(
            instance: Any, element: Element, plugin: Plugin = JavaPlugin.getProvidingPlugin(
                AnvilGui::class.java
            )
        ): AnvilGui {
            if (!element.hasAttribute("title")) {
                throw XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set")
            }

            val anvilGui = AnvilGui(element.getAttribute("title"), plugin)
            anvilGui.initializeOrThrow(instance, element)

            if (element.hasAttribute("populate")) {
                return anvilGui
            }

            val childNodes = element.childNodes

            for (index in 0 until childNodes.length) {
                val item = childNodes.item(index)

                if (item.nodeType != Node.ELEMENT_NODE) {
                    continue
                }

                val componentElement = item as Element

                if (!componentElement.tagName.equals("component", ignoreCase = true)) {
                    throw XMLLoadException("Gui element contains non-component tags")
                }

                if (!componentElement.hasAttribute("name")) {
                    throw XMLLoadException("Component tag does not have a name specified")
                }

                var component = when (componentElement.getAttribute("name")) {
                    "first-item" -> anvilGui.firstItemComponent
                    "second-item" -> anvilGui.secondItemComponent
                    "result" -> anvilGui.resultComponent
                    "player-inventory" -> anvilGui.playerInventoryComponent
                    else -> throw XMLLoadException("Unknown component name")
                }

                component.load(instance, componentElement, plugin)
            }

            return anvilGui
        }

        /**
         * Loads an anvil gui from an XML file.
         *
         * @param instance the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @return the loaded anvil gui
         * @since 0.8.0
         */
        @Contract(pure = true)
        fun load(instance: Any, inputStream: InputStream): AnvilGui? {
            return load(
                instance, inputStream, JavaPlugin.getProvidingPlugin(
                    AnvilGui::class.java
                )
            )
        }
    }
}
