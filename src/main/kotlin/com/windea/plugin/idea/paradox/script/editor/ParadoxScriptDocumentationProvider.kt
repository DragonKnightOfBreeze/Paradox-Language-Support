package com.windea.plugin.idea.paradox.script.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*
import com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationTypes.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxScriptDocumentationProvider : AbstractDocumentationProvider() {
	companion object {
		private val _iconTitle = message("paradox.documentation.icon")
		private val _effectTitle = message("paradox.documentation.effect")
		private val _tagsTitle = message("paradox.documentation.tags")
	}
	
	override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
		if(originalElement != null && originalElement.elementType == COMMAND_FIELD_ID) return getScriptedLocInfo(originalElement)
		return when(element) {
			is ParadoxScriptVariableName -> getQuickNavigateInfo(element.parent, originalElement) //防止意外情况
			is ParadoxScriptVariable -> getVariableInfo(element)
			is ParadoxScriptProperty -> getPropertyInfo(element)
			else -> null
		}
	}
	
	private fun getVariableInfo(element: ParadoxScriptVariable): String {
		return buildString {
			definition {
				element.paradoxFileInfo?.path?.let { paradoxPath -> append("[").append(paradoxPath).append("]<br>") }
				append("(script variable) <b>").append(element.name.escapeXml()).append("</b>")
				element.unquotedValue?.let { unquotedValue -> append(" = ").append(unquotedValue.escapeXml()) }
			}
		}
	}
	
	private fun getPropertyInfo(element: ParadoxScriptProperty): String {
		val name = element.name
		val definitionInfo = element.paradoxDefinitionInfo
		if(definitionInfo != null) return getDefinitionInfo(element, definitionInfo)
		return buildString {
			definition {
				element.paradoxFileInfo?.path?.let { append("[").append(it).append("]<br>") }
				append("(script property) <b>").append(name.escapeXml()).append("</b>")
				element.truncatedValue?.let { truncatedValue -> append(" = ").append(truncatedValue.escapeXml()) }
			}
		}
	}
	
	private fun getDefinitionInfo(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String {
		return buildString {
			definition {
				element.paradoxFileInfo?.path?.let { append("[").append(it).append("]") }
				val (name, type, subtypes, localisation) = definitionInfo
				append("<br>(definition) <b>").append(name.escapeXml()).append("</b>: ").append(type)
				if(subtypes.isNotEmpty()) {
					subtypes.joinTo(this, ", ", ", ")
				}
				if(localisation.isNotEmpty()) {
					append("<br>")
					for((k, v) in localisation) {
						append("<br>(definition localisation) ").append(k.value).append(" = <b>").appendPsiLink("#", v).append("</b>")
					}
				}
			}
		}
	}
	
	private fun getScriptedLocInfo(element: PsiElement): String {
		val name = element.text
		return buildString {
			definition {
				append("(localisation command field) <b>").append(name).append("</b>")
				append("<br>")
				append("<br>(definition) <b>").append(name.escapeXml()).append("</b>: ").append("scripted_loc")
			}
		}
	}
	
	override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
		if(originalElement != null && originalElement.elementType == COMMAND_FIELD_ID) return getScriptedLocDoc(originalElement)
		return when(element) {
			is ParadoxScriptVariableName -> generateDoc(element.parent, originalElement) //防止意外情况
			is ParadoxScriptVariable -> getVariableDoc(element)
			is ParadoxScriptProperty -> getPropertyDoc(element)
			else -> null
		}
	}
	
	private fun getVariableDoc(element: ParadoxScriptVariable): String {
		return buildString {
			definition {
				element.paradoxFileInfo?.path?.let { paradoxPath -> append("[").append(paradoxPath).append("]<br>") }
				append("(script variable) <b>").append(element.name).append("</b>")
				element.unquotedValue?.let { unquotedValue -> append(" = ").append(unquotedValue.escapeXml()) }
			}
			//之前的单行注释文本
			if(settings.renderLineCommentText) {
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
		val name = element.name
		val definitionInfo = element.paradoxDefinitionInfo
		if(definitionInfo != null) return getDefinitionDoc(element, definitionInfo)
		return buildString {
			definition {
				element.paradoxFileInfo?.path?.let { append("[").append(it).append("]<br>") }
				append("(script property) <b>").append(name.escapeXml()).append("</b>")
				element.truncatedValue?.let { truncatedValue -> append(" = ").append(truncatedValue.escapeXml()) }
			}
			//之前的单行注释文本
			if(settings.renderLineCommentText) {
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
		generate(element.project)
		return buildString {
			definition {
				element.paradoxFileInfo?.path?.let { append("[").append(it).append("]") }
				val (name, type, subtypes, localisation) = definitionInfo
				append("<br>(definition) <b>").append(name.escapeXml()).append("</b>: ").append(type)
				if(subtypes.isNotEmpty()) {
					subtypes.joinTo(this, ", ", ", ")
				}
				if(localisation.isNotEmpty()) {
					append("<br>")
					for((k, v) in localisation) {
						append("<br>(definition localisation) ").append(k.value).append(" = <b>").appendPsiLink("#", v).append("</b>")
					}
				}
			}
			//之前的单行注释文本
			if(settings.renderLineCommentText) {
				val docText = getDocTextFromPreviousComment(element)
				if(docText.isNotEmpty()) {
					content {
						append(docText)
					}
				}
			}
			//本地化文本
			if(settings.renderDefinitionText) {
				val localisation = definitionInfo.localisation
				if(localisation.isNotEmpty()) {
					val richTexts = mutableListOf<Pair<String, String>>()
					for((name, key) in localisation) {
						val e = findLocalisation(key, element.paradoxLocale, element.project, hasDefault = true)
						val richText = e?.renderText() ?: continue
						val sectionName = name.value.toCapitalizedWords()
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
	
	private fun getScriptedLocDoc(element: PsiElement): String {
		val name = element.text
		return buildString {
			definition {
				append("(localisation command field) <b>").append(name).append("</b>")
				append("<br>")
				append("<br>(definition) <b>").append(name.escapeXml()).append("</b>: ").append("scripted_loc")
			}
		}
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		return when {
			link == null || context == null -> null
			link.startsWith("#") -> getLocalisationLink(link.drop(1), context)
			link.startsWith("$") -> getScriptLink(link.drop(1), context)
			else -> null
		}
	}
	
	private fun getLocalisationLink(link: String, context: PsiElement): ParadoxLocalisationProperty? {
		return findLocalisation(link, context.paradoxLocale, context.project, hasDefault = true)
	}
	
	private fun getScriptLink(link: String, context: PsiElement): ParadoxScriptProperty? {
		return findDefinition(link, null, context.project)
	}
}
