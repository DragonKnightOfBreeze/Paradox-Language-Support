package icu.windea.pls.core.data

import icu.windea.pls.core.util.io.skipUtf8Bom
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.serializer
import java.io.InputStream
import java.io.OutputStream

/**
 * 用于数据处理（例如序列化和反序列化）的 JSON 服务。
 *
 * 基于 [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization)。
 *
 * 统一提供 [read] 和 [write] 方法作为读写 JSON 的入口：
 * - 读取时统一兼容 UTF-8 BOM（参见 [InputStream.skipUtf8Bom]）。
 * - 写入时统一使用不包含 BOM 的 UTF-8 编码。
 * - 通过 `json5` 参数选择是否使用 JSON5 格式（宽松模式）。
 */
object JsonService {
    private val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
        encodeDefaults = true
        explicitNulls = true
        ignoreUnknownKeys = true
    }
    private val json5 = Json(json) {
        isLenient = true // for unquoted property keys
        allowTrailingComma = true // for trailing commas
        allowComments = true // not for YAML-like comments (e.g., `# ...`)
    }

    private fun getFormat(json5: Boolean): Json {
        return if (json5) this.json5 else this.json
    }

    /** 从输入流中读取 JSON，兼容 UTF-8 BOM。 */
    fun <T> read(serializer: DeserializationStrategy<T>, inputStream: InputStream, json5: Boolean = false): T {
        return getFormat(json5).decodeFromStream(serializer, inputStream.skipUtf8Bom())
    }

    /** 从输入流中读取 JSON，兼容 UTF-8 BOM。 */
    inline fun <reified T> read(inputStream: InputStream, json5: Boolean = false): T {
        return read(serializer(), inputStream, json5)
    }

    /** 从字符串中读取 JSON，兼容 UTF-8 BOM 字符。 */
    fun <T> read(serializer: DeserializationStrategy<T>, text: String, json5: Boolean = false): T {
        return getFormat(json5).decodeFromString(serializer, text.withoutUtf8Bom())
    }

    /** 从字符串中读取 JSON，兼容 UTF-8 BOM 字符。 */
    inline fun <reified T> read(text: String, json5: Boolean = false): T {
        return read(serializer(), text, json5)
    }

    /** 将 JSON 写入输出流（不包含 BOM）。 */
    fun <T> write(serializer: SerializationStrategy<T>, value: T, outputStream: OutputStream, json5: Boolean = false) {
        getFormat(json5).encodeToStream(serializer, value, outputStream)
    }

    /** 将 JSON 写入输出流（不包含 BOM）。 */
    inline fun <reified T> write(value: T, outputStream: OutputStream, json5: Boolean = false) {
        write(serializer(), value, outputStream, json5)
    }

    /** 将 JSON 转为字符串。 */
    fun <T> write(serializer: SerializationStrategy<T>, value: T, json5: Boolean = false): String {
        return getFormat(json5).encodeToString(serializer, value)
    }

    /** 将 JSON 转为字符串。 */
    inline fun <reified T> write(value: T, json5: Boolean = false): String {
        return write(serializer(), value, json5)
    }

    private fun String.withoutUtf8Bom(): String {
        return if (startsWith('\uFEFF')) substring(1) else this
    }
}
