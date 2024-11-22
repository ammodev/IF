package com.github.stefvanschie.inventoryframework.pane

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.InventoryComponent
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui
import com.github.stefvanschie.inventoryframework.pane.MasonryPane
import com.github.stefvanschie.inventoryframework.pane.util.Slot
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import kotlin.math.min

/**
 * This pane holds panes and decides itself where every pane should go. It tries to put every pane in the top left
 * corner and will move rightwards and downwards respectively if the top left corner is already in use. Depending on the
 * order and size of the panes, this may leave empty spaces in certain spots. Do note however that the order of panes
 * isn't always preserved. If there is a gap left in which a pane with a higher index can fit, it will be put there,
 * even if there are panes with a lower index after it. Panes that do not fit will not be displayed.
 *
 * @since 0.3.0
 */
class MasonryPane : Pane, Orientable {
    /**
     * A list of panes that should be displayed
     */
    override val panes: MutableList<Pane> = ArrayList()

    /**
     * The orientation of the items in this pane
     */
    override var orientation: Orientable.Orientation = Orientable.Orientation.HORIZONTAL

    /**
     * Creates a new masonry pane
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @param priority the priority of the pane
     * @since 0.10.8
     */
    constructor(slot: Slot, length: Int, height: Int, priority: Priority) : super(
        slot,
        length,
        height,
        priority
    )

    constructor(x: Int, y: Int, length: Int, height: Int, priority: Priority) : super(
        x,
        y,
        length,
        height,
        priority
    )

    /**
     * Creates a new masonry pane
     *
     * @param slot the slot of the pane
     * @param length the length of the pane
     * @param height the height of the pane
     * @since 0.10.8
     */
    constructor(slot: Slot, length: Int, height: Int) : super(slot, length, height)

    constructor(x: Int, y: Int, length: Int, height: Int) : super(x, y, length, height)

    constructor(length: Int, height: Int) : super(length, height)

    override fun display(
        inventoryComponent: InventoryComponent, paneOffsetX: Int, paneOffsetY: Int, maxLength: Int,
        maxHeight: Int
    ) {
        val length: Int = (min(
            length.toDouble(),
            maxLength.toDouble()
        ) - paneOffsetX).toInt()
        val height: Int = (min(
            height.toDouble(),
            maxHeight.toDouble()
        ) - paneOffsetY).toInt()

        val positions: Array<IntArray> = Array(length) { IntArray(height) }

        for (array: IntArray in positions) {
            Arrays.fill(array, -1)
        }

        for (paneIndex in panes.indices) {
            val pane: Pane = panes.get(paneIndex)

            if (!pane.isVisible()) {
                continue
            }

            if (orientation == Orientable.Orientation.HORIZONTAL) {
                outerLoop@ for (y in 0 until height) {
                    for (x in 0 until length) {
                        //check whether the pane fits
                        var fits: Boolean = true

                        paneFits@ for (i in 0 until pane.getLength()) {
                            for (j in 0 until pane.getHeight()) {
                                if (x + i >= positions.size || y + j >= positions.get(x + i).size || positions.get(
                                        x + i
                                    ).get(y + j) != -1
                                ) {
                                    fits = false
                                    break@paneFits
                                }
                            }
                        }

                        if (fits) {
                            for (i in 0 until pane.getLength()) {
                                for (j in 0 until pane.getHeight()) {
                                    positions.get(x + i).get(y + j) = paneIndex
                                }
                            }

                            pane.setX(x)
                            pane.setY(y)

                            pane.display(
                                inventoryComponent,
                                paneOffsetX + getSlot().getX(length),
                                paneOffsetY + getSlot().getY(length),
                                min(
                                    this.length.toDouble(),
                                    maxLength.toDouble()
                                ).toInt(),
                                min(
                                    this.height.toDouble(),
                                    maxHeight.toDouble()
                                ).toInt()
                            )
                            break@outerLoop
                        }
                    }
                }
            } else if (orientation == Orientable.Orientation.VERTICAL) {
                outerLoop@ for (x in 0 until length) {
                    for (y in 0 until height) {
                        //check whether the pane fits
                        var fits: Boolean = true

                        paneFits@ for (i in 0 until pane.getHeight()) {
                            for (j in 0 until pane.getLength()) {
                                if (x + j >= positions.size || y + i >= positions.get(x + j).size || positions.get(
                                        x + j
                                    ).get(y + i) != -1
                                ) {
                                    fits = false
                                    break@paneFits
                                }
                            }
                        }

                        if (fits) {
                            for (i in 0 until pane.getLength()) {
                                for (j in 0 until pane.getHeight()) {
                                    positions.get(x + i).get(y + j) = paneIndex
                                }
                            }

                            pane.setX(x)
                            pane.setY(y)

                            pane.display(
                                inventoryComponent,
                                paneOffsetX + getSlot().getX(length),
                                paneOffsetY + getSlot().getY(length),
                                min(
                                    this.length.toDouble(),
                                    maxLength.toDouble()
                                ).toInt(),
                                min(
                                    this.height.toDouble(),
                                    maxHeight.toDouble()
                                ).toInt()
                            )
                            break@outerLoop
                        }
                    }
                }
            }
        }
    }

    override fun click(
        gui: Gui, inventoryComponent: InventoryComponent,
        event: InventoryClickEvent, slot: Int, paneOffsetX: Int, paneOffsetY: Int, maxLength: Int,
        maxHeight: Int
    ): Boolean {
        val length: Int =
            min(length.toDouble(), maxLength.toDouble()).toInt()
        val height: Int =
            min(height.toDouble(), maxHeight.toDouble()).toInt()

        val paneSlot: Slot = getSlot()

        val xPosition: Int = paneSlot.getX(maxLength)
        val yPosition: Int = paneSlot.getY(maxLength)

        val totalLength: Int = inventoryComponent.length

        val adjustedSlot: Int =
            slot - (xPosition + paneOffsetX) - totalLength * (yPosition + paneOffsetY)

        val x: Int = adjustedSlot % totalLength
        val y: Int = adjustedSlot / totalLength

        if (x < 0 || x >= length || y < 0 || y >= height) {
            return false
        }

        callOnClick(event)

        var success: Boolean = false

        for (pane: Pane in ArrayList<Pane>(panes)) {
            if (!pane.isVisible()) {
                continue
            }

            success = success || pane.click(
                gui, inventoryComponent, event, slot, paneOffsetX + xPosition,
                paneOffsetY + yPosition, length, height
            )
        }

        return success
    }

    @Contract(pure = true)
    override fun copy(): MasonryPane {
        val masonryPane: MasonryPane = MasonryPane(getSlot(), length, height, getPriority())

        for (pane: Pane in panes) {
            masonryPane.addPane(pane.copy())
        }

        masonryPane.setVisible(isVisible())
        masonryPane.onClick = onClick
        masonryPane.orientation = orientation

        masonryPane.uuid = uuid

        return masonryPane
    }

    /**
     * Adds a pane to this masonry pane
     *
     * @param pane the pane to add
     * @since 0.3.0
     */
    fun addPane(pane: Pane) {
        panes.add(pane)
    }

    override val items: Collection<GuiItem>
        get() {
            return getPanes().stream().flatMap { pane: Pane -> pane.getItems().stream() }
                .collect(Collectors.toList())
        }

    override fun getPanes(): Collection<Pane> {
        val panes: MutableCollection<Pane> = HashSet()

        this.panes.forEach(Consumer { p: Pane ->
            panes.addAll(p.getPanes())
            panes.add(p)
        })

        return panes
    }

    override fun clear() {
        panes.clear()
    }

    override fun getOrientation(): Orientable.Orientation {
        return orientation
    }

    override fun setOrientation(orientation: Orientable.Orientation) {
        this.orientation = orientation
    }

    companion object {
        /**
         * Loads a masonry pane from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @param plugin the plugin that will be the owner of the created items
         * @return the masonry pane
         * @since 0.10.8
         */
        @JvmStatic
        fun load(instance: Any, element: Element, plugin: Plugin): MasonryPane {
            try {
                val masonryPane: MasonryPane = MasonryPane(
                    element.getAttribute("length").toInt(),
                    element.getAttribute("height").toInt()
                )

                Pane.Companion.load(masonryPane, instance, element)
                Orientable.Companion.load(masonryPane, element)

                if (element.hasAttribute("populate")) {
                    return masonryPane
                }

                val childNodes: NodeList = element.getChildNodes()

                for (j in 0 until childNodes.getLength()) {
                    val pane: Node = childNodes.item(j)

                    if (pane.getNodeType() != Node.ELEMENT_NODE) {
                        continue
                    }

                    masonryPane.addPane(Gui.loadPane(instance, pane, plugin))
                }

                return masonryPane
            } catch (exception: NumberFormatException) {
                throw XMLLoadException(exception)
            }
        }

        /**
         * Loads a masonry pane from a given element
         *
         * @param instance the instance class
         * @param element the element
         * @return the masonry pane
         */
        @Deprecated(
            """this method is no longer used internally and has been superseded by
                  {@link #load(Object, Element, Plugin)}"""
        )
        fun load(instance: Any, element: Element): MasonryPane {
            return load(
                instance, element, JavaPlugin.getProvidingPlugin(
                    MasonryPane::class.java
                )
            )
        }
    }
}
