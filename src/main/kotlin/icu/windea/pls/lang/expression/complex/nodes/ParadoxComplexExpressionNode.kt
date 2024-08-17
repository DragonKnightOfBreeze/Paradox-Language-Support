package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.expression.complex.*

/**
 * 复杂表达式的节点。复杂表达式由数个节点组成，本身也是一个节点。
 *
 * 对应对应范围的文本，可以指定如何进行代码高亮，以及如何解析为引用，以及解析失败时的错误提示。
 */
interface ParadoxComplexExpressionNode {
    val text: String
    val rangeInExpression: TextRange
    val nodes: List<ParadoxComplexExpressionNode> get() = emptyList()
    val configGroup: CwtConfigGroup
    
    fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey? = null
    
    fun getAttributesKeyConfig(element: ParadoxExpressionElement): CwtConfig<*>? = null
    
    fun getReference(element: ParadoxExpressionElement): PsiReference? = null
    
    fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? = null
    
    abstract class Base : ParadoxComplexExpressionNode {
        override fun equals(other: Any?): Boolean {
            return this === other || (other is ParadoxComplexExpressionNode && this.javaClass == other.javaClass && text == other.text)
        }
        
        override fun hashCode(): Int {
            return text.hashCode()
        }
        
        override fun toString(): String {
            return text
        }
    }
}
