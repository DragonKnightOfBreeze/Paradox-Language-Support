package icu.windea.pls.core.data

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer

abstract class JsonModuleWithType<T : Any>(type: Class<T>) : SimpleModule() {
    init {
        val module = this
        addSerializer(type, object : StdSerializer<T>(type) {
            override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) {
                module.serialize(value, gen, provider)
            }
        })
        addDeserializer(type, object : StdDeserializer<T>(type) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T {
                return module.deserialize(p, ctxt)
            }
        })
    }

    abstract fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider)

    abstract fun deserialize(p: JsonParser, ctxt: DeserializationContext): T
}
