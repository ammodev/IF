package com.github.stefvanschie.inventoryframework.gui.type.util

import com.github.stefvanschie.inventoryframework.HumanEntityCache
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.GuiListener
import com.github.stefvanschie.inventoryframework.gui.type.*
import com.github.stefvanschie.inventoryframework.pane.*
import com.github.stefvanschie.inventoryframework.pane.component.*
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil.instance
import com.github.stefvanschie.inventoryframework.util.TriFunction
import com.github.stefvanschie.inventoryframework.util.XMLUtil.invokeMethod
import com.github.stefvanschie.inventoryframework.util.XMLUtil.loadFieldAttribute
import com.github.stefvanschie.inventoryframework.util.XMLUtil.loadOnEventAttribute
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.logging.Level
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * The base class of all GUIs
 */
abstract class Gui(
    /**
     * The plugin that owns this gui
     */
    protected val plugin: Plugin
) {
    /**
     * The inventory of this gui
     */
    protected var inventory: Inventory? = null

    /**
     * Gets the human entity cache used for this gui
     *
     * @return the human entity cache
     * @see HumanEntityCache
     *
     * @since 0.5.4
     */
    /**
     * A player cache for storing player's inventories
     */
    @get:Contract(pure = true)
    val humanEntityCache: HumanEntityCache = HumanEntityCache()

    /**
     * The consumer that will be called once a players clicks in the top-half of the gui
     */
    protected var onTopClick: Consumer<InventoryClickEvent>? = null

    /**
     * The consumer that will be called once a players clicks in the bottom-half of the gui
     */
    protected var onBottomClick: Consumer<InventoryClickEvent>? = null

    /**
     * The consumer that will be called once a players clicks in the gui or in their inventory
     */
    protected var onGlobalClick: Consumer<InventoryClickEvent>? = null

    /**
     * The consumer that will be called once a player clicks outside of the gui screen
     */
    protected var onOutsideClick: Consumer<InventoryClickEvent>? = null

    /**
     * The consumer that will be called once a player drags in the top-half of the gui
     */
    protected var onTopDrag: Consumer<InventoryDragEvent>? = null

    /**
     * The consumer that will be called once a player drags in the bottom-half of the gui
     */
    protected var onBottomDrag: Consumer<InventoryDragEvent>? = null

    /**
     * The consumer that will be called once a player drags in the gui or their inventory
     */
    protected var onGlobalDrag: Consumer<InventoryDragEvent>? = null

    /**
     * The consumer that will be called once a player closes the gui
     */
    protected var onClose: Consumer<InventoryCloseEvent>? = null

    /**
     * Gets whether this gui is being updated, as invoked by [.update]. This returns true if this is the case
     * and false otherwise.
     *
     * @return whether this gui is being updated
     * @since 0.5.15
     */
    /**
     * Whether this gui is updating (as invoked by [.update]), true if this is the case, false otherwise. This
     * is used to indicate that inventory close events due to updating should be ignored.
     */
    @get:Contract(pure = true)
    var isUpdating: Boolean = false

    /**
     * The parent gui. This gui will be navigated to once a player closes this gui. If this is null, the player will not
     * be redirected to another gui once they close this gui.
     */
    private var parent: Gui? = null

    /**
     * Shows a gui to a player
     *
     * @param humanEntity the human entity to show the gui to
     */
    abstract fun show(humanEntity: HumanEntity)

    /**
     * Makes a copy of this gui and returns it. This makes a deep copy of the gui. This entails that the underlying
     * panes will be copied as per their [Pane.copy] and miscellaneous data will be copied. The copy of this gui,
     * will however have no viewers even if this gui currently has viewers. With this, cache data for viewers will also
     * be non-existent for the copied gui. The original owning plugin of the gui is preserved, but the plugin will not
     * be deeply copied. The returned gui will never be reference equal to the current gui.
     *
     * @return a copy of the gui
     * @since 0.6.2
     */
    @Contract(pure = true)
    abstract fun copy(): Gui

    /**
     * This should delegate the provided inventory click event to the right pane, which can then handle this click event
     * further. This should not call any internal click handlers, since those will already have been activated.
     *
     * @param event the event to delegate
     * @since 0.8.0
     */
    abstract fun click(event: InventoryClickEvent)

    /**
     * Gets whether the player inventory is currently in use. This means whether the player inventory currently has an
     * item in it.
     *
     * @return true if the player inventory is occupied, false otherwise
     * @since 0.8.0
     */
    abstract val isPlayerInventoryUsed: Boolean

    @get:Contract(pure = true)
    abstract val viewerCount: Int

    @get:Contract(pure = true)
    abstract val viewers: List<HumanEntity>

    /**
     * Update the gui for everyone
     */
    fun update() {
        isUpdating = true

        for (viewer in viewers) {
            val cursor = viewer.itemOnCursor
            viewer.setItemOnCursor(ItemStack(Material.AIR))

            show(viewer)

            viewer.setItemOnCursor(cursor)
        }

        if (!isUpdating) throw AssertionError("Gui#isUpdating became false before Gui#update finished")

        isUpdating = false
    }

    /**
     * Adds the specified inventory and gui, so we can properly intercept clicks.
     *
     * @param inventory the inventory for the specified gui
     * @param gui the gui belonging to the specified inventory
     * @since 0.8.1
     */
    protected fun addInventory(inventory: Inventory, gui: Gui) {
        GUI_INVENTORIES[inventory] = gui
    }

    /**
     * Initializes standard fields from a Gui from a given input stream.
     * Throws a [RuntimeException] instead of returning null in case of a failure.
     *
     * @param instance the class instance for all reflection lookups
     * @param element the gui element
     * @see .load
     */
    protected open fun initializeOrThrow(instance: Any, element: Element) {
        if (element.hasAttribute("field")) loadFieldAttribute(instance, element, this)

        if (element.hasAttribute("onTopClick")) {
            setOnTopClick(
                loadOnEventAttribute(
                    instance,
                    element, InventoryClickEvent::class.java, "onTopClick"
                )
            )
        }

        if (element.hasAttribute("onBottomClick")) {
            setOnBottomClick(
                loadOnEventAttribute(
                    instance,
                    element, InventoryClickEvent::class.java, "onBottomClick"
                )
            )
        }

        if (element.hasAttribute("onGlobalClick")) {
            setOnGlobalClick(
                loadOnEventAttribute(
                    instance,
                    element, InventoryClickEvent::class.java, "onGlobalClick"
                )
            )
        }

        if (element.hasAttribute("onOutsideClick")) {
            setOnOutsideClick(
                loadOnEventAttribute(
                    instance,
                    element, InventoryClickEvent::class.java, "onOutsideClick"
                )
            )
        }

        if (element.hasAttribute("onTopDrag")) {
            setOnTopDrag(
                loadOnEventAttribute(
                    instance,
                    element, InventoryDragEvent::class.java, "onTopDrag"
                )
            )
        }

        if (element.hasAttribute("onBottomDrag")) {
            setOnBottomDrag(
                loadOnEventAttribute(
                    instance,
                    element, InventoryDragEvent::class.java, "onBottomDrag"
                )
            )
        }

        if (element.hasAttribute("onGlobalDrag")) {
            setOnGlobalDrag(
                loadOnEventAttribute(
                    instance,
                    element, InventoryDragEvent::class.java, "onGlobalDrag"
                )
            )
        }

        if (element.hasAttribute("onClose")) {
            setOnClose(
                loadOnEventAttribute(
                    instance,
                    element, InventoryCloseEvent::class.java, "onClose"
                )
            )
        }

        if (element.hasAttribute("populate")) {
            invokeMethod(instance, element.getAttribute("populate"), this, Gui::class.java)
        }
    }

    /**
     * Set the consumer that should be called whenever this gui is clicked in.
     *
     * @param onTopClick the consumer that gets called
     */
    fun setOnTopClick(onTopClick: Consumer<InventoryClickEvent>?) {
        this.onTopClick = onTopClick
    }

    /**
     * Calls the consumer (if it's not null) that was specified using [.setOnTopClick],
     * so the consumer that should be called whenever this gui is clicked in.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    fun callOnTopClick(event: InventoryClickEvent) {
        callCallback(onTopClick, event, "onTopClick")
    }

    /**
     * Set the consumer that should be called whenever the inventory is clicked in.
     *
     * @param onBottomClick the consumer that gets called
     */
    fun setOnBottomClick(onBottomClick: Consumer<InventoryClickEvent>?) {
        this.onBottomClick = onBottomClick
    }

    /**
     * Calls the consumer (if it's not null) that was specified using [.setOnBottomClick],
     * so the consumer that should be called whenever the inventory is clicked in.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    fun callOnBottomClick(event: InventoryClickEvent) {
        callCallback(onBottomClick, event, "onBottomClick")
    }

    /**
     * Set the consumer that should be called whenever this gui or inventory is clicked in.
     *
     * @param onGlobalClick the consumer that gets called
     */
    fun setOnGlobalClick(onGlobalClick: Consumer<InventoryClickEvent>?) {
        this.onGlobalClick = onGlobalClick
    }

    /**
     * Calls the consumer (if it's not null) that was specified using [.setOnGlobalClick],
     * so the consumer that should be called whenever this gui or inventory is clicked in.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    fun callOnGlobalClick(event: InventoryClickEvent) {
        callCallback(onGlobalClick, event, "onGlobalClick")
    }

    /**
     * Set the consumer that should be called whenever a player clicks outside the gui.
     *
     * @param onOutsideClick the consumer that gets called
     * @since 0.5.7
     */
    fun setOnOutsideClick(onOutsideClick: Consumer<InventoryClickEvent>?) {
        this.onOutsideClick = onOutsideClick
    }

    /**
     * Calls the consumer (if it's not null) that was specified using [.setOnOutsideClick],
     * so the consumer that should be called whenever a player clicks outside the gui.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    fun callOnOutsideClick(event: InventoryClickEvent) {
        callCallback(onOutsideClick, event, "onOutsideClick")
    }

    /**
     * Set the consumer that should be called whenever this gui's top half is dragged in.
     *
     * @param onTopDrag the consumer that gets called
     * @since 0.9.0
     */
    fun setOnTopDrag(onTopDrag: Consumer<InventoryDragEvent>?) {
        this.onTopDrag = onTopDrag
    }

    /**
     * Calls the consumer (if it's not null) that was specified using [.setOnTopDrag],
     * so the consumer that should be called whenever this gui's top half is dragged in.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.9.0
     */
    fun callOnTopDrag(event: InventoryDragEvent) {
        callCallback(onTopDrag, event, "onTopDrag")
    }

    /**
     * Set the consumer that should be called whenever the inventory is dragged in.
     *
     * @param onBottomDrag the consumer that gets called
     * @since 0.9.0
     */
    fun setOnBottomDrag(onBottomDrag: Consumer<InventoryDragEvent>?) {
        this.onBottomDrag = onBottomDrag
    }

    /**
     * Calls the consumer (if it's not null) that was specified using [.setOnBottomDrag],
     * so the consumer that should be called whenever the inventory is dragged in.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.9.0
     */
    fun callOnBottomDrag(event: InventoryDragEvent) {
        callCallback(onBottomDrag, event, "onBottomDrag")
    }

    /**
     * Set the consumer that should be called whenever this gui or inventory is dragged in.
     *
     * @param onGlobalDrag the consumer that gets called
     * @since 0.9.0
     */
    fun setOnGlobalDrag(onGlobalDrag: Consumer<InventoryDragEvent>?) {
        this.onGlobalDrag = onGlobalDrag
    }

    /**
     * Calls the consumer (if it's not null) that was specified using [.setOnGlobalDrag],
     * so the consumer that should be called whenever this gui or inventory is dragged in.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    fun callOnGlobalDrag(event: InventoryDragEvent) {
        callCallback(onGlobalDrag, event, "onGlobalDrag")
    }

    /**
     * Set the consumer that should be called whenever this gui is closed.
     *
     * @param onClose the consumer that gets called
     */
    fun setOnClose(onClose: Consumer<InventoryCloseEvent>?) {
        this.onClose = onClose
    }

    /**
     * Calls the consumer (if it's not null) that was specified using [.setOnClose],
     * so the consumer that should be called whenever this gui is closed.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    fun callOnClose(event: InventoryCloseEvent) {
        callCallback(onClose, event, "onClose")
    }

    /**
     * Calls the specified consumer (if it's not null) with the specified parameter,
     * catching and logging all exceptions it might throw.
     *
     * @param callback the consumer to call if it isn't null
     * @param event the value the consumer should accept
     * @param callbackName the name of the action, used for logging
     * @param <T> the type of the value the consumer is accepting
    </T> */
    protected fun <T : InventoryEvent?> callCallback(
        callback: Consumer<in T?>?,
        event: T, callbackName: String
    ) {
        if (callback == null) {
            return
        }

        try {
            callback.accept(event)
        } catch (t: Throwable) {
            var message = "Exception while handling $callbackName"
            if (event is InventoryClickEvent) {
                val clickEvent = event as InventoryClickEvent
                message += ", slot=" + clickEvent.slot
            }

            plugin.logger.log(Level.SEVERE, message, t)
        }
    }

    /**
     * The parent gui will be shown to the specified [HumanEntity]. If no parent gui is set, then this method will
     * silently do nothing.
     *
     * @param humanEntity the human entity to redirect
     * @since 0.10.14
     */
    fun navigateToParent(humanEntity: HumanEntity) {
        if (this.parent == null) {
            return
        }

        parent!!.show(humanEntity)
    }

    /**
     * Sets the parent gui to the provided gui. This is the gui that a player will be navigated to once they close this
     * gui. The navigation will occur after the close event handler, set by [.setOnClose], is called. If
     * there was already a previous parent set, the provided gui will override the previous one.
     *
     * @param gui the new parent gui
     * @since 0.10.14
     */
    fun setParent(gui: Gui) {
        this.parent = gui
    }

    /**
     * Constructs a new gui with the provided plugin.
     *
     * @param plugin the plugin
     * @since 0.10.8
     */
    init {
        if (!hasRegisteredListeners) {
            Bukkit.getPluginManager().registerEvents(GuiListener(plugin), plugin)

            hasRegisteredListeners = true
        }
    }

    companion object {
        /**
         * The pane mapping which will allow users to register their own panes to be used in XML files
         */
        private val PANE_MAPPINGS: MutableMap<String, TriFunction<in Any, in Element, in Plugin, out Pane>> =
            HashMap()

        /**
         * The gui mappings which determine which gui type belongs to which identifier
         */
        private val GUI_MAPPINGS: MutableMap<String, TriFunction<in Any, in Element, in Plugin, out Gui>> =
            HashMap()

        /**
         * A map containing the relations between inventories and their respective gui. This is needed because Bukkit and
         * Spigot ignore inventory holders for beacons, brewing stands, dispensers, droppers, furnaces and hoppers. The
         * inventory holder for beacons is already being set properly via NMS, but this contains the other inventory types.
         */
        private val GUI_INVENTORIES: MutableMap<Inventory, Gui> = WeakHashMap()

        /**
         * Whether listeners have ben registered by some gui
         */
        private var hasRegisteredListeners = false

        /**
         * Gets a gui from the specified inventory. Only guis of type beacon, brewing stand, dispenser, dropper, furnace and
         * hopper can be retrieved.
         *
         * @param inventory the inventory to get the gui from
         * @return the gui or null if the inventory doesn't have an accompanying gui
         * @since 0.8.1
         */
        @Contract(pure = true)
        fun getGui(inventory: Inventory): Gui? {
            return GUI_INVENTORIES[inventory]
        }

        /**
         * Loads a Gui from a given input stream.
         *
         * @param instance the class instance for all reflection lookups
         * @param inputStream the file
         * @return the gui or null if the loading failed
         * @throws XMLLoadException if loading could not finish successfully, due to e.g., a malformed file
         * @see .load
         * @since 0.10.8
         */
        /**
         * Loads a Gui from a given input stream.
         * Returns null instead of throwing an exception in case of a failure.
         *
         * @param instance the class instance for all reflection lookups
         * @param inputStream the file
         * @return the gui or null if the loading failed
         * @throws XMLLoadException if loading could not finish successfully, due to e.g., a malformed file
         */
        @JvmOverloads
        fun load(
            instance: Any, inputStream: InputStream, plugin: Plugin = JavaPlugin.getProvidingPlugin(
                Gui::class.java
            )
        ): Gui? {
            try {
                val document =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
                val documentElement = document.documentElement

                documentElement.normalize()

                if (!documentElement.hasAttribute("type")) {
                    throw XMLLoadException("Type attribute must be specified when loading via Gui.load")
                }

                val type = documentElement.getAttribute("type")
                val mapping = GUI_MAPPINGS[type]
                    ?: throw XMLLoadException("Type attribute '$type' is invalid")

                return mapping.apply(instance, documentElement, plugin)
            } catch (e: SAXException) {
                e.printStackTrace()
                return null
            } catch (e: ParserConfigurationException) {
                e.printStackTrace()
                return null
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        /**
         * Registers a property that can be used inside an XML file to add additional new properties.
         *
         * @param attributeName the name of the property. This is the same name you'll be using to specify the property
         * type in the XML file.
         * @param function how the property should be processed. This converts the raw text input from the XML node value
         * into the correct object type.
         * @throws IllegalArgumentException when a property with this name is already registered.
         */
        fun registerProperty(attributeName: String, function: Function<String?, Any?>) {
            Pane.registerProperty(attributeName, function)
        }

        /**
         * Registers a name that can be used inside an XML file to add custom panes
         *
         * @param name the name of the pane to be used in the XML file
         * @param triFunction how the pane loading should be processed
         * @throws IllegalArgumentException when a pane with this name is already registered
         * @see .registerPane
         * @since 0.10.8
         */
        fun registerPane(
            name: String,
            triFunction: TriFunction<in Any, in Element, in Plugin, out Pane>
        ) {
            require(!PANE_MAPPINGS.containsKey(name)) { "pane name '$name' is already registered" }

            PANE_MAPPINGS[name] = triFunction
        }

        /**
         * Registers a name that can be used inside an XML file to add custom panes
         *
         * @param name the name of the pane to be used in the XML file
         * @param biFunction how the pane loading should be processed
         * @throws IllegalArgumentException when a pane with this name is already registered
         */
        fun registerPane(name: String, biFunction: BiFunction<Any?, Element?, Pane>) {
            registerPane(
                name
            ) { `object`: Any?, element: Element?, plugin: Plugin? ->
                biFunction.apply(
                    `object`,
                    element
                )
            }
        }

        /**
         * Registers a type that can be used inside an XML file to specify the gui type
         *
         * @param name the name of the type of gui to be used in an XML file
         * @param triFunction how the gui creation should be processed
         * @throws IllegalArgumentException when a gui type with this name is already registered
         * @since 0.10.8
         */
        fun registerGui(
            name: String,
            triFunction: TriFunction<in Any, in Element, in Plugin, out Gui>
        ) {
            require(!GUI_MAPPINGS.containsKey(name)) { "Gui name '$name' is already registered" }

            GUI_MAPPINGS[name] = triFunction
        }

        /**
         * Registers a type that can be used inside an XML file to specify the gui type
         *
         * @param name the name of the type of gui to be used in an XML file
         * @param biFunction how the gui creation should be processed
         * @throws IllegalArgumentException when a gui type with this name is already registered
         */
        @Deprecated(
            """this method is no longer used internally and has been superseded by
                  {@link #registerPane(String, TriFunction)}"""
        )
        fun registerGui(
            name: String,
            biFunction: BiFunction<in Any?, in Element?, out Gui>
        ) {
            registerGui(
                name
            ) { `object`: Any?, element: Element?, plugin: Plugin? ->
                biFunction.apply(
                    `object`,
                    element
                )
            }
        }

        /**
         * Loads a pane by the given instance and node
         *
         * @param instance the instance
         * @param node the node
         * @param plugin the plugin to load the pane with
         * @return the pane
         * @throws XMLLoadException if the name of the node does not correspond to a valid pane.
         * @since 0.10.8
         */
        /**
         * Loads a pane by the given instance and node
         *
         * @param instance the instance
         * @param node the node
         * @return the pane
         */
        @JvmOverloads
        fun loadPane(
            instance: Any, node: Node, plugin: Plugin = JavaPlugin.getProvidingPlugin(
                Gui::class.java
            )
        ): Pane {
            val name = node.nodeName
            val mapping = PANE_MAPPINGS[name]
                ?: throw XMLLoadException("Pane '$name' is not registered or does not exist")

            return mapping.apply(instance, node as Element, plugin)
        }

        init {
            registerPane("masonrypane",
                TriFunction<Any, Element, Plugin, Pane> { obj: Any?, instance: Element?, element: Plugin? ->
                    MasonryPane.load(
                        instance!!, element
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Pane>)
            registerPane("outlinepane",
                TriFunction<Any, Element, Plugin, Pane> { obj: Any?, instance: Element?, element: Plugin? ->
                    OutlinePane.load(
                        instance!!, element
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Pane>)
            registerPane("paginatedpane",
                TriFunction<Any, Element, Plugin, Pane> { obj: Any?, instance: Element?, element: Plugin? ->
                    PaginatedPane.load(
                        instance!!, element
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Pane>)
            registerPane("patternpane",
                TriFunction<Any, Element, Plugin, Pane> { obj: Any?, instance: Element?, element: Plugin? ->
                    PatternPane.load(
                        instance!!, element
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Pane>)
            registerPane("staticpane",
                TriFunction<Any, Element, Plugin, Pane> { obj: Any?, instance: Element?, element: Plugin? ->
                    StaticPane.load(
                        instance!!, element
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Pane>)

            registerPane("cyclebutton",
                TriFunction<Any, Element, Plugin, Pane> { obj: Any?, instance: Element?, element: Plugin? ->
                    CycleButton.load(
                        instance!!, element
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Pane>)
            registerPane("label",
                TriFunction<Any, Element, Plugin, Pane> { obj: Any?, instance: Element?, element: Plugin? ->
                    Label.load(
                        instance!!, element
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Pane>)
            registerPane(
                "pagingbuttons"
            ) { obj: Any?, instance: Element?, element: Plugin? ->
                PagingButtons.load(
                    instance!!, element
                )
            }
            registerPane("percentagebar",
                TriFunction<Any, Element, Plugin, Pane> { obj: Any?, instance: Element?, element: Plugin? ->
                    PercentageBar.load(
                        instance!!, element
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Pane>)
            registerPane("slider",
                TriFunction<Any, Element, Plugin, Pane> { obj: Any?, instance: Element?, element: Plugin? ->
                    Slider.load(
                        instance!!, element
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Pane>)
            registerPane("togglebutton",
                TriFunction<Any, Element, Plugin, Pane> { obj: Any?, instance: Element?, element: Plugin? ->
                    ToggleButton.load(
                        instance!!, element
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Pane>)

            registerGui("anvil",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    AnvilGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("barrel",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    BarrelGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("beacon",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    BeaconGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("blast-furnace",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    BlastFurnaceGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("brewing-stand",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    BrewingStandGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("cartography-table",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    CartographyTableGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("chest",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    ChestGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("crafting-table",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    CraftingTableGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("dispenser",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    DispenserGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("dropper",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    DropperGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("enchanting-table",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    EnchantingTableGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("ender-chest",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    EnderChestGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("furnace",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    FurnaceGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("grindstone",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    GrindstoneGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("hopper",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    HopperGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("merchant",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    MerchantGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("shulker-box",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    ShulkerBoxGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("smithing-table",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    SmithingTableGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("smoker",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    SmokerGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
            registerGui("stonecutter",
                TriFunction<Any, Element, Plugin, Gui> { instance: Any, element: Element, plugin: Plugin ->
                    StonecutterGui.Companion.load(
                        instance,
                        element,
                        plugin
                    )
                } as TriFunction<in Any, in Element, in Plugin, out Gui>)
        }
    }
}
