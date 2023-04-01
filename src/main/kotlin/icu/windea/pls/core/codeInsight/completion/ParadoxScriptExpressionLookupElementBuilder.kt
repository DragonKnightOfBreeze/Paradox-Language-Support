package icu.windea.pls.core.codeInsight.completion

import com.intellij.psi.*
import javax.swing.*

class ParadoxScriptExpressionLookupElementBuilder(
	val element: PsiElement?,
	val lookupString: String,
) {
	var icon: Icon? = null
	var presentableText: String? = null
	var tailText: String? = null
	var typeText: String? = null
	var typeIcon: Icon? = null
	var priority: Double? = null
	var scopeMatched: Boolean = true
	var forceInsertCurlyBraces: Boolean = false
	var bold: Boolean = false
	var caseSensitive: Boolean = true
	
	companion object {
		@JvmStatic
		fun create(lookupString: String): ParadoxScriptExpressionLookupElementBuilder {
			return ParadoxScriptExpressionLookupElementBuilder(null, lookupString)
		}
		
		@JvmStatic
		fun create(element: PsiElement?, lookupString: String): ParadoxScriptExpressionLookupElementBuilder {
			return ParadoxScriptExpressionLookupElementBuilder(element, lookupString)
		}
	}
	
	fun withIcon(icon: Icon?) = apply { this.icon = icon }
	fun withPresentableText(presentableText: String?) = apply { this.presentableText = presentableText }
	fun withTailText(tailText: String?) = apply { this.tailText = tailText }
	fun withTypeText(typeText: String?) = apply { this.typeText = typeText }
	fun withTypeIcon(typeIcon: Icon?) = apply { this.typeIcon = typeIcon }
	fun withPriority(priority: Double?) = apply { this.priority = priority }
	fun withScopeMatched(scopeMatched: Boolean) = apply { this.scopeMatched = scopeMatched }
	fun withForceInsertCurlyBraces(forceInsertCurlyBraces: Boolean) = apply { this.forceInsertCurlyBraces = forceInsertCurlyBraces }
	fun bold() = apply { this.bold = true }
	fun caseInsensitive() = apply { this.caseSensitive = false }
}