package com.github.stefvanschie.inventoryframework.util

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * A utility class for working with skulls
 *
 * @since 0.5.0
 */
object SkullUtil {

    /**
     * Gets a skull from the specified id. The id is the value from the textures.minecraft.net website after the last
     * '/' character.
     *
     * @param id the skull id
     * @return the skull item
     * @since 0.5.0
     */
    fun getSkull(id: String): ItemStack {
        val item = ItemStack(Material.PLAYER_HEAD)
        val itemMeta = item.itemMeta ?: throw NullPointerException("ItemMeta is null")

        setSkull(itemMeta, id)
        item.setItemMeta(itemMeta)

        return item
    }

    /**
     * Sets the skull of an existing [ItemMeta] from the specified id.
     * The id is the value from the textures.minecraft.net website after the last '/' character.
     *
     * @param meta the meta to change
     * @param id the skull id
     */
    @JvmStatic
    fun setSkull(meta: ItemMeta, id: String) {
        val profile = GameProfile(UUID.randomUUID(), "")
        val encodedData = Base64.getEncoder().encode(
            String.format(
                "{textures:{SKIN:{url:\"%s\"}}}",
                "http://textures.minecraft.net/texture/$id"
            ).toByteArray()
        )
        profile.properties.put("textures", Property("textures", String(encodedData)))
        val itemDisplayName = meta.displayName

        try {
            val profileField = meta.javaClass.getDeclaredField("profile")
            profileField.isAccessible = true
            profileField[meta] = profile

            meta.setDisplayName(itemDisplayName)

            // Sets serializedProfile field on meta
            // If it does throw NoSuchMethodException this stops, and meta is correct.
            // Else it has profile and will set the field.
            val setProfile = meta.javaClass.getDeclaredMethod(
                "setProfile",
                GameProfile::class.java
            )
            setProfile.isAccessible = true
            setProfile.invoke(meta, profile)
        } catch (e: NoSuchFieldException) {
            throw RuntimeException(e)
        } catch (e: SecurityException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        } catch (ignored: NoSuchMethodException) {
        }
    }
}
