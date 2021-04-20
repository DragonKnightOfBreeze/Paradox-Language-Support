package com.windea.plugin.idea.paradox.localisation.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.model.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationDocumentationProvider : AbstractDocumentationProvider() {
	override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
		return when {
			element is ParadoxLocalisationProperty -> getPropertyInfo(element)
			element is ParadoxLocalisationLocale -> getLocaleInfo(element)
			element is ParadoxLocalisationIcon -> getIconInfo(element)
			element is ParadoxLocalisationSequentialNumber -> getSequentialNumberInfo(element)
			element is ParadoxLocalisationCommandScope -> getCommandScopeInfo(element)
			element is ParadoxLocalisationCommandField -> getCommandFieldInfo(element)
			element is ParadoxLocalisationColorfulText -> getColorInfo(element)
			else -> null
		}
	}
	
	private fun getPropertyInfo(element: ParadoxLocalisationProperty): String {
		return getLocalisationInfo(element)
		//return buildString {
		//	definition {
		//		element.paradoxFileInfo?.let{ fileInfo -> appendFileInfo(fileInfo).appendBr()}
		//		append("(localisation property) <b>").append(element.name).append("</b>")
		//	}
		//}
	}
	
	private fun getLocalisationInfo(element: ParadoxLocalisationProperty): String {
		return buildString {
			val name = element.name
			definition {
				element.paradoxFileInfo?.let{ fileInfo -> appendFileInfo(fileInfo).appendBr()}
				append("(localisation) <b>").append(name).append("</b>")
			}
		}
	}
	
	private fun getLocaleInfo(element: ParadoxLocalisationLocale): String {
		return buildString {
			val name = element.name
			definition {
				append("(localisation locale) <b>").append(name).append("</b>")
			}
		}
	}
	
	private fun getIconInfo(element: ParadoxLocalisationIcon): String {
		return buildString {
			val name = element.name
			definition {
				append("(localisation icon) <b>").append(name).append("</b>")
			}
		}
	}
	
	private fun getSequentialNumberInfo(element: ParadoxLocalisationSequentialNumber): String {
		return buildString {
			val name = element.name
			definition {
				append("(localisation sequential number) <b>").append(name).append("</b>")
			}
		}
	}
	
	private fun getCommandScopeInfo(element: ParadoxLocalisationCommandScope): String {
		return buildString {
			val name = element.name
			definition {
				append("(localisation command scope) <b>").append(name).append("</b>")
			}
		}
	}
	
	private fun getCommandFieldInfo(element: ParadoxLocalisationCommandField): String {
		return buildString {
			val name = element.name
			definition {
				append("(localisation command field) <b>").append(name).append("</b>")
			}
		}
	}
	
	private fun getColorInfo(element: ParadoxLocalisationColorfulText): String {
		return buildString {
			val name = element.name
			definition {
				append("(localisation color) <b>").append(name).append("</b>")
			}
		}
	}
	
	override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
		return when {
			element is ParadoxLocalisationProperty -> getPropertyDoc(element)
			element is ParadoxLocalisationLocale -> getLocaleDoc(element)
			element is ParadoxLocalisationIcon -> getIconDoc(element)
			element is ParadoxLocalisationSequentialNumber -> getSequentialNumberDoc(element)
			element is ParadoxLocalisationCommandScope -> getCommandScopeDoc(element)
			element is ParadoxLocalisationCommandField -> getCommandFieldDoc(element)
			element is ParadoxLocalisationColorfulText -> getColorDoc(element)
			else -> null
		}
	}
	
	private fun getPropertyDoc(element: ParadoxLocalisationProperty): String {
		return getLocalisationDoc(element)
		//val name = element.name
		//return buildString {
		//	definition {
		//		element.paradoxFileInfo?.let{ fileInfo -> appendFileInfo(fileInfo).appendBr()}
		//		append("(localisation property) <b>").append(name).append("</b>")
		//	}
		//}
	}
	
	private fun getLocalisationDoc(element: ParadoxLocalisationProperty): String {
		val name = element.name
		return buildString {
			definition {
				element.paradoxFileInfo?.let{ fileInfo -> appendFileInfo(fileInfo).appendBr()}
				append("(localisation) <b>").append(name).append("</b>")
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
			if(settings.renderLocalisationText) {
				val richText = element.renderText()
				if(richText.isNotEmpty()) {
					sections {
						section("Text", richText)
					}
				}
			}
		}
	}
	
	private fun getLocaleDoc(element: ParadoxLocalisationLocale): String {
		return buildString {
			val name = element.name
			definition {
				append("(localisation locale) <b>").append(name).append("</b>")
			}
			val paradoxLocale = element.paradoxLocale
			if(paradoxLocale != null) {
				content {
					append(paradoxLocale.description)
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
			val name = element.name
			definition {
				append("(localisation sequential number) <b>").append(name).append("</b>")
			}
			val paradoxSequentialNumber = element.paradoxSequentialNumber
			if(paradoxSequentialNumber != null) {
				content {
					append(paradoxSequentialNumber.description)
				}
			}
		}
	}
	
	private fun getCommandScopeDoc(element: ParadoxLocalisationCommandScope): String {
		return buildString {
			val name = element.name
			definition {
				append("(localisation command scope) <b>").append(name).append("</b>")
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
			val name = element.name
			definition {
				append("(localisation command field) <b>").append(name).append("</b>")
			}
			val paradoxCommandField = element.paradoxCommandField
			if(paradoxCommandField != null) {
				content {
					append(paradoxCommandField.description)
				}
			}
		}
	}
	
	private fun getColorDoc(element: ParadoxLocalisationColorfulText): String {
		return buildString {
			val name = element.name
			definition {
				append("(localisation color) <b>").append(name).append("</b>")
			}
			val paradoxColor = element.paradoxColor
			if(paradoxColor != null) {
				val description = paradoxColor.description
				val colorText = paradoxColor.colorText
				content {
					append(description).append(" (").append(colorText).append(")") //注明颜色
				}
			}
		}
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		if(link == null || context == null) return null
		return resolveLink(link,context)
	}
}
