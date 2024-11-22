package com.github.stefvanschie.inventoryframework.pane.util

import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import org.jetbrains.annotations.Contract
import java.util.*
import kotlin.math.min

/**
 * A mask for [OutlinePane]s that specifies in which positions the items should be placed. Objects of this class
 * are immutable.
 *
 * @since 0.5.16
 */
class Mask {
    /**
     * A two-dimensional array of booleans indicating which slots are 'enabled' and which ones are 'disabled'. This
     * two-dimensional array is constructed in a row-major order fashion.
     */
    private val mask: Array<BooleanArray>

    /**
     * Creates a mask based on the strings provided. Each string is a row for the mask and each character is a cell of
     * that row that indicates a slot for the mask. When the character is a 0, the slot will be considered 'disabled';
     * when the character is a 1, the slot will be considered 'enabled'. When there are any other characters in the
     * string, an [IllegalArgumentException] will be thrown. When multiple strings have a different length an
     * [IllegalArgumentException] will be thrown.
     *
     * @param mask a var-arg of strings that represent this mask
     * @throws IllegalArgumentException when a string contains an illegal character or when strings have different
     * lengths
     * @since 0.5.16
     */
    constructor(vararg mask: String) {
        this.mask = Array(mask.size) { BooleanArray(if (mask.size == 0) 0 else mask[0].length) }

        for (row in mask.indices) {
            val length = mask[row].length

            require(length == this.mask[row].size) { "Lengths of each string should be equal" }

            for (column in 0 until length) {
                val character = mask[row][column]

                if (character == '0') {
                    this.mask[row][column] = false
                } else if (character == '1') {
                    this.mask[row][column] = true
                } else {
                    throw IllegalArgumentException("Strings may only contain '0' and '1'")
                }
            }
        }
    }

    /**
     * Creates a mask based on the two-dimensional boolean array provided. Each array is a row for the mask and each
     * index is a cell of that row that indicates a slot for the mask. When the boolean is false, the slot will be
     * considered 'disabled'; when the boolean is true, the slot will be considered 'enabled'.
     *
     * @param mask a two-dimensional boolean array of booleans that represent this mask
     * @since 0.9.1
     */
    private constructor(mask: Array<BooleanArray>) {
        this.mask = mask
    }

    /**
     * Creates a new maks with the specified height. If the new height is smaller than the previous height, the excess
     * values will be truncated. If the new height is longer than the previous height, additional values will be added
     * which are enabled. If the height is the same as the previous mask, this will simply return a new mask identical
     * to this one.
     *
     * @param height the new height of the mask
     * @return a new mask with the specified height
     * @since 0.9.1
     */
    @Contract(pure = true)
    fun setHeight(height: Int): Mask {
        val newRows = Array(height) {
            BooleanArray(
                length
            )
        }

        for (index in 0 until min(height.toDouble(), this.height.toDouble()).toInt()) {
            System.arraycopy(mask[index], 0, newRows[index], 0, mask[index].size)
        }

        for (index in min(height.toDouble(), this.height.toDouble()) until height) {
            newRows[index] = BooleanArray(length)

            Arrays.fill(newRows[index], true)
        }

        return Mask(newRows)
    }

    /**
     * Creates a new maks with the specified length. If the new length is smaller than the previous length, the excess
     * values will be truncated. If the new length is longer than the previous length, additional values will be added
     * which are enabled. If the length is the same as the previous mask, this will simply return a new mask identical
     * to this one.
     *
     * @param length the new length of the mask
     * @return a new mask with the specified length
     * @since 0.9.1
     */
    @Contract(pure = true)
    fun setLength(length: Int): Mask {
        val newRows = Array(height) { BooleanArray(length) }

        for (index in mask.indices) {
            val newRow = BooleanArray(length)

            System.arraycopy(
                mask[index], 0, newRow, 0,
                min(length.toDouble(), mask[index].size.toDouble()).toInt()
            )

            Arrays.fill(
                newRow,
                min(length.toDouble(), mask[index].size.toDouble()),
                newRow.size,
                true
            )

            newRows[index] = newRow
        }

        return Mask(newRows)
    }

    /**
     * Returns the amount of slots in this mask that are 'enabled'.
     *
     * @return amount of enabled slots
     * @since 0.5.16
     */
    fun amountOfEnabledSlots(): Int {
        var amount = 0

        for (row in mask) {
            for (cell in row) {
                if (cell) {
                    amount++
                }
            }
        }

        return amount
    }

    /**
     * Gets the column of this mask at the specified index. The values indicate the state of the slots for that slot:
     * true indicates that the slot is 'enabled'; false indicates that the slot is 'disabled'. The
     * returned array is a copy of the original; modifications to the returned array will not be reflected in the mask.
     *
     * @param index the column index
     * @return the column of this mask
     * @since 0.5.16
     */
    fun getColumn(index: Int): BooleanArray {
        val column = BooleanArray(mask.size)

        for (i in 0 until height) {
            column[i] = mask[i][index]
        }

        return column
    }

    /**
     * Gets the row of this mask at the specified index. The values indicate the state of the slots for that slot:
     * true indicates that the slot is 'enabled'; false indicates that the slot is 'disabled'. The
     * returned array is a copy of the original; modifications to the returned array will not be reflected in the mask.
     *
     * @param index the row index
     * @return the row of this mask
     * @since 0.5.16
     */
    fun getRow(index: Int): BooleanArray {
        val row = mask[index]

        return row.copyOf(row.size)
    }

    /**
     * Gets whether the slot at the specified row and column is 'enabled' or not. This returns true if it is
     * 'enabled' and false if it is 'disabled'.
     *
     * @param x the x coordinate of the slot
     * @param y the y coordinate of the slot
     * @return whether the slot is enabled or not
     * @since 0.5.16
     */
    fun isEnabled(x: Int, y: Int): Boolean {
        return mask[y][x]
    }

    val length: Int
        /**
         * Gets the length of this mask
         *
         * @return the length
         * @since 0.5.16
         */
        get() = mask[0].size

    val height: Int
        /**
         * Gets the height of this mask
         *
         * @return the height
         * @since 0.5.16
         */
        get() = mask.size

    override fun equals(`object`: Any?): Boolean {
        if (this === `object`) {
            return true
        }

        if (`object` == null || javaClass != `object`.javaClass) {
            return false
        }

        val mask = `object` as Mask

        return this.mask.contentDeepEquals(mask.mask)
    }

    override fun hashCode(): Int {
        return mask.contentDeepHashCode()
    }

    override fun toString(): String {
        return "Mask{" +
                "mask=" + mask.contentDeepToString() +
                '}'
    }
}
