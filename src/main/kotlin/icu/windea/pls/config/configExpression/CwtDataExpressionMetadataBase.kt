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
        val wildcard by registerKey(this) { false }
        val condition by registerKey(this) { false }
        val ignoreCase by registerKey(this) { false }
        val intRange by registerKey<IntRangeInfo>(this)
        val floatRange by registerKey<FloatRangeInfo>(this)
        val suffixes by registerKey<Set<String>>(this)
    }

    override var value by Keys.value
    override var wildcard by Keys.wildcard
    override var condition by Keys.condition
    override var ignoreCase by Keys.ignoreCase
    override var intRange by Keys.intRange
    override var floatRange by Keys.floatRange
    override var suffixes by Keys.suffixes
}

typealias CwtDataExpressionMetadataBuilder = CwtDataExpressionMetadataBase.() -> Unit

typealias CwtDataExpressionMetadataBuilderWithInput = CwtDataExpressionMetadataBase.(String) -> Unit

fun CwtDataExpressionMetadataBuilderWithInput.acceptInput(input: String): CwtDataExpressionMetadataBuilder = { invoke(this, input) }
