package com.github.stefvanschie.inventoryframework.util.version;

import com.github.stefvanschie.inventoryframework.abstraction.AnvilInventory;
import com.github.stefvanschie.inventoryframework.abstraction.BeaconInventory;
import com.github.stefvanschie.inventoryframework.abstraction.CartographyTableInventory;
import com.github.stefvanschie.inventoryframework.abstraction.EnchantingTableInventory;
import com.github.stefvanschie.inventoryframework.abstraction.GrindstoneInventory;
import com.github.stefvanschie.inventoryframework.abstraction.MerchantInventory;
import com.github.stefvanschie.inventoryframework.abstraction.SmithingTableInventory;
import com.github.stefvanschie.inventoryframework.abstraction.StonecutterInventory;
import com.github.stefvanschie.inventoryframework.exception.UnsupportedVersionException;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class containing versioning related methods.
 *
 * @since 0.8.0
 */
public class VersionMatcher {

  /**
   * The different anvil inventories for different versions
   */
  private static final EnumMap<Version, Class<? extends AnvilInventory>> ANVIL_INVENTORIES;

  /**
   * The different beacon inventories for different versions
   */
  private static final EnumMap<Version, Class<? extends BeaconInventory>> BEACON_INVENTORIES;

  /**
   * The different cartography table inventories for different versions
   */
  private static final EnumMap<Version, Class<? extends CartographyTableInventory>> CARTOGRAPHY_TABLE_INVENTORIES;

  /**
   * The different enchanting table inventories for different versions
   */
  private static final EnumMap<Version, Class<? extends EnchantingTableInventory>> ENCHANTING_TABLE_INVENTORIES;

  /**
   * The different grindstone inventories for different versions
   */
  private static final EnumMap<Version, Class<? extends GrindstoneInventory>> GRINDSTONE_INVENTORIES;

  /**
   * The different merchant inventories for different versions
   */
  private static final EnumMap<Version, Class<? extends MerchantInventory>> MERCHANT_INVENTORIES;

  /**
   * The different smithing table inventories for different versions
   */
  private static final EnumMap<Version, Class<? extends SmithingTableInventory>> SMITHING_TABLE_INVENTORIES;

  /**
   * The different stonecutter inventories for different versions
   */
  private static final EnumMap<Version, Class<? extends StonecutterInventory>> STONECUTTER_INVENTORIES;

  /**
   * Gets a new anvil inventory for the specified version of the specified inventory holder.
   *
   * @param version         the version to get the inventory of
   * @param inventoryHolder the inventory holder
   * @return the anvil inventory
   * @since 0.8.0
   */
  @NotNull
  @Contract(pure = true)
  public static AnvilInventory newAnvilInventory(@NotNull Version version,
      @NotNull InventoryHolder inventoryHolder) {
    try {
      return ANVIL_INVENTORIES.get(version).getConstructor(InventoryHolder.class)
          .newInstance(inventoryHolder);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
             NoSuchMethodException exception) {
      throw new IllegalStateException(exception);
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
  @NotNull
  @Contract(pure = true)
  public static BeaconInventory newBeaconInventory(@NotNull Version version,
      @NotNull InventoryHolder inventoryHolder) {
    try {
      return BEACON_INVENTORIES.get(version).getConstructor(InventoryHolder.class)
          .newInstance(inventoryHolder);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
             NoSuchMethodException exception) {
      throw new IllegalStateException(exception);
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
  @NotNull
  @Contract(pure = true)
  public static CartographyTableInventory newCartographyTableInventory(@NotNull Version version,
      @NotNull InventoryHolder inventoryHolder) {
    try {
      Class<? extends CartographyTableInventory> clazz = CARTOGRAPHY_TABLE_INVENTORIES.get(version);

      return clazz.getConstructor(InventoryHolder.class).newInstance(inventoryHolder);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
             NoSuchMethodException exception) {
      throw new IllegalStateException(exception);
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
  @NotNull
  @Contract(pure = true)
  public static EnchantingTableInventory newEnchantingTableInventory(@NotNull Version version,
      @NotNull InventoryHolder inventoryHolder) {
    try {
      Class<? extends EnchantingTableInventory> clazz = ENCHANTING_TABLE_INVENTORIES.get(version);

      return clazz.getConstructor(InventoryHolder.class).newInstance(inventoryHolder);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
             NoSuchMethodException exception) {
      throw new IllegalStateException(exception);
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
  @NotNull
  @Contract(pure = true)
  public static GrindstoneInventory newGrindstoneInventory(@NotNull Version version,
      @NotNull InventoryHolder inventoryHolder) {
    try {
      Class<? extends GrindstoneInventory> clazz = GRINDSTONE_INVENTORIES.get(version);

      return clazz.getConstructor(InventoryHolder.class).newInstance(inventoryHolder);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
             NoSuchMethodException exception) {
      throw new IllegalStateException(exception);
    }
  }

  /**
   * Gets a new merchant inventory for the specified version.
   *
   * @param version the version to get the inventory of
   * @return the merchant inventory
   * @since 0.10.1
   */
  @NotNull
  @Contract(pure = true)
  public static MerchantInventory newMerchantInventory(@NotNull Version version) {
    try {
      return MERCHANT_INVENTORIES.get(version).getConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
             NoSuchMethodException exception) {
      throw new IllegalStateException(exception);
    }
  }

  /**
   * Gets a new smithing table inventory for the specified version of the specified inventory
   * holder. If a smithing table is requested for a version that does not have smithing tables, an
   * {@link UnsupportedVersionException} is thrown.
   *
   * @param version         the version to get the inventory of
   * @param inventoryHolder the inventory holder
   * @return the smithing table inventory
   * @throws UnsupportedVersionException when a smithing table is requested on a version without
   *                                     smithing tables
   * @since 0.10.9
   */
  @NotNull
  @Contract(pure = true)
  public static SmithingTableInventory newSmithingTableInventory(@NotNull Version version,
      @NotNull InventoryHolder inventoryHolder) {
    if (!version.existsSmithingTable()) {
      throw new UnsupportedVersionException(
          "Modern smithing tables didn't exist in version " + version);
    }

    try {
      Class<? extends SmithingTableInventory> clazz = SMITHING_TABLE_INVENTORIES.get(version);

      return clazz.getConstructor(InventoryHolder.class).newInstance(inventoryHolder);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
             NoSuchMethodException exception) {
      throw new IllegalStateException(exception);
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
  @NotNull
  @Contract(pure = true)
  public static StonecutterInventory newStonecutterInventory(@NotNull Version version,
      @NotNull InventoryHolder inventoryHolder) {
    try {
      Class<? extends StonecutterInventory> clazz = STONECUTTER_INVENTORIES.get(version);

      return clazz.getConstructor(InventoryHolder.class).newInstance(inventoryHolder);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
             NoSuchMethodException exception) {
      throw new IllegalStateException(exception);
    }
  }

  static {
    ANVIL_INVENTORIES = new EnumMap<>(Version.class);
    ANVIL_INVENTORIES.put(Version.V1_20_0,
        com.github.stefvanschie.inventoryframework.nms.v1_20_0.AnvilInventoryImpl.class);
    ANVIL_INVENTORIES.put(Version.V1_20_1,
        com.github.stefvanschie.inventoryframework.nms.v1_20_1.AnvilInventoryImpl.class);
    ANVIL_INVENTORIES.put(Version.V1_20_2,
        com.github.stefvanschie.inventoryframework.nms.v1_20_2.AnvilInventoryImpl.class);
    ANVIL_INVENTORIES.put(Version.V1_20_3_4,
        com.github.stefvanschie.inventoryframework.nms.v1_20_3.AnvilInventoryImpl.class);
    ANVIL_INVENTORIES.put(Version.V1_20_6,
        com.github.stefvanschie.inventoryframework.nms.v1_20_6.AnvilInventoryImpl.class);
    ANVIL_INVENTORIES.put(Version.V1_21_0,
        com.github.stefvanschie.inventoryframework.nms.v1_21_0.AnvilInventoryImpl.class);
    ANVIL_INVENTORIES.put(Version.V1_21_1,
        com.github.stefvanschie.inventoryframework.nms.v1_21_1.AnvilInventoryImpl.class);
    ANVIL_INVENTORIES.put(Version.V1_21_2_3,
        com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.AnvilInventoryImpl.class);

    BEACON_INVENTORIES = new EnumMap<>(Version.class);
    BEACON_INVENTORIES.put(Version.V1_20_0,
        com.github.stefvanschie.inventoryframework.nms.v1_20_0.BeaconInventoryImpl.class);
    BEACON_INVENTORIES.put(Version.V1_20_1,
        com.github.stefvanschie.inventoryframework.nms.v1_20_1.BeaconInventoryImpl.class);
    BEACON_INVENTORIES.put(Version.V1_20_2,
        com.github.stefvanschie.inventoryframework.nms.v1_20_2.BeaconInventoryImpl.class);
    BEACON_INVENTORIES.put(Version.V1_20_3_4,
        com.github.stefvanschie.inventoryframework.nms.v1_20_3.BeaconInventoryImpl.class);
    BEACON_INVENTORIES.put(Version.V1_20_6,
        com.github.stefvanschie.inventoryframework.nms.v1_20_6.BeaconInventoryImpl.class);
    BEACON_INVENTORIES.put(Version.V1_21_0,
        com.github.stefvanschie.inventoryframework.nms.v1_21_0.BeaconInventoryImpl.class);
    BEACON_INVENTORIES.put(Version.V1_21_1,
        com.github.stefvanschie.inventoryframework.nms.v1_21_1.BeaconInventoryImpl.class);
    BEACON_INVENTORIES.put(Version.V1_21_2_3,
        com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.BeaconInventoryImpl.class);

    CARTOGRAPHY_TABLE_INVENTORIES = new EnumMap<>(Version.class);
    CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_20_0,
        com.github.stefvanschie.inventoryframework.nms.v1_20_0.CartographyTableInventoryImpl.class);
    CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_20_1,
        com.github.stefvanschie.inventoryframework.nms.v1_20_1.CartographyTableInventoryImpl.class);
    CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_20_2,
        com.github.stefvanschie.inventoryframework.nms.v1_20_2.CartographyTableInventoryImpl.class);
    CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_20_3_4,
        com.github.stefvanschie.inventoryframework.nms.v1_20_3.CartographyTableInventoryImpl.class);
    CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_20_6,
        com.github.stefvanschie.inventoryframework.nms.v1_20_6.CartographyTableInventoryImpl.class);
    CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_21_0,
        com.github.stefvanschie.inventoryframework.nms.v1_21_0.CartographyTableInventoryImpl.class);
    CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_21_1,
        com.github.stefvanschie.inventoryframework.nms.v1_21_1.CartographyTableInventoryImpl.class);
    CARTOGRAPHY_TABLE_INVENTORIES.put(Version.V1_21_2_3,
        com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.CartographyTableInventoryImpl.class);

    ENCHANTING_TABLE_INVENTORIES = new EnumMap<>(Version.class);
    ENCHANTING_TABLE_INVENTORIES.put(Version.V1_20_0,
        com.github.stefvanschie.inventoryframework.nms.v1_20_0.EnchantingTableInventoryImpl.class);
    ENCHANTING_TABLE_INVENTORIES.put(Version.V1_20_1,
        com.github.stefvanschie.inventoryframework.nms.v1_20_1.EnchantingTableInventoryImpl.class);
    ENCHANTING_TABLE_INVENTORIES.put(Version.V1_20_2,
        com.github.stefvanschie.inventoryframework.nms.v1_20_2.EnchantingTableInventoryImpl.class);
    ENCHANTING_TABLE_INVENTORIES.put(Version.V1_20_3_4,
        com.github.stefvanschie.inventoryframework.nms.v1_20_3.EnchantingTableInventoryImpl.class);
    ENCHANTING_TABLE_INVENTORIES.put(Version.V1_20_6,
        com.github.stefvanschie.inventoryframework.nms.v1_20_6.EnchantingTableInventoryImpl.class);
    ENCHANTING_TABLE_INVENTORIES.put(Version.V1_21_0,
        com.github.stefvanschie.inventoryframework.nms.v1_21_0.EnchantingTableInventoryImpl.class);
    ENCHANTING_TABLE_INVENTORIES.put(Version.V1_21_1,
        com.github.stefvanschie.inventoryframework.nms.v1_21_1.EnchantingTableInventoryImpl.class);
    ENCHANTING_TABLE_INVENTORIES.put(Version.V1_21_2_3,
        com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.EnchantingTableInventoryImpl.class);

    GRINDSTONE_INVENTORIES = new EnumMap<>(Version.class);
    GRINDSTONE_INVENTORIES.put(Version.V1_20_0,
        com.github.stefvanschie.inventoryframework.nms.v1_20_0.GrindstoneInventoryImpl.class);
    GRINDSTONE_INVENTORIES.put(Version.V1_20_1,
        com.github.stefvanschie.inventoryframework.nms.v1_20_1.GrindstoneInventoryImpl.class);
    GRINDSTONE_INVENTORIES.put(Version.V1_20_2,
        com.github.stefvanschie.inventoryframework.nms.v1_20_2.GrindstoneInventoryImpl.class);
    GRINDSTONE_INVENTORIES.put(Version.V1_20_3_4,
        com.github.stefvanschie.inventoryframework.nms.v1_20_3.GrindstoneInventoryImpl.class);
    GRINDSTONE_INVENTORIES.put(Version.V1_20_6,
        com.github.stefvanschie.inventoryframework.nms.v1_20_6.GrindstoneInventoryImpl.class);
    GRINDSTONE_INVENTORIES.put(Version.V1_21_0,
        com.github.stefvanschie.inventoryframework.nms.v1_21_0.GrindstoneInventoryImpl.class);
    GRINDSTONE_INVENTORIES.put(Version.V1_21_1,
        com.github.stefvanschie.inventoryframework.nms.v1_21_1.GrindstoneInventoryImpl.class);
    GRINDSTONE_INVENTORIES.put(Version.V1_21_2_3,
        com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.GrindstoneInventoryImpl.class);

    MERCHANT_INVENTORIES = new EnumMap<>(Version.class);
    MERCHANT_INVENTORIES.put(Version.V1_20_0,
        com.github.stefvanschie.inventoryframework.nms.v1_20_0.MerchantInventoryImpl.class);
    MERCHANT_INVENTORIES.put(Version.V1_20_1,
        com.github.stefvanschie.inventoryframework.nms.v1_20_1.MerchantInventoryImpl.class);
    MERCHANT_INVENTORIES.put(Version.V1_20_2,
        com.github.stefvanschie.inventoryframework.nms.v1_20_2.MerchantInventoryImpl.class);
    MERCHANT_INVENTORIES.put(Version.V1_20_3_4,
        com.github.stefvanschie.inventoryframework.nms.v1_20_3.MerchantInventoryImpl.class);
    MERCHANT_INVENTORIES.put(Version.V1_20_6,
        com.github.stefvanschie.inventoryframework.nms.v1_20_6.MerchantInventoryImpl.class);
    MERCHANT_INVENTORIES.put(Version.V1_21_0,
        com.github.stefvanschie.inventoryframework.nms.v1_21_0.MerchantInventoryImpl.class);
    MERCHANT_INVENTORIES.put(Version.V1_21_1,
        com.github.stefvanschie.inventoryframework.nms.v1_21_1.MerchantInventoryImpl.class);
    MERCHANT_INVENTORIES.put(Version.V1_21_2_3,
        com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.MerchantInventoryImpl.class);

    SMITHING_TABLE_INVENTORIES = new EnumMap<>(Version.class);
    SMITHING_TABLE_INVENTORIES.put(Version.V1_20_0,
        com.github.stefvanschie.inventoryframework.nms.v1_20_0.SmithingTableInventoryImpl.class);
    SMITHING_TABLE_INVENTORIES.put(Version.V1_20_1,
        com.github.stefvanschie.inventoryframework.nms.v1_20_1.SmithingTableInventoryImpl.class);
    SMITHING_TABLE_INVENTORIES.put(Version.V1_20_2,
        com.github.stefvanschie.inventoryframework.nms.v1_20_2.SmithingTableInventoryImpl.class);
    SMITHING_TABLE_INVENTORIES.put(Version.V1_20_3_4,
        com.github.stefvanschie.inventoryframework.nms.v1_20_3.SmithingTableInventoryImpl.class);
    SMITHING_TABLE_INVENTORIES.put(Version.V1_20_6,
        com.github.stefvanschie.inventoryframework.nms.v1_20_6.SmithingTableInventoryImpl.class);
    SMITHING_TABLE_INVENTORIES.put(Version.V1_21_0,
        com.github.stefvanschie.inventoryframework.nms.v1_21_0.SmithingTableInventoryImpl.class);
    SMITHING_TABLE_INVENTORIES.put(Version.V1_21_1,
        com.github.stefvanschie.inventoryframework.nms.v1_21_1.SmithingTableInventoryImpl.class);
    SMITHING_TABLE_INVENTORIES.put(Version.V1_21_2_3,
        com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.SmithingTableInventoryImpl.class);

    STONECUTTER_INVENTORIES = new EnumMap<>(Version.class);
    STONECUTTER_INVENTORIES.put(Version.V1_20_0,
        com.github.stefvanschie.inventoryframework.nms.v1_20_0.StonecutterInventoryImpl.class);
    STONECUTTER_INVENTORIES.put(Version.V1_20_1,
        com.github.stefvanschie.inventoryframework.nms.v1_20_1.StonecutterInventoryImpl.class);
    STONECUTTER_INVENTORIES.put(Version.V1_20_2,
        com.github.stefvanschie.inventoryframework.nms.v1_20_2.StonecutterInventoryImpl.class);
    STONECUTTER_INVENTORIES.put(Version.V1_20_3_4,
        com.github.stefvanschie.inventoryframework.nms.v1_20_3.StonecutterInventoryImpl.class);
    STONECUTTER_INVENTORIES.put(Version.V1_20_6,
        com.github.stefvanschie.inventoryframework.nms.v1_20_6.StonecutterInventoryImpl.class);
    STONECUTTER_INVENTORIES.put(Version.V1_21_0,
        com.github.stefvanschie.inventoryframework.nms.v1_21_0.StonecutterInventoryImpl.class);
    STONECUTTER_INVENTORIES.put(Version.V1_21_1,
        com.github.stefvanschie.inventoryframework.nms.v1_21_1.StonecutterInventoryImpl.class);
    STONECUTTER_INVENTORIES.put(Version.V1_21_2_3,
        com.github.stefvanschie.inventoryframework.nms.v1_21_2_3.StonecutterInventoryImpl.class);
  }
}
