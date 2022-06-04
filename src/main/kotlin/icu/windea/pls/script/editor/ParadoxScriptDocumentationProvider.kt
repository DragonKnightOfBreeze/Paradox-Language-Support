package icu.windea.pls.script.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*

class ParadoxScriptDocumentationProvider : AbstractDocumentationProvider() {
	override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, `object`: Any?, element: PsiElement?): PsiElement? {
		if(`object` is PsiElement) return `object`
		return super.getDocumentationElementForLookupItem(psiManager, `object`, element)
	}
	
	override fun getDocumentationElementForLink(psiManager: PsiManager?, link: String?, context: PsiElement?): PsiElement? {
		if(link == null || context == null) return null
		return resolveLink(link, context)
	}
	
	override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
		return when(element) {
			is ParadoxScriptVariableName -> getQuickNavigateInfo(element.parent, originalElement) //防止意外情况
			is ParadoxScriptVariable -> getScriptedVariableInfo(element)
			is IParadoxScriptParameter -> getParameterInfo(element)
			is ParadoxScriptProperty -> getPropertyInfo(element)
			else -> null
		}
	}
	
	private fun getScriptedVariableInfo(element: ParadoxScriptVariable): String {
		val name = element.name
		return buildString {
			buildScriptedVariableDefinition(element, name)
		}
	}
	
	private fun getParameterInfo(element: IParadoxScriptParameter): String {
		val name = element.name
		return buildString {
			buildParameterDefinition(element, name)
		}
	}
	
	private fun getPropertyInfo(element: ParadoxScriptProperty): String {
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) return getDefinitionInfo(element, definitionInfo)
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
	
	override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
		return when(element) {
			is ParadoxScriptVariableName -> generateDoc(element.parent, originalElement) //防止意外情况
			is ParadoxScriptVariable -> getScriptedVariableDoc(element)
			is IParadoxScriptParameter -> getParameterDoc(element)
			is ParadoxScriptProperty -> getPropertyDoc(element)
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
	
	private fun getParameterDoc(element: IParadoxScriptParameter): String {
		val name = element.name
		return buildString {
			buildParameterDefinition(element, name)
		}
	}
	
	private fun getPropertyDoc(element: ParadoxScriptProperty): String {
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) return getDefinitionDoc(element, definitionInfo)
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
			val pictureTargetMap = mutableMapOf<String, Tuple2<PsiFile, Int>>()
			buildDefinitionDefinition(element, definitionInfo, localisationTargetMap, pictureTargetMap)
			buildLineCommentContent(element)
			val sections = mutableMapOf<String, String>()
			buildRelatedPictureSections(pictureTargetMap, sections)
			buildRelatedLocalisationSections(localisationTargetMap, sections)
			buildDefinitionSections(sections)
		}
	}
	
	private fun StringBuilder.buildScriptedVariableDefinition(element: ParadoxScriptVariable, name: String) {
		definition {
			//加上文件信息
			appendFileInfoHeader(element.fileInfo, element.project)
			//加上定义信息
			append(PlsDocBundle.message("name.script.scriptedVariable")).append(" <b>@").append(name.escapeXmlOrAnonymous()).append("</b>")
			element.unquotedValue?.let { unquotedValue -> append(" = ").append(unquotedValue.escapeXml()) }
		}
	}
	
	private fun StringBuilder.buildParameterDefinition(element: IParadoxScriptParameter, name: String) {
		definition {
			//加上文件信息
			appendFileInfoHeader(element.fileInfo, element.project)
			//加上定义信息
			append(PlsDocBundle.message("name.script.parameter")).append(" <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
		}
	}
	
	private fun StringBuilder.buildPropertyDefinition(element: ParadoxScriptProperty, name: String) {
		definition {
			//加上文件信息
			appendFileInfoHeader(element.fileInfo, element.project)
			//加上定义信息
			append(PlsDocBundle.message("name.script.property")).append(" <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
			element.value?.let { value -> append(" = ").append(value.escapeXml()) }
		}
	}
	
	private fun StringBuilder.buildDefinitionDefinition(
		element: ParadoxScriptProperty,
		definitionInfo: ParadoxDefinitionInfo,
		localisationTargetMap: MutableMap<String, ParadoxLocalisationProperty>? = null,
		pictureTargetMap: MutableMap<String, Tuple2<PsiFile, Int>>? = null
	) {
		definition {
			//加上文件信息
			appendFileInfoHeader(element.fileInfo, element.project)
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
			val localisation = definitionInfo.localisation
			if(localisation.isNotEmpty()) {
				val project = element.project
				val localisationKeys = mutableSetOf<String>()
				val usedLocalisationTargetMap = localisationTargetMap ?: mutableMapOf()
				for((key, locationExpression, required) in localisation) {
					if(!usedLocalisationTargetMap.containsKey(key)) {
						val (targetKey, target) = locationExpression.resolve(definitionInfo.name, element, inferParadoxLocale(), project) ?: continue //发生意外，直接跳过
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
			val pictures = definitionInfo.pictures
			if(pictures.isNotEmpty()) {
				val project = element.project
				val pictureKeys = mutableSetOf<String>()
				val usedPictureTargetMap = pictureTargetMap ?: mutableMapOf()
				for((key, locationExpression, required) in pictures) {
					if(!usedPictureTargetMap.containsKey(key)) {
						val (filePath, target, frame) = locationExpression.resolve(definitionInfo.name, element, project) ?: continue //发生意外，直接跳过
						if(target != null) usedPictureTargetMap.put(key, tupleOf(target, frame))
						if(required || target != null) {
							if(pictureKeys.add(key)) {
								appendBr()
								append(PlsDocBundle.message("name.script.relatedPicture")).append(" ").append(key).append(" = ").appendFilePathLink(filePath, element, resolved = target != null)
							}
						}
					}
				}
			}
		}
	}
	
	private fun buildRelatedPictureSections(map: MutableMap<String, Tuple2<PsiFile, Int>>, sections: MutableMap<String, String>) {
		//加上DDS图片预览图
		if(getSettings().scriptRenderRelatedPictures) {
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
	
	private fun StringBuilder.buildLineCommentContent(element: PsiElement) {
		//加上单行注释文本
		if(getSettings().scriptRenderLineComment) {
			val docText = getDocTextFromPreviousComment(element)
			if(docText != null && docText.isNotEmpty()) {
				content {
					append(docText)
				}
			}
		}
	}
}
