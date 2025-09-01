package icu.windea.pls.core.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * 统一的对象映射器集合。
 */
object ObjectMappers {
    /** JSON 映射器，预配置常用特性（自动模块、忽略未知字段、不缩进等）。 */
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
