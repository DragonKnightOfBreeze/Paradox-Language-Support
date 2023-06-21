package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.*

class CwtDeclarationConfigContext(
    contextElement: PsiElement,
    val definitionName: String?,
    val definitionType: String,
    val definitionSubtypes: List<String>?,
    val configGroup: CwtConfigGroup,
    val matchOptions: Int = ParadoxConfigMatcher.Options.Default
) : UserDataHolderBase() {
    private val contextElementPointer = contextElement.createPointer()
    
    val contextElement: PsiElement? get() = contextElementPointer.element
    
    val injectors = CwtDeclarationConfigInjector.EP_NAME.extensionList.filter { it.supports(this) }
}