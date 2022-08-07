package icu.windea.pls.script.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.expression.reference.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

sealed class ParadoxScriptExpressionInfo(
	val text: String,
	val textRange: TextRange,
	val directlyResolved: PsiElement? = null,
	val directlyResolvedList: List<PsiElement>? = null
) {
	abstract fun getReference(element: ParadoxScriptExpressionElement): PsiReference
	
	open fun isUnresolved(element: ParadoxScriptExpressionElement): Boolean {
		if(directlyResolved == null) return true
		val reference = getReference(element)
		if(reference is PsiPolyVariantReference) return reference.multiResolve(false).isEmpty()
		return reference.resolve() == null
	}
	
	open fun getUnresolvedError(): ParadoxScriptExpressionError? = null
	
	open fun getAttributesKey(): TextAttributesKey? = null
	
	open fun getAttributesKeyExpressions(element: ParadoxScriptExpressionElement): List<CwtKvExpression> = emptyList()
}

class ParadoxScriptScopeExpressionInfo(
	text: String,
	textRange: TextRange,
	directlyResolved: PsiElement?,
	private val possiblePrefixSet: MutableSet<String>? = null
) : ParadoxScriptExpressionInfo(text, textRange, directlyResolved) {
	override fun getReference(element: ParadoxScriptExpressionElement): ParadoxScriptScopeReference {
		return ParadoxScriptScopeReference(element, textRange, directlyResolved)
	}
	
	override fun getUnresolvedError(): ParadoxScriptExpressionError {
		if(possiblePrefixSet.isNullOrEmpty()){
			return ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.scope.unresolvedScope", text), textRange, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
		} else {
			val possiblePrefixListText = possiblePrefixSet.take(3).joinToString(limit = 3) { "'$it'" } 
			return ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.scope.unresolvedScope.1", text, possiblePrefixListText), textRange, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
		}
	}
	
	override fun getAttributesKey(): TextAttributesKey {
		return when {
			directlyResolved is CwtProperty -> {
				when(CwtConfigType.resolve(directlyResolved)) {
					CwtConfigType.SystemScope -> ParadoxScriptAttributesKeys.SYSTEM_SCOPE_KEY
					CwtConfigType.Scope -> ParadoxScriptAttributesKeys.SCOPE_KEY
					else -> ParadoxScriptAttributesKeys.SCOPE_KEY
				}
			}
			else -> ParadoxScriptAttributesKeys.SCOPE_KEY
		}
	}
}

class ParadoxScriptScopeFieldPrefixExpressionInfo(
	text: String,
	textRange: TextRange,
	directlyResolvedList: List<PsiElement>?,
	val linkConfigs: List<CwtLinkConfig>
) : ParadoxScriptExpressionInfo(text, textRange, null, directlyResolvedList) {
	override fun getReference(element: ParadoxScriptExpressionElement): ParadoxScriptScopeFieldPrefixReference {
		return ParadoxScriptScopeFieldPrefixReference(element, textRange, directlyResolvedList)
	}
	
	override fun getAttributesKey(): TextAttributesKey {
		return ParadoxScriptAttributesKeys.SCOPE_FIELD_PREFIX_KEY
	}
}

class ParadoxScriptScopeFieldDataSourceExpressionInfo(
	text: String,
	textRange: TextRange,
	val linkConfigs: List<CwtLinkConfig>
) : ParadoxScriptExpressionInfo(text, textRange) {
	val sortedLinkConfigs = linkConfigs.sortedByDescending { it.dataSource!!.priority } //需要按照优先级重新排序
	
	override fun getReference(element: ParadoxScriptExpressionElement): ParadoxScriptScopeFieldDataSourceReference {
		return ParadoxScriptScopeFieldDataSourceReference(element, textRange, sortedLinkConfigs)
	}
	
	override fun isUnresolved(element: ParadoxScriptExpressionElement): Boolean {
		//特殊处理可能是value的情况
		if(linkConfigs.any { it.dataSource?.type == CwtDataTypes.Value }) return false
		return super.isUnresolved(element)
	}
	
	override fun getUnresolvedError(): ParadoxScriptExpressionError {
		val dataSourcesText = linkConfigs.joinToString { "'${it.dataSource!!.value!!}'" }
		return ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.scopeField.unresolvedDs", text, dataSourcesText), textRange, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
	}
	
	override fun getAttributesKeyExpressions(element: ParadoxScriptExpressionElement): List<CwtValueExpression> {
		val result = getReference(element).multiResolve(false)
			.filterIsInstance<ParadoxScriptScopeFieldDataSourceResolveResult>()
			.map { it.expression }
		if(result.isNotEmpty()) return result
		//特殊处理可能是value的情况
		return linkConfigs.mapNotNull { it.dataSource }.filter { it.type == CwtDataTypes.Value }
	}
}

class ParadoxScriptValueOfValueFieldExpressionInfo(
	text: String,
	textRange: TextRange,
	directlyResolved: PsiElement?,
	private val possiblePrefixSet: MutableSet<String>? = null
) : ParadoxScriptExpressionInfo(text, textRange, directlyResolved) {
	override fun getReference(element: ParadoxScriptExpressionElement): ParadoxScriptValueOfValueFieldReference {
		return ParadoxScriptValueOfValueFieldReference(element, textRange, directlyResolved)
	}
	
	override fun getUnresolvedError(): ParadoxScriptExpressionError {
		if(possiblePrefixSet.isNullOrEmpty()){
			return ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.value.unresolvedValue", text), textRange, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
		} else {
			val possiblePrefixListText = possiblePrefixSet.take(3).joinToString(limit = 3) { "'$it'" }
			return ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.value.unresolvedValue.1", text, possiblePrefixListText), textRange, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
		}
	}
	
	override fun getAttributesKey(): TextAttributesKey {
		return when {
			directlyResolved is CwtProperty -> {
				when(CwtConfigType.resolve(directlyResolved)) {
					CwtConfigType.SystemScope -> ParadoxScriptAttributesKeys.SYSTEM_SCOPE_KEY
					CwtConfigType.Scope -> ParadoxScriptAttributesKeys.SCOPE_KEY
					else -> ParadoxScriptAttributesKeys.SCOPE_KEY
				}
			}
			else -> ParadoxScriptAttributesKeys.SCOPE_KEY
		}
	}
}

class ParadoxScriptValueFieldPrefixExpressionInfo(
	text: String,
	textRange: TextRange,
	directlyResolvedList: List<PsiElement>?,
	val linkConfigs: List<CwtLinkConfig>
) : ParadoxScriptExpressionInfo(text, textRange, null, directlyResolvedList) {
	override fun getReference(element: ParadoxScriptExpressionElement): ParadoxScriptValueFieldPrefixReference {
		return ParadoxScriptValueFieldPrefixReference(element, textRange, directlyResolvedList)
	}
	
	override fun getAttributesKey(): TextAttributesKey {
		return ParadoxScriptAttributesKeys.VALUE_FIELD_PREFIX_KEY
	}
}

class ParadoxScriptValueFieldDataSourceExpressionInfo(
	text: String,
	textRange: TextRange,
	val linkConfigs: List<CwtLinkConfig>
) : ParadoxScriptExpressionInfo(text, textRange) {
	val sortedLinkConfigs = linkConfigs.sortedByDescending { it.dataSource!!.priority } //需要按照优先级重新排序
	
	override fun getReference(element: ParadoxScriptExpressionElement): ParadoxScriptValueFieldDataSourceReference {
		return ParadoxScriptValueFieldDataSourceReference(element, textRange, sortedLinkConfigs)
	}
	
	override fun isUnresolved(element: ParadoxScriptExpressionElement): Boolean {
		//特殊处理可能是value的情况
		if(linkConfigs.any { it.dataSource?.type == CwtDataTypes.Value }) return false
		return super.isUnresolved(element)
	}
	
	override fun getUnresolvedError(): ParadoxScriptExpressionError {
		val dataSourcesText = linkConfigs.joinToString { "'${it.dataSource!!.value!!}'" }
		return ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.valueField.unresolvedDs", text, dataSourcesText), textRange, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
	}
	
	override fun getAttributesKeyExpressions(element: ParadoxScriptExpressionElement): List<CwtValueExpression> {
		val result = getReference(element).multiResolve(false)
			.filterIsInstance<ParadoxScriptValueFieldDataSourceResolveResult>()
			.map { it.expression }
		if(result.isNotEmpty()) return result
		//特殊处理可能是value的情况
		return linkConfigs.mapNotNull { it.dataSource }.filter { it.type == CwtDataTypes.Value }
	}
}