package icu.windea.pls.script.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxScriptDocumentationProvider : AbstractDocumentationProvider() {
	override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
		return when(element) {
			is ParadoxScriptVariableName -> getQuickNavigateInfo(element.parent, originalElement) //防止意外情况
			is ParadoxScriptVariable -> getVariableInfo(element)
			is ParadoxScriptProperty -> getPropertyInfo(element)
			else -> null
		}
	}
	
	private fun getVariableInfo(element: ParadoxScriptVariable): String {
		return buildString {
			val name = element.name
			definition {
				element.fileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				append("(script variable) <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
				element.unquotedValue?.let { unquotedValue -> append(" = ").append(unquotedValue.escapeXml()) }
			}
		}
	}
	
	private fun getPropertyInfo(element: ParadoxScriptProperty): String {
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) {
			return getDefinitionInfo(element, definitionInfo)
		}
		return buildString {
			val name = element.name
			definition {
				element.fileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				append("(script property) <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
				element.truncatedValue?.let { truncatedValue -> append(" = ").append(truncatedValue.escapeXml()) }
			}
		}
	}
	
	private fun getDefinitionInfo(element: ParadoxScriptProperty, definitionInfo: definitionInfo): String {
		val localisation = definitionInfo.localisation
		return buildString {
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
		return buildString {
			val name = element.name
			definition {
				element.fileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				append("(script variable) <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
				element.unquotedValue?.let { unquotedValue -> append(" = ").append(unquotedValue.escapeXml()) }
			}
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
	
	private fun getPropertyDoc(element: ParadoxScriptProperty): String {
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) {
			return getDefinitionDoc(element, definitionInfo)
		}
		return buildString {
			val name = element.name
			definition {
				element.fileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				append("(script property) <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
				element.truncatedValue?.let { truncatedValue -> append(" = ").append(truncatedValue.escapeXml()) }
			}
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
	
	private fun getDefinitionDoc(element: ParadoxScriptProperty, definitionInfo: definitionInfo): String {
		val type = definitionInfo.type
		val localisation = definitionInfo.localisation
		return buildString {
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
			//单行注释文本
			if(renderLineCommentText) {
				val docText = getDocTextFromPreviousComment(element)
				if(docText.isNotEmpty()) {
					content {
						append(docText)
					}
				}
			}
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
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		if(link == null || context == null) return null
		return resolveLink(link, context)
	}
}
