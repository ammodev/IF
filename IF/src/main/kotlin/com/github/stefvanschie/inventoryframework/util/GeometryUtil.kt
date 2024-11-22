package com.github.stefvanschie.inventoryframework.util

import org.jetbrains.annotations.Contract
import java.util.*

object GeometryUtil {
    /**
     * Calculates a clockwise rotation across a two dimensional grid
     *
     * @param x the standard x coordinate
     * @param y the standard y coordinate
     * @param length the length of the grid
     * @param height the height of the grid
     * @param rotation the rotation in degrees
     * @return a pair of new coordinates, with the x coordinate being the key and the y coordinate being the value
     */
    @JvmStatic
    @Contract(pure = true)
    fun processClockwiseRotation(
        x: Int, y: Int, length: Int, height: Int,
        rotation: Int
    ): Map.Entry<Int, Int> {
        var newX = x
        var newY = y

        if (rotation == 90) {
            newX = height - 1 - y
            newY = x
        } else if (rotation == 180) {
            newX = length - 1 - x
            newY = height - 1 - y
        } else if (rotation == 270) {
            newX = y
            newY = length - 1 - x
        }

        return AbstractMap.SimpleEntry(newX, newY)
    }

    /**
     * Calculates a counter clockwise rotation across a two dimensional grid. This is the same as calling
     * [.processClockwiseRotation] with 360 - rotation as the rotation.
     *
     * @param x the standard x coordinate
     * @param y the standard y coordinate
     * @param length the length of the grid
     * @param height the height of the grid
     * @param rotation the rotation in degrees
     * @return a pair of new coordinates, with the x coordinate being the key and the y coordinate being the value
     */
    @JvmStatic
    @Contract(pure = true)
    fun processCounterClockwiseRotation(
        x: Int, y: Int, length: Int, height: Int,
        rotation: Int
    ): Map.Entry<Int, Int> {
        return processClockwiseRotation(x, y, length, height, 360 - rotation)
    }
}
