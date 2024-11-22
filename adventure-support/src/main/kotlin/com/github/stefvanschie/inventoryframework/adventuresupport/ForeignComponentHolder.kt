package com.github.stefvanschie.inventoryframework.adventuresupport

import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.Merchant
import org.bukkit.inventory.meta.ItemMeta
import org.jetbrains.annotations.Contract

/**
 * A [ComponentHolder] implementation for platforms where Adventure isn't natively supported.
 * Adventure components are converted to legacy Strings before passed to the Bukkit API.
 *
 * @see NativeComponentHolder
 *
 * @since 0.10.0
 */
internal class ForeignComponentHolder(value: Component) : ComponentHolder(value) {
    /**
     * A [StringHolder] wrapping [.asLegacyString].
     * This class depends on [StringHolder] to reduce code duplication.
     */
    private val legacy: StringHolder = StringHolder.of(asLegacyString())

    @Contract(pure = true)
    override fun asInventoryTitle(holder: InventoryHolder?, type: InventoryType): Inventory {
        return legacy.asInventoryTitle(holder, type)
    }

    @Contract(pure = true)
    override fun asInventoryTitle(holder: InventoryHolder?, size: Int): Inventory {
        return legacy.asInventoryTitle(holder, size)
    }

    @Contract(pure = true)
    override fun asMerchantTitle(): Merchant {
        return legacy.asMerchantTitle()
    }

    override fun asItemDisplayName(meta: ItemMeta) {
        legacy.asItemDisplayName(meta)
    }

    override fun asItemLoreAtEnd(meta: ItemMeta) {
        legacy.asItemLoreAtEnd(meta)
    }
}
