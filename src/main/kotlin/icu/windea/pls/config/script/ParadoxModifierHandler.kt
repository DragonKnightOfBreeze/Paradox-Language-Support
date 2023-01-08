package icu.windea.pls.config.script

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.script.config.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

object ParadoxModifierHandler {
	//可通过运行游戏后输出的modifiers.log判断到底会生成哪些修正
	//修正会由特定的定义类型生成
	//TODO 修正会由经济类型（economic_category）的声明生成
	
	@JvmStatic
	fun matchesModifier(name: String, configGroup: CwtConfigGroup): Boolean {
		val modifierName = name.lowercase()
		//预定义的非生成的修正
		val modifierConfig = configGroup.modifiers[modifierName]?.takeIf { it.template.isEmpty() }
		if(modifierConfig != null) return true
		//生成的修正，生成源可以未定义
		val templateExpression = resolveModifierTemplate(modifierName, configGroup)
		if(templateExpression != null) return true
		return false
	}
	
	@JvmStatic
	fun getModifierInfo(element: ParadoxScriptStringExpressionElement): ParadoxModifierInfo? {
		return getModifierInfoFromCache(element)
	}
	
	private fun getModifierInfoFromCache(element: ParadoxScriptStringExpressionElement): ParadoxModifierInfo? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedModifierInfoKey) {
			val project = element.project
			val value = resolveModifierInfo(element, project)
			//invalidate on any script psi change
			val tracker = PsiModificationTracker.getInstance(project).forLanguage(ParadoxScriptLanguage)
			CachedValueProvider.Result.create(value, tracker)
		}
	}
	
	private fun resolveModifierInfo(element: ParadoxScriptStringExpressionElement, project: Project): ParadoxModifierInfo? {
		val name = element.value
		val gameType = selectGameType(element) ?: return null
		val configGroup = getCwtConfig(project).getValue(gameType)
		val modifierName = name.lowercase()
		//尝试解析为预定义的非生成的修正
		val modifierConfig = configGroup.modifiers[modifierName]?.takeIf { it.template.isEmpty() }
		//尝试解析为生成的修正，生成源未定义时，使用预定义的修正
		val isKey = element is ParadoxScriptPropertyKey
		var generatedModifierConfig: CwtModifierConfig? = null
		val templateExpression = resolveModifierTemplate(modifierName, configGroup, isKey)
		if(templateExpression != null) {
			val canResolve = templateExpression.referenceNodes.all {
				val reference = it.getReference(element)
				reference == null || reference.canResolve()
			}
			if(canResolve) {
				val templateString = templateExpression.template.expressionString
				generatedModifierConfig = configGroup.modifiers[templateString]
			}
		}
		if(modifierConfig == null && generatedModifierConfig == null) return null
		return ParadoxModifierInfo(modifierName, gameType, modifierConfig, generatedModifierConfig, templateExpression)
	}
	
	private fun resolveModifierTemplate(name: String, configGroup: CwtConfigGroup, isKey: Boolean? = null): ParadoxTemplateExpression? {
		val text = name
		val textRange = TextRange.create(0, text.length)
		//不能直接这样做
		//return configGroup.modifiers.values.firstNotNullOfOrNull { config ->
		//	ParadoxTemplateExpression.resolve(text, textRange, config.template, configGroup, isKey)
		//}
		return configGroup.modifierTemplates.firstNotNullOfOrNull { template ->
			ParadoxTemplateExpression.resolve(text, textRange, template, configGroup, isKey)
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