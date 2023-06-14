package icu.windea.pls.lang.model

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*

data class ParadoxDefinitionHierarchyInfo(
    val supportId: String,
    val expression: String,
    val configExpression: String,
    val isKey: Boolean,
    val definitionName: String,
    val definitionType: String,
    val definitionSubtypes: List<String>,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType,
) : UserDataHolderBase(), ParadoxExpressionInfo {
    constructor(
        supportId: String,
        expression: String,
        config: CwtMemberConfig<*>,
        definitionInfo: ParadoxDefinitionInfo,
        elementOffset: Int
    ) : this(
        supportId, expression, config.expression.expressionString, config.expression is CwtKeyExpression,
        definitionInfo.name, definitionInfo.type, definitionInfo.subtypes, elementOffset, definitionInfo.gameType
    )
    
    @Volatile override var file: PsiFile? = null
    
    val resolvedConfigExpression: CwtDataExpression by lazy {
        if(isKey) CwtKeyExpression.resolve(configExpression) else CwtValueExpression.resolve(configExpression)
    }
}
