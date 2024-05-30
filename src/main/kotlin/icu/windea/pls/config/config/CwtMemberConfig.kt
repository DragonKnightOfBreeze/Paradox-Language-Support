package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*

sealed interface CwtMemberConfig<out T : CwtMemberElement> : UserDataHolder, CwtConfig<T>, CwtValueAware, CwtConfigsAware, CwtDocumentationAware, CwtOptionsAware {
    override val configs: List<CwtMemberConfig<*>>?
    var parentConfig: CwtMemberConfig<*>?
    var inlineableConfig: CwtInlineableConfig<@UnsafeVariance T, CwtMemberConfig<@UnsafeVariance T>>?
    
    val valueExpression: CwtDataExpression
    override val expression: CwtDataExpression
    
    override fun resolved(): CwtMemberConfig<T> = inlineableConfig?.config?.castOrNull<CwtMemberConfig<T>>() ?: this
    
    override fun resolvedOrNull(): CwtMemberConfig<T>? = inlineableConfig?.config?.castOrNull<CwtMemberConfig<T>>()
    
    override fun toString(): String
    
    object Keys : KeyRegistry("CwtMemberConfig")
}

fun CwtMemberConfig<*>.delegated(
    configs: List<CwtMemberConfig<*>>? = this.configs,
    parentConfig: CwtMemberConfig<*>? = this.parentConfig
): CwtMemberConfig<*> {
    return when(this) {
        is CwtPropertyConfig -> this.delegated(configs, parentConfig)
        is CwtValueConfig -> this.delegated(configs, parentConfig)
    }
}

