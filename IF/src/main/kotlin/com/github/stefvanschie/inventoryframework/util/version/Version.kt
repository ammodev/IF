package com.github.stefvanschie.inventoryframework.util.version

import com.github.stefvanschie.inventoryframework.exception.UnsupportedVersionException
import org.bukkit.Bukkit
import org.jetbrains.annotations.Contract
import java.util.*

/**
 * The different supported NMS versions
 *
 * @since 0.8.0
 */
enum class Version {

    /**
     * Version 1.20.0
     *
     * @since 0.10.14
     */
    V1_20_0,

    /**
     * Version 1.20.1
     *
     * @since 0.10.14
     */
    V1_20_1,

    /**
     * Version 1.20.2
     *
     * @since 0.10.12
     */
    V1_20_2,

    /**
     * Version 1.20.3 - 1.20.4
     *
     * @since 0.10.13
     */
    V1_20_3_4,

    /**
     * Version 1.20.5
     *
     * @since 0.10.14
     */
    V1_20_5,

    /**
     * Version 1.20.6
     *
     * @since 0.10.14
     */
    V1_20_6,

    /**
     * Version 1.21.0
     *
     * @since 0.10.18
     */
    V1_21_0,

    /**
     * Version 1.21.1
     *
     * @since 0.10.18
     */
    V1_21_1,

    /**
     * Version 1.21.2 - 1.21.3
     *
     * @since 0.10.18
     */
    V1_21_2_3;

    @get:Contract(pure = true)
    val isInventoryViewInterface: Boolean
        /**
         * Checks whether the [InventoryView] class is an interface on this version.
         *
         * @return true if the class is an interface, false otherwise
         * @since 0.10.16
         */
        get() = INTERFACE_INVENTORY_VIEW.contains(
            this
        )

    /**
     * Checks whether modern smithing tables exist on this version. Returns true if they do, otherwise
     * false.
     *
     * @return true if modern smithing tables are available
     * @since 0.10.10
     */
    fun existsSmithingTable(): Boolean {
        return SMITHING_TABLE_VERSIONS.contains(
            this
        )
    }

    companion object {
        /**
         * A collection of versions on which modern smithing tables are available.
         */
        private val SMITHING_TABLE_VERSIONS: Collection<Version> = EnumSet.of(
            V1_20_0, V1_20_1, V1_20_2, V1_20_3_4, V1_20_5, V1_20_6,
            V1_21_0, V1_21_1, V1_21_2_3
        )

        /**
         * A collection of versions on which [InventoryView] is an interface.
         */
        private val INTERFACE_INVENTORY_VIEW: Collection<Version> = EnumSet.of(
            V1_21_0, V1_21_1, V1_21_2_3
        )

        @JvmStatic
        @get:Contract(pure = true)
        val version: Version
            /**
             * Gets the version currently being used. If the used version is not supported, an
             * [UnsupportedVersionException] will be thrown.
             *
             * @return the version of the current instance
             * @since 0.8.0
             */
            get() {
                val version =
                    Bukkit.getBukkitVersion().split("-".toRegex())
                        .dropLastWhile { it.isEmpty() }.toTypedArray()[0]

                return when (version) {
                    "1.20" -> V1_20_0
                    "1.20.1" -> V1_20_1
                    "1.20.2" -> V1_20_2
                    "1.20.3", "1.20.4" -> V1_20_3_4
                    "1.20.5" -> V1_20_5
                    "1.20.6" -> V1_20_6
                    "1.21" -> V1_21_0
                    "1.21.1" -> V1_21_1
                    "1.21.2", "1.21.3" -> V1_21_2_3
                    else -> throw UnsupportedVersionException(
                        "The server version provided is not supported"
                    )
                }
            }
    }
}
