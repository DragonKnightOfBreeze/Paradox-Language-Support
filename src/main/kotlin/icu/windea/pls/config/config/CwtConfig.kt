package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*

interface CwtConfig<out T : PsiElement> : UserDataHolder {
    val pointer: SmartPsiElementPointer<out T>
    val configGroup: CwtConfigGroup

    val configExpression: CwtDataExpression? get() = null
}
