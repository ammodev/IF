package com.github.stefvanschie.inventoryframework.exception

/**
 * An exception indicating that the provided version is not supported.
 *
 * @param message the message to show
 *
 * @since 0.8.0
 */
class UnsupportedVersionException(message: String) : RuntimeException(message)
