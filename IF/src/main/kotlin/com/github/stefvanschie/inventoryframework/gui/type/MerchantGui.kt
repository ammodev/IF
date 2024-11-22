package com.github.stefvanschie.inventoryframework.gui.type

import com.github.stefvanschie.inventoryframework.HumanEntityCache
import com.github.stefvanschie.inventoryframework.abstraction.MerchantInventory
import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder.Companion.of
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder
import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.MerchantGui
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui
import com.github.stefvanschie.inventoryframework.pane.Pane.Companion.loadItem
import com.github.stefvanschie.inventoryframework.util.InventoryViewUtil.instance
import com.github.stefvanschie.inventoryframework.util.XMLUtil.loadOnEventAttribute
import com.github.stefvanschie.inventoryframework.util.version.Version.Companion.version
import com.github.stefvanschie.inventoryframework.util.version.VersionMatcher.newMerchantInventory
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.TradeSelectEvent
import org.bukkit.inventory.*
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import java.util.AbstractMap.SimpleImmutableEntry
import java.util.function.Consumer
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * Represents a gui in the form of a merchant.
 *
 * @since 0.10.0
 */
class MerchantGui @JvmOverloads constructor(
    title: TextHolder, plugin: Plugin = JavaPlugin.getProvidingPlugin(
        MerchantGui::class.java
    )
) : NamedGui(title, plugin) {
    /**
     * The consumer that will be called once a players selects a trade listed
     * on the left side of the gui
     */
    private var onTradeSelect: Consumer<in TradeSelectEvent?>? = null

    /**
     * Gets the inventory component representing the input
     *
     * @return the input component
     * @since 0.10.0
     */
    /**
     * Represents the inventory component for the input
     */
    @get:Contract(pure = true)
    var inputComponent: InventoryComponent = InventoryComponent(2, 1)
        private set

    /**
     * Gets the inventory component representing the player inventory
     *
     * @return the player inventory component
     * @since 0.10.0
     */
    /**
     * Represents the inventory component for the player inventory
     */
    @get:Contract(pure = true)
    var playerInventoryComponent: InventoryComponent = InventoryComponent(9, 4)
        private set

    /**
     * The merchant holding the trades and inventory
     */
    private var merchant: Merchant? = null

    /**
     * The human entities viewing this gui
     */
    override val viewers: MutableList<HumanEntity> = ArrayList()

    /**
     * The trades of this merchant with their price differences. The differences are the difference between the new
     * price and the original price.
     */
    private val trades: MutableList<Map.Entry<MerchantRecipe, Int>> = ArrayList()

    /**
     * The experience of this merchant. Values below zero indicate that the experience should be hidden.
     */
    private var experience: Int = -1

    /**
     * The level of this merchant. A value of zero indicates this villager doesn't have a level.
     */
    private var level: Int = 0

    /**
     * The internal merchant inventory
     */
    private val merchantInventory: MerchantInventory = newMerchantInventory(
        version
    )

    /**
     * Creates a merchant gui with the given title.
     *
     * @param title the title
     * @since 0.10.0
     */
    constructor(title: String) : this(of(title))

    /**
     * Constructs a new merchant gui for the given `plugin`.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .MerchantGui
     * @since 0.10.8
     */
    constructor(title: String, plugin: Plugin) : this(of(title), plugin)

    /**
     * Constructs a new merchant gui for the given `plugin`.
     *
     * @param title the title/name of this gui.
     * @param plugin the owning plugin of this gui
     * @see .MerchantGui
     * @since 0.10.8
     */
    /**
     * Creates a merchant gui with the given title.
     *
     * @param title the title
     * @since 0.10.0
     */
    init {
        this.merchant = getTitleHolder().asMerchantTitle()
    }

    /**
     * Set the consumer that should be called whenever a trade is selected
     * in this gui.
     *
     * @param onTradeSelect the consumer that gets called
     */
    fun setOnTradeSelect(onTradeSelect: Consumer<in TradeSelectEvent?>?) {
        this.onTradeSelect = onTradeSelect
    }

    /**
     * Calls the consumer (if it's not null) that was specified using [.setOnTradeSelect],
     * so the consumer that should be called whenever a trade is selected in this gui.
     * Catches and logs all exceptions the consumer might throw.
     *
     * @param event the event to handle
     */
    fun callOnTradeSelect(event: TradeSelectEvent) {
        callCallback(onTradeSelect, event, "onTradeSelect")
    }

    override fun initializeOrThrow(instance: Any, element: Element) {
        super.initializeOrThrow(instance, element)

        if (element.hasAttribute("onTradeSelect")) {
            setOnTradeSelect(
                loadOnEventAttribute<TradeSelectEvent?>(
                    instance,
                    element, TradeSelectEvent::class.java, "onTradeSelect"
                )
            )
        }
    }

    override fun show(humanEntity: HumanEntity) {
        require(humanEntity is Player) { "Merchants can only be opened by players" }

        if (isDirty()) {
            this.merchant = getTitleHolder().asMerchantTitle()
            markChanges()
        }

        val view: InventoryView? = humanEntity.openMerchant(merchant!!, true)

        checkNotNull(view) { "Merchant could not be opened" }

        val inventory: Inventory = instance.getTopInventory(view)

        addInventory(inventory, this)

        inventory.clear()

        inputComponent.display(inventory, 0)
        playerInventoryComponent.display()

        if (playerInventoryComponent.hasItem()) {
            val humanEntityCache: HumanEntityCache = getHumanEntityCache()

            if (!humanEntityCache.contains(humanEntity)) {
                humanEntityCache.storeAndClear(humanEntity)
            }

            playerInventoryComponent.placeItems(humanEntity.getInventory(), 0)
        }

        viewers.add(humanEntity)

        val player: Player = humanEntity

        if (this.experience >= 0 || this.level > 0) {
            merchantInventory.sendMerchantOffers(player, this.trades, this.level, this.experience)

            return
        }

        val discount: Boolean = false

        for (trade: Map.Entry<MerchantRecipe, Int> in this.trades) {
            if (trade.value != 0) {
                merchantInventory.sendMerchantOffers(
                    player,
                    this.trades,
                    this.level,
                    this.experience
                )

                break
            }
        }
    }

    override fun copy(): Gui {
        val gui: MerchantGui = MerchantGui(getTitleHolder(), super.plugin)

        gui.inputComponent = inputComponent.copy()
        gui.playerInventoryComponent = playerInventoryComponent.copy()

        gui.experience = experience
        gui.level = level

        for (trade: Map.Entry<MerchantRecipe, Int> in trades) {
            val originalRecipe: MerchantRecipe = trade.key

            val result: ItemStack = originalRecipe.getResult().clone()
            val uses: Int = originalRecipe.getUses()
            val maxUses: Int = originalRecipe.getMaxUses()
            val experienceReward: Boolean = originalRecipe.hasExperienceReward()
            val villagerExperience: Int = originalRecipe.getVillagerExperience()
            val priceMultiplier: Float = originalRecipe.getPriceMultiplier()

            val recipe: MerchantRecipe = MerchantRecipe(
                result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier
            )

            for (ingredient: ItemStack in originalRecipe.getIngredients()) {
                recipe.addIngredient(ingredient.clone())
            }

            gui.trades.add(SimpleImmutableEntry(recipe, trade.value))
        }

        gui.setOnTopClick(this.onTopClick)
        gui.setOnBottomClick(this.onBottomClick)
        gui.setOnGlobalClick(this.onGlobalClick)
        gui.setOnOutsideClick(this.onOutsideClick)
        gui.setOnTradeSelect(this.onTradeSelect)
        gui.setOnClose(this.onClose)

        return gui
    }

    override fun click(event: InventoryClickEvent) {
        val rawSlot: Int = event.getRawSlot()

        if (rawSlot >= 0 && rawSlot <= 1) {
            inputComponent.click(this, event, rawSlot)
        } else if (rawSlot != 2) {
            playerInventoryComponent.click(this, event, rawSlot - 3)
        }
    }

    /**
     * Adds a trade to this gui. The specified discount is the difference between the old price and the new price. For
     * example, if a price was decreased from five to two, the discount would be three.
     *
     * @param recipe the recipe to add
     * @param discount the discount
     * @since 0.10.1
     */
    /**
     * Adds a trade to this gui. This will not set a discount on the trade. For specifiying discounts, see
     * [.addTrade].
     *
     * @param recipe the recipe to add
     * @since 0.10.0
     */
    @JvmOverloads
    fun addTrade(recipe: MerchantRecipe, discount: Int = 0) {
        trades.add(SimpleImmutableEntry(recipe, -discount))

        val recipes: MutableList<MerchantRecipe> = ArrayList(
            merchant!!.getRecipes()
        )

        recipes.add(recipe)

        merchant!!.setRecipes(recipes)
    }

    /**
     * Sets the experience of this merchant gui. Setting the experience will make the experience bar visible, even if
     * the amount of experience is zero. Note that if the level of this merchant gui has not been set via
     * [.setLevel] that the experience will always show as zero even when set to something else. Experience
     * must be greater than or equal to zero. Attempting to set the experience to below zero will throw an
     * [IllegalArgumentException].
     *
     * @param experience the experience to set
     * @since 0.10.1
     * @throws IllegalArgumentException when the experience is below zero
     */
    fun setExperience(experience: Int) {
        require(experience >= 0) { "Experience must be greater than or equal to zero" }

        this.experience = experience
    }

    /**
     * Sets the level of this merchant gui. This is a value between one and five and will visibly change the gui by
     * appending the level of the villager to the title. These are displayed as "Novice", "Apprentice", "Journeyman",
     * "Expert" and "Master" respectively (when the player's locale is set to English). When an argument is supplied
     * that is not within one and five, an [IllegalArgumentException] will be thrown.
     *
     * @param level the numeric level
     * @since 0.10.1
     * @throws IllegalArgumentException when the level is not between one and five
     */
    fun setLevel(level: Int) {
        require(!(level < 0 || level > 5)) { "Level must be between one and five" }

        this.level = level
    }

    /**
     * Handles a human entity closing this gui.
     *
     * @param humanEntity the human entity who's closing this gui
     * @since 0.10.0
     */
    fun handleClose(humanEntity: HumanEntity) {
        viewers.remove(humanEntity)
    }

    override val isPlayerInventoryUsed: Boolean
        get() {
            return playerInventoryComponent.hasItem()
        }

    @get:Contract(pure = true)
    override val viewerCount: Int
        get() {
            return viewers.size
        }

    @Contract(pure = true)
    override fun getViewers(): List<HumanEntity> {
        return ArrayList(this.viewers)
    }

    companion object {
        /**
         * Loads a merchant gui from an XML file.
         *
         * @param instance the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @param plugin the plugin that will be the owner of the created gui
         * @return the loaded merchant gui
         * @see .load
         * @since 0.10.8
         */
        @Contract(pure = true)
        fun load(instance: Any, inputStream: InputStream, plugin: Plugin): MerchantGui? {
            try {
                val document: Document =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
                val documentElement: Element = document.getDocumentElement()

                documentElement.normalize()

                return load(instance, documentElement, plugin)
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
         * Loads a merchant gui from the specified element, applying code references to the provided instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element the element to load the gui from
         * @param plugin the plugin that will be the owner of the created gui
         * @return the loaded merchant gui
         * @see .load
         * @since 0.10.8
         */
        @Contract(pure = true)
        fun load(instance: Any, element: Element, plugin: Plugin): MerchantGui {
            if (!element.hasAttribute("title")) {
                throw XMLLoadException("Provided XML element's gui tag doesn't have the mandatory title attribute set")
            }

            val merchantGui: MerchantGui = MerchantGui(element.getAttribute("title"), plugin)
            merchantGui.initializeOrThrow(instance, element)

            if (element.hasAttribute("populate")) {
                return merchantGui
            }

            val childNodes: NodeList = element.getChildNodes()

            for (index in 0 until childNodes.getLength()) {
                val item: Node = childNodes.item(index)

                if (item.getNodeType() != Node.ELEMENT_NODE) {
                    continue
                }

                val nestedElement: Element = item as Element
                val tagName: String = nestedElement.getTagName()

                if (tagName.equals("component", ignoreCase = true)) {
                    if (!nestedElement.hasAttribute("name")) {
                        throw XMLLoadException("Component tag does not have a name specified")
                    }

                    var component: InventoryComponent

                    when (nestedElement.getAttribute("name")) {
                        "input" -> component =
                            merchantGui.inputComponent

                        "player-inventory" -> component = merchantGui.playerInventoryComponent
                        else -> throw XMLLoadException("Unknown component name")
                    }

                    component.load(instance, nestedElement, plugin)
                } else if (tagName.equals("trade", ignoreCase = true)) {
                    val tradeNodes: NodeList = nestedElement.getChildNodes()

                    val ingredients: MutableList<ItemStack> = ArrayList(2)
                    var result: ItemStack? = null

                    for (tradeIndex in 0 until tradeNodes.getLength()) {
                        val tradeNode: Node = tradeNodes.item(tradeIndex)

                        if (tradeNode.getNodeType() != Node.ELEMENT_NODE) {
                            continue
                        }

                        val tradeElement: Element = tradeNode as Element

                        if (tradeElement.getTagName().equals("ingredient", ignoreCase = true)) {
                            if (ingredients.size >= 2) {
                                throw XMLLoadException("Too many ingredients specified, must be no more than two")
                            }

                            val ingredientNodes: NodeList = tradeElement.getChildNodes()

                            for (ingredientIndex in 0 until ingredientNodes.getLength()) {
                                val ingredientNode: Node = ingredientNodes.item(ingredientIndex)

                                if (ingredientNode.getNodeType() != Node.ELEMENT_NODE) {
                                    continue
                                }

                                ingredients.add(
                                    loadItem(
                                        instance,
                                        ingredientNode as Element
                                    ).getItem()
                                )
                            }
                        } else if (tradeElement.getTagName().equals("result", ignoreCase = true)) {
                            val resultNodes: NodeList = tradeElement.getChildNodes()

                            for (resultIndex in 0 until resultNodes.getLength()) {
                                val resultNode: Node = resultNodes.item(resultIndex)

                                if (resultNode.getNodeType() != Node.ELEMENT_NODE) {
                                    continue
                                }

                                if (result != null) {
                                    throw XMLLoadException("Multiple results specified for the same trade")
                                }

                                result = loadItem(instance, resultNode as Element).getItem()
                            }
                        } else {
                            throw XMLLoadException("Trade element is neither an ingredient nor a result")
                        }
                    }

                    if (result == null) {
                        throw XMLLoadException("Trade must have a result specified")
                    }

                    if (ingredients.size < 1) {
                        throw XMLLoadException("Trade must have at least one ingredient")
                    }

                    val recipe: MerchantRecipe = MerchantRecipe(result, Int.MAX_VALUE)

                    recipe.setIngredients(ingredients)

                    merchantGui.addTrade(recipe)
                } else {
                    throw XMLLoadException("Nested element is neither a component nor a trade")
                }
            }

            return merchantGui
        }

        /**
         * Loads a merchant gui from an XML file.
         *
         * @param instance the instance on which to reference fields and methods
         * @param inputStream the input stream containing the XML data
         * @return the loaded merchant gui
         * @since 0.10.0
         */
        @Contract(pure = true)
        fun load(instance: Any, inputStream: InputStream): MerchantGui? {
            return load(
                instance, inputStream, JavaPlugin.getProvidingPlugin(
                    MerchantGui::class.java
                )
            )
        }

        /**
         * Loads a merchant gui from the specified element, applying code references to the provided instance.
         *
         * @param instance the instance on which to reference fields and methods
         * @param element the element to load the gui from
         * @return the loaded merchant gui
         * @since 0.10.0
         */
        @Contract(pure = true)
        fun load(instance: Any, element: Element): MerchantGui {
            return load(
                instance, element, JavaPlugin.getProvidingPlugin(
                    MerchantGui::class.java
                )
            )
        }
    }
}
