package icu.windea.pls.script.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.util.*
import icu.windea.pls.core.selector.*

class ParadoxScriptDocumentationProvider : AbstractDocumentationProvider() {
	override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, `object`: Any?, element: PsiElement?): PsiElement? {
		if(`object` is PsiElement) return `object`
		return super.getDocumentationElementForLookupItem(psiManager, `object`, element)
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		if(link == null || context == null) return null
		return resolveScope(link, context)
	}
	
	override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
		return when(element) {
			is ParadoxScriptVariable -> getScriptedVariableInfo(element)
			is IParadoxScriptInputParameter -> getInputParameterInfo(element)
			is IParadoxScriptParameter -> {
				if(originalElement != null && originalElement.elementType != PARAMETER_ID) {
					getInputParameterInfo(element)
				} else {
					getParameterInfo(element)
				}
			}
			is ParadoxScriptProperty -> getPropertyInfo(element)
			is ParadoxScriptExpressionElement -> {
				val config = element.getConfig()
				when(config?.expression?.type) {
					CwtDataTypes.Value, CwtDataTypes.ValueSet -> getValueSetValueInfo(element, config)
					else -> if(element is ParadoxScriptPropertyKey) generateDoc(element.parent, originalElement) else null
				}
			}
			else -> null
		}
	}
	
	private fun getScriptedVariableInfo(element: ParadoxScriptVariable): String {
		val name = element.name
		return buildString {
			buildScriptedVariableDefinition(element, name)
		}
	}
	
	private fun getInputParameterInfo(element: ParadoxScriptNamedElement): String {
		return buildString {
			buildInputParameterDefinition(element, element.name.orEmpty())
		}
	}
	
	private fun getParameterInfo(element: IParadoxScriptParameter): String {
		val name = element.name
		return buildString {
			buildParameterDefinition(element, name)
		}
	}
	
	private fun getPropertyInfo(element: ParadoxScriptProperty): String? {
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) return getDefinitionInfo(element, definitionInfo)
		val definitionElementInfo = element.definitionElementInfo
		if(definitionElementInfo != null) return null //不为无法解析的属性元素提供文档
		val name = element.name
		return buildString {
			buildPropertyDefinition(element, name)
		}
	}
	
	private fun getDefinitionInfo(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String {
		return buildString {
			buildDefinitionDefinition(element, definitionInfo, null, null)
		}
	}
	
	private fun getValueSetValueInfo(element: ParadoxScriptExpressionElement, config: CwtKvConfig<*>): String {
		return buildString {
			buildValueSetValueDefinition(element, config)
		}
	}
	
	override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
		return when(element) {
			is ParadoxScriptVariable -> getScriptedVariableDoc(element)
			is IParadoxScriptInputParameter -> getInputParameterDoc(element)
			is IParadoxScriptParameter -> {
				if(originalElement != null && originalElement.elementType != PARAMETER_ID) {
					getInputParameterDoc(element)
				} else {
					getParameterDoc(element)
				}
			}
			is ParadoxScriptProperty -> getPropertyDoc(element)
			is ParadoxScriptExpressionElement -> {
				val config = element.getConfig()
				when(config?.expression?.type) {
					CwtDataTypes.Value, CwtDataTypes.ValueSet -> getValueSetValueDoc(element, config)
					else -> if(element is ParadoxScriptPropertyKey) generateDoc(element.parent, originalElement) else null
				}
			}
			else -> null
		}
	}
	
	private fun getScriptedVariableDoc(element: ParadoxScriptVariable): String {
		val name = element.name
		return buildString {
			buildScriptedVariableDefinition(element, name)
			buildLineCommentContent(element)
		}
	}
	
	private fun getInputParameterDoc(element: ParadoxScriptNamedElement): String {
		return buildString {
			buildInputParameterDefinition(element, element.name.orEmpty())
		}
	}
	
	private fun getParameterDoc(element: IParadoxScriptParameter): String {
		val name = element.name
		return buildString {
			buildParameterDefinition(element, name)
		}
	}
	
	private fun getPropertyDoc(element: ParadoxScriptProperty): String? {
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) return getDefinitionDoc(element, definitionInfo)
		val definitionElementInfo = element.definitionElementInfo
		if(definitionElementInfo != null) return null //不为无法解析的属性元素提供文档
		val name = element.name
		return buildString {
			buildPropertyDefinition(element, name)
			buildLineCommentContent(element)
		}
	}
	
	private fun getDefinitionDoc(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String {
		return buildString {
			//在definition部分，相关图片信息显示在相关本地化信息之后，在sections部分则显示在之前
			val localisationTargetMap = mutableMapOf<String, ParadoxLocalisationProperty>()
			val imageTargetMap = mutableMapOf<String, Tuple2<PsiFile, Int>>()
			buildDefinitionDefinition(element, definitionInfo, localisationTargetMap, imageTargetMap)
			buildExtDocContent(definitionInfo)
			buildLineCommentContent(element)
			buildParametersContent(element, definitionInfo)
			val sections = mutableMapOf<String, String>()
			buildRelatedImageSections(imageTargetMap, sections)
			buildRelatedLocalisationSections(localisationTargetMap, sections)
			buildDefinitionSections(sections)
		}
	}
	
	private fun getValueSetValueDoc(element: ParadoxScriptExpressionElement, config: CwtKvConfig<*>): String {
		return buildString {
			buildValueSetValueDefinition(element, config)
		}
	}
	
	
	private fun StringBuilder.buildScriptedVariableDefinition(element: ParadoxScriptVariable, name: String) {
		definition {
			//加上文件信息
			appendFileInfoHeader(element.fileInfo)
			//加上定义信息
			append(PlsDocBundle.message("name.script.scriptedVariable")).append(" <b>@").append(name.escapeXmlOrAnonymous()).append("</b>")
			element.unquotedValue?.let { unquotedValue -> append(" = ").append(unquotedValue.escapeXml()) }
		}
	}
	
	private fun StringBuilder.buildInputParameterDefinition(element: PsiElement, name: String) {
		definition {
			//加上文件信息
			appendFileInfoHeader(element.fileInfo)
			//加上定义信息
			append(PlsDocBundle.message("name.script.inputParameter")).append(" <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
		}
	}
	
	private fun StringBuilder.buildParameterDefinition(element: IParadoxScriptParameter, name: String) {
		definition {
			//加上文件信息
			appendFileInfoHeader(element.fileInfo)
			//加上定义信息
			append(PlsDocBundle.message("name.script.parameter")).append(" <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
		}
	}
	
	private fun StringBuilder.buildPropertyDefinition(element: ParadoxScriptProperty, name: String) {
		definition {
			//加上文件信息
			appendFileInfoHeader(element.fileInfo)
			//加上定义信息
			append(PlsDocBundle.message("name.script.property")).append(" <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
			element.value?.let { value -> append(" = ").append(value.escapeXml()) }
		}
	}
	
	private fun StringBuilder.buildDefinitionDefinition(
		element: ParadoxScriptProperty,
		definitionInfo: ParadoxDefinitionInfo,
		localisationTargetMap: MutableMap<String, ParadoxLocalisationProperty>? = null,
		imageTargetMap: MutableMap<String, Tuple2<PsiFile, Int>>? = null
	) {
		definition {
			//加上文件信息
			appendFileInfoHeader(element.fileInfo)
			//加上定义信息
			val name = definitionInfo.name
			val typeLinkText = buildString {
				val gameType = definitionInfo.gameType
				val typeConfig = definitionInfo.typeConfig
				val typeLink = "${gameType.id}.types.${typeConfig.name}"
				appendCwtLink(typeConfig.name, typeLink, typeConfig.pointer.element)
				val subtypeConfigs = definitionInfo.subtypeConfigs
				if(subtypeConfigs.isNotEmpty()) {
					for(subtypeConfig in subtypeConfigs) {
						append(", ")
						val subtypeLink = "$typeLink.${subtypeConfig.name}"
						appendCwtLink(subtypeConfig.name, subtypeLink, subtypeConfig.pointer.element)
					}
				}
			}
			append(PlsDocBundle.message("name.script.definition")).append(" <b>").append(name.escapeXmlOrAnonymous()).append("</b>: ").append(typeLinkText)
			
			//加上相关本地化信息：去重后的一组本地化的键名，不包括可选且没有对应的本地化的项，按解析顺序排序
			val localisationInfos = definitionInfo.localisation
			if(localisationInfos.isNotEmpty()) {
				val project = element.project
				val localisationKeys = mutableSetOf<String>()
				val usedLocalisationTargetMap = localisationTargetMap ?: mutableMapOf()
				for((key, locationExpression, required) in localisationInfos) {
					if(!usedLocalisationTargetMap.containsKey(key)) {
						val selector = localisationSelector().gameTypeFrom(element).preferRootFrom(element).preferLocale(preferredParadoxLocale())
						val (targetKey, target) = locationExpression.resolve(definitionInfo.name, element, project, selector = selector) ?: continue //发生意外，直接跳过
						if(target != null) usedLocalisationTargetMap.put(key, target)
						if(required || target != null) {
							if(localisationKeys.add(key)) {
								appendBr()
								append(PlsDocBundle.message("name.script.relatedLocalisation")).append(" ").append(key).append(" = ").appendLocalisationLink(targetKey, element, resolved = target != null)
							}
						}
					}
				}
			}
			//加上相关图片信息：去重后的一组DDS文件的filePath，或者sprite的definitionKey，不包括可选且没有对应的图片的项，按解析顺序排序
			val imagesInfos = definitionInfo.images
			if(imagesInfos.isNotEmpty()) {
				val project = element.project
				val imageKeys = mutableSetOf<String>()
				val usedImageTargetMap = imageTargetMap ?: mutableMapOf()
				for((key, locationExpression, required) in imagesInfos) {
					if(!usedImageTargetMap.containsKey(key)) {
						val (filePath, target, frame) = locationExpression.resolve(definitionInfo.name, element, project) ?: continue //发生意外，直接跳过
						if(target != null) usedImageTargetMap.put(key, tupleOf(target, frame))
						if(required || target != null) {
							if(imageKeys.add(key)) {
								appendBr()
								append(PlsDocBundle.message("name.script.relatedImage")).append(" ").append(key).append(" = ").appendFilePathLink(filePath, element, resolved = target != null)
							}
						}
					}
				}
			}
		}
	}
	
	private fun StringBuilder.buildExtDocContent(definitionInfo: ParadoxDefinitionInfo) {
		//加上从PlsExtDocBundle中得到的文档文本
		val docText = PlsExtDocBundle.message(definitionInfo.name, definitionInfo.type, definitionInfo.gameType)
		if(docText != null && docText.isNotEmpty()) {
			content {
				append(docText)
			}
		}
	}
	
	private fun StringBuilder.buildParametersContent(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
		//如果定义支持参数且拥有参数，则在文档中显示
		if(getSettings().scriptShowParameters) {
			if(definitionInfo.type in definitionInfo.configGroup.definitionTypesSupportParameters) {
				val parameterMap = element.parameterMap
				if(parameterMap.isNotEmpty()) {
					content {
						append(PlsDocBundle.message("content.parameters", parameterMap.keys.joinToString()))
					}
				}
			}
		}
	}
	
	private fun buildRelatedImageSections(map: MutableMap<String, Tuple2<PsiFile, Int>>, sections: MutableMap<String, String>) {
		//加上DDS图片预览图
		if(getSettings().scriptRenderRelatedImages) {
			if(map.isNotEmpty()) {
				for((key, tuple) in map) {
					val (target, frame) = tuple
					val url = ParadoxDdsUrlResolver.resolveByFile(target.virtualFile, frame)
					val tag = buildString { appendImgTag(url) }
					sections.put(key.toCapitalizedWords(), tag)
				}
			}
		}
	}
	
	private fun buildRelatedLocalisationSections(map: Map<String, ParadoxLocalisationProperty>, sections: MutableMap<String, String>) {
		//加上渲染后的相关本地化文本
		if(getSettings().scriptRenderRelatedLocalisation) {
			if(map.isNotEmpty()) {
				for((key, target) in map) {
					val richText = ParadoxLocalisationTextRenderer.render(target)
					sections.put(key.toCapitalizedWords(), richText)
				}
			}
		}
	}
	
	private fun StringBuilder.buildDefinitionSections(sections: MutableMap<String, String>) {
		if(sections.isEmpty()) return
		sections {
			for((key, value) in sections) {
				section(key, value)
			}
		}
	}
	
	private fun StringBuilder.buildValueSetValueDefinition(element: ParadoxScriptExpressionElement, config: CwtKvConfig<*>) {
		definition {
			//不加上文件信息
			//加上定义信息
			append(PlsDocBundle.message("name.cwt.valueSetValue")).append(" <b>").append(element.value.escapeXmlOrAnonymous()).append("</b>")
			//加上分组信息
			val valueSetName = config.expression.value
			if(valueSetName != null && valueSetName.isNotEmpty()) {
				append(": ").append(valueSetName)
			}
		}
	}
	
	private fun StringBuilder.buildLineCommentContent(element: PsiElement) {
		//加上单行注释文本
		if(getSettings().scriptRenderLineComment) {
			val docText = getLineCommentDocText(element)
			if(docText != null && docText.isNotEmpty()) {
				content {
					append(docText)
				}
			}
		}
	}
}
