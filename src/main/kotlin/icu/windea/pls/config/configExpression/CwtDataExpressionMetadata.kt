package icu.windea.pls.config.configExpression

import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo

/**
 * 数据表达式的元数据。
 *
 * @see CwtDataExpression
 */
interface CwtDataExpressionMetadata {
    val value: String? get() = null
    val wildcard: Boolean get() = false
    val condition: Boolean get() = false
    val ignoreCase: Boolean get() = false
    val intRange: IntRangeInfo? get() = null
    val floatRange: FloatRangeInfo? get() = null
    val suffixes: Set<String>? get() = null

    companion object {
        @JvmField val EMPTY: CwtDataExpressionMetadata = EmptyCwtDataExpressionMetadata
    }
}

typealias CwtDataExpressionMetadataBuilder = CwtDataExpressionMetadataBase.() -> Unit

typealias CwtDataExpressionMetadataBuilderWithInput = CwtDataExpressionMetadataBase.(String) -> Unit

fun CwtDataExpressionMetadataBuilderWithInput.acceptInput(input: String): CwtDataExpressionMetadataBuilder = { invoke(this, input) }

private object EmptyCwtDataExpressionMetadata : CwtDataExpressionMetadata
