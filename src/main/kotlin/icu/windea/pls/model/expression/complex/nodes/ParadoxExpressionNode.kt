package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.annotation.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.complex.errors.*
import icu.windea.pls.script.psi.*

interface ParadoxExpressionNode {
    val text: String
    val rangeInExpression: TextRange
    val nodes: List<ParadoxExpressionNode> get() = emptyList()
    
    fun getAttributesKey(): TextAttributesKey? = null
    
    fun getAttributesKeyConfig(element: ParadoxScriptStringExpressionElement): CwtConfig<*>? = null
    
    fun getReference(element: ParadoxScriptStringExpressionElement): PsiReference? = null
    
    fun getUnresolvedError(element: ParadoxScriptStringExpressionElement): ParadoxExpressionError? = null
}

