package icu.windea.pls.lang

import com.intellij.codeInsight.completion.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.script.psi.*

object ParadoxModifierHandler {
	//可通过运行游戏后输出的modifiers.log判断到底会生成哪些修正
	//不同的游戏类型存在一些通过不同逻辑生成的修正
	//插件使用的modifiers.cwt中应当去除生成的修正
	
	fun matchesModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.DEFAULT): Boolean {
		//先判断是否存在对应的预定义的非生成的修正
		if(matchesPredefinedModifier(name, configGroup)) return true
		//否则基于解析器逻辑判断
		return ParadoxModifierSupport.matchModifier(name, element, configGroup, matchType)
	}
	
	fun matchesPredefinedModifier(name: String, configGroup: CwtConfigGroup): Boolean {
		val predefinedModifierConfig = configGroup.predefinedModifiers[name]
		if(predefinedModifierConfig != null) return true
		return false
	}
	
	fun resolveModifier(element: ParadoxScriptStringExpressionElement) : ParadoxModifierElement? {
		val name = element.value
		val gameType = selectGameType(element) ?: return null
		val project = element.project
		val configGroup = getCwtConfig(project).get(gameType)
		return resolveModifier(name, element, configGroup)
	}
	
	fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
		//尝试解析为生成的修正
		val generatedModifier = ParadoxModifierSupport.resolveModifier(name, element, configGroup)
		if(generatedModifier != null) return generatedModifier
		//尝试解析为预定义的非生成的修正
		return resolvePredefinedModifier(name, element, configGroup)
	}
	
	fun resolvePredefinedModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
		val predefinedModifierConfig = configGroup.predefinedModifiers[name]
		if(predefinedModifierConfig == null) return null
		val project = configGroup.project
		val gameType = configGroup.gameType ?: return null
		return ParadoxModifierElement(element, name, predefinedModifierConfig, gameType, project)
	}
	
	fun completeModifier(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val element = contextElement
		if(element !is ParadoxScriptStringExpressionElement) return
		val modifierNames = mutableSetOf<String>()
		//提示预定义的修正
		completePredefinedModifier(context, result, modifierNames)
		//提示生成的修正
		ParadoxModifierSupport.completeModifier(context, result, modifierNames)
	}
	
	fun completePredefinedModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>): Unit = with(context) {
		val element = contextElement
		if(element !is ParadoxScriptStringExpressionElement) return
		val modifiers = configGroup.predefinedModifiers
		if(modifiers.isEmpty()) return
		for(modifierConfig in modifiers.values) {
			//排除重复的
			if(!modifierNames.add(modifierConfig.name)) continue
			
			//排除不匹配modifier的supported_scopes的情况
			val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, modifierConfig.supportedScopes, configGroup)
			if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
			
			val tailText = ParadoxConfigHandler.getScriptExpressionTailText(modifierConfig.config, withExpression = false)
			val template = modifierConfig.template
			if(template.isNotEmpty()) continue
			val typeFile = modifierConfig.pointer.containingFile
			val name = modifierConfig.name
			val modifierElement = resolvePredefinedModifier(name, element, configGroup)
			val builder = ParadoxScriptExpressionLookupElementBuilder.create(modifierElement, name)
				.withIcon(PlsIcons.Modifier)
				.withTailText(tailText)
				.withTypeText(typeFile?.name)
				.withTypeIcon(typeFile?.icon)
				.withScopeMatched(scopeMatched)
			//.withPriority(PlsCompletionPriorities.modifierPriority)
			result.addScriptExpressionElement(context, builder)
		}
	}
	
	fun getModifierCategories(element: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
		val modifierCategories = ParadoxModifierSupport.getModifierCategories(element)
		if(modifierCategories != null) return modifierCategories
		return element.modifierConfig?.categoryConfigMap
	}
	
	//TODO 检查修正的相关本地化和图标到底是如何确定的
	
	fun getModifierNameKeys(modifierName: String): List<String> {
		//mod_$, ALL_UPPER_CASE is ok.
		return buildList {
			val nameKey = "mod_${modifierName}"
			add(nameKey)
			add(nameKey.uppercase())
		}
	}
	
	fun getModifierDescKeys(modifierName: String): List<String> {
		//mod_$_desc, ALL_UPPER_CASE is ok.
		return buildList {
			val descKey = "mod_${modifierName}_desc"
			add(descKey)
			add(descKey.uppercase())
		}
	}
	
	fun getModifierIconPaths(modifierName: String): List<String> {
		//gfx/interface/icons/modifiers/mod_$.dds
		return buildList {
			add("gfx/interface/icons/modifiers/mod_${modifierName}.dds")
		}
	}
	
	//documentation helper methods
	
	fun getCategoriesText(categories: Set<String>, gameType: ParadoxGameType?, contextElement: PsiElement): String {
		return buildString {
			var appendSeparator = false
			append("<code>")
			for(category in categories) {
				if(appendSeparator) append(", ") else appendSeparator = true
				appendCwtLink("${gameType.linkToken}modifier_categories/$category", category, contextElement)
			}
			append("</code>")
		}
	}
	
	fun getScopeText(scopeId: String, gameType: ParadoxGameType?, contextElement: PsiElement): String {
		return buildString {
			append("<code>")
			ParadoxScopeHandler.buildScopeDoc(scopeId, gameType, contextElement, this)
			append("</code>")
		}
	}
	
	fun getScopesText(scopeIds: Set<String>, gameType: ParadoxGameType?, contextElement: PsiElement): String {
		return buildString {
			var appendSeparator = false
			append("<code>")
			for(scopeId in scopeIds) {
				if(appendSeparator) append(", ") else appendSeparator = true
				ParadoxScopeHandler.buildScopeDoc(scopeId, gameType, contextElement, this)
			}
			append("</code>")
		}
	}
}