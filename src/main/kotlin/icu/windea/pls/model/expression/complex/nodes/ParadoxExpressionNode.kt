package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.complex.errors.*
import icu.windea.pls.script.psi.*

interface ParadoxExpressionNode : AttributesKeyAware {
    val text: String
    val rangeInExpression: TextRange
    val nodes: List<ParadoxExpressionNode> get() = emptyList()
    
    fun annotate(element: ParadoxScriptStringExpressionElement, holder: AnnotationHolder) {}
    
    fun getReference(element: ParadoxScriptStringExpressionElement): PsiReference? = null
    
    fun getUnresolvedError(element: ParadoxScriptStringExpressionElement): ParadoxExpressionError? = null
}

