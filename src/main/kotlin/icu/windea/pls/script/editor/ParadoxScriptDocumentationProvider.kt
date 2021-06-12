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
				element.paradoxFileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				append("(script variable) <b>").append(name.escapeXml()).append("</b>")
				element.unquotedValue?.let { unquotedValue -> append(" = ").append(unquotedValue.escapeXml()) }
			}
		}
	}
	
	private fun getPropertyInfo(element: ParadoxScriptProperty): String {
		val definition = element.paradoxDefinitionInfo
		if(definition != null) {
			return getDefinitionInfo(element, definition)
		}
		return buildString {
			val name = element.name
			definition {
				element.paradoxFileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				append("(script property) <b>").append(name.escapeXml()).append("</b>")
				element.truncatedValue?.let { truncatedValue -> append(" = ").append(truncatedValue.escapeXml()) }
			}
		}
	}
	
	private fun getDefinitionInfo(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String {
		val localisation = definitionInfo.localisation
		return buildString {
			definition {
				element.paradoxFileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				val name = definitionInfo.name.ifEmpty { anonymousString }
				val typeText = definitionInfo.typeText
				append("(definition) <b>").append(name.escapeXml()).append("</b>: ").append(typeText)
			}
			if(localisation.isNotEmpty()) {
				definition {
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
				element.paradoxFileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				append("(script variable) <b>").append(name).append("</b>")
				element.unquotedValue?.let { unquotedValue -> append(" = ").append(unquotedValue.escapeXml()) }
			}
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
	
	private fun getPropertyDoc(element: ParadoxScriptProperty): String {
		val definition = element.paradoxDefinitionInfo
		if(definition != null) {
			return getDefinitionDoc(element, definition)
		}
		return buildString {
			val name = element.name
			definition {
				element.paradoxFileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				append("(script property) <b>").append(name.escapeXml()).append("</b>")
				element.truncatedValue?.let { truncatedValue -> append(" = ").append(truncatedValue.escapeXml()) }
			}
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
	
	private fun getDefinitionDoc(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String {
		val type = definitionInfo.type
		val localisation = definitionInfo.localisation
		return buildString {
			definition {
				element.paradoxFileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
				val name = definitionInfo.name.ifEmpty { anonymousString }
				val typeText = definitionInfo.typeText
				append("(definition) <b>").append(name.escapeXml()).append("</b>: ").append(typeText)
				
				if(localisation.isNotEmpty()) {
					for((n, kn) in localisation) {
						if(kn.isEmpty()) continue //不显示keyName为空（匿名）的definitionLocalisation
						appendBr()
						append("(definition localisation) ").append(n).append(" = <b>").appendPsiLink("#", kn).append("</b>")
					}
				}
			}
			//单行注释文本
			if(getSettings().renderLineCommentText) {
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
			if(getSettings().renderDefinitionText) {
				if(localisation.isNotEmpty()) {
					val richTexts = mutableListOf<Pair<String, String>>()
					for((n, kn) in localisation) {
						if(kn.isEmpty()) continue //不显示keyName为空（匿名）的definitionLocalisation
						val e = findLocalisation(kn, element.paradoxLocale, element.project, hasDefault = true)
						val richText = e?.renderText() ?: continue
						//TODO sectionName暂时使用cwt规则文件中types.type[...].localisation的key，尽管不能保证规范性
						//val sectionName = n.toCapitalizedWords()
						val sectionName = n
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
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		if(link == null || context == null) return null
		return resolveLink(link, context)
	}
}
