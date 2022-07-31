package icu.windea.pls.cwt.editor

import com.intellij.lang.documentation.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.*
import icu.windea.pls.util.selector.*
import java.util.*

class CwtDocumentationProvider : AbstractDocumentationProvider() {
	override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, `object`: Any?, element: PsiElement?): PsiElement? {
		if(`object` is PsiElement) return `object`
		return super.getDocumentationElementForLookupItem(psiManager, `object`, element)
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		if(link == null || context == null) return null
		return resolveScope(link, context)
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
			val configType = CwtConfigType.resolve(element)
			buildPropertyDefinition(element, originalElement, name, configType, false)
		}
	}
	
	private fun getStringInfo(element: CwtString, originalElement: PsiElement?): String {
		return buildString {
			val name = element.name
			val configType = CwtConfigType.resolve(element)
			buildStringDefinition(element, originalElement, name, configType, false)
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
			val configType = CwtConfigType.resolve(element)
			val project = element.project
			buildPropertyDefinition(element, originalElement, name, configType, true)
			buildLocalisationContent(element, name, configType, project)
			buildDocumentationContent(element)
			buildTypeBasedContent(element, originalElement, name, configType, project)
			buildSupportedScopesContent(element, originalElement, name, configType, project)
		}
	}
	
	private fun getStringDoc(element: CwtString, originalElement: PsiElement?): String {
		return buildString {
			val name = element.name
			val configType = CwtConfigType.resolve(element)
			val project = element.project
			buildStringDefinition(element, originalElement, name, configType, true)
			buildLocalisationContent(element, name, configType, project)
			buildDocumentationContent(element)
		}
	}
	
	private fun StringBuilder.buildPropertyDefinition(element: CwtProperty, originalElement: PsiElement?, name: String, configType: CwtConfigType?, showDetail: Boolean) {
		definition {
			if(originalElement?.language != ParadoxScriptLanguage || configType?.isReference == true) {
				if(configType != null) append(configType.text)
				append(" <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
				//加上类型信息
				if(configType?.hasType == true) {
					val typeName = element.parentOfType<CwtProperty>()?.name?.substringIn('[', ']')?.takeIfNotEmpty()
					if(typeName != null && typeName.isNotEmpty()) {
						append(": ").append(typeName)
					}
				}
			} else {
				val prefix = when {
					originalElement is ParadoxScriptPropertyKey || originalElement.parent is ParadoxScriptPropertyKey -> PlsDocBundle.message("name.script.definitionProperty")
					originalElement is ParadoxScriptValue || originalElement.parent is ParadoxScriptValue -> PlsDocBundle.message("name.script.definitionValue")
					else -> null
				}
				val originalName = originalElement.text.unquote()
				if(prefix != null) append(prefix)
				append(" <b>").append(originalName.escapeXmlOrAnonymous()).append("</b>")
				if(showDetail && name != originalName) { //这里不忽略大小写
					grayed {
						append(" by ").append(name.escapeXmlOrAnonymous())
					}
				}
			}
			
			//基于规则类型提供额外的定义信息
			//val project = element.project
			//when(configType) {
			//	//为definitionProperty提供关于scope的额外文档注释（附加scope的psiLink）
			//	null -> {
			//		val propertyElement = getDefinitionProperty(originalElement) ?: return@definition
			//		if(propertyElement.valueType != ParadoxValueType.BlockType) return@definition //仅限block
			//		val gameType = propertyElement.fileInfo?.gameType ?: return@definition
			//		val config = propertyElement.getPropertyConfig() ?: return@definition
			//		val scopeMap = CwtConfigHandler.mergeScope(config.scopeMap, propertyElement.definitionElementInfo?.scope)
			//		for((sk, sv) in scopeMap) {
			//			val scopeLink = "${gameType.id}.scopes.$sv"
			//			appendBr().append(PlsDocBundle.message("name.cwt.scope")).append(" ").append(sk).append(" = ").appendCwtLink(sv, scopeLink, null)
			//		}
			//	}
			//	//为alias提供supported_scopes的额外文档注释（如果有的话）
			//	CwtConfigType.Alias -> {
			//		//同名的alias支持的scopes应该是一样的
			//		val gameType = originalElement?.let { it.fileInfo?.gameType } ?: return@definition
			//		val configGroup = getCwtConfig(project)[gameType] ?: return@definition
			//		val index = name.indexOf(':')
			//		if(index == -1) return@definition
			//		val aliasName = name.substring(0, index)
			//		val aliasSubName = name.substring(index + 1)
			//		val aliasGroup = configGroup.aliases[aliasName] ?: return@definition
			//		val aliases = aliasGroup[aliasSubName] ?: return@definition
			//		val supportedScopesText = aliases.firstOrNull()?.supportedScopesText ?: return@definition
			//		appendBr().append("supported_scopes = $supportedScopesText")
			//	}
			//	else -> pass()
			//}
		}
	}
	
	private fun StringBuilder.buildStringDefinition(element: CwtString, originalElement: PsiElement?, name: String, configType: CwtConfigType?, showDetail: Boolean) {
		definition {
			if(originalElement?.language != ParadoxScriptLanguage || configType?.isReference == true) {
				if(configType != null) append(configType.text).append(" ")
				append("<b>").append(name.escapeXmlOrAnonymous()).append("</b>")
				//加上类型信息
				if(configType?.hasType == true) {
					val typeName = element.parentOfType<CwtProperty>()?.name?.substringIn('[', ']')?.takeIfNotEmpty()
					if(typeName != null && typeName.isNotEmpty()) {
						append(": ").append(typeName)
					}
				}
			} else {
				val prefix = when {
					originalElement is ParadoxScriptPropertyKey || originalElement.parent is ParadoxScriptPropertyKey -> PlsDocBundle.message("name.script.definitionProperty")
					originalElement is ParadoxScriptValue || originalElement.parent is ParadoxScriptValue -> PlsDocBundle.message("name.script.definitionValue")
					else -> null
				}
				val originalName = originalElement.text.unquote()
				if(prefix != null) append(prefix)
				append(" <b>").append(originalName.escapeXmlOrAnonymous()).append("</b>")
				if(showDetail && name != originalName) { //这里不忽略大小写
					grayed {
						append(" by ").append(name.escapeXmlOrAnonymous())
					}
				}
			}
		}
	}
	
	private fun StringBuilder.buildLocalisationContent(element: PsiElement, name: String, configType: CwtConfigType?, project: Project) {
		val locationExpression = configType?.localisation?.let { CwtLocalisationLocationExpression.resolve(it) } ?: return
		val key = locationExpression.resolvePlaceholder(name) ?: return
		val selector = localisationSelector().gameTypeFrom(element).preferRootFrom(element).preferLocale(preferredParadoxLocale())
		val localisation = findLocalisation(key, project, selector = selector) ?: return
		content {
			ParadoxLocalisationTextRenderer.renderTo(localisation, this)
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
	
	private fun StringBuilder.buildTypeBasedContent(element: CwtProperty, originalElement: PsiElement?, name: String, configType: CwtConfigType?, project: Project) {
		when(configType) {
			//为scope（link）显示名字、描述、输入作用域、输出作用域
			CwtConfigType.Link -> {
				val gameType = originalElement?.let { it.fileInfo?.gameType } ?: return
				val configGroup = getCwtConfig(project)[gameType] ?: return
				val linkConfig = configGroup.linksNotData[name] ?: return
				val nameToUse = CwtConfigHandler.getScopeName(name, configGroup)
				val descToUse = linkConfig.desc
				val inputScopeNamesToUse = linkConfig.inputScopes?.joinToString { CwtConfigHandler.getScopeName(it, configGroup) }
				val outputScopeNameToUse = linkConfig.outputScope?.let { CwtConfigHandler.getScopeName(it, configGroup) }
				if(inputScopeNamesToUse == null && outputScopeNameToUse == null) return
				content {
					append(nameToUse).appendBr()
					if(descToUse != null && descToUse.isNotEmpty()) append(descToUse).appendBr()
					appendBr()
					if(!inputScopeNamesToUse.isNullOrEmpty()) {
						append(PlsDocBundle.message("content.inputScopes", inputScopeNamesToUse)).appendBr()
					}
					if(outputScopeNameToUse != null) {
						append(PlsDocBundle.message("content.outputScope", outputScopeNameToUse))
					}
				}
			}
			else -> pass()
		}
	}
	
	private fun StringBuilder.buildSupportedScopesContent(element: CwtProperty, originalElement: PsiElement?, name: String, configType: CwtConfigType?, project: Project) {
		//为alias modifier localisation_command等提供支持的作用域的文档注释
		//仅为脚本文件中的引用提供
		var supportedScopeNames: Set<String>? = null
		when(configType) {
			CwtConfigType.Modifier -> {
				val gameType = originalElement?.let { it.fileInfo?.gameType } ?: return
				val configGroup = getCwtConfig(project)[gameType] ?: return
				val modifierConfig = configGroup.modifiers[name] ?: return
				supportedScopeNames = modifierConfig.supportedScopeNames
			}
			CwtConfigType.LocalisationCommand -> {
				val gameType = originalElement?.let { it.fileInfo?.gameType } ?: return
				val configGroup = getCwtConfig(project)[gameType] ?: return
				val localisationCommandConfig = configGroup.localisationCommands[name] ?: return
				supportedScopeNames = localisationCommandConfig.supportedScopeNames
			}
			else -> pass()
		}
		if(supportedScopeNames != null && supportedScopeNames.isNotEmpty()) {
			val supportedScopeNamesToUse = supportedScopeNames.joinToString(", ")
			content {
				append(PlsDocBundle.message("content.supportedScopes", supportedScopeNamesToUse))
			}
		}
	}
}