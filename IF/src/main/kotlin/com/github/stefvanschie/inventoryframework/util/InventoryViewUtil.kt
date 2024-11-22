package com.github.stefvanschie.inventoryframework.util

import com.github.stefvanschie.inventoryframework.inventoryview.abstraction.AbstractInventoryViewUtil
import com.github.stefvanschie.inventoryframework.inventoryview.interface_.InventoryViewUtil
import com.github.stefvanschie.inventoryframework.util.version.Version.Companion.version
import org.jetbrains.annotations.Contract

/**
 * A utility class for working with [InventoryView]s across different definitions.
 *
 * @since 0.10.16
 */
object InventoryViewUtil {
    
    /**
     * The underlying implementation.
     */
    private var IMPLEMENTATION: AbstractInventoryViewUtil? = null

    @JvmStatic
    @get:Contract(pure = true)
    val instance: AbstractInventoryViewUtil
        /**
         * Gets the instance of this class to use for the current version.
         *
         * @return an instance of a utility class
         * @since 0.10.16
         */
        get() {
            if (IMPLEMENTATION == null) {
                if (version.isInventoryViewInterface) {
                    IMPLEMENTATION = InventoryViewUtil
                } else {
                    IMPLEMENTATION =
                        com.github.stefvanschie.inventoryframework.inventoryview.abstractclass.InventoryViewUtil
                }
            }

            return IMPLEMENTATION!!
        }
}
