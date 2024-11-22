package com.github.stefvanschie.inventoryframework.pane

import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder.Companion.deserialize
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.exception.XMLReflectionException
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.Pane
import com.github.stefvanschie.inventoryframework.pane.util.Mask
import com.github.stefvanschie.inventoryframework.pane.util.Slot
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil.instance
import com.github.stefvanschie.inventoryframework.util.SkullUtil.setSkull
import com.github.stefvanschie.inventoryframework.util.UUIDTagType
import com.github.stefvanschie.inventoryframework.util.XMLUtil.invokeMethod
import com.github.stefvanschie.inventoryframework.util.XMLUtil.loadFieldAttribute
import com.github.stefvanschie.inventoryframework.util.XMLUtil.loadOnEventAttribute
import com.google.common.primitives.Primitives
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

/**
 * The base class for all panes.
 */
abstract class Pane {
    /**
     * The starting position of this pane, which is 0 by default
     */
    @Deprecated("")
    protected var x: Int = 0

    @Deprecated("")
    protected var y: Int = 0

    /**
     * The position of this pane, which is (0,0) by default
     */
    protected var slot: Slot = Slot.Companion.fromXY(0, 0)

    /**
     * Returns the length of this pane
     *
     * @return the length
     */
    /**
     * Set the length of this pane
     *
     * @param length the new length
     */
    /**
     * Length is horizontal, height is vertical
     */
    @get:Contract(pure = true)
    open var length: Int
    /**
     * Returns the height of this pane
     *
     * @return the height
     */
    /**
     * Set the height of this pane
     *
     * @param height the new height
     */
    @get:Contract(pure = true)
    open var height: Int

    /**
     * Returns the pane's visibility state
     *
     * @return the pane's visibility
     */
    /**
     * Sets whether this pane is visible or not
     *
     * @param visible the pane's visibility
     */
    /**
     * The visibility state of the pane
     */
    @get:Contract(pure = true)
    var isVisible: Boolean

    /**
     * Returns the priority of the pane
     *
     * @return the priority
     */
    /**
     * Sets the priority of this pane
     *
     * @param priority the priority
     */
    /**
     * The priority of the pane, determines when it will be rendered
     */
    @JvmField
    var priority: Priority

    /**
     * The consumer that will be called once a players clicks in this pane
     */
    protected var onClick: Consumer<InventoryClickEvent>? = null

    /**
     * Gets the [UUID] associated with this pane.
     *
     * @return the uuid
     * @since 0.7.1
     */
    /**
     * A unique identifier for panes to locate them by
     */
    @get:Contract(pure = true)
    var uUID: UUID
        protected set

    /**
     * Constructs a new default pane
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     * @since 0.10.8
     */
    /**
     * Constructs a new default pane
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     */
    protected constructor(
        slot: Slot,
        length: Int,
        height: Int,
        priority: Priority = Priority.NORMAL
    ) {
        require(!(length == 0 || height == 0)) { "Length and height of pane must be greater than zero" }

        setSlot(slot)

        this.length = length
        this.height = height

        this.priority = priority
        this.isVisible = true

        this.uUID = UUID.randomUUID()
    }

    /**
     * Constructs a new default pane
     *
     * @param x the upper left x coordinate of the pane
     * @param y the upper left y coordinate of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     */
    /**
     * Constructs a new default pane
     *
     * @param x the upper left x coordinate of the pane
     * @param y the upper left y coordinate of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     */
    protected constructor(
        x: Int,
        y: Int,
        length: Int,
        height: Int,
        priority: Priority = Priority.NORMAL
    ) : this(
        Slot.Companion.fromXY(x, y), length, height, priority
    )

    /**
     * Constructs a new default pane, with no position
     *
     * @param length the length of the pane
     * @param height the height of the pane
     */
    protected constructor(length: Int, height: Int) {
        require(!(length == 0 || height == 0)) { "Length and height of pane must be greater than zero" }

        this.length = length
        this.height = height

        this.priority = Priority.NORMAL
        this.isVisible = true

        this.uUID = UUID.randomUUID()
    }

    /**
     * Makes a copy of this pane and returns it. This makes a deep copy of the pane. This entails that the underlying
     * panes and/or items will be copied as well. The returned pane will never be reference equal to the current pane.
     *
     * @return a copy of this pane
     * @since 0.6.2
     */
    @Contract(pure = true)
    open fun copy(): Pane {
        throw UnsupportedOperationException("The implementing pane hasn't overridden the copy method")
    }

    /**
     * Sets the slot of this pane.
     *
     * @param slot the slot
     * @since 0.10.8
     */
    fun setSlot(slot: Slot) {
        this.slot = slot

        //the length should be the length of the parent container, but we don't have that, so just use one
        this.x = slot.getX(1)
        this.y = slot.getY(1)
    }

    /**
     * Set the x coordinate of this pane
     *
     * @param x the new x coordinate
     */
    fun setX(x: Int) {
        this.x = x

        this.slot = Slot.Companion.fromXY(x, getY())
    }

    /**
     * Set the y coordinate of this pane
     *
     * @param y the new y coordinate
     */
    fun setY(y: Int) {
        this.y = y

        this.slot = Slot.Companion.fromXY(getX(), y)
    }

    /**
     * Gets the slot of the position of this pane
     *
     * @return the slot
     * @since 0.10.8
     */
    @Contract(pure = true)
    fun getSlot(): Slot {
        return this.slot
    }

    /**
     * Gets the x coordinate of this pane
     *
     * @return the x coordinate
     */
    @Contract(pure = true)
    @Deprecated(
        """when the slot was specified as an indexed position, this may return the wrong value;
                  {@link #getSlot()} should be used instead"""
    )
    fun getX(): Int {
        return x
    }

    /**
     * Gets the y coordinate of this pane
     *
     * @return the y coordinate
     */
    @Contract(pure = true)
    @Deprecated(
        """when the slot was specified as an indexed position, this may return the wrong value;
                  {@link #getSlot()} should be used instead"""
    )
    fun getY(): Int {
        return y
    }

    /**
     * Has to set all the items in the right spot inside the inventory
     *
     * @param inventoryComponent the inventory component in which the items should be displayed
     * @param paneOffsetX the pane's offset on the x axis
     * @param paneOffsetY the pane's offset on the y axis
     * @param maxLength the maximum length of the pane
     * @param maxHeight the maximum height of the pane
     */
    abstract fun display(
        inventoryComponent: InventoryComponent, paneOffsetX: Int, paneOffsetY: Int,
        maxLength: Int, maxHeight: Int
    )

    /**
     * Called whenever there is being clicked on this pane
     *
     * @param gui the gui in which was clicked
     * @param inventoryComponent the inventory component in which this pane resides
     * @param event the event that occurred while clicking on this item
     * @param slot the slot that was clicked in
     * @param paneOffsetX the pane's offset on the x axis
     * @param paneOffsetY the pane's offset on the y axis
     * @param maxLength the maximum length of the pane
     * @param maxHeight the maximum height of the pane
     * @return whether the item was found or not
     */
    abstract fun click(
        gui: Gui, inventoryComponent: InventoryComponent,
        event: InventoryClickEvent, slot: Int, paneOffsetX: Int, paneOffsetY: Int,
        maxLength: Int, maxHeight: Int
    ): Boolean

    @JvmField
    @get:Contract(pure = true)
    abstract val items: Collection<GuiItem>

    @get:Contract(pure = true)
    abstract val panes: Collection<Pane>

    /**
     * Clears the entire pane of any items/panes. Underlying panes will not be cleared.
     *
     * @since 0.3.2
     */
    abstract fun clear()

    /**
     * Set the consumer that should be called whenever this pane is clicked in.
     *
     * @param onClick the consumer that gets called
     * @since 0.4.0
     */
    fun setOnClick(onClick: Consumer<InventoryClickEvent>?) {
        this.onClick = onClick
    }

    /**
     * Calls the consumer (if it's not null) that was specified using [.setOnClick],
     * so the consumer that should be called whenever this pane is clicked in.
     *
     * @param event the event to handle
     * @since 0.6.0
     */
    protected fun callOnClick(event: InventoryClickEvent) {
        if (onClick == null) {
            return
        }


        try {
            onClick!!.accept(event)
        } catch (t: Throwable) {
            throw RuntimeException(
                ("Exception while handling click event in inventory '"
                        + instance.getTitle(event.getView()) + "', slot=" + event.getSlot() +
                        ", for " + javaClass.getSimpleName() + ", x=" + getX() + ", y=" + getY()
                        + ", length=" + length + ", height=" + height),
                t
            )
        }
    }

    /**
     * An enum representing the rendering priorities for the panes. Uses a similar system to Bukkit's
     * [org.bukkit.event.EventPriority] system
     */
    enum class Priority {
        /**
         * The lowest priority, will be rendered first
         */
        LOWEST {
            override fun isLessThan(priority: Priority): Boolean {
                return priority !== this
            }
        },

        /**
         * A low priority, lower than default
         */
        LOW {
            override fun isLessThan(priority: Priority): Boolean {
                return priority !== this && priority !== LOWEST
            }
        },

        /**
         * A normal priority, the default
         */
        NORMAL {
            override fun isLessThan(priority: Priority): Boolean {
                return priority !== this && priority !== LOW && priority !== LOWEST
            }
        },

        /**
         * A higher priority, higher than default
         */
        HIGH {
            override fun isLessThan(priority: Priority): Boolean {
                return priority === HIGHEST || priority === MONITOR
            }
        },

        /**
         * The highest priority for production use
         */
        HIGHEST {
            override fun isLessThan(priority: Priority): Boolean {
                return priority === MONITOR
            }
        },

        /**
         * The highest priority, will always be called last, should not be used for production code
         */
        MONITOR {
            override fun isLessThan(priority: Priority): Boolean {
                return false
            }
        };

        /**
         * Whether this priority is less than the priority specified.
         *
         * @param priority the priority to compare against
         * @return true if this priority is less than the specified priority, false otherwise
         * @since 0.8.0
         */
        @Contract(pure = true)
        abstract fun isLessThan(priority: Priority): Boolean

        /**
         * Whether this priority is greater than the priority specified.
         *
         * @param priority the priority to compare against
         * @return true if this priority is greater than the specified priority, false otherwise
         * @since 0.8.0
         */
        @Contract(pure = true)
        fun isGreaterThan(priority: Priority): Boolean {
            return !isLessThan(priority) && this !== priority
        }
    }

    companion object {
        /**
         * A map containing the mappings for properties for items
         */
        private val PROPERTY_MAPPINGS: MutableMap<String, Function<String, Any>> = HashMap()

        /**
         * Loads an item from an instance and an element
         *
         * @param instance the instance
         * @param element the element
         * @param plugin the plugin that will be the owner of the created item
         * @return the gui item
         * @see .loadItem
         * @since 0.10.8
         */
        @Contract(pure = true)
        fun loadItem(instance: Any, element: Element, plugin: Plugin): GuiItem {
            val id: String = element.getAttribute("id")
            val material: Material? = Material.matchMaterial(id.uppercase(Locale.getDefault()))

            if (material == null) {
                throw XMLLoadException("Can't find material for '" + id + "'")
            }

            val hasAmount: Boolean = element.hasAttribute("amount")
            val hasDamage: Boolean = element.hasAttribute("damage")
            val amount: Int = if (hasAmount) element.getAttribute("amount").toInt() else 1
            val damage: Short = if (hasDamage) element.getAttribute("damage").toShort() else 0

            val itemStack: ItemStack = ItemStack(material, amount, damage)

            val properties: MutableList<Any> = ArrayList()

            if (element.hasChildNodes()) {
                val childNodes: NodeList = element.getChildNodes()

                for (i in 0 until childNodes.getLength()) {
                    val item: Node = childNodes.item(i)

                    if (item.getNodeType() != Node.ELEMENT_NODE) continue

                    val elementItem: Element = item as Element

                    val nodeName: String = item.getNodeName()

                    if (nodeName == "properties" || nodeName == "lore" || nodeName == "enchantments") {
                        val innerChildNodes: NodeList = item.getChildNodes()

                        for (j in 0 until innerChildNodes.getLength()) {
                            val innerNode: Node = innerChildNodes.item(j)

                            if (innerNode.getNodeType() != Node.ELEMENT_NODE) continue

                            val innerElementChild: Element = innerNode as Element
                            val itemMeta: ItemMeta = Objects.requireNonNull(itemStack.getItemMeta())

                            when (nodeName) {
                                "properties" -> {
                                    if (innerNode.getNodeName() != "property") continue

                                    val propertyType: String =
                                        if (innerElementChild.hasAttribute("type"))
                                            innerElementChild.getAttribute("type")
                                        else
                                            "string"

                                    properties.add(
                                        PROPERTY_MAPPINGS.get(propertyType)!!.apply(
                                            innerElementChild
                                                .getTextContent()
                                        )
                                    )
                                }

                                "lore" -> {
                                    if (innerNode.getNodeName() != "line") continue

                                    deserialize(innerNode.getTextContent())
                                        .asItemLoreAtEnd(itemMeta)
                                    itemStack.setItemMeta(itemMeta)
                                }

                                "enchantments" -> {
                                    if (innerNode.getNodeName() != "enchantment") continue

                                    val enchantment: Enchantment? = Enchantment.getByKey(
                                        NamespacedKey.minecraft(
                                            innerElementChild.getAttribute("id")
                                                .uppercase(Locale.getDefault())
                                        )
                                    )

                                    if (enchantment == null) {
                                        throw XMLLoadException("Enchantment cannot be found")
                                    }

                                    val level: Int = innerElementChild.getAttribute("level").toInt()

                                    itemMeta.addEnchant(enchantment, level, true)
                                    itemStack.setItemMeta(itemMeta)
                                }
                            }
                        }
                    } else if (nodeName == "displayname") {
                        val itemMeta: ItemMeta = Objects.requireNonNull(itemStack.getItemMeta())

                        deserialize(item.getTextContent())
                            .asItemDisplayName(itemMeta)

                        itemStack.setItemMeta(itemMeta)
                    } else if (nodeName == "modeldata") {
                        val itemMeta: ItemMeta = Objects.requireNonNull(itemStack.getItemMeta())

                        itemMeta.setCustomModelData(item.getTextContent().toInt())

                        itemStack.setItemMeta(itemMeta)
                    } else if (nodeName == "skull" && itemStack.getItemMeta() is SkullMeta) {
                        val skullMeta: SkullMeta? = itemStack.getItemMeta() as SkullMeta?

                        if (elementItem.hasAttribute("owner"))
                            skullMeta!!.setOwner(elementItem.getAttribute("owner"))
                        else if (elementItem.hasAttribute("id")) {
                            setSkull(skullMeta!!, elementItem.getAttribute("id"))
                        }

                        itemStack.setItemMeta(skullMeta)
                    }
                }
            }

            var action: Consumer<InventoryClickEvent>? = null

            if (element.hasAttribute("onClick")) {
                val methodName: String = element.getAttribute("onClick")
                for (method: Method in instance.javaClass.getMethods()) {
                    if (method.getName() != methodName) continue

                    val parameterCount: Int = method.getParameterCount()
                    val parameterTypes: Array<Class<*>> = method.getParameterTypes()

                    if (parameterCount == 0) action =
                        Consumer { event: InventoryClickEvent? ->
                            try {
                                //because reflection with lambdas is stupid
                                method.setAccessible(true)
                                method.invoke(instance)
                            } catch (exception: IllegalAccessException) {
                                throw XMLReflectionException(exception)
                            } catch (exception: InvocationTargetException) {
                                throw XMLReflectionException(exception)
                            }
                        }
                    else if (parameterTypes.get(0)
                            .isAssignableFrom(InventoryClickEvent::class.java)
                    ) {
                        if (parameterCount == 1) action =
                            Consumer { event: InventoryClickEvent? ->
                                try {
                                    //because reflection with lambdas is stupid
                                    method.setAccessible(true)
                                    method.invoke(instance, event)
                                } catch (exception: IllegalAccessException) {
                                    throw XMLReflectionException(exception)
                                } catch (exception: InvocationTargetException) {
                                    throw XMLReflectionException(exception)
                                }
                            }
                        else if (parameterCount == properties.size + 1) {
                            var correct: Boolean = true

                            for (i in properties.indices) {
                                val attribute: Any = properties.get(i)

                                if (!(parameterTypes.get(1 + i).isPrimitive() &&
                                            parameterTypes.get(1 + i)
                                                .isAssignableFrom(Primitives.unwrap(attribute.javaClass))) &&
                                    !parameterTypes.get(1 + i).isAssignableFrom(attribute.javaClass)
                                ) correct = false
                            }

                            if (correct) {
                                action =
                                    Consumer { event: InventoryClickEvent ->
                                        try {
                                            //don't ask me why we need to do this, just roll with it (actually I do know why, but it's stupid)
                                            properties.add(0, event)

                                            //because reflection with lambdas is stupid
                                            method.setAccessible(true)
                                            method.invoke(instance, *properties.toTypedArray<Any>())

                                            //since we'll append the event to the list next time again, we need to remove it here again
                                            properties.removeAt(0)
                                        } catch (exception: IllegalAccessException) {
                                            throw XMLReflectionException(exception)
                                        } catch (exception: InvocationTargetException) {
                                            throw XMLReflectionException(exception)
                                        }
                                    }
                            }
                        }
                    }

                    break
                }
            }

            val item: GuiItem = GuiItem(itemStack, action, plugin)

            if (element.hasAttribute("field")) loadFieldAttribute(instance, element, item)

            if (element.hasAttribute("populate")) {
                invokeMethod(instance, element.getAttribute("populate"), item, GuiItem::class.java)
            }

            item.setProperties(properties)

            return item
        }

        /**
         * Loads an item from an instance and an element
         *
         * @param instance the instance
         * @param element the element
         * @return the gui item
         */
        @JvmStatic
        @Contract(pure = true)
        fun loadItem(instance: Any, element: Element): GuiItem {
            return loadItem(
                instance, element, JavaPlugin.getProvidingPlugin(
                    Pane::class.java
                )
            )
        }

        fun load(pane: Pane, instance: Any, element: Element) {
            pane.setSlot(Slot.Companion.deserialize(element))

            if (element.hasAttribute("priority")) pane.priority =
                Priority.valueOf(
                    element.getAttribute("priority").uppercase(Locale.getDefault())
                )

            if (element.hasAttribute("visible")) pane.isVisible =
                element.getAttribute("visible").toBoolean()

            if (element.hasAttribute("field")) loadFieldAttribute(instance, element, pane)

            if (element.hasAttribute("onClick")) pane.setOnClick(
                loadOnEventAttribute(
                    instance, element,
                    InventoryClickEvent::class.java, "onClick"
                )
            )

            if (element.hasAttribute("populate")) {
                val attribute: String = element.getAttribute("populate")
                for (method: Method in instance.javaClass.getMethods()) {
                    if (method.getName() != attribute) continue

                    try {
                        method.setAccessible(true)
                        method.invoke(instance, pane)
                    } catch (exception: IllegalAccessException) {
                        throw XMLLoadException(exception)
                    } catch (exception: InvocationTargetException) {
                        throw XMLLoadException(exception)
                    }
                }
            }
        }

        /**
         * Checks whether a [GuiItem] is the same item as the given [ItemStack]. The item will be compared using
         * internal data. When the item does not have this data, this method will return false. If the item does have such
         * data, but its value does not match, false is also returned. This method will not mutate any of the provided
         * arguments.
         *
         * @param guiItem the gui item to check
         * @param item the item which the gui item should be checked against
         * @return true if the [GuiItem] matches the [ItemStack], false otherwise
         * @since 0.10.14
         */
        @Contract(pure = true)
        protected fun matchesItem(guiItem: GuiItem, item: ItemStack): Boolean {
            val meta: ItemMeta? = item.getItemMeta()

            if (meta == null) {
                return false
            }

            return guiItem.getUUID() == meta.getPersistentDataContainer()
                .get(guiItem.getKey(), UUIDTagType.INSTANCE)
        }

        /**
         * Finds a type of [GuiItem] from the provided collection of items based on the provided [ItemStack].
         * The items will be compared using internal data. When the item does not have this data, this method will return
         * null. If the item does have such data, but its value cannot be found in the provided list, null is also returned.
         * This method will not mutate any of the provided arguments, nor any of the contents inside of the arguments. The
         * provided collection may be unmodifiable if preferred. This method will always return a type of [GuiItem]
         * that is in the provided collection - when the returned result is not null - such that an element E inside the
         * provided collection reference equals the returned type of [GuiItem].
         *
         * @param items a collection of items in which will be searched
         * @param item the item for which an [GuiItem] should be found
         * @param <T> a type of GuiItem, which will be used in the provided collection and as return type
         * @return the found type of [GuiItem] or null if none was found
         * @since 0.5.14
        </T> */
        @Contract(pure = true)
        protected fun <T : GuiItem?> findMatchingItem(items: Collection<T>, item: ItemStack): T? {
            for (guiItem: T in items) {
                if (matchesItem(guiItem, item)) {
                    return guiItem
                }
            }

            return null
        }

        /**
         * Creates a pane which displays as a border around the outside of the pane consisting of the provided item. The
         * slot, length and height parameters are used for the respective properties of the pane. If either the length or
         * height is negative an [IllegalArgumentException] will be thrown.
         *
         * @param slot the slot of the pane
         * @param length the length of the pane
         * @param height the height of the pane
         * @param item the item of which the border is made
         * @return the created pane which displays a border
         * @since 0.10.8
         * @throws IllegalArgumentException if length or height is negative
         */
        @Contract(pure = true)
        fun createBorder(slot: Slot, length: Int, height: Int, item: GuiItem): Pane {
            require(length >= 0) { "Length should be non-negative" }

            require(height >= 0) { "Height should be non-negative" }

            val mask: Array<String?> = arrayOfNulls(height)

            if (height > 0) {
                mask.get(0) = createLine(length)
            }

            if (height > 1) {
                mask.get(height - 1) = createLine(length)
            }

            for (yIndex in 1 until height - 1) {
                val builder: StringBuilder = StringBuilder("1")

                for (i in 0 until length - 2) {
                    builder.append('0')
                }

                mask.get(yIndex) = builder.append('1').toString()
            }

            val pane: OutlinePane = OutlinePane(slot, length, height)
            pane.applyMask(Mask(*mask))
            pane.addItem(item)
            pane.setRepeat(true)

            return pane
        }

        /**
         * Creates a pane which displays as a border around the outside of the pane consisting of the provided item. The x,
         * y, length and height parameters are used for the respective properties of the pane. If either the length or
         * height is negative an [IllegalArgumentException] will be thrown.
         *
         * @param x the x coordinate of the pane
         * @param y the y coordinate of the pane
         * @param length the length of the pane
         * @param height the height of the pane
         * @param item the item of which the border is made
         * @return the created pane which displays a border
         * @since 0.10.7
         * @throws IllegalArgumentException if length or height is negative
         */
        @Contract(pure = true)
        fun createBorder(x: Int, y: Int, length: Int, height: Int, item: GuiItem): Pane {
            return createBorder(Slot.Companion.fromXY(x, y), length, height, item)
        }

        /**
         * Registers a property that can be used inside an XML file to add additional new properties.
         * The use of [Gui.registerProperty] is preferred over this method.
         *
         * @param attributeName the name of the property. This is the same name you'll be using to specify the property
         * type in the XML file.
         * @param function how the property should be processed. This converts the raw text input from the XML node value
         * into the correct object type.
         * @throws IllegalArgumentException when a property with this name is already registered.
         */
        fun registerProperty(attributeName: String, function: Function<String, Any>) {
            require(!PROPERTY_MAPPINGS.containsKey(attributeName)) { "property '" + attributeName + "' is already registered" }

            PROPERTY_MAPPINGS.put(attributeName, function)
        }

        /**
         * Creates a string containing the character '1' repeated length amount of times. If the provided length is negative
         * an [IllegalArgumentException] will be thrown.
         *
         * @param length the length of the string
         * @return the string containing '1's
         * @since 0.10.7
         * @throws IllegalArgumentException if length is negative
         */
        @Contract(pure = true)
        private fun createLine(length: Int): String {
            require(length >= 0) { "Length should be non-negative" }

            val builder: StringBuilder = StringBuilder()

            for (i in 0 until length) {
                builder.append('1')
            }

            return builder.toString()
        }

        init {
            PROPERTY_MAPPINGS.put("boolean",
                Function { s: String -> s.toBoolean() })
            PROPERTY_MAPPINGS.put("byte",
                Function { s: String -> s.toByte() })
            PROPERTY_MAPPINGS.put("character",
                Function { value: String -> value.get(0) })
            PROPERTY_MAPPINGS.put("double",
                Function { s: String -> s.toDouble() })
            PROPERTY_MAPPINGS.put("float",
                Function { s: String -> s.toFloat() })
            PROPERTY_MAPPINGS.put("integer",
                Function { s: String -> s.toInt() })
            PROPERTY_MAPPINGS.put("long",
                Function { s: String -> s.toLong() })
            PROPERTY_MAPPINGS.put("short",
                Function { s: String -> s.toShort() })
            PROPERTY_MAPPINGS.put("string",
                Function { value: String -> value })
        }
    }
}
