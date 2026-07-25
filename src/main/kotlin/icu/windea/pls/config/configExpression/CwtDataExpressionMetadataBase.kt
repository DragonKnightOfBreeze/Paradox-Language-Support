package icu.windea.pls.config.configExpression

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue

/** @see icu.windea.pls.inject.injectors.addon.InlinedDelegateFieldCodeInjectors.CwtDataExpressionMetadataBase */
abstract class CwtDataExpressionMetadataBase : UserDataHolderBase(), CwtDataExpressionMetadata {
    object Keys : KeyRegistry() {
        val value by registerKey<String>(this)
        val wildcard by registerKey<Boolean>(this) { false }
        val condition by registerKey<Boolean>(this) { false }
        val ignoreCase by registerKey<Boolean>(this) { false }
        val intRange by registerKey<IntRangeInfo>(this)
        val floatRange by registerKey<FloatRangeInfo>(this)
        val suffixes by registerKey<Set<String>>(this)
    }

    // NOTE 3.0.1 常量类型（`CwtDataTypes.Constant`）和模板类型（`CwtDataTypes.Template`）的数据表达式，实际上并不需要元数据（`value`），直接从 `expressionString` 获取原始的表达式字符串即可。

    // region Accessors

    override var value by Keys.value
    override var wildcard by Keys.wildcard
    override var condition by Keys.condition
    override var ignoreCase by Keys.ignoreCase
    override var intRange by Keys.intRange
    override var floatRange by Keys.floatRange
    override var suffixes by Keys.suffixes

    // endregion
}
