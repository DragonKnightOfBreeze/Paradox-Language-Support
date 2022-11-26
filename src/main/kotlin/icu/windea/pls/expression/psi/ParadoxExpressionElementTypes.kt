// This is a generated file. Not intended for manual editing.
package icu.windea.pls.expression.psi

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.expression.psi.impl.*

interface ParadoxExpressionElementTypes {
	object Factory {
		fun createElement(node: ASTNode): PsiElement {
			val type = node.elementType
			if(type === DUMMY_SCOPE_FIELD) {
				return ParadoxDummyScopeFieldImpl(node)
			} else if(type === SCOPE_FIELD_EXPRESSION) {
				return ParadoxScopeFieldExpressionImpl(node)
			} else if(type === SCOPE_LINK) {
				return ParadoxScopeLinkImpl(node)
			} else if(type === SCOPE_LINK_FROM_DATA) {
				return ParadoxScopeLinkFromDataImpl(node)
			} else if(type === SYSTEM_SCOPE) {
				return ParadoxSystemScopeImpl(node)
			}
			throw AssertionError("Unknown element type: $type")
		}
	}
	
	companion object {
		@JvmField val DUMMY_SCOPE_FIELD: IElementType = ParadoxExpressionElementType("DUMMY_SCOPE_FIELD")
		@JvmField val SCOPE_FIELD: IElementType = ParadoxExpressionElementType("SCOPE_FIELD")
		@JvmField val SCOPE_FIELD_EXPRESSION: IElementType = ParadoxExpressionElementType("SCOPE_FIELD_EXPRESSION")
		@JvmField val SCOPE_LINK: IElementType = ParadoxExpressionElementType("SCOPE_LINK")
		@JvmField val SCOPE_LINK_FROM_DATA: IElementType = ParadoxExpressionElementType("SCOPE_LINK_FROM_DATA")
		@JvmField val SYSTEM_SCOPE: IElementType = ParadoxExpressionElementType("SYSTEM_SCOPE")
		@JvmField val AT: IElementType = ParadoxExpressionTokenType("@")
		@JvmField val BOOLEAN_TOKEN: IElementType = ParadoxExpressionTokenType("boolean_token")
		@JvmField val DOT: IElementType = ParadoxExpressionTokenType(".")
		@JvmField val FLOAT_TOKEN: IElementType = ParadoxExpressionTokenType("float_token")
		val IDENTIFIER_TOKEN: IElementType = ParadoxExpressionTokenType("identifier_token")
		@JvmField val INT_TOKEN: IElementType = ParadoxExpressionTokenType("int_token")
		@JvmField val PIPE: IElementType = ParadoxExpressionTokenType("|")
		@JvmField val PREFIX_TOKEN: IElementType = ParadoxExpressionTokenType("prefix_token")
		@JvmField val STRING_TOKEN: IElementType = ParadoxExpressionTokenType("string_token")
	}
}
