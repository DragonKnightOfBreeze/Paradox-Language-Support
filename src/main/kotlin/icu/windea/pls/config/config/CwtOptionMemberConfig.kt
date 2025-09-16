package icu.windea.pls.config.config

import com.intellij.psi.PsiElement
import icu.windea.pls.model.CwtType

sealed interface CwtOptionMemberConfig<out T : PsiElement> : CwtDetachedConfig {
    val value: String
    val valueType: CwtType
    val optionConfigs: List<CwtOptionMemberConfig<*>>?
}
