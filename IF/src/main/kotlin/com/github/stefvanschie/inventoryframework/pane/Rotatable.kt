package com.github.stefvanschie.inventoryframework.pane

import org.jetbrains.annotations.Contract
import org.w3c.dom.Element

/**
 * An interface for panes that are rotatable
 *
 * @since 0.3.0
 */
interface Rotatable {
    /**
     * Sets the rotation of this pane. The rotation is in degrees and can only be in increments of 90. Anything higher
     * than 360, will be lowered to a value in between [0, 360) while maintaining the same rotational value. E.g. 450
     * degrees becomes 90 degrees, 1080 degrees becomes 0, etc.
     *
     * This method fails for any pane that has a length and height which are unequal.
     *
     * @param rotation the rotation of this pane, must be divisible by 90.
     * @throws UnsupportedOperationException when the length and height of the pane are not the same
     * @throws IllegalArgumentException when the rotation isn't a multiple of 90
     * @since 0.3.0
     */
    @get:Contract(pure = true)
    var rotation: Int

    companion object {
        /**
         * Loads all elements regarding a [Rotatable] [Pane] for the specified pane. The mutable pane contains
         * the changes made.
         *
         * @param rotatable the rotatable pane's elements to be applied
         * @param element the XML element for this pane
         * @since 0.3.0
         */
        fun load(rotatable: Rotatable, element: Element) {
            if (element.hasAttribute("rotation")) {
                rotatable.rotation = element.getAttribute("rotation").toInt()
            }
        }
    }
}
