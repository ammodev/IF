package com.github.stefvanschie.inventoryframework.exception

/**
 * An exception indicating that something went wrong while trying to load a [Gui] from an XML file.
 *
 * @since 0.3.0
 */
class XMLLoadException : RuntimeException {
    
    /**
     * Constructs the exception with a given message
     *
     * @param message the message to show
     * @since 0.3.0
     */
    constructor(message: String) : super(message)

    /**
     * Constructs the exception with a given cause
     *
     * @param cause the cause of this exception
     * @since 0.3.1
     */
    constructor(cause: Throwable) : super(cause)

    /**
     * Constructs the exception with a given message and cause
     *
     * @param message the message to show
     * @param cause the cause of this exception
     */
    constructor(message: String, cause: Throwable) : super(message, cause)
}
