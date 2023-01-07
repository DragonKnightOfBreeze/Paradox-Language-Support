package icu.windea.pls.core.editor

import ai.grazie.utils.*
import com.intellij.lang.documentation.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.script.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

@Suppress("UNUSED_PARAMETER")
class ParadoxDocumentationProvider : AbstractDocumentationProvider() {
	override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, `object`: Any?, element: PsiElement?): PsiElement? {
		if(`object` is PsiElement) return `object`
		return super.getDocumentationElementForLookupItem(psiManager, `object`, element)
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		if(link == null || context == null) return null
		return resolveLink(link, context)
	}
	
	override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?): String? {
		return when(element) {
			is ParadoxParameterElement -> getParameterInfo(element, originalElement)
			//is ParadoxArgument -> getParameterInfo(element, originalElement)
			//is ParadoxParameter -> getParameterInfo(element, originalElement)
			is ParadoxValueSetValueElement -> getValueSetValueInfo(element, originalElement)
			is ParadoxScriptStringExpressionElement -> {
				//TODO 直接基于ParadoxValueSetValueElement进行索引，从而下面的代码不再需要
				val config = ParadoxCwtConfigHandler.resolveConfigs(element).firstOrNull()
				if(config != null && config.expression.type.isValueSetValueType()) {
					return getValueSetValueInfo(element, originalElement)
				}
				null
			}
			is ParadoxModifierElement -> getModifierInfo(element, originalElement)
			else -> null
		}
	}
	
	//private fun getParameterInfo(element: PsiElement, originalElement: PsiElement?): String? {
	//	val resolved = element.references.firstOrNull()?.resolve() as? ParadoxParameterElement ?: return null
	//	return getParameterInfo(resolved, originalElement)
	//}
	
	private fun getParameterInfo(element: ParadoxParameterElement, originalElement: PsiElement?): String {
		return buildString {
			buildParameterDefinition(element)
		}
	}
	
	private fun getValueSetValueInfo(element: PsiElement, originalElement: PsiElement?): String? {
		val resolved = element.references.firstOrNull()?.resolve() as? ParadoxValueSetValueElement ?: return null
		return getValueSetValueInfo(resolved, originalElement)
	}
	
	private fun getValueSetValueInfo(element: ParadoxValueSetValueElement, originalElement: PsiElement?): String {
		val name = element.name
		val valueSetNames = element.valueSetNames
		val configGroup = getCwtConfig(element.project).getValue(element.gameType)
		return buildString {
			buildValueSetValueDefinition(name, valueSetNames, configGroup)
		}
	}
	
	private fun getModifierInfo(element: ParadoxModifierElement, originalElement: PsiElement?): String {
		return buildString {
			buildModifierDefinition(element)
		}
	}
	
	override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
		return when(element) {
			is ParadoxParameterElement -> getParameterDoc(element, originalElement)
			//is ParadoxArgument -> getParameterDoc(element, originalElement)
			//is ParadoxParameter -> getParameterDoc(element, originalElement)
			is ParadoxValueSetValueElement -> getValueSetValueDoc(element, originalElement)
			is ParadoxScriptStringExpressionElement -> {
				val config = ParadoxCwtConfigHandler.resolveConfigs(element).firstOrNull()
				if(config != null && config.expression.type.isValueSetValueType()) {
					return getValueSetValueDoc(element, originalElement)
				}
				null
			}
			is ParadoxModifierElement -> getModifierDoc(element, originalElement)
			else -> null
		}
	}
	
	//private fun getParameterDoc(element: PsiElement, originalElement: PsiElement?): String? {
	//	val resolved = element.reference?.resolve() as? ParadoxParameterElement ?: return null
	//	return getParameterInfo(resolved, originalElement)
	//}
	
	private fun getParameterDoc(element: ParadoxParameterElement, originalElement: PsiElement?): String {
		return buildString {
			buildParameterDefinition(element)
		}
	}
	
	private fun getValueSetValueDoc(element: PsiElement, originalElement: PsiElement?): String? {
		val resolved = element.references.firstOrNull()?.resolve() as? ParadoxValueSetValueElement ?: return null
		return getValueSetValueInfo(resolved, originalElement)
	}
	
	private fun getValueSetValueDoc(element: ParadoxValueSetValueElement, originalElement: PsiElement?): String {
		val name = element.name
		val valueSetNames = element.valueSetNames
		val configGroup = getCwtConfig(element.project).getValue(element.gameType)
		return buildString {
			buildValueSetValueDefinition(name, valueSetNames, configGroup)
		}
	}
	
	private fun getModifierDoc(element: ParadoxModifierElement, originalElement: PsiElement?): String {
		return buildString {
			buildModifierDefinition(element)
		}
	}
	
	private fun StringBuilder.buildParameterDefinition(element: ParadoxParameterElement) {
		definition {
			//不加上文件信息
			
			//加上名字
			val name = element.name
			append(PlsDocBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
			
			
			//加上定义信息
			val definitionName = element.definitionName
			val definitionType = element.definitionType
			if(definitionType.isEmpty()) return@definition
			val gameType = element.gameType
			appendBr()
			append(PlsDocBundle.message("ofDefinition"))
			append(" ")
			append(definitionName.escapeXml().orAnonymous())
			append(": ")
			
			val type = definitionType.first()
			val typeLink = "${gameType.id}/types/${type}"
			appendCwtLink(type, typeLink)
			for((index, t) in definitionType.withIndex()) {
				if(index == 0) continue
				append(", ")
				val subtypeLink = "$typeLink/${t}"
				appendCwtLink(t, subtypeLink)
			}
		}
	}
	
	private fun StringBuilder.buildValueSetValueDefinition(name: String, valueSetNames: List<String>, configGroup: CwtConfigGroup) {
		definition {
			//不加上文件信息
			append(PlsDocBundle.message("prefix.valueSetValue")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
			var appendSeparator = false
			for(valueSetName in valueSetNames) {
				if(appendSeparator) append(" | ") else appendSeparator = true
				val valueConfig = configGroup.values[valueSetName]
				if(valueConfig != null) {
					val gameType = configGroup.gameType
					val typeLink = "${gameType.id}/values/${valueSetName}"
					append(": ").appendCwtLink(valueSetName, typeLink)
				} else {
					append(": ").append(valueSetName)
				}
			}
		}
	}
	
	private fun StringBuilder.buildModifierDefinition(element: ParadoxModifierElement) {
		definition {
			//没有文件信息
			
			//加上名字
			val name = element.name
			append(PlsDocBundle.message("prefix.modifier")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
			
			//加上模版信息
			val templateExpression = element.templateExpression
			if(templateExpression != null) {
				val configGroup = templateExpression.configGroup
				val gameType = element.gameType
				val template = templateExpression.template
				val templateString = template.expressionString
				appendBr().append(PlsDocBundle.message("by")).append(" ")
				appendCwtLink(templateString, "${gameType.id}/modifiers/$templateString")
				
				//加上生成源信息
				val referenceNodes = templateExpression.referenceNodes
				if(referenceNodes.isNotEmpty()) {
					appendBr().append(PlsDocBundle.message("generatedFrom")).append(" ")
					var appendSeparator = false
					for(referenceNode in referenceNodes) {
						if(appendSeparator) append(", ") else appendSeparator = true
						val configExpression = referenceNode.configExpression ?: continue
						when(configExpression.type) {
							CwtDataType.Modifier -> {
								val modifierName = referenceNode.text
								append(PlsDocBundle.message("prefix.modifier"))
								append(" ")
								append(modifierName)
							}
							CwtDataType.Definition -> {
								val definitionName = referenceNode.text
								val definitionType = configExpression.value!!
								append(PlsDocBundle.message("prefix.definition"))
								append(" ")
								appendDefinitionLink(gameType, definitionName, definitionType, element)
							}
							CwtDataType.Enum -> {
								val enumValueName = referenceNode.text
								val enumName = configExpression.value!!
								if(configGroup.enums.containsKey(enumName)) {
									append(PlsDocBundle.message("prefix.enumValue"))
									append(" ")
									appendCwtLink(enumName, "${gameType.id}/enums/${enumName}/${enumValueName}", element)
								} else if(configGroup.complexEnums.containsKey(enumName)) {
									append(PlsDocBundle.message("prefix.complexEnumValue"))
									append(" ")
									append(enumValueName)
								} else {
									//unexpected
									append(PlsDocBundle.message("prefix.enumValue"))
									append(" ")
									append(enumValueName)
								}
							}
							CwtDataType.Value -> {
								val valueSetName = referenceNode.text
								val valueName = configExpression.value!!
								if(configGroup.values.containsKey(valueName)) {
									append(PlsDocBundle.message("prefix.valueSetValue"))
									append(" ")
									appendCwtLink(valueName, "${gameType.id}/values/${valueName}/${valueSetName}", element)
								} else {
									append(PlsDocBundle.message("prefix.valueSetValue"))
									append(" ")
									append(valueName)
								}
							}
							else -> pass()
						}
					}
				}
			}
		}
	}
	
	private fun getReferenceElement(originalElement: PsiElement?): PsiElement? {
		val element = when {
			originalElement == null -> return null
			originalElement.elementType == TokenType.WHITE_SPACE -> originalElement.prevSibling ?: return null
			else -> originalElement
		}
		return when {
			element is LeafPsiElement -> element.parent
			else -> element
		}
	}
}