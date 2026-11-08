package icu.windea.pls.core.data

import kotlinx.serialization.json.Json

/**
 * JSON 读写选项。
 *
 * @property format 预构建的 [Json] 实例。若不为 `null`，则直接使用它，并忽略其余格式化配置。
 * @property prettyPrint 是否格式化输出。
 * @property prettyPrintIndent 格式化输出时使用的缩进。
 * @property encodeDefaults 是否输出带默认值的字段。
 * @property explicitNulls 是否输出 `null` 字段。
 * @property ignoreUnknownKeys 是否忽略未知字段。
 * @property isLenient 是否使用宽松模式（例如允许不带引号的键），通常用于 JSON5。
 * @property allowTrailingComma 是否允许尾随逗号。
 * @property allowComments 是否允许注释。
 * @property skipUtf8Bom 读取时是否跳过 UTF-8 BOM。
 */
data class JsonOptions(
    val format: Json? = null,
    val prettyPrint: Boolean = true,
    val prettyPrintIndent: String = "  ",
    val encodeDefaults: Boolean = true,
    val explicitNulls: Boolean = true,
    val ignoreUnknownKeys: Boolean = true,
    val isLenient: Boolean = false,
    val allowTrailingComma: Boolean = false,
    val allowComments: Boolean = false,
    val skipUtf8Bom: Boolean = true,
) {
    companion object {
        @JvmField val DEFAULT = JsonOptions()

        /** JSON5 模式：允许注释、尾随逗号和不带引号的键。 */
        @JvmField val JSON5 = JsonOptions(
            isLenient = true,
            allowTrailingComma = true,
            allowComments = true,
        )
    }
}
