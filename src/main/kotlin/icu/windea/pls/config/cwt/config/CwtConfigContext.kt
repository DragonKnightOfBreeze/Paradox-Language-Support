package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.cwt.*

data class CwtConfigContext(
    val contextElement: PsiElement,
    val definitionName: String?,
    val definitionType: String,
    val definitionSubtypes: List<String>?,
    val configGroup: CwtConfigGroup
)