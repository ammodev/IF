package com.github.stefvanschie.inventoryframework.gui.type.util

import com.github.stefvanschie.inventoryframework.adventuresupport.StringHolder.Companion.of
import com.github.stefvanschie.inventoryframework.adventuresupport.TextHolder
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.Contract

abstract class NamedGui @JvmOverloads constructor(
    title: TextHolder, plugin: Plugin = JavaPlugin.getProvidingPlugin(
        NamedGui::class.java
    )
) : Gui(plugin) {
    /**
     * The title of this gui
     */
    private var title: TextHolder? = null

    /**
     * Gets whether this title is dirty or not i.e. whether the title has changed.
     *
     * @return whether the title is dirty
     * @since 0.10.0
     */
    /**
     * Whether the title is dirty i.e., has changed
     */
    @get:Contract(pure = true)
    var isDirty: Boolean = false
        private set

    /**
     * Constructs a new gui with a title
     *
     * @param title the title/name of this gui
     * @since 0.8.0
     */
    constructor(title: String) : this(of(title))

    /**
     * Constructs a new gui with a title for the given `plugin`.
     *
     * @param title the title/name of this gui
     * @param plugin the owning plugin of this gui
     * @see .NamedGui
     * @since 0.10.8
     */
    constructor(title: String, plugin: Plugin) : this(of(title), plugin)

    /**
     * Constructs a new gui with a title for the given `plugin`.
     *
     * @param title the title/name of this gui
     * @param plugin the owning plugin of this gui
     * @see .NamedGui
     * @since 0.10.8
     */
    /**
     * Constructs a new gui with a title
     *
     * @param title the title/name of this gui
     * @since 0.10.0
     */
    init {
        this.title = title
    }

    /**
     * Sets the title for this inventory.
     *
     * @param title the title
     */
    fun setTitle(title: String) {
        setTitle(of(title))
    }

    /**
     * Sets the title for this inventory.
     *
     * @param title the title
     * @since 0.10.0
     */
    fun setTitle(title: TextHolder) {
        this.title = title
        this.isDirty = true
    }

    /**
     * Returns the title of this gui as a legacy string.
     *
     * @return the title
     * @since 0.8.0
     */
    @Contract(pure = true)
    fun getTitle(): String {
        return title!!.asLegacyString()
    }

    @get:Contract(pure = true)
    val titleHolder: TextHolder
        /**
         * Returns the title of this GUI in a wrapped form.
         *
         * @return the title
         * @since 0.10.0
         */
        get() = title!!

    /**
     * Marks that the changes present here have been accepted. This sets dirty to false. If dirty was already false,
     * this will do nothing.
     *
     * @since 0.10.0
     */
    fun markChanges() {
        this.isDirty = false
    }
}
