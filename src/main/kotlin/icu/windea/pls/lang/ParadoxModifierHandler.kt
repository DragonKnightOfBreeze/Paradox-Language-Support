package icu.windea.pls.lang

import com.intellij.codeInsight.completion.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.lang.support.*
import icu.windea.pls.script.psi.*

object ParadoxModifierHandler {
	//可通过运行游戏后输出的modifiers.log判断到底会生成哪些修正
	//修正会由特定的定义类型生成
	//对于Stellaris：修正会由经济类型（economic_category）的声明生成
	
	@JvmStatic
	fun matchesModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.ALL): Boolean {
		//先判断是否存在对应的预定义的非生成的修正
		if(matchesPredefinedModifier(name, configGroup)) return true
		//否则基于解析器逻辑判断
		return ParadoxModifierSupport.matchModifier(name, element, configGroup, matchType)
	}
	
	@JvmStatic
	fun matchesPredefinedModifier(name: String, configGroup: CwtConfigGroup): Boolean {
		val predefinedModifierConfig = configGroup.predefinedModifiers[name]
		if(predefinedModifierConfig != null) return true
		return false
	}
	
	@JvmStatic
	fun resolveModifier(element: ParadoxScriptStringExpressionElement) : ParadoxModifierElement? {
		val name = element.value
		val gameType = selectGameType(element) ?: return null
		val project = element.project
		val configGroup = getCwtConfig(project).getValue(gameType)
		return resolveModifier(name, element, configGroup)
	}
	
	@JvmStatic
	fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
		//当任何可能包含生成源的脚本文件发生变化时清空缓存 - 应当兼容name和configGroup的变化
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedModifierElementKey) {
			val value = doResolveModifier(name, element, configGroup)
			val tracker = ParadoxModificationTrackerProvider.getInstance().Modifier
			CachedValueProvider.Result.create(value, tracker)
		}
	}
	
	private fun doResolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
		//尝试解析为生成的修正
		val generatedModifier = ParadoxModifierSupport.resolveModifier(name, element, configGroup)
		if(generatedModifier != null) return generatedModifier
		//尝试解析为预定义的非生成的修正
		return resolvePredefinedModifier(name, element, configGroup)
	}
	
	@JvmStatic
	fun resolvePredefinedModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
		val predefinedModifierConfig = configGroup.predefinedModifiers[name]
		if(predefinedModifierConfig == null) return null
		val project = configGroup.project
		val gameType = configGroup.gameType ?: return null
		return ParadoxModifierElement(element, name, predefinedModifierConfig, gameType, project)
	}
	
	@JvmStatic
	fun completeModifier(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val element = contextElement
		if(element !is ParadoxScriptStringExpressionElement) return
		val modifierNames = mutableSetOf<String>()
		//提示生成的修饰符
		ParadoxModifierSupport.completeModifier(context, result, modifierNames)
		//提示预定义的修饰符
		completePredefinedModifier(context, result, modifierNames)
	}
	
	@JvmStatic
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
			
			val tailText = CwtConfigHandler.getScriptExpressionTailText(modifierConfig.config, withExpression = false)
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
	
	//TODO 检查修正的相关本地化和图标到底是如何确定的
	
	@JvmStatic
	fun getModifierNameKeys(modifierName: String, configGroup: CwtConfigGroup): List<String> {
		//mod_$, mod_country_$, ALL_UPPER_CASE is ok.
		val modifier = configGroup.modifiers[modifierName]
		val isCountryModifier = isCountryModifier(modifierName, modifier)
		return buildList {
			val nameKey = "mod_${modifierName}"
			add(nameKey)
			add(nameKey.uppercase())
			if(isCountryModifier) {
				val countryNameKey = "mod_country_${modifierName}"
				add(countryNameKey)
				add(countryNameKey.uppercase())
			}
		}
	}
	
	@JvmStatic
	fun getModifierDescKeys(modifierName: String, configGroup: CwtConfigGroup): List<String> {
		//mod_$_desc, mod_country_$_desc, ALL_UPPER_CASE is ok.
		val modifier = configGroup.modifiers[modifierName]
		val isCountryModifier = isCountryModifier(modifierName, modifier)
		return buildList {
			val descKey = "mod_${modifierName}_desc"
			add(descKey)
			add(descKey.uppercase())
			if(isCountryModifier) {
				val countryDescKey = "mod_country_${modifierName}_desc"
				add(countryDescKey)
				add(countryDescKey.uppercase())
			}
		}
	}
	
	@JvmStatic
	fun getModifierIconPaths(modifierName: String, configGroup: CwtConfigGroup): List<String> {
		//gfx/interface/icons/modifiers/mod_$.dds
		//gfx/interface/icons/modifiers/mod_country_$.dds
		val modifier = configGroup.modifiers[modifierName]
		val isCountryModifier = isCountryModifier(modifierName, modifier)
		return buildList {
			add("gfx/interface/icons/modifiers/mod_${modifierName}.dds")
			if(isCountryModifier) {
				add("gfx/interface/icons/modifiers/mod_country_${modifierName}.dds")
			}
		}
	}
	
	private fun isCountryModifier(modifierName: String, modifier: CwtModifierConfig?): Boolean {
		return (!modifierName.startsWith("country_")
			&& (modifier != null && modifier.categories.any { it.equals("country", true) || it.equals("countries", true) }))
	}
}