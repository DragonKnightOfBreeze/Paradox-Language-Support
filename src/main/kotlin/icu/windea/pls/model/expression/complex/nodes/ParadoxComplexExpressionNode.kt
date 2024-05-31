package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.psi.*

/**
 * 复杂表达式的节点。复杂表达式由数个节点组成，本身也是一个节点。
 *
 * 对应对应范围的文本，可以指定如何进行代码高亮，以及如何解析为引用，以及解析失败时的错误提示。
 */
interface ParadoxComplexExpressionNode {
    val text: String
    val rangeInExpression: TextRange
    val nodes: List<ParadoxComplexExpressionNode> get() = emptyList()
    
    fun getAttributesKey(): TextAttributesKey? = null
    
    fun getAttributesKeyConfig(element: ParadoxScriptStringExpressionElement): CwtConfig<*>? = null
    
    fun getReference(element: ParadoxScriptStringExpressionElement): PsiReference? = null
    
    fun getUnresolvedError(element: ParadoxScriptStringExpressionElement): ParadoxComplexExpressionError? = null
}

