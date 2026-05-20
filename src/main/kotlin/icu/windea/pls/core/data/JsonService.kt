package icu.windea.pls.core.data

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JsonService {
    val jsonMapper by lazy {
        jacksonObjectMapper().apply {
            // findAndRegisterModules() // 排除，否则会出现类路径的问题
            enable(SerializationFeature.INDENT_OUTPUT)
            enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
            enable(JsonParser.Feature.IGNORE_UNDEFINED)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }
    val json5Mapper by lazy {
        jsonMapper.copy().apply {
            enable(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature())
            enable(JsonReadFeature.ALLOW_YAML_COMMENTS.mappedFeature())
            enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES.mappedFeature())
            enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature())
        }
    }
}
