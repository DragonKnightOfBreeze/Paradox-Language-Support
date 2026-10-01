package icu.windea.pls.core.data

import kotlinx.serialization.json.Json

/**
 * JSON 序列化服务。
 *
 * 基于 [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization)。
 *
 * 直接暴露不可变的 [Json] 实例，从而保持类型化 API 和调用点的透明性。
 */
object JsonService {
    val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
        encodeDefaults = true
        explicitNulls = true
        ignoreUnknownKeys = true
    }
    val json5 = Json(json) {
        isLenient = true // for unquoted property keys
        allowTrailingComma = true // for trailing commas
        allowComments = true // not for YAML-like comments (e.g., `# ...`)
    }
}
