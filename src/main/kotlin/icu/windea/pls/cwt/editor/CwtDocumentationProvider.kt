package icu.windea.pls.cwt.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.cwt.psi.*
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
			is CwtString -> getStringInfo(element)
			else -> null
		}
	}
	
	private fun getPropertyInfo(element: CwtProperty, originalElement: PsiElement?): String {
		val name = element.name
		return buildString {
			buildPropertyDefinition(element, originalElement, name)
		}
	}
	
	private fun getStringInfo(element: CwtString): String {
		val name = element.name
		return buildString {
			buildStringDefinition(element, name)
		}
	}
	
	override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
		return when(element) {
			is CwtProperty -> getPropertyDoc(element, originalElement)
			is CwtString -> getStringDoc(element)
			else -> null
		}
	}
	
	private fun getPropertyDoc(element: CwtProperty, originalElement: PsiElement?): String {
		val name = element.name
		return buildString {
			buildPropertyDefinition(element, originalElement, name)
			buildDocumentationContent(element)
		}
	}
	
	private fun getStringDoc(element: CwtString): String {
		val name = element.name
		return buildString {
			buildStringDefinition(element, name)
			buildDocumentationContent(element)
		}
	}
	
	private fun StringBuilder.buildPropertyDefinition(element: CwtProperty, originalElement: PsiElement?, name: String) {
		val project = element.project
		val configType = element.configType
		definition {
			if(configType != null) append("(").append(configType.text).append(") ") else append("(property) ")
			append("<b>").append(name.escapeXmlOrAnonymous()).append("</b>")
			when(configType) {
				//为definitionProperty提供关于scope的额外文档注释（附加scope的psiLink）
				null -> {
					val propertyElement = getDefinitionProperty(originalElement) ?: return@definition
					val gameType = propertyElement.gameType ?: return@definition
					val config = propertyElement.propertyConfig ?: return@definition
					val scopeMap = mergeScope(config.scopeMap, propertyElement.definitionPropertyInfo?.scope)
					for((sk, sv) in scopeMap) {
						val scopeLink = "${gameType.id}.scopes.$sv"
						appendBr().append("(scope) ").append(sk).append(" = ").appendCwtLink(scopeLink, sv, null)
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
	
	private fun StringBuilder.buildStringDefinition(element: CwtString, name: String) {
		val configTypeText = element.configType?.text
		definition {
			if(configTypeText != null) append("(").append(configTypeText).append(") ")
			append("<b>").append(name.escapeXmlOrAnonymous()).append("</b>")
		}
	}
	
	private fun StringBuilder.buildDocumentationContent(element: PsiElement) {
		//文档注释，以###开始
		val documentation = getDocumentation(element)
		if(documentation != null) {
			content {
				append(documentation)
			}
		}
	}
	
	private fun getDocumentation(element: PsiElement): String? {
		var current: PsiElement = element
		var documentationLines: LinkedList<String>? = null
		while(true) {
			current = current.prevSibling ?: break
			when {
				current is CwtDocumentationComment -> {
					val documentationText = current.documentationText
					if(documentationText != null) {
						if(documentationLines == null) documentationLines = LinkedList()
						documentationLines.addFirst(documentationText.text)
					}
				}
				current is PsiWhiteSpace || current is PsiComment -> continue
				else -> break
			}
		}
		return documentationLines?.joinToString("\n")
	}
	
	private fun getDefinitionProperty(originalElement: PsiElement?): ParadoxScriptProperty? {
		if(originalElement == null) return null
		val parent = originalElement.parent ?: return null
		val keyElement = parent as? ParadoxScriptPropertyKey ?: parent.parent as? ParadoxScriptPropertyKey ?: return null
		return keyElement.parent as? ParadoxScriptProperty
	}
}