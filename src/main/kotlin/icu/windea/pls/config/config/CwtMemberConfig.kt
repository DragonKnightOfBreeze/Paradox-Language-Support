package icu.windea.pls.config.config

import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.model.CwtType

sealed interface CwtMemberConfig<out T : CwtMemberElement> : CwtConfig<T> {
    val value: String
    val valueType: CwtType
    val configs: List<CwtMemberConfig<*>>?
    val optionConfigs: List<CwtOptionMemberConfig<*>>?

    var parentConfig: CwtMemberConfig<*>?

    val valueExpression: CwtDataExpression
    override val configExpression: CwtDataExpression

    override fun toString(): String

    object Keys : KeyRegistry()
}
