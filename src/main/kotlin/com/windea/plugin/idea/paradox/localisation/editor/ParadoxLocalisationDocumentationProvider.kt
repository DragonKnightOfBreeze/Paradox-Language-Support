package com.windea.plugin.idea.paradox.localisation.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.core.settings.*
import com.windea.plugin.idea.paradox.localisation.psi.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxLocalisationDocumentationProvider : AbstractDocumentationProvider() {
	companion object {
		private val _textTitle = message("paradox.documentation.text")
	}
	
	private val state = ParadoxSettingsState.getInstance()
	
	override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
		return when {
			element is ParadoxLocalisationProperty -> getPropertyInfo(element)
			element is ParadoxLocalisationLocale -> getLocaleInfo(element)
			element is ParadoxLocalisationIcon -> getIconInfo(element)
			element is ParadoxLocalisationSequentialNumber -> getSequentialNumberInfo(element)
			element is ParadoxLocalisationColorfulText -> getColorInfo(element)
			element is ParadoxLocalisationCommandScope -> getCommandScopeInfo(element)
			element is ParadoxLocalisationCommandField -> getCommandFieldInfo(element)
			else -> null
		}
	}
	
	private fun getPropertyInfo(element: ParadoxLocalisationProperty): String {
		return getLocalisationInfo(element)
		//return buildString {
		//	definition {
		//		element.paradoxFileInfo?.path?.let { append("[").append(it).append("]<br>") }
		//		append("(localisation property) <b>").append(element.name).append("</b>")
		//	}
		//}
	}
	
	private fun getLocalisationInfo(element: ParadoxLocalisationProperty): String {
		return buildString {
			definition {
				element.paradoxFileInfo?.path?.let { append("[").append(it).append("]<br>") }
				append("(localisation) <b>").append(element.name).append("</b>")
			}
		}
	}
	
	private fun getLocaleInfo(element: ParadoxLocalisationLocale): String {
		return buildString {
			definition {
				append("(localisation locale) <b>").append(element.name).append("</b>")
			}
		}
	}
	
	private fun getIconInfo(element: ParadoxLocalisationIcon): String {
		return buildString {
			definition {
				append("(localisation icon) <b>").append(element.name).append("</b>")
			}
		}
	}
	
	private fun getSequentialNumberInfo(element: ParadoxLocalisationSequentialNumber): String {
		return buildString {
			definition {
				append("(localisation sequential number) <b>").append(element.name).append("</b>")
			}
		}
	}
	
	private fun getCommandScopeInfo(element: ParadoxLocalisationCommandScope): String {
		return buildString {
			definition {
				append("(localisation command scope) <b>").append(element.name).append("</b>")
			}
		}
	}
	
	private fun getCommandFieldInfo(element: ParadoxLocalisationCommandField): String {
		return buildString {
			definition {
				append("(localisation command field) <b>").append(element.name).append("</b>")
			}
		}
	}
	
	private fun getColorInfo(element: ParadoxLocalisationColorfulText): String {
		return buildString {
			definition {
				append("(localisation color) <b>").append(element.name).append("</b>")
			}
		}
	}
	
	override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
		return when {
			element is ParadoxLocalisationProperty -> getPropertyDoc(element)
			element is ParadoxLocalisationLocale -> getLocaleDoc(element)
			element is ParadoxLocalisationIcon -> getIconDoc(element)
			element is ParadoxLocalisationCommandScope -> getCommandScopeDoc(element)
			element is ParadoxLocalisationCommandField -> getCommandFieldDoc(element)
			element is ParadoxLocalisationSequentialNumber -> getSequentialNumberDoc(element)
			element is ParadoxLocalisationColorfulText -> getColorDoc(element)
			else -> null
		}
	}
	
	private fun getPropertyDoc(element: ParadoxLocalisationProperty): String {
		return getLocalisationDoc(element)
		//val name = element.name
		//return buildString {
		//	definition {
		//		element.paradoxFileInfo?.path?.let { append("[").append(it).append("]<br>") }
		//		append("(localisation property) <b>").append(name).append("</b>")
		//	}
		//}
	}
	
	private fun getLocalisationDoc(element: ParadoxLocalisationProperty): String {
		val name = element.name
		return buildString {
			definition {
				element.paradoxFileInfo?.path?.let { append("[").append(it).append("]<br>") }
				append("(localisation) <b>").append(name).append("</b>")
			}
			//之前的单行注释文本
			if(state.renderLineCommentText) {
				val docText = getDocTextFromPreviousComment(element)
				if(docText.isNotEmpty()) {
					content {
						append(docText)
					}
				}
			}
			//本地化文本
			if(state.renderLocalisationText) {
				val richText = element.propertyValue?.renderRichText()
				if(richText != null) {
					sections {
						section(_textTitle, richText)
					}
				}
			}
		}
	}
	
	private fun getLocaleDoc(element: ParadoxLocalisationLocale): String {
		return buildString {
			definition {
				append("(localisation locale) <b>").append(element.name).append("</b>")
				val description = element.paradoxLocale?.description
				if(description != null) {
					content {
						append(description)
					}
				}
			}
		}
	}
	
	private fun getIconDoc(element: ParadoxLocalisationIcon): String {
		val name = element.name
		return buildString {
			definition {
				append("(localisation icon) <b>").append(name).append("</b>")
			}
			//图标
			val iconUrl = name.resolveIconUrl()
			if(iconUrl.isNotEmpty()) {
				content {
					appendIconTag(iconUrl)
				}
			}
		}
	}
	
	private fun getSequentialNumberDoc(element: ParadoxLocalisationSequentialNumber): String {
		return buildString {
			definition {
				append("(localisation sequential number) <b>").append(element.name).append("</b>")
				val description = element.paradoxSequentialNumber?.description
				if(description != null) {
					content {
						append(description)
					}
				}
			}
		}
	}
	
	private fun getColorDoc(element: ParadoxLocalisationColorfulText): String {
		return buildString {
			definition {
				append("(localisation color) <b>").append(element.name).append("</b>")
				val description = element.paradoxColor?.description
				if(description != null) {
					content {
						append(description)
					}
				}
			}
		}
	}
	
	private fun getCommandScopeDoc(element: ParadoxLocalisationCommandScope): String {
		return buildString {
			definition {
				append("(localisation command scope) <b>").append(element.name).append("</b>")
			}
			//来自规则文件
			val paradoxCommandScope = element.paradoxCommandScope
			if(paradoxCommandScope != null) {
				content {
					append(paradoxCommandScope.description)
				}
			}
		}
	}
	
	private fun getCommandFieldDoc(element: ParadoxLocalisationCommandField): String {
		return buildString {
			definition {
				append("(localisation command field) <b>").append(element.name).append("</b>")
			}
			val paradoxCommandField = element.paradoxCommandField
			if(paradoxCommandField != null) {
				content {
					append(paradoxCommandField.description)
				}
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
		return findLocalisation(link, getLocale(context), context.project, defaultToFirst = true)
	}
	
	private fun getScriptLink(link: String, context: PsiElement): ParadoxScriptProperty? {
		return findDefinition(link, null, context.project)
	}
	
	private fun getLocale(element: PsiElement): ParadoxLocale? {
		val file = element.containingFile
		return if(file is ParadoxLocalisationFile) file.paradoxLocale else inferredParadoxLocale
	}
}
