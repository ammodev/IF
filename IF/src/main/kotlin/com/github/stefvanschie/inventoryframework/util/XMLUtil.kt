package com.github.stefvanschie.inventoryframework.util

import com.github.stefvanschie.inventoryframework.exception.XMLLoadException
import com.github.stefvanschie.inventoryframework.exception.XMLReflectionException
import org.bukkit.event.Event
import org.jetbrains.annotations.Contract
import org.w3c.dom.Element
import java.lang.reflect.InvocationTargetException
import java.util.function.Consumer

object XMLUtil {
    /**
     * Loads an event consumer from the given instance and element
     *
     * @param instance the object instance
     * @param element the element
     * @param eventType the type of the event
     * @param name the name of the attribute
     * @return the consumer to be called on click
     * @param <T> the type of the event
    </T> */
    @Contract(pure = true)
    fun <T : Event?> loadOnEventAttribute(
        instance: Any, element: Element,
        eventType: Class<T>, name: String
    ): Consumer<T>? {
        val attribute = element.getAttribute(name)
        for (method in instance.javaClass.methods) {
            if (method.name != attribute) continue

            val parameterCount = method.parameterCount
            val eventParameter = if (parameterCount == 0) {
                false
            } else if (parameterCount == 1 &&
                eventType.isAssignableFrom(method.parameterTypes[0])
            ) {
                true
            } else {
                continue
            }

            return Consumer { event: T ->
                try {
                    method.isAccessible = true
                    if (eventParameter) {
                        method.invoke(instance, event)
                    } else {
                        method.invoke(instance)
                    }
                } catch (e: IllegalAccessException) {
                    throw XMLReflectionException(e)
                } catch (e: InvocationTargetException) {
                    throw XMLReflectionException(e)
                }
            }
        }

        return null
    }

    /**
     * Invokes the method by the given name on the given instance with the provided argument. The method should have
     * the exact name specified and the exact parameter as specified. If the method cannot be accessed or found, this
     * will throw an [XMLLoadException].
     *
     * @param instance the instance on which to call the method
     * @param methodName the name of the method to invoke
     * @param argument the argument to provide for the invocation
     * @param parameter the parameter of the method
     * @since 0.10.3
     * @throws XMLLoadException if the method cannot be accessed or found
     */
    fun invokeMethod(
        instance: Any, methodName: String, argument: Any,
        parameter: Class<*>
    ) {
        try {
            val method = instance.javaClass.getMethod(methodName, parameter)

            method.isAccessible = true
            method.invoke(instance, argument)
        } catch (exception: IllegalAccessException) {
            throw XMLLoadException(exception)
        } catch (exception: InvocationTargetException) {
            throw XMLLoadException(exception)
        } catch (exception: NoSuchMethodException) {
            throw XMLLoadException(exception)
        }
    }

    /**
     * Sets a field from the given instance and element to the specified value
     *
     * @param instance the class instance the field is located in
     * @param element the element from which the field is specified
     * @param value the field's new value
     */
    @JvmStatic
    fun loadFieldAttribute(instance: Any, element: Element, value: Any?) {
        try {
            val field = instance.javaClass.getField(element.getAttribute("field"))

            field.isAccessible = true
            field[instance] = value
        } catch (e: NoSuchFieldException) {
            throw XMLLoadException(e)
        } catch (e: IllegalAccessException) {
            throw XMLLoadException(e)
        }
    }
}
