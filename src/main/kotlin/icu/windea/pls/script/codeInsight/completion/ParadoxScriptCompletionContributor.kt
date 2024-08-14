package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

class ParadoxScriptCompletionContributor : CompletionContributor() {
	init {
		//当用户可能正在输入一个关键字（布尔值或子句）时提示
		val keywordPattern = psiElement()
			.withElementType(ParadoxScriptTokenSets.STRING_TOKENS)
			.withParent(psiElement(ParadoxScriptString::class.java))
		extend(CompletionType.BASIC, keywordPattern, ParadoxKeywordCompletionProvider())
		
		//当用户可能正在输入一个scriptedVariableReference的名字时提示
		val scriptedVariableReferencePattern = psiElement()
			.withElementType(ParadoxScriptTokenSets.SCRIPTED_VARIABLE_REFERENCE_TOKENS)
		extend(scriptedVariableReferencePattern, ParadoxScriptedVariableCompletionProvider())
		
		//当用户可能正在输入一个scriptExpression时提示
		val expressionPattern = psiElement()
			.withElementType(ParadoxScriptTokenSets.KEY_OR_STRING_TOKENS)
		extend(expressionPattern, ParadoxScriptExpressionCompletionProvider())
		
		//当用户可能正在输入一个eventId时提示
		val eventIdPattern = psiElement()
			.withElementType(ParadoxScriptTokenSets.STRING_TOKENS)
			.withParent(psiElement(ParadoxScriptString::class.java)
				.withParent(psiElement(ParadoxScriptProperty::class.java)
					.withParent(psiElement(ParadoxScriptBlock::class.java)
						.withParent(psiElement(ParadoxScriptProperty::class.java)))))
		extend(eventIdPattern, ParadoxEventIdCompletionProvider())
		
		//当用户可能正在输入一个parameter的名字时提示
		val parameterPattern = psiElement()
			.withElementType(ParadoxScriptTokenSets.PARAMETER_TOKENS)
		extend(parameterPattern, ParadoxParameterCompletionProvider())
        
        //当用户可能正在输入内联脚本调用的key（即"inline_script"）使提示
        val inlineScriptInvocationPattern = psiElement()
            .withElementType(ParadoxScriptTokenSets.KEY_OR_STRING_TOKENS)
        extend(inlineScriptInvocationPattern, ParadoxInlineScriptInvocationCompletionProvider())
		
		//当用户可能正在输入一个scriptedVariable的名字时提示（除非用户也可能正在输入一个引用的名字）
		val scriptedVariableNamePattern = psiElement()
			.withElementType(ParadoxScriptTokenSets.SCRIPTED_VARIABLE_NAME_TOKENS)
		extend(CompletionType.BASIC, scriptedVariableNamePattern, ParadoxScriptedVariableNameCompletionProvider())
		
		//当用户可能正在输入一个定义的名字时提示
		val definitionNamePattern = psiElement()
			.withElementType(ParadoxScriptTokenSets.KEY_OR_STRING_TOKENS)
		extend(CompletionType.BASIC, definitionNamePattern, ParadoxDefinitionNameCompletionProvider())
		
		//当用户可能正在输入一个变量名时提示
		val variableNamePattern = psiElement()
			.withElementType(ParadoxScriptTokenSets.STRING_TOKENS)
		extend(CompletionType.BASIC, variableNamePattern, ParadoxVariableNameCompletionProvider())
	}
	
	override fun beforeCompletion(context: CompletionInitializationContext) {
		context.dummyIdentifier = PlsConstants.dummyIdentifier
	}
	
	@Suppress("RedundantOverride")
	override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
		super.fillCompletionVariants(parameters, result)
	}
}
