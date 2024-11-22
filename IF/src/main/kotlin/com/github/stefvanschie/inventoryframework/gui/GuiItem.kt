package com.github.stefvanschie.inventoryframework.gui

import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil.instance
import com.github.stefvanschie.inventoryframework.util.UUIDTagType
import org.bukkit.NamespacedKey
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.Plugin
import org.jetbrains.annotations.Contract
import java.util.*
import java.util.function.Consumer
import java.util.logging.Level
import java.util.logging.Logger

/**
 * An item for in an inventory
 */
class GuiItem private constructor(
    /**
     * The items shown
     */
    @get:Contract(pure = true) var item: ItemStack,
    /**
     * An action for the inventory
     */
    private var action: Consumer<InventoryClickEvent>?,
    /**
     * The logger to log errors with
     */
    private val logger: Logger,
    /**
     * The [NamespacedKey] that specifies the location of the (internal) [UUID] in [PersistentDataContainer]s.
     * The [PersistentDataType] that should be used is [UUIDTagType].
     */
    @get:Contract(pure = true) val key: NamespacedKey
) {
    /**
     * Gets the namespaced key used for this item.
     *
     * @return the namespaced key
     * @since 0.10.8
     */

    /**
     * List of item's properties
     */
    private var properties: List<Any>

    /**
     * Returns the item
     *
     * @return the item that belongs to this gui item
     */
    /**
     * Overwrites the current item with the provided item.
     *
     * @param item the item to set
     * @since 0.10.8
     */

    /**
     * Whether this item is visible or not
     */
    private var visible: Boolean = true

    /**
     * Gets the [UUID] associated with this [GuiItem]. This is for internal use only, and should not be
     * used.
     *
     * @return the [UUID] of this item
     * @since 0.5.9
     */
    /**
     * Internal UUID for keeping track of this item
     */
    @get:Contract(pure = true)
    var uUID: UUID = UUID.randomUUID()
        private set

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     * @param action the action called whenever an interaction with this item happens
     * @param plugin the owning plugin of this item
     * @see .GuiItem
     * @since 0.10.8
     */
    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     * @param action the action called whenever an interaction with this item happens
     */
    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     */
    @JvmOverloads
    constructor(item: ItemStack, action: Consumer<InventoryClickEvent?>? = Consumer? {
        event: org.bukkit.event.inventory.InventoryClickEvent? ->
    }, plugin: org.bukkit.plugin.Plugin = org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(GuiItem::
    class.java)) : this(item, action, plugin.getLogger(), org.bukkit.NamespacedKey(plugin, "IF-uuid"))

    /**
     * Creates a new gui item based on the item stack and action
     *
     * @param item the item stack
     * @param plugin the owning plugin of this item
     * @see .GuiItem
     * @since 0.10.8
     */
    constructor(item: ItemStack, plugin: Plugin) : this(
        item,
        Consumer<InventoryClickEvent?>? {
        event: org.bukkit.event.inventory.InventoryClickEvent? ->
    }, plugin)

    /**
     * Creates a new gui item based on the given item, action, logger, and key. The logger will be used for logging
     * exceptions and the key is used for identification of this item.
     *
     * @param item the item stack
     * @param action the action called whenever an interaction with this item happens
     * @param logger the logger used for logging exceptions
     * @param key the key to identify this item with
     * @since 0.10.10
     */
    init {
        this.properties = ArrayList()

        //remove this call after the removal of InventoryComponent#setItem(ItemStack, int, int)
        applyUUID()
    }

    /**
     * Makes a copy of this gui item and returns it. This makes a deep copy of the gui item. This entails that the
     * underlying item will be copied as per their [ItemStack.clone] and miscellaneous data will be copied in
     * such a way that they are identical. The returned gui item will never be reference equal to the current gui item.
     *
     * @return a copy of the gui item
     * @since 0.6.2
     */
    @Contract(pure = true)
    fun copy(): GuiItem {
        val guiItem: GuiItem = GuiItem(
            item.clone(),
            action, this.logger, this.key
        )

        guiItem.visible = visible
        guiItem.uUID = uUID
        guiItem.properties = ArrayList(properties)
        val meta: ItemMeta? = guiItem.item.getItemMeta()

        if (meta != null) {
            meta.getPersistentDataContainer().set(key, UUIDTagType.INSTANCE, guiItem.uUID)
            guiItem.item.setItemMeta(meta)
        }

        return guiItem
    }

    /**
     * Calls the handler of the [InventoryClickEvent]
     * if such a handler was specified in the constructor.
     * Catches and logs all exceptions the handler might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    fun callAction(event: InventoryClickEvent) {
        if (action == null) {
            return
        }

        try {
            action!!.accept(event)
        } catch (t: Throwable) {
            logger.log(
                Level.SEVERE, ("Exception while handling click event in inventory '"
                        + instance.getTitle(event.getView()) + "', slot=" + event.getSlot() +
                        ", item=" + item.getType()), t
            )
        }
    }

    /**
     * Sets the internal UUID of this gui item onto the underlying item. Previously set UUID will be overwritten by the
     * current UUID. If the underlying item does not have an item meta, this method will silently do nothing.
     *
     * @since 0.9.3
     */
    fun applyUUID() {
        val meta: ItemMeta? = item.getItemMeta()

        if (meta != null) {
            meta.getPersistentDataContainer().set(
                this.key, UUIDTagType.INSTANCE,
                uUID
            )
            item.setItemMeta(meta)
        }
    }

    /**
     * Sets the action to be executed when a human entity clicks on this item.
     *
     * @param action the action of this item
     * @since 0.7.1
     */
    fun setAction(action: Consumer<InventoryClickEvent>) {
        this.action = action
    }

    /**
     * Returns the list of properties
     *
     * @return the list of properties that belong to this gui item
     * @since 0.7.2
     */
    @Contract(pure = true)
    fun getProperties(): List<Any> {
        return properties
    }

    /**
     * Sets the list of properties for this gui item
     *
     * @param properties list of new properties
     * @since 0.7.2
     */
    fun setProperties(properties: List<Any>) {
        this.properties = properties
    }

    /**
     * Returns whether or not this item is visible
     *
     * @return true if this item is visible, false otherwise
     */
    fun isVisible(): Boolean {
        return visible
    }

    /**
     * Sets the visibility of this item to the new visibility
     *
     * @param visible the new visibility
     */
    fun setVisible(visible: Boolean) {
        this.visible = visible
    }
}
