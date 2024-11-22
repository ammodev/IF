package com.github.stefvanschie.inventoryframework.pane

import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import java.util.*

/**
 * An interface for panes that can have different orientations
 *
 * @since 0.3.0
 */
interface Orientable {
    /**
     * Sets the orientation of this outline pane
     *
     * @param orientation the new orientation
     * @since 0.3.0
     */
    @get:Contract(pure = true)
    var orientation: Orientation

    /**
     * An orientation for outline panes
     *
     * @since 0.3.0
     */
    enum class Orientation {
        /**
         * A horizontal orientation, will outline every item from the top-left corner going to the right and down
         *
         * @since 0.3.0
         */
        HORIZONTAL,

        /**
         * A vertical orientation, will outline every item from the top-left corner going down and to the right
         *
         * @since 0.3.0
         */
        VERTICAL
    }

    companion object {
        /**
         * Loads all elements regarding a [Orientable] [Pane] for the specified pane. The mutable pane contains
         * the changes made.
         *
         * @param orientable the orientable pane's elements to be applied
         * @param element the XML element for this pane
         * @since 0.3.0
         */
        fun load(orientable: Orientable, element: Element) {
            if (element.hasAttribute("orientation")) {
                orientable.orientation =
                    Orientation.valueOf(
                        element.getAttribute("orientation")
                            .uppercase(Locale.getDefault())
                    )
            }
        }
    }
}
