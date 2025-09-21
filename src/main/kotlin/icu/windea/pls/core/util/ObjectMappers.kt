package icu.windea.pls.core.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * Jackson `ObjectMapper` 工具集。
 */
object ObjectMappers {
    /**
     * 预配置的 JSON 映射器。
     */
    val jsonMapper by lazy {
        jacksonObjectMapper().apply {
            // findAndRegisterModules() // 排除，否则会出现类路径的问题
            disable(SerializationFeature.INDENT_OUTPUT)
            enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
            enable(JsonParser.Feature.IGNORE_UNDEFINED)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }
}
