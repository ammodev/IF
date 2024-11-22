package com.github.stefvanschie.inventoryframework.font

import com.github.stefvanschie.inventoryframework.font.util.Font
import com.github.stefvanschie.inventoryframework.util.CSVUtil
import com.github.stefvanschie.inventoryframework.util.SkullUtil
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import java.io.IOException
import java.util.stream.Collectors

/**
 * A font for characters with a space as default character. Only one instance of this class should ever exist and should
 * be used everywhere.
 *
 * @param defaultCharacter the default character to use when a requested character cannot be found
 * @param filePath the relative file path to the csv file containing the character mappings
 *
 * @since 0.5.0
 */
class CSVFont(private val defaultCharacter: Char, filePath: String) : Font() {

    /**
     * A map with all the items and their dedicated characters
     */
    private var characterMappings: Map<Char, ItemStack>? = null

    init {
        try {
            javaClass.getResourceAsStream(filePath).use { inputStream ->
                characterMappings = inputStream?.let {
                    CSVUtil.readAll(it).stream()
                        .collect(
                            Collectors.toMap(
                                { keys: Array<String> -> keys[0][0] },
                                { values: Array<String> -> SkullUtil.getSkull(values[1]) })
                        )
                }
            }
        } catch (exception: IOException) {
            throw RuntimeException("Error loading CSV-based font: $filePath", exception)
        }
    }

    override val defaultItem: ItemStack? = characterMappings?.get(defaultCharacter)

    @Contract(pure = true)
    override fun toItem(character: Char) = characterMappings?.get(character)
}
