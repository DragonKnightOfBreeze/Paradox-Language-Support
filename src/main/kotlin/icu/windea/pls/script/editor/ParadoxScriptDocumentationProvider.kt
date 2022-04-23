package icu.windea.pls.script.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

class ParadoxScriptDocumentationProvider : AbstractDocumentationProvider() {
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
			is ParadoxScriptVariableName -> getQuickNavigateInfo(element.parent, originalElement) //防止意外情况
			is ParadoxScriptVariable -> getVariableInfo(element)
			is ParadoxScriptProperty -> getPropertyInfo(element)
			else -> null
		}
	}
	
	private fun getVariableInfo(element: ParadoxScriptVariable): String {
		val name = element.name
		return buildString {
			buildVariableDefinition(element, name)
		}
	}
	
	private fun getPropertyInfo(element: ParadoxScriptProperty): String {
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) return getDefinitionInfo(element, definitionInfo)
		val name = element.name
		return buildString {
			buildPropertyDefinition(element, name)
		}
	}
	
	private fun getDefinitionInfo(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String {
		val localisation = definitionInfo.localisation
		return buildString {
			buildDefinitionDefinition(element, definitionInfo, localisation)
		}
	}
	
	override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
		return when(element) {
			is ParadoxScriptVariableName -> generateDoc(element.parent, originalElement) //防止意外情况
			is ParadoxScriptVariable -> getVariableDoc(element)
			is ParadoxScriptProperty -> getPropertyDoc(element)
			else -> null
		}
	}
	
	private fun getVariableDoc(element: ParadoxScriptVariable): String {
		val name = element.name
		return buildString {
			buildVariableDefinition(element, name)
			buildLineCommentContent(element)
		}
	}
	
	private fun getPropertyDoc(element: ParadoxScriptProperty): String {
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) return getDefinitionDoc(element, definitionInfo)
		val name = element.name
		return buildString {
			buildPropertyDefinition(element, name)
			buildLineCommentContent(element)
		}
	}
	
	private fun getDefinitionDoc(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String {
		val type = definitionInfo.type
		val localisation = definitionInfo.localisation
		return buildString {
			buildDefinitionDefinition(element, definitionInfo, localisation)
			buildLineCommentContent(element)
			buildDefinitionIconSections(type, element)
			buildRelatedLocalisationSections(localisation, element)
		}
	}
	
	private fun StringBuilder.buildVariableDefinition(element: ParadoxScriptVariable, name: String) {
		definition {
			element.fileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
			append("(variable) <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
			element.unquotedValue?.let { unquotedValue -> append(" = ").append(unquotedValue.escapeXml()) }
		}
	}
	
	private fun StringBuilder.buildPropertyDefinition(element: ParadoxScriptProperty, name: String) {
		definition {
			element.fileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
			append("(script property) <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
			element.truncatedValue?.let { truncatedValue -> append(" = ").append(truncatedValue.escapeXml()) }
		}
	}
	
	private fun StringBuilder.buildDefinitionDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo,
		localisation: List<ParadoxRelatedLocalisationInfo>) {
		definition {
			//加上定义的文件信息
			element.fileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
			//加上定义的名称和类型信息
			val name = definitionInfo.name
			val typeLinkText = buildString {
				val gameType = definitionInfo.gameType
				val typeConfig = definitionInfo.typeConfig
				val typeLink = "${gameType.id}.types.${typeConfig.name}"
				appendCwtLink(typeConfig.name, typeLink, typeConfig.pointer.element)
				val subtypeConfigs = definitionInfo.subtypeConfigs
				if(subtypeConfigs.isNotEmpty()) {
					for(subtypeConfig in subtypeConfigs) {
						append(", ")
						val subtypeLink = "$typeLink.${subtypeConfig.name}"
						appendCwtLink(subtypeConfig.name, subtypeLink, subtypeConfig.pointer.element)
					}
				}
			}
			append("(definition) <b>").append(name.escapeXmlOrAnonymous()).append("</b>: ").append(typeLinkText)
			//加上定义的相关本地化信息
			if(localisation.isNotEmpty()) {
				for((n, kn) in localisation) {
					if(kn.isEmpty()) continue //不显示keyName为空的relatedLocalisation
					appendBr()
					append("(related localisation) ").append(n).append(" = ").appendLocalisationLink(kn, element)
				}
			}
		}
	}
	
	private fun StringBuilder.buildDefinitionIconSections(type: String, element: ParadoxScriptProperty) {
		//sprite图标
		if(type == "sprite" || type == "spriteType") {
			val iconUrl = element.resolveIconUrl(false)
			if(iconUrl.isNotEmpty()) {
				sections {
					val iconTag = buildString { appendIconTag(iconUrl).append("&nbsp;") } //让图标垂直居中显示
					section("Icon", iconTag)
				}
			}
		}
	}
	
	private fun StringBuilder.buildRelatedLocalisationSections(localisation: List<ParadoxRelatedLocalisationInfo>,
		element: ParadoxScriptProperty) {
		//本地化文本
		if(getSettings().renderDefinitionText) {
			if(localisation.isNotEmpty()) {
				val richTexts = mutableListOf<Pair<String, String>>()
				for((n, kn) in localisation) {
					if(kn.isEmpty()) continue //不显示keyName为空的Name为空的relatedLocalisation
					val e = findLocalisation(kn, element.localeConfig, element.project, hasDefault = true)
					val richText = e?.renderText() ?: continue
					richTexts.add(n to richText)
				}
				if(richTexts.isNotEmpty()) {
					sections {
						for((title, richText) in richTexts) {
							section(title, richText)
						}
					}
				}
			}
		}
	}
	
	private fun StringBuilder.buildLineCommentContent(element: PsiElement) {
		//单行注释文本
		if(getSettings().renderLineCommentText) {
			val docText = getDocTextFromPreviousComment(element)
			if(docText.isNotEmpty()) {
				content {
					append(docText)
				}
			}
		}
	}
}
