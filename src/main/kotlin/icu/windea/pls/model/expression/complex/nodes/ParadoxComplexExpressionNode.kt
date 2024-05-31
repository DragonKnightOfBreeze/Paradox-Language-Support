package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.psi.*

interface ParadoxComplexExpressionNode {
    val text: String
    val rangeInExpression: TextRange
    val nodes: List<ParadoxComplexExpressionNode> get() = emptyList()
    
    fun getAttributesKey(): TextAttributesKey? = null
    
    fun getAttributesKeyConfig(element: ParadoxScriptStringExpressionElement): CwtConfig<*>? = null
    
    fun getReference(element: ParadoxScriptStringExpressionElement): PsiReference? = null
    
    fun getUnresolvedError(element: ParadoxScriptStringExpressionElement): ParadoxComplexExpressionError? = null
}

