package icu.windea.pls.script.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
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
			is ParadoxScriptVariable -> getVariableInfo(element)
			is ParadoxScriptProperty -> getPropertyInfo(element)
			else -> null
		}
	}
	
	private fun getVariableInfo(element: ParadoxScriptVariable): String {
		val name = element.name
		return buildString {
			buildVariableDefinition(element, name)
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
			is ParadoxScriptVariable -> getVariableDoc(element)
			is ParadoxScriptProperty -> getPropertyDoc(element)
			else -> null
		}
	}
	
	private fun getVariableDoc(element: ParadoxScriptVariable): String {
		val name = element.name
		return buildString {
			buildVariableDefinition(element, name)
			buildLineCommentContent(element)
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
			val localisationTargetMap = mutableMapOf<String, ParadoxLocalisationProperty>()
			val pictureTargetMap = mutableMapOf<String, PsiElement>()
			buildDefinitionDefinition(element, definitionInfo, localisationTargetMap, pictureTargetMap)
			buildLineCommentContent(element)
			buildRelatedPictureSections(pictureTargetMap)
			buildRelatedLocalisationSections(localisationTargetMap)
		}
	}
	
	private fun StringBuilder.buildVariableDefinition(element: ParadoxScriptVariable, name: String) {
		definition {
			//加上文件信息
			element.fileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
			//加上定义信息
			append("(variable) <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
			element.unquotedValue?.let { unquotedValue -> append(" = ").append(unquotedValue.escapeXml()) }
		}
	}
	
	private fun StringBuilder.buildPropertyDefinition(element: ParadoxScriptProperty, name: String) {
		definition {
			//加上文件信息
			element.fileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
			//加上定义信息
			append("(script property) <b>").append(name.escapeXmlOrAnonymous()).append("</b>")
			element.truncatedValue?.let { truncatedValue -> append(" = ").append(truncatedValue.escapeXml()) }
		}
	}
	
	private fun StringBuilder.buildDefinitionDefinition(
		element: ParadoxScriptProperty,
		definitionInfo: ParadoxDefinitionInfo,
		localisationTargetMap: MutableMap<String, ParadoxLocalisationProperty>? = null,
		pictureTargetMap: MutableMap<String, PsiElement>? = null
	) {
		definition {
			//加上文件信息
			element.fileInfo?.let { fileInfo -> appendFileInfo(fileInfo).appendBr() }
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
			append("(definition) <b>").append(name.escapeXmlOrAnonymous()).append("</b>: ").append(typeLinkText)
			//加上相关本地化信息：去重后的一组本地化的键名，不包括可选且没有对应的本地化的项，按解析顺序排序
			val localisation = definitionInfo.localisation
			if(localisation.isNotEmpty()) {
				val project = element.project
				val localisationKeys = mutableSetOf<String>()
				val usedLocalisationTargetMap = localisationTargetMap ?: mutableMapOf()
				for((key, location) in localisation) {
					if(!usedLocalisationTargetMap.containsKey(key)) {
						val target = findLocalisationByLocation(location, inferParadoxLocale(), project)
						if(target != null) usedLocalisationTargetMap.put(key, target)
					}
				}
				for((key, location,required) in localisation) {
					if(required || usedLocalisationTargetMap.containsKey(key)) {
						if(localisationKeys.add(key)) {
							appendBr()
							append("(related localisation) ").append(key).append(" = ").appendLocalisationLink(location, element)
						}
					}
				}
			}
			//加上相关图片信息：去重后的一组DDS文件的filePath，或者sprite的definitionKey，不包括可选且没有对应的图片的项，按解析顺序排序
			val pictures = definitionInfo.pictures
			if(pictures.isNotEmpty()) {
				val project = element.project
				val picturesKeys = mutableSetOf<String>()
				val usedPicturesTargetMap = pictureTargetMap ?: mutableMapOf()
				for((key, location) in pictures) {
					if(!usedPicturesTargetMap.containsKey(key)) {
						val target = findPictureByLocation(location, project)
						if(target != null) usedPicturesTargetMap.put(key, target)
					}
				}
				for((key, location,required) in pictures) {
					if(required || usedPicturesTargetMap.containsKey(key)) {
						if(picturesKeys.add(key)) {
							appendBr()
							append("(related pictures) ").append(key).append(" = ")
							//根据是否以dds后缀名结尾，判断location是filepath还是definitionKey
							if(location.endsWith(".dds", true)){
								appendFilePathLink(location, element)
							} else {
								appendDefinitionLink(location, "sprite|spriteType", element)
							}
						}
					}
				}
			}
		}
	}
	
	private fun StringBuilder.buildRelatedPictureSections(linkMap: MutableMap<String, PsiElement>) {
		//加上DDS图片预览图
		if(getSettings().scriptRenderRelatedPictures) {
			if(linkMap.isNotEmpty()) {
				sections {
					for((key, target) in linkMap) {
						val url = when(target) {
							is ParadoxScriptProperty -> ParadoxDdsUrlResolver.resolveBySprite(target)
							is PsiFile -> ParadoxDdsUrlResolver.resolveByFile(target.virtualFile)
							else -> continue
						}
						val tag = buildString { appendImgTag(url) }
						section(key, tag)
					}
				}
			}
		}
	}
	
	private fun StringBuilder.buildRelatedLocalisationSections(targetMap: Map<String, ParadoxLocalisationProperty>) {
		//加上渲染后的相关本地化文本
		if(getSettings().scriptRenderRelatedLocalisation) {
			if(targetMap.isNotEmpty()) {
				sections {
					for((key, target) in targetMap) {
						val richText = target.renderText()
						section(key, richText)
					}
				}
			}
		}
	}
	
	private fun StringBuilder.buildLineCommentContent(element: PsiElement) {
		//加上单行注释文本
		if(getSettings().scriptRenderLineComment) {
			val docText = getDocTextFromPreviousComment(element)
			if(docText!= null && docText.isNotEmpty()) {
				content {
					append(docText)
				}
			}
		}
	}
}
