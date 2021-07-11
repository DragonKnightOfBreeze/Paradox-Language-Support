package icu.windea.pls.cwt.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import java.util.*

class CwtDocumentationProvider : AbstractDocumentationProvider() {
	override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
		return when(element) {
			is CwtProperty -> getPropertyInfo(element)
			is CwtString -> getStringInfo(element)
			else -> null
		}
	}
	
	private fun getPropertyInfo(element: CwtProperty): String {
		return buildString {
			val name = element.name
			val configTypeText = element.configType?.text
			definition {
				if(configTypeText != null) append("(").append(configTypeText).append(") ")
				else append ("(property) ")
				append("<b>").append(name.escapeXmlOrAnonymous()).append("</b>")
			}
		}
	}
	
	private fun getStringInfo(element: CwtString): String {
		return buildString {
			val name = element.name
			val configTypeText = element.configType?.text
			definition {
				if(configTypeText != null) append("(").append(configTypeText).append(") ")
				append("<b>").append(name.escapeXmlOrAnonymous()).append("</b>")
			}
		}
	}
	
	override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
		return when(element) {
			is CwtProperty -> getPropertyDoc(element)
			is CwtString -> getStringDoc(element)
			else -> null
		}
	}
	
	private fun getPropertyDoc(element: CwtProperty): String {
		return buildString {
			val name = element.name
			val configTypeText = element.configType?.text
			definition {
				if(configTypeText != null) append("(").append(configTypeText).append(") ")
				else append ("(property) ")
				append("<b>").append(name.escapeXmlOrAnonymous()).append("</b>")
			}
			//文档注释，以###开始
			val documentation = getDocumentation(element)
			if(documentation != null) {
				content {
					append(documentation)
				}
			}
		}
	}
	
	private fun getStringDoc(element: CwtString): String {
		return buildString {
			val name = element.name
			val configTypeText = element.configType?.text
			definition {
				if(configTypeText != null) append("(").append(configTypeText).append(") ")
				append("<b>").append(name.escapeXmlOrAnonymous()).append("</b>")
			}
			//文档注释，以###开始
			val documentation = getDocumentation(element)
			if(documentation != null) {
				content {
					append(documentation)
				}
			}
		}
	}
	
	private fun getDocumentation(element: PsiElement): String? {
		var current: PsiElement = element
		var documentationLines: LinkedList<String>? = null
		while(true) {
			current = current.prevSibling ?: break
			when {
				current is CwtDocumentationComment -> {
					val documentationText = current.documentationText
					if(documentationText != null) {
						if(documentationLines == null) documentationLines = LinkedList()
						documentationLines.addFirst(documentationText.text)
					}
				}
				current is PsiWhiteSpace || current is PsiComment -> continue
				else -> break
			}
		}
		return documentationLines?.joinToString("\n")
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		if(link == null || context == null) return null
		return resolveLink(link, context)
	}
}