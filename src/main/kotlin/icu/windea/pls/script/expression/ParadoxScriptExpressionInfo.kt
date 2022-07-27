package icu.windea.pls.script.expression

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.expression.reference.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

sealed class ParadoxScriptExpressionInfo(
	val text: String,
	val textRange: TextRange
) {
	abstract fun getReference(element: ParadoxScriptExpressionElement): PsiReference
	
	abstract fun getAttributesKey(): TextAttributesKey?
}

class ParadoxScriptScopeExpressionInfo(
	text: String,
	textRange: TextRange,
	val resolved: PsiElement?
) : ParadoxScriptExpressionInfo(text, textRange) {
	override fun getReference(element: ParadoxScriptExpressionElement): PsiReference {
		return ParadoxScriptScopeReference(element, textRange, resolved)
	}
	
	override fun getAttributesKey(): TextAttributesKey {
		return when {
			resolved is CwtProperty -> {
				when(CwtConfigType.resolve(resolved)) {
					CwtConfigType.SystemScope -> ParadoxScriptAttributesKeys.SYSTEM_SCOPE_KEY
					CwtConfigType.Scope -> ParadoxScriptAttributesKeys.SCOPE_KEY
					else -> ParadoxScriptAttributesKeys.SCOPE_KEY
				}
			}
			else -> ParadoxScriptAttributesKeys.SCOPE_KEY
		}
	}
}