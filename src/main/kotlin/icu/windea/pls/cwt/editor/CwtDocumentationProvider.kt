package icu.windea.pls.cwt.editor

import com.intellij.lang.documentation.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.tool.*
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
			val configType = CwtConfigType.resolve(element)
			val project = element.project
			val gameType = ParadoxSelectorUtils.selectGameType(originalElement?.takeIf { it.language == ParadoxScriptLanguage })
			val configGroup = gameType?.let { getCwtConfig(project).getValue(it) }
			buildPropertyDefinition(element, originalElement, name, configType, configGroup, false)
		}
	}
	
	private fun getStringInfo(element: CwtString, originalElement: PsiElement?): String {
		return buildString {
			val name = element.name
			val configType = CwtConfigType.resolve(element)
			val project = element.project
			val gameType = ParadoxSelectorUtils.selectGameType(originalElement?.takeIf { it.language == ParadoxScriptLanguage })
			val configGroup = gameType?.let { getCwtConfig(project).getValue(it) }
			buildStringDefinition(element, originalElement, name, configType, configGroup, false)
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
			val gameType = ParadoxSelectorUtils.selectGameType(originalElement?.takeIf { it.language == ParadoxScriptLanguage })
			val configGroup = gameType?.let { getCwtConfig(project).getValue(it) }
			buildPropertyDefinition(element, originalElement, name, configType, configGroup, true)
			buildLocalisationContent(originalElement, name, configType, configGroup)
			val sectionMap = buildDocumentationContent(element)
			buildScopeContent(element, originalElement, name, configType, configGroup)
			buildSupportedScopesContent(element, originalElement, name, configType, configGroup)
			if(sectionMap.isNotEmpty()){
				sections {
					for((sectionTitle, sectionValue) in sectionMap) {
						section(sectionTitle, sectionValue)
					}
				}
			}
		}
	}
	
	private fun getStringDoc(element: CwtString, originalElement: PsiElement?): String {
		return buildString {
			val name = element.name
			val configType = CwtConfigType.resolve(element)
			val project = element.project
			val gameType = ParadoxSelectorUtils.selectGameType(originalElement?.takeIf { it.language == ParadoxScriptLanguage })
			val configGroup = gameType?.let { getCwtConfig(project).getValue(it) }
			buildStringDefinition(element, originalElement, name, configType, configGroup, true)
			buildLocalisationContent(originalElement, name, configType, configGroup)
			val sectionMap = buildDocumentationContent(element)
			if(sectionMap.isNotEmpty()){
				sections {
					for((sectionTitle, sectionValue) in sectionMap) {
						section(sectionTitle, sectionValue)
					}
				}
			}
		}
	}
	
	private fun StringBuilder.buildPropertyDefinition(element: CwtProperty, originalElement: PsiElement?, name: String, configType: CwtConfigType?, configGroup: CwtConfigGroup?, showDetail: Boolean) {
		definition {
			if(originalElement?.language != ParadoxScriptLanguage || configType?.isReference == true) {
				if(configType != null) append(configType.text).append(" ")
				append("<b>").append(name.escapeXmlOrAnonymous()).append("</b>")
				//加上类型信息
				val typeCategory = configType?.typeCategory
				if(typeCategory != null) {
					val typeElement = element.parentOfType<CwtProperty>()
					val typeName = typeElement?.name?.substringIn('[', ']')?.takeIfNotEmpty()
					if(typeName != null && typeName.isNotEmpty()) {
						//在脚本文件中显示为链接
						if(configGroup != null) {
							val gameType = configGroup.gameType
							val typeLink = "${gameType.id}/${typeCategory}/${typeName}"
							append(": ").appendCwtLink(typeName, typeLink, typeElement)
						} else {
							append(": ").append(typeName)
						}
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
	
	private fun StringBuilder.buildStringDefinition(element: CwtString, originalElement: PsiElement?, name: String, configType: CwtConfigType?, configGroup: CwtConfigGroup?, showDetail: Boolean) {
		definition {
			if(originalElement?.language != ParadoxScriptLanguage || configType?.isReference == true) {
				if(configType != null) append(configType.text).append(" ")
				append("<b>").append(name.escapeXmlOrAnonymous()).append("</b>")
				//加上类型信息
				val typeCategory = configType?.typeCategory
				if(typeCategory != null) {
					val typeElement = element.parentOfType<CwtProperty>()
					val typeName = typeElement?.name?.substringIn('[', ']')?.takeIfNotEmpty()
					if(typeName != null && typeName.isNotEmpty()) {
						//在脚本文件中显示为链接
						if(configGroup != null) {
							val gameType = configGroup.gameType
							val typeLink = "${gameType.id}/${typeCategory}/${typeName}"
							append(": ").appendCwtLink(typeName, typeLink, typeElement)
						} else {
							append(": ").append(typeName)
						}
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
	
	private fun StringBuilder.buildLocalisationContent(originalElement: PsiElement?, name: String, configType: CwtConfigType?, configGroup: CwtConfigGroup?) {
		if(originalElement?.language != ParadoxScriptLanguage) return
		if(configGroup == null) return
		val locationExpression = configType?.localisation?.let { CwtLocalisationLocationExpression.resolve(it) } ?: return
		val key = locationExpression.resolvePlaceholder(name) ?: return
		ProgressManager.checkCanceled()
		val selector = localisationSelector().gameType(configGroup.gameType).preferRootFrom(originalElement).preferLocale(preferredParadoxLocale())
		val localisation = findLocalisation(key, configGroup.project, selector = selector) ?: return
		content {
			ParadoxLocalisationTextRenderer.renderTo(localisation, this)
		}
	}
	
	private fun StringBuilder.buildDocumentationContent(element: PsiElement): Map<String, String> {
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
		return buildMap {
			if(since != null) put(PlsDocBundle.message("title.since"), since)
		}
	}
	
	private fun StringBuilder.buildScopeContent(element: CwtProperty, originalElement: PsiElement?, name: String, configType: CwtConfigType?, configGroup: CwtConfigGroup?) {
		when(configType) {
			//为link提示名字、描述、输入作用域、输出作用域的文档注释
			//仅为脚本文件中的引用提供
			CwtConfigType.Link -> {
				if(configGroup == null) return
				val linkConfig = configGroup.links[name] ?: return
				val nameToUse = CwtConfigHandler.getScopeName(name, configGroup)
				val descToUse = linkConfig.desc
				val inputScopeNames = linkConfig.inputScopeNames.joinToString()
				val outputScopeName = linkConfig.outputScopeName
				content {
					append(nameToUse)
					if(descToUse != null && descToUse.isNotEmpty()) {
						appendBr()
						append(descToUse)
					}
				}
				content {
					append(PlsDocBundle.message("content.inputScopes", inputScopeNames))
					appendBr()
					append(PlsDocBundle.message("content.outputScope", outputScopeName))
				}
			}
			else -> pass()
		}
	}
	
	private fun StringBuilder.buildSupportedScopesContent(element: CwtProperty, originalElement: PsiElement?, name: String, configType: CwtConfigType?, configGroup: CwtConfigGroup?) {
		//为alias modifier localisation_command等提供分类、支持的作用域的文档注释
		//仅为脚本文件中的引用提供
		var categoryNames: Set<String>? = null
		var supportedScopeNames: Set<String>? = null
		when(configType) {
			CwtConfigType.Modifier -> {
				if(configGroup == null) return
				val modifierConfig = configGroup.modifiers[name] ?: return
				categoryNames = modifierConfig.categoryConfigMap.keys
				supportedScopeNames = modifierConfig.supportedScopeNames
			}
			CwtConfigType.LocalisationCommand -> {
				if(configGroup == null) return
				val localisationCommandConfig = configGroup.localisationCommands[name] ?: return
				supportedScopeNames = localisationCommandConfig.supportedScopeNames
			}
			CwtConfigType.Alias -> {
				//TODO 有些alias的supported_scopes信息并没有同步到最新版本的CWT规则文件中，需要另外写日志解析器进行解析
				val expressionElement = originalElement?.parent?.castOrNull<ParadoxExpressionAwareElement>() ?: return
				val config = ParadoxCwtConfigHandler.resolveConfig(expressionElement)?.castOrNull<CwtPropertyConfig>()
				val aliasConfig = config?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return
				supportedScopeNames = aliasConfig.supportedScopeNames
			}
			else -> pass()
		}
		content {
			var appendBr = false
			if(!categoryNames.isNullOrEmpty()) {
				appendBr = true
				val categoryNamesToUse = categoryNames.joinToString(", ")
				append(PlsDocBundle.message("content.categories", categoryNamesToUse))
			}
			if(!supportedScopeNames.isNullOrEmpty()) {
				if(appendBr) appendBr()
				val supportedScopeNamesToUse = supportedScopeNames.joinToString(", ")
				append(PlsDocBundle.message("content.supportedScopes", supportedScopeNamesToUse))
			}
		}
	}
}