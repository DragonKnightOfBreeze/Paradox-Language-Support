package com.windea.plugin.idea.paradox.localisation.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*
import com.windea.plugin.idea.paradox.script.psi.*
import com.windea.plugin.idea.paradox.settings.*

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
			element is ParadoxLocalisationCommandKey -> getCommandKeyInfo(element)
			element is ParadoxLocalisationSerialNumber -> getSerialNumberInfo(element)
			element is ParadoxLocalisationColorfulText -> getColorInfo(element)
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
	
	private fun getLocalisationInfo(element: ParadoxLocalisationProperty):String{
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
	
	private fun getCommandKeyInfo(element: ParadoxLocalisationCommandKey): String {
		return buildString {
			definition {
				append("(localisation command key) <b>").append(element.name).append("</b>")
			}
		}
	}
	
	private fun getSerialNumberInfo(element: ParadoxLocalisationSerialNumber): String {
		return buildString {
			definition {
				append("(localisation serial number) <b>").append(element.name).append("</b>")
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
			element is ParadoxLocalisationCommandKey -> getCommandKeyDoc(element)
			element is ParadoxLocalisationSerialNumber -> getSerialNumberDoc(element)
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
	
	private fun getLocalisationDoc(element:ParadoxLocalisationProperty):String{
		val name = element.name
		return buildString {
			definition {
				element.paradoxFileInfo?.path?.let { append("[").append(it).append("]<br>") }
				append("(localisation) <b>").append(name).append("</b>")
			}
			//之前的单行注释文本
			getDocTextFromPreviousComment(element).ifNotEmpty { docText->
				content {
					append(docText)
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
					append(" - ").append(description)
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
	
	private fun getCommandKeyDoc(element: ParadoxLocalisationCommandKey): String {
		return buildString {
			definition {
				append("(localisation command key) <b>").append(element.name).append("</b>")
			}
		}
	}
	
	private fun getSerialNumberDoc(element: ParadoxLocalisationSerialNumber): String {
		return buildString {
			definition {
				append("(localisation serial number) <b>").append(element.name).append("</b>")
				val description = element.paradoxSerialNumber?.description
				if(description != null) {
					append(" - ").append(description)
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
					append(" - ").append(description)
				}
			}
		}
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		return when{
			link == null || context == null -> null
			link.startsWith("#") -> getLocalisationLink(link, context)
			link.startsWith("$") -> getScriptLink(link,context)
			else -> null
		}
	}
	
	private fun getLocalisationLink(link: String, context: PsiElement): ParadoxLocalisationProperty? {
		return findLocalisationPropertyOrFirst(link.drop(1), getLocale(context), context.project)
	}
	
	private fun getScriptLink(link:String,context: PsiElement): ParadoxScriptProperty?{
		return findScriptProperty(link.drop(1),context.project)
	}
	
	private fun getLocale(element:PsiElement):ParadoxLocale?{
		val file = element.containingFile
		return if(file is ParadoxLocalisationFile) file.paradoxLocale  else inferredParadoxLocale
	}
}
