package icu.windea.pls.core.data

import kotlinx.serialization.json.Json

/**
 * JSON 序列化服务。
 *
 * 基于 [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization)。
 */
object JsonService {
    val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
    }
    val json5 = Json(json) {
        isLenient = true
        allowTrailingComma = true
        allowComments = true
    }
}

