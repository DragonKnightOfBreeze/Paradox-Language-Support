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
     * 预配置的 JSON 映射器：
     * - 自动注册模块（Kotlin、JavaTime 等）；
     * - 关闭缩进输出（`INDENT_OUTPUT`）；
     * - 忽略未知字段与未定义值（`IGNORE_UNKNOWN`/`IGNORE_UNDEFINED`）；
     * - 反序列化时忽略未知属性（`FAIL_ON_UNKNOWN_PROPERTIES` 关闭）。
     */
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
