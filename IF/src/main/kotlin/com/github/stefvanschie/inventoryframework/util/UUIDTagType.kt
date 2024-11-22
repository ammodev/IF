package com.github.stefvanschie.inventoryframework.util

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer
import java.util.*

/**
 * A [PersistentDataType] implementation that adds support for [UUID]s.
 *
 * @since 0.6.0
 */
class UUIDTagType private constructor() : PersistentDataType<ByteArray, UUID> {
    override fun getPrimitiveType(): Class<ByteArray> {
        return ByteArray::class.java
    }

    override fun getComplexType(): Class<UUID> {
        return UUID::class.java
    }

    override fun toPrimitive(complex: UUID, context: PersistentDataAdapterContext): ByteArray {
        val buffer = ByteBuffer.wrap(ByteArray(16))
        buffer.putLong(complex.mostSignificantBits)
        buffer.putLong(complex.leastSignificantBits)
        return buffer.array()
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): UUID {
        val buffer = ByteBuffer.wrap(primitive)
        val most = buffer.getLong()
        val least = buffer.getLong()
        return UUID(most, least)
    }

    companion object {
        /**
         * The one and only instance of this class.
         * Since this class stores no state information (apart from this field),
         * the usage of a single instance is safe even across multiple threads.
         */
        @JvmField
        val INSTANCE: UUIDTagType = UUIDTagType()
    }
}
