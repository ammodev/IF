package com.github.stefvanschie.inventoryframework.pane.util

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import java.util.*

/**
 * A slot represents a position in some type of container. Implementors of this class represent slots in different ways.
 *
 * @since 0.10.8
 */
interface Slot {
    /**
     * Gets the x coordinate of this slot.
     *
     * @param length the length of the parent container
     * @return the x coordinate of this slot
     * @since 0.10.8
     */
    @Contract(pure = true)
    fun getX(length: Int): Int

    /**
     * Gets the y coordinate of this slot.
     *
     * @param length the length of the parent container
     * @return the y coordinate of this slot
     * @since 0.10.8
     */
    @Contract(pure = true)
    fun getY(length: Int): Int

    /**
     * A class representing a slot based on an (x, y) coordinate pair.
     *
     * @since 0.10.8
     */
    class XY
    /**
     * Creates a new slot based on an (x, y) coordinate pair.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @since 0.10.8
     */(
        /**
         * The (x, y) coordinate pair
         */
        private val x: Int, private val y: Int
    ) : Slot {
        override fun getX(length: Int): Int {
            return this.x
        }

        override fun getY(length: Int): Int {
            return this.y
        }

        override fun equals(`object`: Any?): Boolean {
            if (this === `object`) {
                return true
            }

            if (`object` == null || javaClass != `object`.javaClass) {
                return false
            }

            val xy = `object` as XY

            return x == xy.x && y == xy.y
        }

        override fun hashCode(): Int {
            return Objects.hash(x, y)
        }
    }

    /**
     * A class representing a slot based on an index.
     *
     * @since 0.10.8
     */
    class Indexed
    /**
     * Creates a new slot based on an index.
     *
     * @param index the index of this slot
     * @since 0.10.8
     */(
        /**
         * The index of this slot.
         */
        private val index: Int
    ) : Slot {
        /**
         * {@inheritDoc}
         *
         * If `length` is zero, this will throw an [IllegalArgumentException].
         *
         * @param length {@inheritDoc}
         * @return {@inheritDoc}
         * @throws IllegalArgumentException when `length` is zero
         */
        @Contract(pure = true)
        override fun getX(length: Int): Int {
            require(length != 0) { "Length may not be zero" }

            return this.index % length
        }

        /**
         * {@inheritDoc}
         *
         * If `length` is zero, this will throw an [IllegalArgumentException].
         *
         * @param length {@inheritDoc}
         * @return {@inheritDoc}
         * @throws IllegalArgumentException when `length` is zero
         */
        @Contract(pure = true)
        override fun getY(length: Int): Int {
            require(length != 0) { "Length may not be zero" }

            return this.index / length
        }

        override fun equals(`object`: Any?): Boolean {
            if (this === `object`) {
                return true
            }

            if (`object` == null || javaClass != `object`.javaClass) {
                return false
            }

            val indexed = `object` as Indexed

            return index == indexed.index
        }

        override fun hashCode(): Int {
            return index
        }
    }

    companion object {
        /**
         * Deserializes the slot from an element. The slot may either be provided as an (x, y) coordinate pair via the "x"
         * and "y" attributes; or as an index via the "index" attribute. If both forms are present, an
         * [XMLLoadException] will be thrown. If only the "x" or the "y" attribute is present, but not both, an
         * [XMLLoadException] will be thrown. If none of the aforementioned attributes appear, an
         * [XMLLoadException] will be thrown. If any of these attributes contain a value that is not an integer, an
         * [XMLLoadException] will be thrown. Otherwise, this will return a slot based on the present attributes.
         *
         * @param element the element from which to retrieve the attributes for the slot
         * @return the deserialized slot
         * @throws XMLLoadException if "x", "y", and "index" attributes are present; if only an "x" attribute is present; if
         * only a "y" attribute is present; if no "x", "y", or "index" attribute is present; or if
         * the "x", "y", or "index" attribute contain a value that is not an integer.
         */
        @Contract(value = "_ -> new", pure = true)
        fun deserialize(element: Element): Slot {
            val hasX = element.hasAttribute("x")
            val hasY = element.hasAttribute("y")
            val hasIndex = element.hasAttribute("index")

            if (hasX && hasY && !hasIndex) {
                val x: Int
                val y: Int

                try {
                    x = element.getAttribute("x").toInt()
                    y = element.getAttribute("y").toInt()
                } catch (exception: NumberFormatException) {
                    throw XMLLoadException("The x or y attribute does not have an integer as value")
                }

                return fromXY(x, y)
            }

            if (hasIndex && !hasX && !hasY) {
                val index: Int

                try {
                    index = element.getAttribute("index").toInt()
                } catch (exception: NumberFormatException) {
                    throw XMLLoadException("The index attribute does not have an integer as value")
                }

                return fromIndex(index)
            }

            throw XMLLoadException("The combination of x, y and index attributes is invalid")
        }

        /**
         * Creates a new slot based on an (x, y) coordinate pair.
         *
         * @param x the x coordinate
         * @param y the y coordinate
         * @return the slot representing this position
         * @since 0.10.8
         */
        @JvmStatic
        @Contract(value = "_, _ -> new", pure = true)
        fun fromXY(x: Int, y: Int): Slot {
            return XY(x, y)
        }

        /**
         * Creates a new slot based on an index. This index is relative to the parent container this slot will be used in.
         *
         * @param index the index
         * @return the slot representing this relative position
         * @since 0.10.8
         */
        @JvmStatic
        @Contract("_ -> new")
        fun fromIndex(index: Int): Slot {
            return Indexed(index)
        }
    }
}
