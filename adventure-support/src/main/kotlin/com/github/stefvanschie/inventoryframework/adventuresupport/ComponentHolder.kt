package com.github.stefvanschie.inventoryframework.adventuresupport

import com.google.gson.JsonElement
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.apache.commons.lang3.Validate
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract

/**
 * Wrapper of an Adventure [Component].
 *
 * @param component the Adventure component this instance should wrap
 *
 * @since 0.10.0
 */
abstract class ComponentHolder(
    @get:Contract(pure = true) val component: Component
) : TextHolder() {

    /**
     * Gets the wrapped Adventure component in a JSON representation.
     *
     * @return the contained Adventure component as JSON
     * @since 0.10.0
     */
    @Contract(pure = true)
    fun asJson(): JsonElement {
        return GsonComponentSerializer.gson().serializeToTree(
            component
        )
    }

    @Contract(pure = true)
    override fun toString(): String {
        return javaClass.simpleName + "{" + component + "}"
    }

    override fun hashCode(): Int {
        return component.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other != null && javaClass == other.javaClass && component == (other as ComponentHolder).component
    }

    @Contract(pure = true)
    override fun asLegacyString(): String {
        return legacySerializer.serialize(component)
    }

    companion object {
        /**
         * Whether the server platform natively supports Adventure.
         * A null value indicates that we don't yet know this: it hasn't been determined yet.
         * This field should not be used directly, use [.isNativeAdventureSupport] instead.
         */
        private var isNativeAdventureSupport: Boolean? = null
            /**
             * Gets whether the server platform natively supports Adventure.
             * Native Adventure support means that eg. [ItemMeta.displayName]
             * is a valid method.
             *
             * @return whether the server platform natively supports Adventure
             * @since 0.10.0
             */
            get() {
                if (field == null) {
                    try {
                        val component: Component =
                            Component.text("test")
                        val holder = NativeComponentHolder(component)


                        //If NoSuchMethodError or something is thrown we can assume that
                        //Adventure components are not natively supported by the server platform
                        holder.asInventoryTitle(null, 9)
                        holder.asInventoryTitle(null, InventoryType.HOPPER)

                        val meta = ItemStack(Material.STONE).itemMeta
                        holder.asItemDisplayName(meta)
                        holder.asItemLoreAtEnd(meta)

                        field = true
                    } catch (_: Throwable) {
                        field = false
                    }
                }
                return field
            }

        /**
         * The serializer to use when converting wrapped values to legacy strings.
         * A null value indicates that we haven't created the serializer yet.
         * This field should not be used directly, use [.getLegacySerializer] instead.
         */
        val legacySerializer =
            LegacyComponentSerializer.builder()
                .character(LegacyComponentSerializer.SECTION_CHAR)
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat().build()

        /**
         * Wraps the specified Adventure component.
         *
         * @param value the value to wrap
         * @return an instance that wraps the specified value
         * @since 0.10.0
         */
        @Contract(pure = true)
        fun of(value: Component): ComponentHolder {
            Validate.notNull(value, "value mustn't be null")
            return if (isNativeAdventureSupport!!)
                NativeComponentHolder(value)
            else
                ForeignComponentHolder(value)
        }
    }
}
