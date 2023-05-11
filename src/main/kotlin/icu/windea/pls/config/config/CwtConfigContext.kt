package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.lang.config.*

data class CwtConfigContext(
    val contextElement: PsiElement,
    val definitionName: String?,
    val definitionType: String,
    val definitionSubtypes: List<String>?,
    val configGroup: CwtConfigGroup,
    val matchType: Int = CwtConfigMatchType.DEFAULT
): UserDataHolderBase() {
    val injectors = CwtDeclarationConfigInjector.EP_NAME.extensionList.filter { it.supports(this) }
}