import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.google.protobuf.Message;


class ProtobufSerializer<T : Message?>(clazz: Class<T>) : RedisSerializer<T?> {
    private val clazz: Class<T>

    init {
        this.clazz = clazz
    }

    override fun serialize(t: T?): ByteArray? {
        return if (t == null) null else (t as Message).toByteArray()
    }

    override fun deserialize(bytes: ByteArray?): T? {
        var t: T? = null
        if (bytes != null) {
            try {
                t = parseFrom(clazz, bytes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return t
    }

    private fun parseFrom(clazz: Class<out Message?>, bytes: ByteArray): T {
        var method: Method? = methodCache[clazz]
        if (method == null) {
            method = clazz.getMethod("parseFrom", ByteArray::class.java)
            methodCache[clazz] = method
        }
        return method?.invoke(clazz, bytes) as T
    }

    companion object {
        private val methodCache: ConcurrentHashMap<Class<*>, Method?> = ConcurrentHashMap<Class<*>, Method?>()
    }
}