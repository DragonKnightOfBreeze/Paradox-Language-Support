package icu.windea.pls.cwt.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.config.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.psi.*
import java.util.*

class CwtDocumentationProvider : AbstractDocumentationProvider() {
	override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
		return when(element) {
			is CwtProperty -> getPropertyInfo(element,originalElement)
			is CwtString -> getStringInfo(element)
			else -> null
		}
	}
	
	private fun getPropertyInfo(element: CwtProperty, originalElement: PsiElement?): String {
		return buildString {
			val name = element.name
			val configType = element.configType
			definition {
				if(configType != null) append("(").append(configType.text).append(") ")
				else append ("(property) ")
				append("<b>").append(name.escapeXmlOrAnonymous()).append("</b>")
				when(configType) {
					//为definitionProperty提供关于scope的额外文档注释
					null -> {
						val propertyElement = getDefinitionProperty(element, originalElement)?:return@buildString
						val config = propertyElement.propertyConfig ?:return@buildString
						val scopeMap = mergeScope(config.scopeMap,propertyElement.definitionPropertyInfo?.scope)
						for((sk, sv) in scopeMap) {
							appendBr().append("(scope) ").append(sk).append(" = ").append(sv)
						}
					}
					//为localisation_command提供关于scope的额外文档注释
					CwtConfigType.LocalisationCommand -> {
						//可以直接从psi文本中读取
						val supportedScopesText = element.value?.text ?: "?"
						appendBr().append("supported_scopes = $supportedScopesText")
					}
					//为modifier提供关于scope的额外文档注释
					CwtConfigType.Modifier -> {
						val gameType = originalElement?.gameType?: return@buildString
						val configGroup = getConfig()[gameType] ?: return@buildString
						val categories = element.value?.value?: return@buildString
						val category = configGroup.modifierCategories[categories]
							?: configGroup.modifierCategoryIdMap[categories]?: return@buildString
						val supportedScopesText = category.supportedScopesText
						appendBr().append("supported_scopes = $supportedScopesText")
					}
					else -> pass()
				}
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
			is CwtProperty -> getPropertyDoc(element,originalElement)
			is CwtString -> getStringDoc(element)
			else -> null
		}
	}
	
	private fun getPropertyDoc(element: CwtProperty, originalElement: PsiElement?): String {
		return buildString {
			val name = element.name
			val configType = element.configType
			definition {
				if(configType != null) append("(").append(configType.text).append(") ")
				else append ("(property) ")
				append("<b>").append(name.escapeXmlOrAnonymous()).append("</b>")
				when(configType) {
					//为definitionProperty提供特殊的文档注释（scope)
					null -> {
						val propertyElement = getDefinitionProperty(element, originalElement)?:return@buildString
						val config = propertyElement.propertyConfig ?:return@buildString
						val scopeMap = mergeScope(config.scopeMap,propertyElement.definitionPropertyInfo?.scope)
						for((sk, sv) in scopeMap) {
							appendBr().append("(scope) ").append(sk).append(" = ").append(sv)
						}
					}
					//为localisation_command提供关于scope的额外文档注释
					CwtConfigType.LocalisationCommand -> {
						//可以直接从psi文本中读取
						val supportedScopesText = element.value?.text ?: "?"
						appendBr().append("supported_scopes = $supportedScopesText")
					}
					//为modifier提供关于scope的额外文档注释
					CwtConfigType.Modifier -> {
						val gameType = originalElement?.gameType?: return@buildString
						val configGroup = getConfig()[gameType] ?: return@buildString
						val categories = element.value?.value?: return@buildString
						val category = configGroup.modifierCategories[categories]
							?: configGroup.modifierCategoryIdMap[categories]?: return@buildString
						val supportedScopesText = category.supportedScopesText
						appendBr().append("supported_scopes = $supportedScopesText")
					}
					else -> pass()
				}
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
	
	private fun getDefinitionProperty(element:CwtProperty,originalElement: PsiElement?):ParadoxScriptProperty?{
		//如果element.text对应的expression的类型是const，并且originalElement是scriptPropertyKeyId或scriptProperty
		if(CwtKeyExpression.resolve(element.text.unquote()).type != CwtKeyExpression.Type.Constant) return null
		if(originalElement == null) return null
		val parent = originalElement.parent?:return null
		val keyElement =  parent as? ParadoxScriptPropertyKey ?: parent.parent as? ParadoxScriptPropertyKey ?:return null 
		return keyElement.parent as? ParadoxScriptProperty
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		if(link == null || context == null) return null
		return resolveLink(link, context)
	}
}