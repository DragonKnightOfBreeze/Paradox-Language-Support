package icu.windea.pls.core.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.nio.file.Path

private const val jsonBom = "\uFEFF"

/**
 * JSON 序列化服务。
 *
 * 基于 kotlinx-serialization，配置项尽量与原 jackson 实现保持行为一致：
 * - 开启缩进输出（2 个空格），对应 `SerializationFeature.INDENT_OUTPUT`；
 * - 忽略未知字段，对应关闭 `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES`；
 * - 不输出 null 字段，对应 `@JsonInclude(NON_NULL)`；
 * - 输出带默认值的字段，对应 jackson 默认会写出这些属性。
 */
@OptIn(ExperimentalSerializationApi::class)
object JsonService {
    /** 标准的 JSON 实例。 */
    val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    /**
     * 兼容 JSON5 的 JSON 实例。
     *
     * 注意：kotlinx-serialization 本身不支持注释，读取前需先用 [stripJson5Comments] 去除注释。
     */
    val json5 = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
        isLenient = true
        allowTrailingComma = true
    }
}

/** 读取文本，并移除可能存在的 UTF-8 BOM。 */
fun File.readJsonText(): String = readText().removePrefix(jsonBom)

/** 读取文本，并移除可能存在的 UTF-8 BOM。 */
fun Path.readJsonText(): String = toFile().readJsonText()

/** 读取文本，并移除可能存在的 UTF-8 BOM。 */
fun InputStream.readJsonText(): String = reader().use { it.readText() }.removePrefix(jsonBom)
