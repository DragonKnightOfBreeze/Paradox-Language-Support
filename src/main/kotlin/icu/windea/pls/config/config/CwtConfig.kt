package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * CWT规则。
 */
interface CwtConfig<out T : PsiElement> : UserDataHolder {
    val pointer: SmartPsiElementPointer<out T>
    val configGroup: CwtConfigGroup
    val configExpression: CwtDataExpression? get() = null
}
