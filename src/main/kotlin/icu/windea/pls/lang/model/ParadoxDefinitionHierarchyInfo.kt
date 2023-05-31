package icu.windea.pls.lang.model

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.expression.*

data class ParadoxDefinitionHierarchyInfo(
    val supportId: String,
    val expression: String,
    val configExpression: CwtDataExpression,
    val definitionName: String,
    val definitionType: String,
    val definitionSubtypes: List<String>,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : UserDataHolderBase(), ParadoxExpressionInfo {
    @Volatile override var file: PsiFile? = null
}
