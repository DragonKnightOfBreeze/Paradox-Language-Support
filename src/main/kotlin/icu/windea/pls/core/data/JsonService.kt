package icu.windea.pls.core.data

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JsonService {
    val mapper by lazy {
        jacksonObjectMapper().apply {
            // findAndRegisterModules() // 排除，否则会出现类路径的问题
            enable(SerializationFeature.INDENT_OUTPUT)
            enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
            enable(JsonParser.Feature.IGNORE_UNDEFINED)
            enable(JsonParser.Feature.ALLOW_COMMENTS)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }

    // @Suppress("unused")
    // val compactMapper by lazy {
    //     mapper.copy().apply {
    //         disable(SerializationFeature.INDENT_OUTPUT)
    //     }
    // }
}
