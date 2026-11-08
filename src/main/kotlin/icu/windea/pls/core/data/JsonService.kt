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
import java.util.concurrent.ConcurrentHashMap

/**
 * 用于数据处理（例如序列化和反序列化）的 JSON 服务。
 *
 * 基于 [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization)。
 *
 * 统一提供 [read] 和 [write] 方法，作为读写 JSON 的入口：
 * - 读取时通过 [JsonOptions.skipUtf8Bom] 控制是否兼容 UTF-8 BOM（参见 [InputStream.skipUtf8Bom]）。
 * - 写入时统一使用不包含 BOM 的 UTF-8 编码。
 * - 通过选项参数 `options` 选择底层使用的 [Json] 配置（例如 [JsonOptions.JSON5]）。
 */
object JsonService {
    private val formats = ConcurrentHashMap<JsonOptions, Json>()

    private fun JsonOptions.resolveFormat(): Json {
        format?.let { return it }
        return formats.computeIfAbsent(this) { options ->
            Json {
                prettyPrint = options.prettyPrint
                prettyPrintIndent = options.prettyPrintIndent
                encodeDefaults = options.encodeDefaults
                explicitNulls = options.explicitNulls
                ignoreUnknownKeys = options.ignoreUnknownKeys
                isLenient = options.isLenient
                allowTrailingComma = options.allowTrailingComma
                allowComments = options.allowComments
            }
        }
    }

    private fun InputStream.resolveUtf8Bom(options: JsonOptions): InputStream {
        return if (options.skipUtf8Bom) skipUtf8Bom() else this
    }

    private fun String.resolveUtf8Bom(options: JsonOptions): String {
        return if (options.skipUtf8Bom) withoutUtf8Bom() else this
    }

    /** 从输入流中读取 JSON，可通过 [JsonOptions.skipUtf8Bom] 兼容 UTF-8 BOM。 */
    fun <T> read(serializer: DeserializationStrategy<T>, inputStream: InputStream, options: JsonOptions = JsonOptions.DEFAULT): T {
        return options.resolveFormat().decodeFromStream(serializer, inputStream.resolveUtf8Bom(options))
    }

    /** 从输入流中读取 JSON，可通过 [JsonOptions.skipUtf8Bom] 兼容 UTF-8 BOM。 */
    inline fun <reified T> read(inputStream: InputStream, options: JsonOptions = JsonOptions.DEFAULT): T {
        return read(serializer(), inputStream, options)
    }

    /** 从输入流中读取 JSON，可通过 [JsonOptions.skipUtf8Bom] 兼容 UTF-8 BOM。 */
    fun <T> read(serializer: DeserializationStrategy<T>, inputStream: InputStream, json5: Boolean): T {
        return read(serializer, inputStream, json5Options(json5))
    }

    /** 从输入流中读取 JSON，可通过 [JsonOptions.skipUtf8Bom] 兼容 UTF-8 BOM。 */
    inline fun <reified T> read(inputStream: InputStream, json5: Boolean): T {
        return read(serializer(), inputStream, json5Options(json5))
    }

    /** 从字符串中读取 JSON，可通过 [JsonOptions.skipUtf8Bom] 兼容 UTF-8 BOM 字符。 */
    fun <T> read(serializer: DeserializationStrategy<T>, text: String, options: JsonOptions = JsonOptions.DEFAULT): T {
        return options.resolveFormat().decodeFromString(serializer, text.resolveUtf8Bom(options))
    }

    /** 从字符串中读取 JSON，可通过 [JsonOptions.skipUtf8Bom] 兼容 UTF-8 BOM 字符。 */
    inline fun <reified T> read(text: String, options: JsonOptions = JsonOptions.DEFAULT): T {
        return read(serializer(), text, options)
    }

    /** 从字符串中读取 JSON，可通过 [JsonOptions.skipUtf8Bom] 兼容 UTF-8 BOM 字符。 */
    fun <T> read(serializer: DeserializationStrategy<T>, text: String, json5: Boolean): T {
        return read(serializer, text, json5Options(json5))
    }

    /** 从字符串中读取 JSON，可通过 [JsonOptions.skipUtf8Bom] 兼容 UTF-8 BOM 字符。 */
    inline fun <reified T> read(text: String, json5: Boolean): T {
        return read(serializer(), text, json5Options(json5))
    }

    /** 将 JSON 写入输出流（不包含 BOM）。 */
    fun <T> write(serializer: SerializationStrategy<T>, value: T, outputStream: OutputStream, options: JsonOptions = JsonOptions.DEFAULT) {
        options.resolveFormat().encodeToStream(serializer, value, outputStream)
    }

    /** 将 JSON 写入输出流（不包含 BOM）。 */
    inline fun <reified T> write(value: T, outputStream: OutputStream, options: JsonOptions = JsonOptions.DEFAULT) {
        write(serializer(), value, outputStream, options)
    }

    /** 将 JSON 写入输出流（不包含 BOM）。 */
    fun <T> write(serializer: SerializationStrategy<T>, value: T, outputStream: OutputStream, json5: Boolean) {
        write(serializer, value, outputStream, json5Options(json5))
    }

    /** 将 JSON 写入输出流（不包含 BOM）。 */
    inline fun <reified T> write(value: T, outputStream: OutputStream, json5: Boolean) {
        write(serializer(), value, outputStream, json5Options(json5))
    }

    /** 将 JSON 转为字符串。 */
    fun <T> write(serializer: SerializationStrategy<T>, value: T, options: JsonOptions = JsonOptions.DEFAULT): String {
        return options.resolveFormat().encodeToString(serializer, value)
    }

    /** 将 JSON 转为字符串。 */
    inline fun <reified T> write(value: T, options: JsonOptions = JsonOptions.DEFAULT): String {
        return write(serializer(), value, options)
    }

    /** 将 JSON 转为字符串。 */
    fun <T> write(serializer: SerializationStrategy<T>, value: T, json5: Boolean): String {
        return write(serializer, value, json5Options(json5))
    }

    /** 将 JSON 转为字符串。 */
    inline fun <reified T> write(value: T, json5: Boolean): String {
        return write(serializer(), value, json5Options(json5))
    }

    @PublishedApi
    internal fun json5Options(json5: Boolean): JsonOptions {
        return if (json5) JsonOptions.JSON5 else JsonOptions.DEFAULT
    }

    private fun String.withoutUtf8Bom(): String {
        return if (startsWith('\uFEFF')) substring(1) else this
    }
}
