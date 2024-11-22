package com.github.stefvanschie.inventoryframework.pane

import org.jetbrains.annotations.Contract
import org.w3c.dom.Element

/**
 * An interface for panes that can be flipped
 *
 * @since 0.3.0
 */
interface Flippable {
    /**
     * Sets whether this pane should flip its items horizontally
     *
     * @param flipHorizontally whether the pane should flip items horizontally
     * @since 0.3.0
     */
    fun flipHorizontally(flipHorizontally: Boolean)

    /**
     * Sets whether this pane should flip its items vertically
     *
     * @param flipVertically whether the pane should flip items vertically
     * @since 0.3.0
     */
    fun flipVertically(flipVertically: Boolean)

    @get:Contract(pure = true)
    val isFlippedHorizontally: Boolean

    @get:Contract(pure = true)
    val isFlippedVertically: Boolean

    companion object {
        /**
         * Loads all elements regarding a [Flippable] [Pane] for the specified pane. The mutable pane contains
         * the changes made.
         *
         * @param flippable the flippable pane's elements to be applied
         * @param element the XML element for this pane
         * @since 0.3.0
         */
        fun load(flippable: Flippable, element: Element) {
            if (element.hasAttribute("flipHorizontally")) flippable.flipHorizontally(
                element.getAttribute(
                    "flipHorizontally"
                ).toBoolean()
            )

            if (element.hasAttribute("flipVertically")) flippable.flipVertically(
                element.getAttribute(
                    "flipVertically"
                ).toBoolean()
            )
        }
    }
}
