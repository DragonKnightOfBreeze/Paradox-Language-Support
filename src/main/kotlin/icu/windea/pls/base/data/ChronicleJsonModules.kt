package icu.windea.pls.base.data

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import icu.windea.pls.core.data.JsonModuleWithType
import icu.windea.pls.model.ParadoxGameType

fun ObjectMapper.registerAllModules() {
    registerModule(ParadoxGameTypeByIdJsonModule())
}

private class ParadoxGameTypeByIdJsonModule : JsonModuleWithType<ParadoxGameType>(ParadoxGameType::class.java) {
    override fun serialize(value: ParadoxGameType, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.id)
    }

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParadoxGameType {
        val id = p.valueAsString
        return ParadoxGameType.get(id) ?: throw IllegalArgumentException("Unknown id: $id")
    }
}
