package icu.windea.pls.script.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.expression.reference.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.reference.*

class ParadoxScriptScopeExpressionInfo(
	text: String,
	textRange: TextRange,
	directlyResolved: PsiElement?,
	private val possiblePrefixSet: Set<String>? = null
) : ParadoxScriptExpressionInfo(text, textRange, directlyResolved) {
	override fun getReference(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): ParadoxScriptScopeReference {
		return ParadoxScriptScopeReference(element, textRange, directlyResolved)
	}
	
	override fun getUnresolvedError(): ParadoxScriptExpressionError {
		if(possiblePrefixSet.isNullOrEmpty()) {
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
	directlyResolvedList: List<PsiElement>?
) : ParadoxScriptExpressionInfo(text, textRange, null, directlyResolvedList) {
	override fun getReference(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): ParadoxScriptScopeFieldPrefixReference {
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
	override fun getReference(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): ParadoxScriptScopeFieldDataSourceReference {
		return ParadoxScriptScopeFieldDataSourceReference(element, textRange, linkConfigs)
	}
	
	override fun isUnresolved(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): Boolean {
		//特殊处理可能是value的情况
		if(linkConfigs.any { it.dataSource?.type == CwtDataTypes.Value }) return false
		return super.isUnresolved(element, config)
	}
	
	override fun getUnresolvedError(): ParadoxScriptExpressionError {
		val dataSourcesText = linkConfigs.joinToString { "'${it.dataSource!!.value!!}'" }
		return ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.scopeField.unresolvedDs", text, dataSourcesText), textRange, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
	}
	
	override fun getAttributesKeyExpressions(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): List<CwtValueExpression> {
		val result = getReference(element, config).multiResolve(false)
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
	private val possiblePrefixSet: Set<String>? = null
) : ParadoxScriptExpressionInfo(text, textRange, directlyResolved) {
	override fun getReference(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): ParadoxScriptValueOfValueFieldReference {
		return ParadoxScriptValueOfValueFieldReference(element, textRange, directlyResolved)
	}
	
	override fun getUnresolvedError(): ParadoxScriptExpressionError {
		if(possiblePrefixSet.isNullOrEmpty()) {
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
	directlyResolvedList: List<PsiElement>?
) : ParadoxScriptExpressionInfo(text, textRange, null, directlyResolvedList) {
	override fun getReference(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): ParadoxScriptValueFieldPrefixReference {
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
	override fun getReference(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): ParadoxScriptValueFieldDataSourceReference {
		return ParadoxScriptValueFieldDataSourceReference(element, textRange, linkConfigs)
	}
	
	override fun isUnresolved(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): Boolean {
		//特殊处理可能是value的情况
		if(linkConfigs.any { it.dataSource?.type == CwtDataTypes.Value }) return false
		return super.isUnresolved(element, config)  
	}
	
	override fun getUnresolvedError(): ParadoxScriptExpressionError {
		val dataSourcesText = linkConfigs.joinToString { "'${it.dataSource!!.value!!}'" }
		return ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.valueField.unresolvedDs", text, dataSourcesText), textRange, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
	}
	
	override fun getAttributesKeyExpressions(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): List<CwtValueExpression> {
		val result = getReference(element, config).multiResolve(false)
			.filterIsInstance<ParadoxScriptValueFieldDataSourceResolveResult>()
			.map { it.expression }
		if(result.isNotEmpty()) return result
		//特殊处理可能是value的情况
		return linkConfigs.mapNotNull { it.dataSource }.filter { it.type == CwtDataTypes.Value }
	}
}

class ParadoxScriptSvParameterExpressionInfo(
	text: String,
	textRange: TextRange,
	val svName: String,
	val configGroup: CwtConfigGroup
) : ParadoxScriptExpressionInfo(text, textRange) {
	override fun getAttributesKey(): TextAttributesKey {
		return ParadoxScriptAttributesKeys.ARGUMENT_KEY
	}
	
	override fun getReference(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): PsiReference {
		return ParadoxOuterParameterReference(element, textRange, svName, "script_value", configGroup)
	}
	
	override fun isUnresolved(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): Boolean {
		return false
	}
	
	//TODO unused SV parameter -> put to UnusedParameterInspection
}

class ParadoxScriptSvParameterValueExpressionInfo(
	text: String,
	textRange: TextRange
) : ParadoxScriptExpressionInfo(text, textRange) {
	override fun getAttributesKey(): TextAttributesKey {
		return if(ParadoxExpressionType.resolve(text).isFloatType()) ParadoxScriptAttributesKeys.NUMBER_KEY else ParadoxScriptAttributesKeys.STRING_KEY
	}
}

class ParadoxScriptValueSetValueExpressionInfo(
	text: String,
	textRange: TextRange
): ParadoxScriptExpressionInfo(text, textRange){
	override fun getReference(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): PsiReference {
		return ParadoxScriptValueSetValueReference(element, textRange, text, config)
	}
	
	override fun isUnresolved(element: ParadoxExpressionAwareElement, config: CwtDataConfig<*>): Boolean {
		return false
	}
	
	override fun getAttributesKey(): TextAttributesKey {
		return ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
	}
	
	//TODO unused value set value -> put to UnusedValueSetValueInspection
}