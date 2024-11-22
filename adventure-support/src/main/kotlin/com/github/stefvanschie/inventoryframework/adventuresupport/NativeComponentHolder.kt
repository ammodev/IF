package com.github.stefvanschie.inventoryframework.adventuresupport

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.Merchant
import org.bukkit.inventory.meta.ItemMeta
import org.jetbrains.annotations.Contract
import java.util.*

/**
 * A [ComponentHolder] implementation for platforms where Adventure is natively supported.
 * Adventure components are directly passed to the Bukkit (Paper) API.
 *
 * @param value the Adventure component this instance should wrap
 *
 * @see ForeignComponentHolder
 *
 * @since 0.10.0
 */
internal class NativeComponentHolder(val value: Component) : ComponentHolder(value) {

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
        meta.displayName(value)
    }

    override fun asItemLoreAtEnd(meta: ItemMeta) {
        val lore = if (meta.hasLore())
            Objects.requireNonNull(meta.lore())
        else
            ArrayList()
        
        lore.add(value)
        meta.lore(lore)
    }
}
