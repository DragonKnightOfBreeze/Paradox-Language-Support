package icu.windea.pls.script.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxScriptDocumentationProvider : AbstractDocumentationProvider() {
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
			buildDefinitionLocalisationSections(localisation, element)
		}
	}
	
	private fun StringBuilder.buildVariableDefinition(element: ParadoxScriptVariable, name: String) {
		definition {
			element.fileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
			append("(script variable) <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
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
		localisation: List<ParadoxDefinitionLocalisationInfo>) {
		definition {
			element.fileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
			val name = definitionInfo.name
			val typeLinkText = definitionInfo.typeLinkText
			append("(definition) <b>").append(name.escapeXmlOrAnonymous()).append("</b>: ").append(typeLinkText)
			
			if(localisation.isNotEmpty()) {
				for((n, kn) in localisation) {
					if(kn.isEmpty()) continue //不显示keyName为空的definitionLocalisation
					appendBr()
					append("(definition localisation) ").append(n).append(" = ").appendLocalisationLink(kn)
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
	
	private fun StringBuilder.buildDefinitionLocalisationSections(localisation: List<ParadoxDefinitionLocalisationInfo>,
		element: ParadoxScriptProperty) {
		//本地化文本
		if(renderDefinitionText) {
			if(localisation.isNotEmpty()) {
				val richTexts = mutableListOf<Pair<String, String>>()
				for((n, kn) in localisation) {
					if(kn.isEmpty()) continue //不显示keyName为空的definitionLocalisation
					val e = findLocalisation(kn, element.localeInfo, element.project, hasDefault = true)
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
		if(renderLineCommentText) {
			val docText = getDocTextFromPreviousComment(element)
			if(docText.isNotEmpty()) {
				content {
					append(docText)
				}
			}
		}
	}
}
