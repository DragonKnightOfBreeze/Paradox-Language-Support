package icu.windea.pls.config.configExpression

import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.metadata.MetadataMapBase
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey

open class CwtDataExpressionMetadataBase : MetadataMapBase(), CwtDataExpressionMetadata {
    // 3.0.1 use explicit code with folding, instead of delegate properties with corresponding code injector, to make things simple

    final override var value: String? // region by Keys.value
        get() = this[Keys.value]
        set(value) = run { this[Keys.value] = value } // endregion
    final override var wildcard: Boolean // region by Keys.wildcard
        get() = this[Keys.wildcard]
        set(value) = run { this[Keys.wildcard] = value } // endregion
    final override var condition: Boolean // region by Keys.condition
        get() = this[Keys.condition]
        set(value) = run { this[Keys.condition] = value } // endregion
    final override var ignoreCase: Boolean // region by Keys.ignoreCase
        get() = this[Keys.ignoreCase]
        set(value) = run { this[Keys.ignoreCase] = value } // endregion
    final override var intRange: IntRangeInfo? // region by Keys.intRange
        get() = this[Keys.intRange]
        set(value) = run { this[Keys.intRange] = value } // endregion
    final override var floatRange: FloatRangeInfo? // region by Keys.floatRange
        get() = this[Keys.floatRange]
        set(value) = run { this[Keys.floatRange] = value } // endregion
    final override var suffixes: Set<String>? // region by Keys.suffixes
        get() = this[Keys.suffixes]
        set(value) = run { this[Keys.suffixes] = value } // endregion
}

object CwtDataExpressionMetadataKeys : KeyRegistry() {
    val value by registerKey<String>(this)
    val wildcard by registerKey(this) { false }
    val condition by registerKey(this) { false }
    val ignoreCase by registerKey(this) { false }
    val intRange by registerKey<IntRangeInfo>(this)
    val floatRange by registerKey<FloatRangeInfo>(this)
    val suffixes by registerKey<Set<String>>(this)
}

private typealias Keys = CwtDataExpressionMetadataKeys

typealias CwtDataExpressionMetadataBuilder = CwtDataExpressionMetadataBase.() -> Unit

typealias CwtDataExpressionMetadataBuilderWithInput = CwtDataExpressionMetadataBase.(String) -> Unit

fun CwtDataExpressionMetadataBuilderWithInput.acceptInput(input: String): CwtDataExpressionMetadataBuilder = { invoke(this, input) }
