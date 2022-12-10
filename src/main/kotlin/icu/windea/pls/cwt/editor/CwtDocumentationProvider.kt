@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.cwt.editor

import com.intellij.lang.documentation.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.handler.ParadoxCwtConfigHandler.resolveConfigs
import icu.windea.pls.core.model.*
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
			buildPropertyDefinition(element, originalElement, name, configType, configGroup, false, null)
		}
	}
	
	private fun getStringInfo(element: CwtString, originalElement: PsiElement?): String? {
		//only for property value or block value
		if(!element.isPropertyValue() && !element.isBlockValue()) return null
		
		return buildString {
			val name = element.name
			val configType = CwtConfigType.resolve(element)
			val project = element.project
			val gameType = ParadoxSelectorUtils.selectGameType(originalElement?.takeIf { it.language == ParadoxScriptLanguage })
			val configGroup = gameType?.let { getCwtConfig(project).getValue(it) }
			buildStringDefinition(element, originalElement, name, configType, configGroup, false, null)
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
			val sections = mutableMapOf<String, String>()
			buildPropertyDefinition(element, originalElement, name, configType, configGroup, true, sections)
			buildDocumentationContent(element)
			buildScopeContent(element, originalElement, name, configType, configGroup)
			buildSupportedScopesContent(element, originalElement, name, configType, configGroup)
			buildSections(sections)
		}
	}
	
	private fun getStringDoc(element: CwtString, originalElement: PsiElement?): String? {
		//only for property value or block value
		if(!element.isPropertyValue() && !element.isBlockValue()) return null
		
		return buildString {
			val name = element.name
			val configType = CwtConfigType.resolve(element)
			val project = element.project
			val gameType = ParadoxSelectorUtils.selectGameType(originalElement?.takeIf { it.language == ParadoxScriptLanguage })
			val configGroup = gameType?.let { getCwtConfig(project).getValue(it) }
			val sections = mutableMapOf<String, String>()
			buildStringDefinition(element, originalElement, name, configType, configGroup, true, sections)
			buildDocumentationContent(element)
			buildSections(sections)
		}
	}
	
	//返回实际使用的相关本地化
	private fun StringBuilder.buildPropertyDefinition(element: CwtProperty, originalElement: PsiElement?, name: String, configType: CwtConfigType?, configGroup: CwtConfigGroup?, showDetail: Boolean, sections: MutableMap<String, String>?) {
		definition {
			if(originalElement?.language != ParadoxScriptLanguage || configType?.isReference == true) {
				if(configType != null) append(configType.nameText).append(" ")
				append("<b>").append(name.escapeXml().orAnonymous()).append("</b>")
				//加上类型信息
				val typeCategory = configType?.category
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
				append(" <b>").append(originalName.escapeXml().orAnonymous()).append("</b>")
				if(showDetail && name != originalName) { //这里不忽略大小写
					grayed {
						append(" by ").append(name.escapeXml().orAnonymous())
					}
				}
			}
			if(configType == CwtConfigType.Modifier) {
				addRelatedLocalisationSectionsForModifiers(element, originalElement, name, configGroup, sections)
			}
		}
	}
	
	private fun StringBuilder.buildStringDefinition(element: CwtString, originalElement: PsiElement?, name: String, configType: CwtConfigType?, configGroup: CwtConfigGroup?, showDetail: Boolean, sections: MutableMap<String, String>?) {
		definition {
			if(originalElement?.language != ParadoxScriptLanguage || configType?.isReference == true) {
				if(configType != null) append(configType.nameText).append(" ")
				append("<b>").append(name.escapeXml().orAnonymous()).append("</b>")
				//加上类型信息
				val typeCategory = configType?.category
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
				append(" <b>").append(originalName.escapeXml().orAnonymous()).append("</b>")
				if(showDetail && name != originalName) { //这里不忽略大小写
					grayed {
						append(" by ").append(name.escapeXml().orAnonymous())
					}
				}
			}
			if(configType == CwtConfigType.Modifier) {
				addRelatedLocalisationSectionsForModifiers(element, originalElement, name, configGroup, sections)
			}
		}
	}
	
	private fun StringBuilder.addRelatedLocalisationSectionsForModifiers(element: PsiElement, originalElement: PsiElement?, name: String, configGroup: CwtConfigGroup?, sections: MutableMap<String, String>?) {
		if(configGroup == null || originalElement == null) return
		if(!getSettings().documentation.renderRelatedLocalisationsForModifiers) return
		//为修饰符渲染相关本地化
		//NOTE 不能确定相关本地化的名字到底是什么，并且从API层面上来说，上下文PSI元素只能是CwtProperty而非ParadoxScriptExpressionElement
		//Name, Desc?
		ProgressManager.checkCanceled()
		val gameType = configGroup.gameType ?: return
		val keys = CwtConfigHandler.getModifierLocalisationNameKeys(name, configGroup)
		val localisation = keys?.firstNotNullOfOrNull {
			val selector = localisationSelector().gameType(gameType).preferRootFrom(originalElement).preferLocale(preferredParadoxLocale())
			//可以为全大写/全小写
			findLocalisation(it, configGroup.project, selector = selector) ?: findLocalisation(it.uppercase(), configGroup.project, selector = selector)
		}
		val descKeys = CwtConfigHandler.getModifierLocalisationDescKeys(name, configGroup)
		val descLocalisation = descKeys?.firstNotNullOfOrNull {
			val descSelector = localisationSelector().gameType(gameType).preferRootFrom(originalElement).preferLocale(preferredParadoxLocale())
			//可以为全大写/全小写
			findLocalisation(it, configGroup.project, selector = descSelector) ?: findLocalisation(it.uppercase(), configGroup.project, selector = descSelector)
		}
		//如果没找到的话，不要在文档中显示相关本地化信息
		if(localisation != null) {
			appendBr()
			append(PlsDocBundle.message("name.script.relatedLocalisation")).append(" ")
			append("Name = ").appendLocalisationLink(gameType, localisation.name, originalElement, resolved = true)
		}
		if(descLocalisation != null) {
			appendBr()
			append(PlsDocBundle.message("name.script.relatedLocalisation")).append(" ")
			append("Desc = ").appendLocalisationLink(gameType, descLocalisation.name, originalElement, resolved = true)
		}
		if(sections != null) {
			if(localisation != null) {
				sections.put("Name", ParadoxLocalisationTextRenderer.render(localisation))
			}
			if(descLocalisation != null) {
				sections.put("Desc", ParadoxLocalisationTextRenderer.render(descLocalisation))
			}
		}
	}
	
	private fun StringBuilder.buildDocumentationContent(element: PsiElement) {
		//渲染文档注释（可能需要作为HTML）和版本号信息
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
						option.name == "since" -> since = option.value
						option.name == "loc_format" && option.value == "html" -> html = true
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
			content {
				append(PlsDocBundle.message("content.since", since))
			}
		}
	}
	
	private fun StringBuilder.buildScopeContent(element: CwtProperty, originalElement: PsiElement?, name: String, configType: CwtConfigType?, configGroup: CwtConfigGroup?) {
		if(configGroup == null) return
		if(!getSettings().documentation.showScopes) return
		//为link提示名字、描述、输入作用域、输出作用域的文档注释
		//仅为脚本文件和本地化文件中的引用提供
		when(configType) {
			CwtConfigType.Link -> {
				val linkConfig = configGroup.links[name] ?: return
				val nameToUse = CwtConfigHandler.getScopeName(name, configGroup)
				val descToUse = linkConfig.desc
				val inputScopeNames = linkConfig.inputScopeNames.joinToString { "<code>$it</code>" }
				val outputScopeName = linkConfig.outputScopeName.let { "<code>$it</code>" }
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
		if(configGroup == null) return
		if(!getSettings().documentation.showScopes) return
		//为alias modifier localisation_command等提供分类、支持的作用域的文档注释
		//仅为脚本文件和本地化文件中的引用提供
		var categoryNames: Set<String>? = null
		var supportedScopeNames: Set<String>? = null
		when(configType) {
			CwtConfigType.Modifier -> {
				val modifierConfig = configGroup.modifiers[name] ?: return
				categoryNames = modifierConfig.categoryConfigMap.keys
				supportedScopeNames = modifierConfig.supportedScopeNames
			}
			CwtConfigType.LocalisationCommand -> {
				val localisationCommandConfig = configGroup.localisationCommands[name] ?: return
				supportedScopeNames = localisationCommandConfig.supportedScopeNames
			}
			CwtConfigType.Alias -> {
				//TODO 有些alias的supported_scopes信息并没有同步到最新版本的CWT规则文件中，需要另外写日志解析器进行解析
				val expressionElement = originalElement?.parent?.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
				val config = resolveConfigs(expressionElement).firstOrNull()?.castOrNull<CwtPropertyConfig>()
				val aliasConfig = config?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return
				supportedScopeNames = aliasConfig.supportedScopeNames
			}
			else -> pass()
		}
		content {
			var appendBr = false
			if(!categoryNames.isNullOrEmpty()) {
				appendBr = true
				val categoryNamesToUse = categoryNames.joinToString(", ") { "<code>$it</code>" }
				append(PlsDocBundle.message("content.categories", categoryNamesToUse))
			}
			if(!supportedScopeNames.isNullOrEmpty()) {
				if(appendBr) appendBr()
				val supportedScopeNamesToUse = supportedScopeNames.joinToString(", ") { "<code>$it</code>" }
				append(PlsDocBundle.message("content.supportedScopes", supportedScopeNamesToUse))
			}
		}
	}
	
	private fun StringBuilder.buildSections(sections: Map<String, String>) {
		if(sections.isEmpty()) return
		sections {
			for((key, value) in sections) {
				section(key, value)
			}
		}
	}
}
