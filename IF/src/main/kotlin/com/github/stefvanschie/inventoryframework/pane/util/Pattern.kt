package com.github.stefvanschie.inventoryframework.pane.util

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.pane.PatternPane
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import org.w3c.dom.Node
import kotlin.math.min

/**
 * A mask for [PatternPane]s that specifies in which positions the items should be placed. Objects of this class
 * are immutable.
 *
 * @since 0.9.8
 */
class Pattern {
    /**
     * A two-dimensional array of characters indicating which slot has which character. The characters are stored as
     * integers to properly support characters outside the 16-bit range. This means that, what would be a surrogate pair
     * in a string, is now a single number. This two-dimensional array is constructed in a row-major order fashion.
     */
    private val pattern: Array<IntArray>

    /**
     * Creates a pattern based on the strings provided. Each string is a row for the pattern and each character is a
     * slot of that row. When multiple strings have a different length an [IllegalArgumentException] will be
     * thrown.
     *
     * Surrogate pairs in strings are treated as a single character and not as two. This means that a string with five
     * surrogate pairs, will count as having five characters, not ten.
     *
     * @param pattern a var-arg of strings that represent this pattern
     * @throws IllegalArgumentException when strings have different lengths
     * @since 0.9.8
     */
    constructor(vararg pattern: String) {
        val rows = pattern.size
        val zeroRows = rows == 0

        this.pattern = Array(rows) {
            IntArray(
                if (zeroRows) 0 else pattern[0].codePointCount(
                    0,
                    pattern[0].length
                )
            )
        }

        if (zeroRows) {
            return
        }

        val globalLength = this.pattern[0].size

        for (index in 0 until rows) {
            val row = pattern[index]
            val length = row.codePointCount(0, row.length)

            require(length == globalLength) {
                "Rows have different lengths, row 1 has " + globalLength + " characters, but row " + index +
                        " has " + length + " characters"
            }

            val values: MutableList<Int> = ArrayList()

            row.codePoints().forEach { e: Int -> values.add(e) }

            for (column in values.indices) {
                this.pattern[index][column] = values[column]
            }
        }
    }

    /**
     * Creates a pattern based on the two-dimensional int array provided. Each array is a row for the pattern and each
     * index is a cell of that row that indicates a slot for the pattern.
     *
     * @param pattern a two-dimensional int array that represent this pattern
     * @since 0.9.8
     */
    private constructor(pattern: Array<IntArray>) {
        this.pattern = pattern
    }

    /**
     * Creates a new pattern with the specified height. If the new height is smaller than the previous height, the
     * excess values will be truncated. If the new height is longer than the previous height, additional values will be
     * added which are the same as the current bottom row. If the height is the same as the previous pattern, this will
     * simply return a new pattern identical to this one.
     *
     * @param height the new height of the pattern
     * @return a new pattern with the specified height
     * @since 0.9.8
     */
    @Contract(pure = true)
    fun setHeight(height: Int): Pattern {
        val newRows = Array(height) {
            IntArray(
                length
            )
        }

        for (index in 0 until min(height.toDouble(), this.height.toDouble()).toInt()) {
            System.arraycopy(pattern[index], 0, newRows[index], 0, pattern[index].size)
        }

        for (index in min(height.toDouble(), this.height.toDouble()) until height) {
            val previousRow = newRows[index - 1]

            newRows[index] = previousRow.copyOf(previousRow.size)
        }

        return Pattern(newRows)
    }

    /**
     * Creates a new pattern with the specified length. If the new length is smaller than the previous length, the excess
     * values will be truncated. If the new length is longer than the previous length, additional values will be added
     * which are the same as the rightmost value on a given row. If the length is the same as the previous pattern, this
     * will simply return a new pattern identical to this one.
     *
     * @param length the new length of the pattern
     * @return a new pattern with the specified length
     * @since 0.9.8
     */
    @Contract(pure = true)
    fun setLength(length: Int): Pattern {
        val newRows = Array(height) { IntArray(length) }

        for (index in pattern.indices) {
            val newRow = IntArray(length)
            val row = pattern[index]
            val minLength = min(length.toDouble(), row.size.toDouble()).toInt()

            System.arraycopy(row, 0, newRow, 0, minLength)

            for (column in minLength until length) {
                newRow[column] = newRow[minLength - 1]
            }

            newRows[index] = newRow
        }

        return Pattern(newRows)
    }

    /**
     * Gets the column of this pattern at the specified index. The values indicate the character of the slots for that
     * slot. The returned array is a copy of the original; modifications to the returned array will not be reflected in
     * the pattern.
     *
     * @param index the column index
     * @return the column of this pattern
     * @throws IllegalArgumentException when the index is outside the pattern's range
     * @since 0.9.8
     */
    @Contract(pure = true)
    fun getColumn(index: Int): IntArray {
        require(index < length) { "Index $index exceeds pattern length" }

        val column = IntArray(pattern[0].size)

        for (i in 0 until height) {
            column[i] = pattern[i][index]
        }

        return column
    }

    /**
     * Gets whether the provided character is present in this pattern. For checking surrogate pairs, the pair should be
     * combined into one single number.
     *
     * @param character the character to look for
     * @return whether the provided character is present
     * @since 0.9.8
     */
    @Contract(pure = true)
    fun contains(character: Int): Boolean {
        for (row in pattern) {
            for (cell in row) {
                if (cell != character) {
                    continue
                }

                return true
            }
        }

        return false
    }

    /**
     * Gets the row of this mask at the specified index. The values indicate the character of the slots for that
     * slot. The returned array is a copy of the original; modifications to the returned array will not be reflected in
     * the pattern.
     *
     * @param index the row index
     * @return the row of this pattern
     * @throws IllegalArgumentException when the index is outside the pattern's range
     * @since 0.9.8
     */
    @Contract(pure = true)
    fun getRow(index: Int): IntArray {
        require(index < height) { "Index $index exceeds pattern height" }

        val row = pattern[index]

        return row.copyOf(row.size)
    }

    /**
     * Gets the character at the specified position. This returns an integer, instead of a character to properly account
     * for characters above the 16-bit range.
     *
     * @param x the x position
     * @param y the y position
     * @return the character at the specified position
     * @throws IllegalArgumentException when the position is out of range
     * @since 0.9.8
     */
    @Contract(pure = true)
    fun getCharacter(x: Int, y: Int): Int {
        require(!(x < 0 || x >= length || y < 0 || y >= height)) { "Position $x, $y is out of range" }

        return pattern[y][x]
    }

    @get:Contract(pure = true)
    val length: Int
        /**
         * Gets the length of this pattern
         *
         * @return the length
         * @since 0.9.8
         */
        get() = pattern[0].size

    @get:Contract(pure = true)
    val height: Int
        /**
         * Gets the height of this pattern
         *
         * @return the height
         * @since 0.9.8
         */
        get() = pattern.size

    override fun equals(`object`: Any?): Boolean {
        if (this === `object`) {
            return true
        }

        if (`object` == null || javaClass != `object`.javaClass) {
            return false
        }

        val pattern = `object` as Pattern

        return this.pattern.contentDeepEquals(pattern.pattern)
    }

    override fun hashCode(): Int {
        return pattern.contentDeepHashCode()
    }

    override fun toString(): String {
        return "Mask{" +
                "mask=" + pattern.contentDeepToString() +
                '}'
    }

    companion object {
        /**
         * Loads a pattern from an xml element.
         *
         * @param element the xml element
         * @return the loaded pattern
         * @since 0.9.8
         */
        @Contract(pure = true)
        fun load(element: Element): Pattern {
            val rows = ArrayList<String>()
            val childNodes = element.childNodes

            for (itemIndex in 0 until childNodes.length) {
                val item = childNodes.item(itemIndex)

                if (item.nodeType != Node.ELEMENT_NODE) {
                    continue
                }

                val child = item as Element
                val name = child.nodeName

                if (name != "row") {
                    throw XMLLoadException("Pattern contains unknown tag $name")
                }

                rows.add(child.textContent)
            }

            return Pattern(*rows.toTypedArray<String>())
        }
    }
}
