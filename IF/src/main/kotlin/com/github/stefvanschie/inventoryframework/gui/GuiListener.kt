package com.github.stefvanschie.inventoryframework.gui

import com.github.stefvanschie.inventoryframework.gui.type.*
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil.instance
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.*
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.inventory.*
import org.bukkit.plugin.Plugin
import org.jetbrains.annotations.Contract
import java.util.logging.Logger

/**
 * Listens to events for [Gui]s. Only one instance of this class gets constructed. (One
 * instance per plugin, but plugins are supposed to shade and relocate IF.)
 *
 * @since 0.5.4
 */
class GuiListener
/**
 * Creates a new listener for all guis for the provided `plugin`.
 *
 * @param plugin the owning plugin of this listener
 * @since 0.10.8
 */(
    /**
     * The owning plugin of this listener.
     */
    private val plugin: Plugin
) : Listener {
    /**
     * A collection of all [Gui] instances that have at least one viewer.
     */
    private val activeGuiInstances: MutableSet<Gui> = HashSet()

    /**
     * Handles clicks in inventories
     *
     * @param event the event fired
     * @since 0.5.4
     */
    @EventHandler(ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        val gui: Gui? = getGui(event.getInventory())

        if (gui == null) {
            return
        }

        val view: InventoryView = event.getView()
        val inventory: Inventory? = instance.getInventory(view, event.getRawSlot())

        if (inventory == null) {
            gui.callOnOutsideClick(event)
            return
        }

        gui.callOnGlobalClick(event)
        if (inventory == instance.getTopInventory(view)) {
            gui.callOnTopClick(event)
        } else {
            gui.callOnBottomClick(event)
        }

        gui.click(event)

        if (event.isCancelled()) {
            Bukkit.getScheduler().runTask(this.plugin, Runnable {
                val playerInventory: PlayerInventory = event.getWhoClicked().getInventory()
                /* due to a client issue off-hand items appear as ghost items, this updates the off-hand correctly
                                  client-side */
                playerInventory.setItemInOffHand(playerInventory.getItemInOffHand())
            })
        }
    }

    /**
     * Resets the items into the correct positions for anvil guis
     *
     * @param event the event fired
     * @since 0.8.0
     */
    @Deprecated("no longer used internally")
    fun resetItemsAnvil(event: InventoryClickEvent) {
        val holder: InventoryHolder? = event.getInventory().getHolder()

        if (holder !is AnvilGui || event.getWhoClicked() !is Player) {
            return
        }

        holder.handleClickEvent(event)
    }

    /**
     * Resets the items into the correct positions for beacon guis
     *
     * @param event the event fired
     * @since 0.8.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun resetItemsBeacon(event: InventoryClickEvent) {
        val holder: InventoryHolder? = event.getInventory().getHolder()

        if (holder !is BeaconGui || event.getWhoClicked() !is Player) {
            return
        }

        holder.handleClickEvent(event)
    }

    /**
     * Resets the items into the correct positions for cartography table guis
     *
     * @param event the event fired
     * @since 0.8.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun resetItemsCartographyTable(event: InventoryClickEvent) {
        val holder: InventoryHolder? = event.getInventory().getHolder()

        if (holder !is CartographyTableGui || event.getWhoClicked() !is Player) {
            return
        }

        holder.handleClickEvent(event)
    }

    /**
     * Resets the items into the correct positions for enchanting table guis
     *
     * @param event the event fired
     * @since 0.8.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun resetItemsEnchantingTable(event: InventoryClickEvent) {
        val holder: InventoryHolder? = event.getInventory().getHolder()

        if (holder !is EnchantingTableGui || event.getWhoClicked() !is Player) {
            return
        }

        holder.handleClickEvent(event)
    }

    /**
     * Resets the items into the correct positions for grindstone guis
     *
     * @param event the event fired
     * @since 0.8.0
     */
    @Deprecated("no longer used internally")
    fun resetItemsGrindstone(event: InventoryClickEvent) {
        val holder: InventoryHolder? = event.getInventory().getHolder()

        if (holder !is GrindstoneGui || event.getWhoClicked() !is Player) {
            return
        }

        holder.handleClickEvent(event)
    }

    /**
     * Resets the items into the correct positions for stonecutter guis
     *
     * @param event the event fired
     * @since 0.8.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun resetItemsStonecutter(event: InventoryClickEvent) {
        val holder: InventoryHolder? = event.getInventory().getHolder()

        if (holder !is StonecutterGui || event.getWhoClicked() !is Player) {
            return
        }

        holder.handleClickEvent(event)
    }

    /**
     * Handles users picking up items while their bottom inventory is in use.
     *
     * @param event the event fired when an entity picks up an item
     * @since 0.6.1
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        val entity: LivingEntity = event.getEntity()

        if (entity !is HumanEntity) {
            return
        }

        val gui: Gui? = getGui(
            instance.getTopInventory(
                entity.getOpenInventory()
            )
        )

        if (gui == null || !gui.isPlayerInventoryUsed()) {
            return
        }

        val leftOver: Int = gui.getHumanEntityCache()
            .add(entity, event.getItem().getItemStack())

        if (leftOver == 0) {
            event.getItem().remove()
        } else {
            val itemStack: ItemStack = event.getItem().getItemStack()

            itemStack.setAmount(leftOver)

            event.getItem().setItemStack(itemStack)
        }

        event.setCancelled(true)
    }

    /**
     * Handles drag events
     *
     * @param event the event fired
     * @since 0.6.1
     */
    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val gui: Gui? = getGui(event.getInventory())

        if (gui == null) {
            return
        }

        val view: InventoryView = event.getView()
        val inventorySlots: Set<Int> = event.getRawSlots()

        if (inventorySlots.size > 1) {
            var top: Boolean = false
            var bottom: Boolean = false

            for (inventorySlot: Int in inventorySlots) {
                val inventory: Inventory? = instance.getInventory(view, inventorySlot)

                if (instance.getTopInventory(view) == inventory) {
                    top = true
                } else if (instance.getBottomInventory(view) == inventory) {
                    bottom = true
                }

                if (top && bottom) {
                    break
                }
            }

            gui.callOnGlobalDrag(event)

            if (top) {
                gui.callOnTopDrag(event)
            }

            if (bottom) {
                gui.callOnBottomDrag(event)
            }
        } else {
            val index: Int = inventorySlots.toTypedArray<Int>().get(0)
            val slotType: InventoryType.SlotType = instance.getSlotType(view, index)

            val even: Boolean = event.getType() == DragType.EVEN

            val clickType: ClickType = if (even) ClickType.LEFT else ClickType.RIGHT
            val inventoryAction: InventoryAction =
                if (even) InventoryAction.PLACE_SOME else InventoryAction.PLACE_ONE

            val previousViewCursor: ItemStack? = instance.getCursor(view)
            // Overwrite getCursor in inventory click event to mimic real event fired by Bukkit.
            instance.setCursor(view, event.getOldCursor())
            //this is a fake click event, firing this may cause other plugins to function incorrectly, so keep it local
            val inventoryClickEvent: InventoryClickEvent = InventoryClickEvent(
                view, slotType, index,
                clickType,
                inventoryAction
            )

            onInventoryClick(inventoryClickEvent)
            // Restore previous cursor only if someone has not changed it manually in onInventoryClick.
            if (instance.getCursor(view) == event.getOldCursor()) {
                instance.setCursor(view, previousViewCursor)
            }

            event.setCancelled(inventoryClickEvent.isCancelled())
        }
    }

    /**
     * Handles the selection of trades in merchant guis
     *
     * @param event the event fired
     */
    @EventHandler(ignoreCancelled = true)
    fun onTradeSelect(event: TradeSelectEvent) {
        val gui: Gui? = getGui(event.getInventory())

        if (gui !is MerchantGui) {
            return
        }

        gui.callOnTradeSelect(event)
    }

    /**
     * Handles closing in inventories
     *
     * @param event the event fired
     * @since 0.5.4
     */
    @EventHandler(ignoreCancelled = true)
    fun onInventoryClose(event: InventoryCloseEvent) {
        val gui: Gui? = getGui(event.getInventory())

        if (gui == null) {
            return
        }

        val humanEntity: HumanEntity = event.getPlayer()
        val playerInventory: PlayerInventory = humanEntity.getInventory()

        //due to a client issue off-hand items appear as ghost items, this updates the off-hand correctly client-side
        playerInventory.setItemInOffHand(playerInventory.getItemInOffHand())

        if (!gui.isUpdating()) {
            gui.callOnClose(event)

            event.getInventory().clear() //clear inventory to prevent items being put back

            gui.getHumanEntityCache().restoreAndForget(humanEntity)

            if (gui.getViewerCount() == 1) {
                activeGuiInstances.remove(gui)
            }

            if (gui is AnvilGui) {
                gui.handleClose(humanEntity)
            } else if (gui is MerchantGui) {
                gui.handleClose(humanEntity)
            } else if (gui is SmithingTableGui) {
                gui.handleClose(humanEntity)
            }

            //Bukkit doesn't like it if you open an inventory while the previous one is being closed
            Bukkit.getScheduler()
                .runTask(this.plugin, Runnable { gui.navigateToParent(humanEntity) })
        }
    }

    /**
     * Registers newly opened inventories
     *
     * @param event the event fired
     * @since 0.5.19
     */
    @EventHandler(ignoreCancelled = true)
    fun onInventoryOpen(event: InventoryOpenEvent) {
        val gui: Gui? = getGui(event.getInventory())

        if (gui == null) {
            return
        }

        activeGuiInstances.add(gui)
    }

    /**
     * Handles the disabling of the plugin
     *
     * @param event the event fired
     * @since 0.5.19
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPluginDisable(event: PluginDisableEvent) {
        if (event.getPlugin() !== this.plugin) {
            return
        }

        var counter: Int = 0 //callbacks might open GUIs, eg. in nested menus
        val maxCount: Int = 10
        while (!activeGuiInstances.isEmpty() && counter++ < maxCount) {
            for (gui: Gui in ArrayList(activeGuiInstances)) {
                for (viewer: HumanEntity in gui.getViewers()) {
                    viewer.closeInventory()
                }
            }
        }

        if (counter == maxCount) {
            val logger: Logger = plugin.getLogger()

            logger.warning(
                ("Unable to close GUIs on plugin disable: they keep getting opened (tried: " + maxCount
                        + " times)")
            )
        }
    }

    /**
     * Gets the gui from the inventory or null if the inventory isn't a gui
     *
     * @param inventory the inventory to get the gui from
     * @return the gui or null if the inventory doesn't have a gui
     * @since 0.8.1
     */
    @Contract(pure = true)
    private fun getGui(inventory: Inventory): Gui? {
        val gui: Gui? = Gui.Companion.getGui(inventory)

        if (gui != null) {
            return gui
        }

        val holder: InventoryHolder? = inventory.getHolder()

        if (holder is Gui) {
            return holder
        }

        return null
    }
}
