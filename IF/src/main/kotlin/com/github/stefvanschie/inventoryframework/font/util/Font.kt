package com.github.stefvanschie.inventoryframework.font.util

import com.github.stefvanschie.inventoryframework.font.CSVFont
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import java.util.*

/**
 * An interface for fonts
 *
 * @since 0.5.0
 */
abstract class Font {

    @get:Contract(pure = true)
    abstract val defaultItem: ItemStack?

    /**
     * Turns the specified character into an [ItemStack] representing the specified character. If there is no item
     * for the specified character this will return null.
     *
     * @param character the character to get an item from
     * @return the item
     * @since 0.5.0
     */
    @Contract(pure = true)
    abstract fun toItem(character: Char): ItemStack?

    companion object {
        /**
         * A map containing font names and mapping them to the fonts
         */
        private val FONT_BY_NAME: MutableMap<String, Font> = HashMap()

        /**
         * The birch planks font
         */
        val BIRCH_PLANKS: Font = CSVFont(' ', "/fonts/birch-planks.csv")

        /**
         * The black font
         */
        val BLACK: Font = CSVFont(' ', "/fonts/black.csv")

        /**
         * The blue font
         */
        val BLUE: Font = CSVFont(' ', "/fonts/blue.csv")

        /**
         * The brown font
         */
        val BROWN: Font = CSVFont(' ', "/fonts/brown.csv")

        /**
         * The cobblestone font
         */
        val COBBLESTONE: Font = CSVFont(' ', "/fonts/cobblestone.csv")

        /**
         * The cyan font
         */
        val CYAN: Font = CSVFont(' ', "/fonts/cyan.csv")

        /**
         * The diamond font
         */
        val DIAMOND: Font = CSVFont(' ', "/fonts/diamond.csv")

        /**
         * The dirt font
         */
        val DIRT: Font = CSVFont(' ', "/fonts/dirt.csv")

        /**
         * The gold font
         */
        val GOLD: Font = CSVFont(' ', "/fonts/gold.csv")

        /**
         * The gray font
         */
        val GRAY: Font = CSVFont(' ', "/fonts/gray.csv")

        /**
         * The green font
         */
        val GREEN: Font = CSVFont(' ', "/fonts/green.csv")

        /**
         * The jungle planks font
         */
        val JUNGLE_PLANKS: Font = CSVFont(' ', "/fonts/jungle-planks.csv")

        /**
         * The letter cube font
         */
        val LETTER_CUBE: Font = CSVFont(' ', "/fonts/letter-cube.csv")

        /**
         * The light blue font
         */
        val LIGHT_BLUE: Font = CSVFont(' ', "/fonts/light-blue.csv")

        /**
         * The light gray font
         */
        val LIGHT_GRAY: Font = CSVFont(' ', "/fonts/light-gray.csv")

        /**
         * The lime font
         */
        val LIME: Font = CSVFont(' ', "/fonts/lime.csv")

        /**
         * The magenta font
         */
        val MAGENTA: Font = CSVFont(' ', "/fonts/magenta.csv")

        /**
         * The monitor font
         */
        val MONITOR: Font = CSVFont(' ', "/fonts/monitor.csv")

        /**
         * The oak log font
         */
        val OAK_LOG: Font = CSVFont(' ', "/fonts/oak-log.csv")

        /**
         * The oak planks font
         */
        val OAK_PLANKS: Font = CSVFont(' ', "/fonts/oak-planks.csv")

        /**
         * The orange font
         */
        val ORANGE: Font = CSVFont(' ', "/fonts/orange.csv")

        /**
         * The pink font
         */
        val PINK: Font = CSVFont(' ', "/fonts/pink.csv")

        /**
         * The plush font
         */
        val PLUSH: Font = CSVFont(' ', "/fonts/plush.csv")

        /**
         * The pumpkin font
         */
        val PUMPKIN: Font = CSVFont('_', "/fonts/pumpkin.csv")

        /**
         * The purple font
         */
        val PURPLE: Font = CSVFont(' ', "/fonts/purple.csv")

        /**
         * The quartz font
         */
        val QUARTZ: Font = CSVFont(' ', "/fonts/quartz.csv")

        /**
         * The rainbow font
         */
        val RAINBOW: Font = CSVFont(' ', "/fonts/rainbow.csv")

        /**
         * The red font
         */
        val RED: Font = CSVFont(' ', "/fonts/red.csv")

        /**
         * The spruce planks font
         */
        val SPRUCE_PLANKS: Font = CSVFont(' ', "/fonts/spruce-planks.csv")

        /**
         * The stone font
         */
        val STONE: Font = CSVFont(' ', "/fonts/stone.csv")

        /**
         * The watermelon font
         */
        val WATERMELON: Font = CSVFont(' ', "/fonts/watermelon.csv")

        /**
         * The white font
         */
        val WHITE: Font = CSVFont(' ', "/fonts/white.csv")

        /**
         * The yellow font
         */
        val YELLOW: Font = CSVFont(' ', "/fonts/yellow.csv")

        /**
         * Gets a font by its name. The name will be made uppercase and spaces will be replaced with underscore before
         * trying to access it.
         *
         * @param name the name of the font
         * @return the font
         * @since 0.5.0
         */
        @Contract(pure = true)
        fun fromName(name: String): Font? {
            return FONT_BY_NAME[name.replace(
                ' ',
                '_'
            ).uppercase(Locale.getDefault())]
        }

        /**
         * Registers a custom font so it can be used in lookups and XML files. The name will be made upper case and spaces
         * will be turned into underscores to ensure a standardized format for all font names.
         *
         * @param name the font name
         * @param font the font
         * @since 0.5.0
         */
        @Contract(pure = true)
        fun registerFont(name: String, font: Font) {
            FONT_BY_NAME[name.replace(
                ' ',
                '_'
            ).uppercase(Locale.getDefault())] = font
        }

        init {
            FONT_BY_NAME["BIRCH_PLANKS"] = BIRCH_PLANKS
            FONT_BY_NAME["BLACK"] = BLACK
            FONT_BY_NAME["BLUE"] = BLUE
            FONT_BY_NAME["BROWN"] = BROWN
            FONT_BY_NAME["COBBLESTONE"] = COBBLESTONE
            FONT_BY_NAME["CYAN"] = CYAN
            FONT_BY_NAME["DIAMOND"] = DIAMOND
            FONT_BY_NAME["DIRT"] = DIRT
            FONT_BY_NAME["GOLD"] = GOLD
            FONT_BY_NAME["GRAY"] = GRAY
            FONT_BY_NAME["GREEN"] = GREEN
            FONT_BY_NAME["JUNGLE_PLANKS"] = JUNGLE_PLANKS
            FONT_BY_NAME["LETTER_CUBE"] = LETTER_CUBE
            FONT_BY_NAME["LIGHT_BLUE"] = LIGHT_BLUE
            FONT_BY_NAME["LIGHT_GRAY"] = LIGHT_GRAY
            FONT_BY_NAME["LIME"] = LIME
            FONT_BY_NAME["MAGENTA"] = MAGENTA
            FONT_BY_NAME["MONITOR"] = MONITOR
            FONT_BY_NAME["OAK_LOG"] = OAK_LOG
            FONT_BY_NAME["OAK_PLANKS"] = OAK_PLANKS
            FONT_BY_NAME["ORANGE"] = ORANGE
            FONT_BY_NAME["PINK"] = PINK
            FONT_BY_NAME["PLUSH"] = PLUSH
            FONT_BY_NAME["PUMPKIN"] = PUMPKIN
            FONT_BY_NAME["PURPLE"] = PURPLE
            FONT_BY_NAME["QUARTZ"] = QUARTZ
            FONT_BY_NAME["RAINBOW"] = RAINBOW
            FONT_BY_NAME["RED"] = RED
            FONT_BY_NAME["SPRUCE_PLANKS"] = SPRUCE_PLANKS
            FONT_BY_NAME["STONE"] = STONE
            FONT_BY_NAME["WATERMELON"] = WATERMELON
            FONT_BY_NAME["WHITE"] = WHITE
            FONT_BY_NAME["YELLOW"] = YELLOW
        }
    }
}
