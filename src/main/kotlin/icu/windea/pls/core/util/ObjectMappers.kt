package icu.windea.pls.core.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object ObjectMappers {
    val jsonMapper by lazy {
        jacksonObjectMapper().apply {
            findAndRegisterModules()
            disable(SerializationFeature.INDENT_OUTPUT)
            enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
            enable(JsonParser.Feature.IGNORE_UNDEFINED)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }
}
