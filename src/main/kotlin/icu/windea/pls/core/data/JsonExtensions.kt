package icu.windea.pls.core.data

import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.*

val jsonMapper by lazy {
    jacksonObjectMapper().apply {
        findAndRegisterModules()
        disable(SerializationFeature.INDENT_OUTPUT)
        enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
        enable(JsonParser.Feature.IGNORE_UNDEFINED)
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }
}
