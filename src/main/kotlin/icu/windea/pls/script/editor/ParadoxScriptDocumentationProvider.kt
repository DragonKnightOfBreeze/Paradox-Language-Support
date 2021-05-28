package icu.windea.pls.script.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationTypes.*
import icu.windea.pls.script.psi.*

class ParadoxScriptDocumentationProvider : AbstractDocumentationProvider() {
	override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
		//处理是scripted_loc的特殊情况
		if(originalElement != null && originalElement.elementType == COMMAND_FIELD_ID) {
			return getScriptLocalisationInfo(originalElement)
		}
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
				element.paradoxFileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				append("(script variable) <b>").append(name.escapeXml()).append("</b>")
				element.unquotedValue?.let { unquotedValue -> append(" = ").append(unquotedValue.escapeXml()) }
			}
		}
	}
	
	private fun getPropertyInfo(element: ParadoxScriptProperty): String {
		val definition = element.paradoxDefinitionInfo
		if(definition != null) return getDefinition(element, definition)
		return buildString {
			val name = element.name
			definition {
				element.paradoxFileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				append("(script property) <b>").append(name.escapeXml()).append("</b>")
				element.truncatedValue?.let { truncatedValue -> append(" = ").append(truncatedValue.escapeXml()) }
			}
		}
	}
	
	private fun getDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String {
		return buildString {
			definition {
				element.paradoxFileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				val name = definitionInfo.name.ifEmpty { anonymousString }
				val typeText = definitionInfo.typeText
				append("(definition) <b>").append(name.escapeXml()).append("</b>: ").append(typeText)
				val localisation = definitionInfo.localisation
				if(localisation.isNotEmpty()) {
					appendBr().appendBr()
					var isFirst = true
					for((n, kn) in localisation) {
						if(kn.isEmpty()) continue //不显示keyName为空（匿名）的definitionLocalisation
						if(isFirst) isFirst = false else appendBr()
						append("(definition localisation) ").append(n).append(" = <b>").appendPsiLink("#", kn).append("</b>")
					}
				}
			}
		}
	}
	
	private fun getScriptLocalisationInfo(element: PsiElement): String {
		return buildString {
			val name = element.text
			definition {
				append("(localisation command field) <b>").append(name).append("</b>")
				appendBr().appendBr()
				append("(definition) <b>").append(name.escapeXml()).append("</b>: ").append("scripted_loc")
			}
		}
	}
	
	override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
		//处理是scripted_loc的特殊情况
		if(originalElement != null && originalElement.elementType == COMMAND_FIELD_ID) {
			return getScriptLocalisationDoc(originalElement)
		}
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
				element.paradoxFileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				append("(script variable) <b>").append(name).append("</b>")
				element.unquotedValue?.let { unquotedValue -> append(" = ").append(unquotedValue.escapeXml()) }
			}
			//之前的单行注释文本
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
	
	private fun getPropertyDoc(element: ParadoxScriptProperty): String {
		val definition = element.paradoxDefinitionInfo
		if(definition != null) return getDefinitionDoc(element, definition)
		return buildString {
			val name = element.name
			definition {
				element.paradoxFileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				append("(script property) <b>").append(name.escapeXml()).append("</b>")
				element.truncatedValue?.let { truncatedValue -> append(" = ").append(truncatedValue.escapeXml()) }
			}
			//之前的单行注释文本
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
	
	private fun getDefinitionDoc(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String {
		return buildString {
			definition {
				element.paradoxFileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				val name = definitionInfo.name.ifEmpty { anonymousString }
				val typeText = definitionInfo.typeText
				append("(definition) <b>").append(name.escapeXml()).append("</b>: ").append(typeText)
				val localisation = definitionInfo.localisation
				if(localisation.isNotEmpty()) {
					appendBr().appendBr()
					var isFirst = true
					for((n,kn) in localisation) {
						if(kn.isEmpty()) continue //不显示keyName为空（匿名）的definitionLocalisation
						if(isFirst) isFirst = false else appendBr()
						append("(definition localisation) ").append(n).append(" = <b>").appendPsiLink("#", kn).append("</b>")
					}
				}
			}
			//之前的单行注释文本
			if(getSettings().renderLineCommentText) {
				val docText = getDocTextFromPreviousComment(element)
				if(docText.isNotEmpty()) {
					content {
						append(docText)
					}
				}
			}
			//本地化文本
			if(getSettings().renderDefinitionText) {
				val localisation = definitionInfo.localisation
				if(localisation.isNotEmpty()) {
					val richTexts = mutableListOf<Pair<String, String>>()
					for((n, kn) in localisation) {
						if(kn.isEmpty()) continue //不显示keyName为空（匿名）的definitionLocalisation
						val e = findLocalisation(kn, element.paradoxLocale, element.project, hasDefault = true)
						val richText = e?.renderText() ?: continue
						val sectionName = n.toCapitalizedWords()
						richTexts.add(sectionName to richText)
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
	
	private fun getScriptLocalisationDoc(element: PsiElement): String {
		return buildString {
			val name = element.text
			definition {
				append("(localisation command field) <b>").append(name).append("</b>")
				appendBr().appendBr()
				append("(definition) <b>").append(name.escapeXml()).append("</b>: ").append("scripted_loc")
			}
		}
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		if(link == null || context == null) return null
		return resolveLink(link, context)
	}
}
