package com.github.stefvanschie.inventoryframework.adventuresupport

import org.bukkit.ChatColor
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.Merchant
import org.bukkit.inventory.meta.ItemMeta
import org.jetbrains.annotations.Contract

/**
 * Immutable wrapper of a text-like value.
 * Support for both Adventure and legacy strings is achieved through this class.
 * To get an instance of this class please refer to either [StringHolder.of]
 * or [ComponentHolder.of].
 * Other methods like [.empty] and [.deserialize]
 * also exist, but their use cases are very limited.
 *
 * @see StringHolder
 *
 * @see ComponentHolder
 *
 * @since 0.10.0
 */
abstract class TextHolder internal constructor() {

    @Contract(pure = true)
    abstract override fun toString(): String

    abstract override fun hashCode(): Int

    abstract override fun equals(other: Any?): Boolean

    /**
     * Converts the text wrapped by this class instance to a legacy string,
     * keeping the original formatting.
     *
     * @return the wrapped value represented as a legacy string
     * @since 0.10.0
     */
    @Contract(pure = true)
    abstract fun asLegacyString(): String

    /**
     * Creates a new inventory with the wrapped value as the inventory's title.
     *
     * @param holder the holder to use for the new inventory
     * @param type the type of inventory to create
     * @return a newly created inventory with the wrapped value as its title
     * @since 0.10.0
     */
    @Contract(pure = true)
    abstract fun asInventoryTitle(holder: InventoryHolder?, type: InventoryType): Inventory

    /**
     * Creates a new inventory with the wrapped value as the inventory's title.
     *
     * @param holder the holder to use for the new inventory
     * @param size the count of slots the inventory should have (normal size restrictions apply)
     * @return a newly created inventory with the wrapped value as its title
     * @since 0.10.0
     */
    @Contract(pure = true)
    abstract fun asInventoryTitle(holder: InventoryHolder?, size: Int): Inventory

    /**
     * Creates a new merchant with the wrapped value as the merchant's title.
     *
     * @return a newly created inventory with the wrapped value as its title
     * @since 0.10.0
     */
    @Contract(pure = true)
    abstract fun asMerchantTitle(): Merchant

    /**
     * Modifies the specified meta: sets the display name to the wrapped value.
     *
     * @param meta the meta whose display name to set
     * @since 0.10.0
     */
    abstract fun asItemDisplayName(meta: ItemMeta)

    /**
     * Modifies the specified meta: adds the wrapped value as a new lore line at the end
     *
     * @param meta the meta whose lore to append to
     * @since 0.10.0
     */
    abstract fun asItemLoreAtEnd(meta: ItemMeta)

    companion object {
        /**
         * Gets an instance that contains no characters and no formatting.
         *
         * @return an instance without any characters or formatting
         * @since 0.10.0
         */
        @Contract(pure = true)
        fun empty(): TextHolder {
            return StringHolder.empty()
        }

        /**
         * Deserializes the specified [String] as a [TextHolder].
         * This method is still WIP and may change drastically in the future:
         *
         *  * Are we going to use MiniMessage if it's present?
         *  * Is MiniMessage going to be opt-in? If yes, how do we opt-in?
         *
         *
         * @param string the raw data to deserialize
         * @return an instance containing the text from the string
         * @since 0.10.0
         */
        @JvmStatic
        @Contract(pure = true)
        fun deserialize(string: String): TextHolder {
            return StringHolder.of(ChatColor.translateAlternateColorCodes('&', string))
        }
    }
}
