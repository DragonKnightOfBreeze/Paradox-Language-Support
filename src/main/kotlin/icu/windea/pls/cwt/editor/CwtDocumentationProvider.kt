package icu.windea.pls.cwt.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.util.*

class CwtDocumentationProvider : AbstractDocumentationProvider() {
	override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, `object`: Any?, element: PsiElement?): PsiElement? {
		if(`object` is PsiElement) return `object`
		return super.getDocumentationElementForLookupItem(psiManager, `object`, element)
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		if(link == null || context == null) return null
		return resolveLink(link, context)
	}
	
	override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
		return when(element) {
			is CwtProperty -> getPropertyInfo(element, originalElement)
			is CwtString -> getStringInfo(element, originalElement)
			else -> null
		}
	}
	
	private fun getPropertyInfo(element: CwtProperty, originalElement: PsiElement?): String {
		return buildString {
			val name = element.name
			buildPropertyDefinition(element, originalElement, name)
		}
	}
	
	private fun getStringInfo(element: CwtString, originalElement: PsiElement?): String {
		return buildString {
			val name = element.name
			buildStringDefinition(element, originalElement, name)
		}
	}
	
	override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
		return when(element) {
			is CwtProperty -> getPropertyDoc(element, originalElement)
			is CwtString -> getStringDoc(element, originalElement)
			else -> null
		}
	}
	
	private fun getPropertyDoc(element: CwtProperty, originalElement: PsiElement?): String {
		return buildString {
			val name = element.name
			buildPropertyDefinition(element, originalElement, name)
			buildDocumentationContent(element)
		}
	}
	
	private fun getStringDoc(element: CwtString, originalElement: PsiElement?): String {
		return buildString {
			val name = element.name
			buildStringDefinition(element, originalElement, name)
			buildDocumentationContent(element)
		}
	}
	
	private fun StringBuilder.buildPropertyDefinition(element: CwtProperty, originalElement: PsiElement?, name: String) {
		definition {
			val configType = element.configType
			if(originalElement == null || configType?.isReference == true) {
				if(configType != null) append(configType.text)
				append(" <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
			} else{
				val originalName = if(originalElement is ParadoxScriptPropertyKey) originalElement.value else originalElement.text
				append(PlsDocBundle.message("name.script.definitionProperty"))
				append(" <b>").append(originalName.escapeXmlOrAnonymous()).append("</b>")
				grayed {
					append(" by ").append(name.escapeXmlOrAnonymous())
				}
			}
			
			//基于规则类型提供额外的定义信息
			val project = element.project
			when(configType) {
				//为definitionProperty提供关于scope的额外文档注释（附加scope的psiLink）
				null -> {
					val propertyElement = getDefinitionProperty(originalElement) ?: return@definition
					if(propertyElement.valueType != ParadoxValueType.BlockType) return@definition //仅限block
					val gameType = propertyElement.gameType ?: return@definition
					val config = propertyElement.getPropertyConfig() ?: return@definition
					val scopeMap = CwtConfigHandler.mergeScope(config.scopeMap, propertyElement.definitionElementInfo?.scope)
					for((sk, sv) in scopeMap) {
						val scopeLink = "${gameType.id}.scopes.$sv"
						appendBr().append(PlsDocBundle.message("name.cwt.scope")).append(" ").append(sk).append(" = ").appendCwtLink(sv, scopeLink, null)
					}
				}
				//为alias提供关于scope的额外文档注释（如果有的话）
				CwtConfigType.Alias -> {
					//同名的alias支持的scopes应该是一样的
					val gameType = originalElement?.gameType ?: return@definition
					val configGroup = getCwtConfig(project)[gameType] ?: return@definition
					val index = name.indexOf(':')
					if(index == -1) return@definition
					val aliasName = name.substring(0, index)
					val aliasSubName = name.substring(index + 1)
					val aliasGroup = configGroup.aliases[aliasName] ?: return@definition
					val aliases = aliasGroup[aliasSubName] ?: return@definition
					val supportedScopesText = aliases.firstOrNull()?.supportedScopesText ?: return@definition
					appendBr().append("supported_scopes = $supportedScopesText")
				}
				//为modifier提供关于scope的额外文档注释
				CwtConfigType.Modifier -> {
					val gameType = originalElement?.gameType ?: return@definition
					val configGroup = getCwtConfig(project)[gameType] ?: return@definition
					val categories = element.value?.value ?: return@definition
					val category = configGroup.modifierCategories[categories]
						?: configGroup.modifierCategoryIdMap[categories] ?: return@definition
					val supportedScopesText = category.supportedScopesText
					appendBr().append("supported_scopes = $supportedScopesText")
				}
				//为localisation_command提供关于scope的额外文档注释
				CwtConfigType.LocalisationCommand -> {
					val gameType = originalElement?.gameType ?: return@definition
					val configGroup = getCwtConfig(project)[gameType] ?: return@definition
					val n = element.name
					val localisationCommand = configGroup.localisationCommands[n] ?: return@definition
					val supportedScopesText = localisationCommand.supportedScopes
					appendBr().append("supported_scopes = $supportedScopesText")
				}
				else -> pass()
			}
		}
	}
	
	private fun StringBuilder.buildStringDefinition(element: CwtString, originalElement: PsiElement?, name: String) {
		definition {
			val configType = element.configType
			if(originalElement == null || configType?.isReference == true) {
				if(configType != null) append(configType.text).append(" ")
				append("<b>").append(name.escapeXmlOrAnonymous()).append("</b>")
			} else {
				val originalName = if(originalElement is ParadoxScriptValue) originalElement.value else originalElement.text
				append(PlsDocBundle.message("name.script.definitionValue"))
				append(" <b>").append(originalName.escapeXmlOrAnonymous()).append("</b>")
				grayed {
					append(" by ").append(name.escapeXmlOrAnonymous())
				}
			}
		}
	}
	
	private fun StringBuilder.buildDocumentationContent(element: PsiElement) {
		//渲染文档注释（作为HTML）和版本号信息
		var current: PsiElement = element
		var lines: LinkedList<String>? = null
		var since: String? = null
		var html = false
		while(true) {
			current = current.prevSibling ?: break
			when {
				current is CwtDocumentationComment -> {
					val documentationText = current.documentationText
					if(documentationText != null) {
						if(lines == null) lines = LinkedList()
						val docText = documentationText.text.trimStart('#').trim() //这里接受HTML
						lines.addFirst(docText)
					}
				}
				current is CwtOptionComment -> {
					val option = current.option ?: continue
					when {
						option.name == "since" -> since = option.optionValue
						option.name == "loc_format" && option.value?.value == "html" -> html = true
					}
				}
				current is PsiWhiteSpace || current is PsiComment -> continue
				else -> break
			}
		}
		val documentation = lines?.joinToString("<br>") { if(html) it else it.escapeXml() }
		if(documentation != null) {
			content {
				append(documentation)
			}
		}
		if(since != null) {
			sections {
				section(PlsDocBundle.message("title.since"), since)
			}
		}
	}
	
	private fun getDefinitionProperty(originalElement: PsiElement?): ParadoxScriptProperty? {
		if(originalElement == null) return null
		if(originalElement.language != ParadoxScriptLanguage) return null
		val parent = originalElement.parent ?: return null
		val keyElement = parent as? ParadoxScriptPropertyKey ?: parent.parent as? ParadoxScriptPropertyKey ?: return null
		return keyElement.parent as? ParadoxScriptProperty
	}
}