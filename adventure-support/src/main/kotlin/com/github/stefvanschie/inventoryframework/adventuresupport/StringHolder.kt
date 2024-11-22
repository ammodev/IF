package com.github.stefvanschie.inventoryframework.adventuresupport

import org.apache.commons.lang3.Validate
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.Merchant
import org.bukkit.inventory.meta.ItemMeta
import org.jetbrains.annotations.Contract
import java.util.*

/**
 * Wrapper of a legacy string value.
 * [org.bukkit.ChatColor] based formatting is used.
 *
 * @param value the legacy string this instance should wrap
 *
 * @since 0.10.0
 */
class StringHolder(private val value: String) : TextHolder() {
    
    @Contract(pure = true)
    override fun toString(): String {
        return javaClass.simpleName + "{" + value + "}"
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other != null && javaClass == other.javaClass && value == (other as StringHolder).value
    }

    @Contract(pure = true)
    override fun asLegacyString(): String {
        return value
    }

    @Contract(pure = true)
    override fun asInventoryTitle(holder: InventoryHolder?, type: InventoryType): Inventory {
        return Bukkit.createInventory(holder, type, value)
    }

    @Contract(pure = true)
    override fun asInventoryTitle(holder: InventoryHolder?, size: Int): Inventory {
        return Bukkit.createInventory(holder, size, value)
    }

    @Contract(pure = true)
    override fun asMerchantTitle(): Merchant {
        return Bukkit.createMerchant(value)
    }

    override fun asItemDisplayName(meta: ItemMeta) {
        meta.setDisplayName(value)
    }

    override fun asItemLoreAtEnd(meta: ItemMeta) {
        val lore = if (meta.hasLore())
            Objects.requireNonNull(meta.lore)
        else
            ArrayList()
        lore.add(value)
        meta.lore = lore
    }

    companion object {
        /**
         * Cached instance which wraps an empty [String].
         */
        private val EMPTY = of("")

        /**
         * Wraps the specified legacy string.
         *
         * @param value the value to wrap
         * @return an instance that wraps the specified value
         * @since 0.10.0
         */
        @JvmStatic
        @Contract(pure = true)
        fun of(value: String): StringHolder {
            Validate.notNull(value, "value mustn't be null")
            return StringHolder(value)
        }

        /**
         * Gets an instance that contains no characters.
         *
         * @return an instance without any characters
         * @since 0.10.0
         */
        @Contract(pure = true)
        fun empty(): StringHolder {
            return EMPTY
        }
    }
}
