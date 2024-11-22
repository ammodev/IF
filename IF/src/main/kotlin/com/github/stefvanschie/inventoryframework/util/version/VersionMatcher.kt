package com.github.stefvanschie.inventoryframework.util.version

import com.github.stefvanschie.inventoryframework.abstraction.*
import com.github.stefvanschie.inventoryframework.exception.UnsupportedVersionException
import com.github.stefvanschie.inventoryframework.nms.v1_20_0.AnvilInventoryImpl
import com.github.stefvanschie.inventoryframework.nms.v1_20_0.BeaconInventoryImpl
import com.github.stefvanschie.inventoryframework.nms.v1_20_0.CartographyTableInventoryImpl
import com.github.stefvanschie.inventoryframework.nms.v1_20_0.EnchantingTableInventoryImpl
import com.github.stefvanschie.inventoryframework.nms.v1_20_1.GrindstoneInventoryImpl
import com.github.stefvanschie.inventoryframework.nms.v1_20_1.MerchantInventoryImpl
import com.github.stefvanschie.inventoryframework.nms.v1_20_1.SmithingTableInventoryImpl
import com.github.stefvanschie.inventoryframework.nms.v1_20_1.StonecutterInventoryImpl
import org.bukkit.inventory.InventoryHolder
import org.jetbrains.annotations.Contract
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * Utility class containing versioning related methods.
 *
 * @since 0.8.0
 */
object VersionMatcher {

    /**
     * The different anvil inventories for different versions
     */
    private val ANVIL_INVENTORIES =
        EnumMap<Version, Class<out AnvilInventory?>>(Version::class.java)

    /**
     * The different beacon inventories for different versions
     */
    private val BEACON_INVENTORIES: EnumMap<Version, Class<out BeaconInventory?>>

    /**
     * The different cartography table inventories for different versions
     */
    private val CARTOGRAPHY_TABLE_INVENTORIES: EnumMap<Version, Class<out CartographyTableInventory?>>

    /**
     * The different enchanting table inventories for different versions
     */
    private val ENCHANTING_TABLE_INVENTORIES: EnumMap<Version, Class<out EnchantingTableInventory?>>

    /**
     * The different grindstone inventories for different versions
     */
    private val GRINDSTONE_INVENTORIES: EnumMap<Version, Class<out GrindstoneInventory?>>

    /**
     * The different merchant inventories for different versions
     */
    private val MERCHANT_INVENTORIES: EnumMap<Version, Class<out MerchantInventory?>>

    /**
     * The different smithing table inventories for different versions
     */
    private val SMITHING_TABLE_INVENTORIES: EnumMap<Version, Class<out SmithingTableInventory?>>

    /**
     * The different stonecutter inventories for different versions
     */
    private val STONECUTTER_INVENTORIES: EnumMap<Version, Class<out StonecutterInventory?>>

    /**
     * Gets a new anvil inventory for the specified version of the specified inventory holder.
     *
     * @param version         the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the anvil inventory
     * @since 0.8.0
     */
    @JvmStatic
    @Contract(pure = true)
    fun newAnvilInventory(
        version: Version,
        inventoryHolder: InventoryHolder
    ): AnvilInventory {
        try {
            return ANVIL_INVENTORIES[version]!!
                .getConstructor(
                    InventoryHolder::class.java
                )
                .newInstance(inventoryHolder)!!
        } catch (exception: InstantiationException) {
            throw IllegalStateException(exception)
        } catch (exception: IllegalAccessException) {
            throw IllegalStateException(exception)
        } catch (exception: InvocationTargetException) {
            throw IllegalStateException(exception)
        } catch (exception: NoSuchMethodException) {
            throw IllegalStateException(exception)
        }
    }

    /**
     * Gets a new beacon inventory for the specified version of the specified inventory holder.
     *
     * @param version         the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the beacon inventory
     * @since 0.8.0
     */
    @JvmStatic
    @Contract(pure = true)
    fun newBeaconInventory(
        version: Version,
        inventoryHolder: InventoryHolder
    ): BeaconInventory {
        try {
            return BEACON_INVENTORIES[version]!!
                .getConstructor(
                    InventoryHolder::class.java
                )
                .newInstance(inventoryHolder)!!
        } catch (exception: InstantiationException) {
            throw IllegalStateException(exception)
        } catch (exception: IllegalAccessException) {
            throw IllegalStateException(exception)
        } catch (exception: InvocationTargetException) {
            throw IllegalStateException(exception)
        } catch (exception: NoSuchMethodException) {
            throw IllegalStateException(exception)
        }
    }

    /**
     * Gets a new cartography table inventory for the specified version of the specified inventory
     * holder.
     *
     * @param version         the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the cartography table inventory
     * @since 0.8.0
     */
    @JvmStatic
    @Contract(pure = true)
    fun newCartographyTableInventory(
        version: Version,
        inventoryHolder: InventoryHolder
    ): CartographyTableInventory {
        try {
            val clazz = CARTOGRAPHY_TABLE_INVENTORIES[version]!!

            return clazz.getConstructor(InventoryHolder::class.java).newInstance(inventoryHolder)!!
        } catch (exception: InstantiationException) {
            throw IllegalStateException(exception)
        } catch (exception: IllegalAccessException) {
            throw IllegalStateException(exception)
        } catch (exception: InvocationTargetException) {
            throw IllegalStateException(exception)
        } catch (exception: NoSuchMethodException) {
            throw IllegalStateException(exception)
        }
    }

    /**
     * Gets a new enchanting table inventory for the specified version of the specified inventory
     * holder.
     *
     * @param version         the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the enchanting table inventory
     * @since 0.8.0
     */
    @JvmStatic
    @Contract(pure = true)
    fun newEnchantingTableInventory(
        version: Version,
        inventoryHolder: InventoryHolder
    ): EnchantingTableInventory {
        try {
            val clazz = ENCHANTING_TABLE_INVENTORIES[version]!!

            return clazz.getConstructor(InventoryHolder::class.java).newInstance(inventoryHolder)!!
        } catch (exception: InstantiationException) {
            throw IllegalStateException(exception)
        } catch (exception: IllegalAccessException) {
            throw IllegalStateException(exception)
        } catch (exception: InvocationTargetException) {
            throw IllegalStateException(exception)
        } catch (exception: NoSuchMethodException) {
            throw IllegalStateException(exception)
        }
    }

    /**
     * Gets a new grindstone inventory for the specified version of the specified inventory holder.
     *
     * @param version         the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the grindstone inventory
     * @since 0.8.0
     */
    @JvmStatic
    @Contract(pure = true)
    fun newGrindstoneInventory(
        version: Version,
        inventoryHolder: InventoryHolder
    ): GrindstoneInventory {
        try {
            val clazz =
                GRINDSTONE_INVENTORIES[version]!!

            return clazz.getConstructor(InventoryHolder::class.java).newInstance(inventoryHolder)!!
        } catch (exception: InstantiationException) {
            throw IllegalStateException(exception)
        } catch (exception: IllegalAccessException) {
            throw IllegalStateException(exception)
        } catch (exception: InvocationTargetException) {
            throw IllegalStateException(exception)
        } catch (exception: NoSuchMethodException) {
            throw IllegalStateException(exception)
        }
    }

    /**
     * Gets a new merchant inventory for the specified version.
     *
     * @param version the version to get the inventory of
     * @return the merchant inventory
     * @since 0.10.1
     */
    @JvmStatic
    @Contract(pure = true)
    fun newMerchantInventory(version: Version): MerchantInventory {
        try {
            return MERCHANT_INVENTORIES[version]!!.getConstructor().newInstance()!!
        } catch (exception: InstantiationException) {
            throw IllegalStateException(exception)
        } catch (exception: IllegalAccessException) {
            throw IllegalStateException(exception)
        } catch (exception: InvocationTargetException) {
            throw IllegalStateException(exception)
        } catch (exception: NoSuchMethodException) {
            throw IllegalStateException(exception)
        }
    }

    /**
     * Gets a new smithing table inventory for the specified version of the specified inventory
     * holder. If a smithing table is requested for a version that does not have smithing tables, an
     * [UnsupportedVersionException] is thrown.
     *
     * @param version         the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the smithing table inventory
     * @throws UnsupportedVersionException when a smithing table is requested on a version without
     * smithing tables
     * @since 0.10.9
     */
    @JvmStatic
    @Contract(pure = true)
    fun newSmithingTableInventory(
        version: Version,
        inventoryHolder: InventoryHolder
    ): SmithingTableInventory {
        if (!version.existsSmithingTable()) {
            throw UnsupportedVersionException(
                "Modern smithing tables didn't exist in version $version"
            )
        }

        try {
            val clazz = SMITHING_TABLE_INVENTORIES[version]!!

            return clazz.getConstructor(InventoryHolder::class.java).newInstance(inventoryHolder)!!
        } catch (exception: InstantiationException) {
            throw IllegalStateException(exception)
        } catch (exception: IllegalAccessException) {
            throw IllegalStateException(exception)
        } catch (exception: InvocationTargetException) {
            throw IllegalStateException(exception)
        } catch (exception: NoSuchMethodException) {
            throw IllegalStateException(exception)
        }
    }

    /**
     * Gets a new stonecutter inventory for the specified version of the specified inventory holder.
     *
     * @param version         the version to get the inventory of
     * @param inventoryHolder the inventory holder
     * @return the stonecutter inventory
     * @since 0.8.0
     */
    @JvmStatic
    @Contract(pure = true)
    fun newStonecutterInventory(
        version: Version,
        inventoryHolder: InventoryHolder
    ): StonecutterInventory {
        try {
            val clazz =
                STONECUTTER_INVENTORIES[version]!!

            return clazz.getConstructor(InventoryHolder::class.java).newInstance(inventoryHolder)!!
        } catch (exception: InstantiationException) {
            throw IllegalStateException(exception)
        } catch (exception: IllegalAccessException) {
            throw IllegalStateException(exception)
        } catch (exception: InvocationTargetException) {
            throw IllegalStateException(exception)
        } catch (exception: NoSuchMethodException) {
            throw IllegalStateException(exception)
        }
    }

    init {
        ANVIL_INVENTORIES[Version.V1_20_0] =
            AnvilInventoryImpl::class.java
        ANVIL_INVENTORIES[Version.V1_20_1] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_1.AnvilInventoryImpl::class.java
        ANVIL_INVENTORIES[Version.V1_20_2] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.AnvilInventoryImpl::class.java
        ANVIL_INVENTORIES[Version.V1_20_3_4] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.AnvilInventoryImpl::class.java
        ANVIL_INVENTORIES[Version.V1_20_6] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.AnvilInventoryImpl::class.java
        ANVIL_INVENTORIES[Version.V1_21_0] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.AnvilInventoryImpl::class.java
        ANVIL_INVENTORIES[Version.V1_21_1] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.AnvilInventoryImpl::class.java
        ANVIL_INVENTORIES[Version.V1_21_2_3] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.AnvilInventoryImpl::class.java

        BEACON_INVENTORIES = EnumMap(
            Version::class.java
        )
        BEACON_INVENTORIES[Version.V1_20_0] =
            BeaconInventoryImpl::class.java
        BEACON_INVENTORIES[Version.V1_20_1] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_1.BeaconInventoryImpl::class.java
        BEACON_INVENTORIES[Version.V1_20_2] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.BeaconInventoryImpl::class.java
        BEACON_INVENTORIES[Version.V1_20_3_4] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.BeaconInventoryImpl::class.java
        BEACON_INVENTORIES[Version.V1_20_6] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.BeaconInventoryImpl::class.java
        BEACON_INVENTORIES[Version.V1_21_0] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.BeaconInventoryImpl::class.java
        BEACON_INVENTORIES[Version.V1_21_1] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.BeaconInventoryImpl::class.java
        BEACON_INVENTORIES[Version.V1_21_2_3] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.BeaconInventoryImpl::class.java

        CARTOGRAPHY_TABLE_INVENTORIES = EnumMap(
            Version::class.java
        )
        CARTOGRAPHY_TABLE_INVENTORIES[Version.V1_20_0] =
            CartographyTableInventoryImpl::class.java
        CARTOGRAPHY_TABLE_INVENTORIES[Version.V1_20_1] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_1.CartographyTableInventoryImpl::class.java
        CARTOGRAPHY_TABLE_INVENTORIES[Version.V1_20_2] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.CartographyTableInventoryImpl::class.java
        CARTOGRAPHY_TABLE_INVENTORIES[Version.V1_20_3_4] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.CartographyTableInventoryImpl::class.java
        CARTOGRAPHY_TABLE_INVENTORIES[Version.V1_20_6] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.CartographyTableInventoryImpl::class.java
        CARTOGRAPHY_TABLE_INVENTORIES[Version.V1_21_0] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.CartographyTableInventoryImpl::class.java
        CARTOGRAPHY_TABLE_INVENTORIES[Version.V1_21_1] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.CartographyTableInventoryImpl::class.java
        CARTOGRAPHY_TABLE_INVENTORIES[Version.V1_21_2_3] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.CartographyTableInventoryImpl::class.java

        ENCHANTING_TABLE_INVENTORIES = EnumMap(
            Version::class.java
        )
        ENCHANTING_TABLE_INVENTORIES[Version.V1_20_0] =
            EnchantingTableInventoryImpl::class.java
        ENCHANTING_TABLE_INVENTORIES[Version.V1_20_1] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_1.EnchantingTableInventoryImpl::class.java
        ENCHANTING_TABLE_INVENTORIES[Version.V1_20_2] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.EnchantingTableInventoryImpl::class.java
        ENCHANTING_TABLE_INVENTORIES[Version.V1_20_3_4] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.EnchantingTableInventoryImpl::class.java
        ENCHANTING_TABLE_INVENTORIES[Version.V1_20_6] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.EnchantingTableInventoryImpl::class.java
        ENCHANTING_TABLE_INVENTORIES[Version.V1_21_0] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.EnchantingTableInventoryImpl::class.java
        ENCHANTING_TABLE_INVENTORIES[Version.V1_21_1] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.EnchantingTableInventoryImpl::class.java
        ENCHANTING_TABLE_INVENTORIES[Version.V1_21_2_3] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.EnchantingTableInventoryImpl::class.java

        GRINDSTONE_INVENTORIES = EnumMap(
            Version::class.java
        )
        GRINDSTONE_INVENTORIES[Version.V1_20_0] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_0.GrindstoneInventoryImpl::class.java
        GRINDSTONE_INVENTORIES[Version.V1_20_1] =
            GrindstoneInventoryImpl::class.java
        GRINDSTONE_INVENTORIES[Version.V1_20_2] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.GrindstoneInventoryImpl::class.java
        GRINDSTONE_INVENTORIES[Version.V1_20_3_4] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.GrindstoneInventoryImpl::class.java
        GRINDSTONE_INVENTORIES[Version.V1_20_6] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.GrindstoneInventoryImpl::class.java
        GRINDSTONE_INVENTORIES[Version.V1_21_0] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.GrindstoneInventoryImpl::class.java
        GRINDSTONE_INVENTORIES[Version.V1_21_1] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.GrindstoneInventoryImpl::class.java
        GRINDSTONE_INVENTORIES[Version.V1_21_2_3] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.GrindstoneInventoryImpl::class.java

        MERCHANT_INVENTORIES = EnumMap(
            Version::class.java
        )
        MERCHANT_INVENTORIES[Version.V1_20_0] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_0.MerchantInventoryImpl::class.java
        MERCHANT_INVENTORIES[Version.V1_20_1] =
            MerchantInventoryImpl::class.java
        MERCHANT_INVENTORIES[Version.V1_20_2] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.MerchantInventoryImpl::class.java
        MERCHANT_INVENTORIES[Version.V1_20_3_4] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.MerchantInventoryImpl::class.java
        MERCHANT_INVENTORIES[Version.V1_20_6] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.MerchantInventoryImpl::class.java
        MERCHANT_INVENTORIES[Version.V1_21_0] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.MerchantInventoryImpl::class.java
        MERCHANT_INVENTORIES[Version.V1_21_1] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.MerchantInventoryImpl::class.java
        MERCHANT_INVENTORIES[Version.V1_21_2_3] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.MerchantInventoryImpl::class.java

        SMITHING_TABLE_INVENTORIES = EnumMap(
            Version::class.java
        )
        SMITHING_TABLE_INVENTORIES[Version.V1_20_0] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_0.SmithingTableInventoryImpl::class.java
        SMITHING_TABLE_INVENTORIES[Version.V1_20_1] =
            SmithingTableInventoryImpl::class.java
        SMITHING_TABLE_INVENTORIES[Version.V1_20_2] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.SmithingTableInventoryImpl::class.java
        SMITHING_TABLE_INVENTORIES[Version.V1_20_3_4] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.SmithingTableInventoryImpl::class.java
        SMITHING_TABLE_INVENTORIES[Version.V1_20_6] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.SmithingTableInventoryImpl::class.java
        SMITHING_TABLE_INVENTORIES[Version.V1_21_0] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.SmithingTableInventoryImpl::class.java
        SMITHING_TABLE_INVENTORIES[Version.V1_21_1] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.SmithingTableInventoryImpl::class.java
        SMITHING_TABLE_INVENTORIES[Version.V1_21_2_3] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.SmithingTableInventoryImpl::class.java

        STONECUTTER_INVENTORIES = EnumMap(
            Version::class.java
        )
        STONECUTTER_INVENTORIES[Version.V1_20_0] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_0.StonecutterInventoryImpl::class.java
        STONECUTTER_INVENTORIES[Version.V1_20_1] =
            StonecutterInventoryImpl::class.java
        STONECUTTER_INVENTORIES[Version.V1_20_2] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_2.StonecutterInventoryImpl::class.java
        STONECUTTER_INVENTORIES[Version.V1_20_3_4] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_3.StonecutterInventoryImpl::class.java
        STONECUTTER_INVENTORIES[Version.V1_20_6] =
            com.github.stefvanschie.inventoryframework.nms.v1_20_6.StonecutterInventoryImpl::class.java
        STONECUTTER_INVENTORIES[Version.V1_21_0] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_0.StonecutterInventoryImpl::class.java
        STONECUTTER_INVENTORIES[Version.V1_21_1] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_1.StonecutterInventoryImpl::class.java
        STONECUTTER_INVENTORIES[Version.V1_21_2_3] =
            com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.StonecutterInventoryImpl::class.java
    }
}
