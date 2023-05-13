package icu.windea.pls.lang

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.script.psi.*

object ParadoxModifierHandler {
	val supportKey = Key.create<ParadoxModifierSupport>("paradox.modifier.support")
	
	//可通过运行游戏后输出的modifiers.log判断到底会生成哪些修正
	//不同的游戏类型存在一些通过不同逻辑生成的修正
	//插件使用的modifiers.cwt中应当去除生成的修正
	
	fun matchesModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.DEFAULT): Boolean {
		return ParadoxModifierSupport.matchModifier(name, element, configGroup, matchType)
	}
	
	fun resolveModifier(element: ParadoxScriptStringExpressionElement) : ParadoxModifierElement? {
		val name = element.value
		val gameType = selectGameType(element) ?: return null
		val project = element.project
		val configGroup = getCwtConfig(project).get(gameType)
		return resolveModifier(name, element, configGroup)
	}
	
	fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
		return ParadoxModifierSupport.resolveModifier(name, element, configGroup)
	}
	
	fun completeModifier(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val element = contextElement
		if(element !is ParadoxScriptStringExpressionElement) return
		val modifierNames = mutableSetOf<String>()
		ParadoxModifierSupport.completeModifier(context, result, modifierNames)
	}
	
	fun getModifierCategories(element: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
		return ParadoxModifierSupport.getModifierCategories(element)
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