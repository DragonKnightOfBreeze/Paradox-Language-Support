@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.script.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.support.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import icu.windea.pls.tool.localisation.*

@Suppress("UnusedReceiverParameter")
class ParadoxScriptDocumentationProvider : AbstractDocumentationProvider() {
    //do not provide special documentations for definition name and complex enum value name declarations,
    //for the expression represents a definition name or complex enum value name, can also be a localisation reference, etc.
    
    override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?): String? {
        return when(element) {
            is ParadoxScriptScriptedVariable -> getScriptedVariableInfo(element)
            is ParadoxScriptProperty -> getPropertyInfo(element)
            //进行代码提示时，这里是有效的代码
            is ParadoxScriptStringExpressionElement -> {
                //only for complex enum value reference
                val referenceElement = getReferenceElement(originalElement)
                if(referenceElement != null && referenceElement !== element) {
                    val complexEnumValueInfo = element.complexEnumValueInfo
                    if(complexEnumValueInfo != null) return generateComplexEnumValueInfo(element, complexEnumValueInfo)
                }
                if(element is ParadoxScriptPropertyKey) {
                    return getQuickNavigateInfo(element.parent, null)
                }
                null
            }
            else -> null
        }
    }
    
    private fun getScriptedVariableInfo(element: ParadoxScriptScriptedVariable): String {
        val name = element.name
        return buildString {
            buildScriptedVariableDefinition(element, name)
        }
    }
    
    private fun getPropertyInfo(element: ParadoxScriptProperty): String? {
        val definitionInfo = element.definitionInfo
        if(definitionInfo != null) return getDefinitionInfo(element, definitionInfo)
        val definitionMemberInfo = element.definitionMemberInfo
        if(definitionMemberInfo != null) return null //不为无法解析的属性元素提供文档
        val name = element.name
        return buildString {
            buildPropertyDefinition(element, name)
        }
    }
    
    private fun getDefinitionInfo(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String {
        return buildString {
            buildDefinitionDefinition(element, definitionInfo, null)
        }
    }
    
    private fun generateComplexEnumValueInfo(element: ParadoxScriptStringExpressionElement, complexEnumValueInfo: icu.windea.pls.lang.model.ParadoxComplexEnumValueInfo): String {
        return buildString {
            buildComplexEnumValueDefinition(element, complexEnumValueInfo)
        }
    }
    
    //private fun getComplexEnumValueInfo(element: ParadoxScriptStringExpressionElement, config: CwtDataConfig<*>): String {
    //	return buildString {
    //		val configGroup = config.info.configGroup
    //		buildComplexEnumValueDefinition(element.value, config.expression.value.orEmpty(), configGroup)
    //	}
    //}
    
    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        return when(element) {
            is ParadoxScriptScriptedVariable -> getScriptedVariableDoc(element)
            is ParadoxScriptProperty -> getPropertyDoc(element)
            is ParadoxScriptStringExpressionElement -> {
                //only for complex enum value reference
                val referenceElement = getReferenceElement(originalElement)
                if(referenceElement != null && referenceElement !== element) {
                    val complexEnumValueInfo = element.complexEnumValueInfo
                    if(complexEnumValueInfo != null) return generateComplexEnumValueDoc(element, complexEnumValueInfo)
                }
                if(element is ParadoxScriptPropertyKey) {
                    return generateDoc(element.parent, null)
                }
                null
            }
            else -> null
        }
    }
    
    private fun getScriptedVariableDoc(element: ParadoxScriptScriptedVariable): String {
        val name = element.name
        return buildString {
            buildScriptedVariableDefinition(element, name)
            buildLineCommentContent(element)
        }
    }
    
    private fun getPropertyDoc(element: ParadoxScriptProperty): String? {
        val definitionInfo = element.definitionInfo
        if(definitionInfo != null) return getDefinitionDoc(element, definitionInfo)
        val definitionMemberInfo = element.definitionMemberInfo
        if(definitionMemberInfo != null) return null //不为无法解析的属性元素提供文档
        val name = element.name
        return buildString {
            buildPropertyDefinition(element, name)
            buildLineCommentContent(element)
        }
    }
    
    private fun getDefinitionDoc(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo): String {
        return buildString {
            //在definition部分，相关图片信息显示在相关本地化信息之后，在sections部分则显示在之前
            //images, localisations
            val sectionsList = List(4) { mutableMapOf<String, String>() }
            buildDefinitionDefinition(element, definitionInfo, sectionsList)
            buildDocumentationContent(element, definitionInfo)
            buildLineCommentContent(element)
            buildSections(sectionsList)
        }
    }
    
    private fun generateComplexEnumValueDoc(element: ParadoxScriptStringExpressionElement, complexEnumValueInfo: icu.windea.pls.lang.model.ParadoxComplexEnumValueInfo): String {
        return buildString {
            buildComplexEnumValueDefinition(element, complexEnumValueInfo)
        }
    }
    
    private fun getComplexEnumValueDoc(element: ParadoxScriptStringExpressionElement, config: CwtDataConfig<*>): String {
        return buildString {
            val configGroup = config.info.configGroup
            buildComplexEnumValueDefinition(element.value, config.expression.value.orEmpty(), configGroup)
        }
    }
    
    private fun StringBuilder.buildScriptedVariableDefinition(element: ParadoxScriptScriptedVariable, name: String) {
        definition {
            //加上文件信息
            appendFileInfoHeader(element.fileInfo)
            //加上定义信息
            append(PlsDocBundle.message("prefix.scriptedVariable")).append(" <b>@").append(name.escapeXml().orAnonymous()).append("</b>")
            val valueElement = element.scriptedVariableValue
            when(valueElement) {
                is ParadoxScriptString -> append(" = ").append(valueElement.text.escapeXml())
                is ParadoxScriptValue -> append(" = ").append(valueElement.value.escapeXml())
            }
        }
    }
    
    private fun StringBuilder.buildPropertyDefinition(element: ParadoxScriptProperty, name: String) {
        definition {
            //加上文件信息
            appendFileInfoHeader(element.fileInfo)
            //加上定义信息
            append(PlsDocBundle.message("prefix.property")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
            val valueElement = element.propertyValue
            when(valueElement) {
                is ParadoxScriptString -> append(" = ").append(valueElement.text.escapeXml())
                is ParadoxScriptValue -> append(" = ").append(valueElement.value.escapeXml())
            }
        }
    }
    
    private fun StringBuilder.buildDefinitionDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sectionsList: List<MutableMap<String, String>>?) {
        definition {
            //加上文件信息
            appendFileInfoHeader(element.fileInfo)
            //加上定义信息
            val name = definitionInfo.name
            val typeLinkText = buildString {
                val gameType = definitionInfo.gameType
                val typeConfig = definitionInfo.typeConfig
                val typeLink = "${gameType.id}/types/${typeConfig.name}"
                appendCwtLink(typeConfig.name, typeLink)
                val subtypeConfigs = definitionInfo.subtypeConfigs
                if(subtypeConfigs.isNotEmpty()) {
                    for(subtypeConfig in subtypeConfigs) {
                        append(", ")
                        val subtypeLink = "$typeLink/${subtypeConfig.name}"
                        appendCwtLink(subtypeConfig.name, subtypeLink)
                    }
                }
            }
            append(PlsDocBundle.message("prefix.definition")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>: ").append(typeLinkText)
            
            //加上相关本地化信息：去重后的一组本地化的键名，不包括可选且没有对应的本地化的项，按解析顺序排序
            addRelatedLocalisationsForDefinition(element, definitionInfo, sectionsList?.get(2))
            
            //加上相关图片信息：去重后的一组DDS文件的filePath，或者sprite的definitionKey，不包括可选且没有对应的图片的项，按解析顺序排序
            addRelatedImagesForDefinition(element, definitionInfo, sectionsList?.get(1))
            
            //加上生成的修正的信息
            addGeneratedModifiersForDefinition(element, definitionInfo)
            
            //加上作用域上下文信息（如果支持）
            addScopeContextForDefinition(element, definitionInfo, sectionsList?.get(0))
            
            //加上参数信息（如果支持且存在）
            addParametersForDefinition(element, definitionInfo, sectionsList?.get(3))
            
            //加上事件类型信息（对于on_action）
            addEventTypeForOnAction(element, definitionInfo)
        }
    }
    
    private fun StringBuilder.addRelatedLocalisationsForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sections: MutableMap<String, String>?) {
        val localisationInfos = definitionInfo.localisation
        if(localisationInfos.isEmpty()) return
        val render = getSettings().documentation.renderRelatedLocalisationsForDefinitions
        val project = element.project
        val map = mutableMapOf<String, String>()
        val sectionKeys = mutableSetOf<String>()
        for((key, locationExpression, required) in localisationInfos) {
            if(sectionKeys.contains(key)) continue
            val selector = localisationSelector(project, element).contextSensitive().preferLocale(preferredParadoxLocale())
            val resolved = locationExpression.resolve(element, definitionInfo, project, selector) ?: continue //发生意外，直接跳过
            if(resolved.message != null) {
                map.put(key, resolved.message)
            } else if(resolved.localisation != null) {
                map.put(key, buildString { appendLocalisationLink(definitionInfo.gameType, resolved.key, element, resolved = true) })
            } else if(required) {
                map.putIfAbsent(key, resolved.key)
            }
            if(resolved.localisation != null) {
                sectionKeys.add(key)
                if(render && sections != null) {
                    //加上渲染后的相关本地化文本
                    val richText = ParadoxLocalisationTextRenderer.render(resolved.localisation)
                    sections.put(key.toCapitalizedWords(), richText)
                }
            }
        }
        for((key, value) in map) {
            appendBr()
            append(PlsDocBundle.message("prefix.relatedLocalisation")).append(" ")
            append(key).append(" = ").append(value)
        }
    }
    
    private fun StringBuilder.addRelatedImagesForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sections: MutableMap<String, String>?) {
        val imagesInfos = definitionInfo.images
        if(imagesInfos.isEmpty()) return
        val render = getSettings().documentation.renderRelatedImagesForDefinitions
        val project = element.project
        val map = mutableMapOf<String, String>()
        val sectionKeys = mutableSetOf<String>()
        for((key, locationExpression, required) in imagesInfos) {
            if(sectionKeys.contains(key)) continue
            val resolved = locationExpression.resolve(element, definitionInfo, project) ?: continue //发生意外，直接跳过
            if(resolved.message != null) {
                map.putIfAbsent(key, resolved.message)
            } else if(resolved.file != null) {
                map.put(key, buildString { appendFilePathLink(resolved.filePath, definitionInfo.gameType, resolved.filePath, element, resolved = true) })
            } else if(required) {
                map.putIfAbsent(key, resolved.filePath)
            }
            if(resolved.file != null) {
                sectionKeys.add(key)
                if(render && sections != null) {
                    //加上DDS图片预览图
                    val url = ParadoxDdsUrlResolver.resolveByFile(resolved.file.virtualFile, resolved.frame)
                    val tag = buildString { appendImgTag(url) }
                    sections.put(key.toCapitalizedWords(), tag)
                }
            }
        }
        for((key, value) in map) {
            appendBr()
            append(PlsDocBundle.message("prefix.relatedImage")).append(" ")
            append(key).append(" = ").append(value)
        }
    }
    
    private fun StringBuilder.addGeneratedModifiersForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
        ParadoxModifierSupport.buildDDocumentationDefinitionForDefinition(element, definitionInfo, this)
    }
    
    private fun StringBuilder.addScopeContextForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sections: MutableMap<String, String>?) {
        //进行代码提示时也显示作用域上下文信息
        //@Suppress("DEPRECATION")
        //if(DocumentationManager.IS_FROM_LOOKUP.get(element) == true) return
        
        val show = getSettings().documentation.showScopeContext
        if(!show) return
        if(sections == null) return
        if(!ParadoxScopeHandler.isScopeContextSupported(element)) return
        val scopeContext = ParadoxScopeHandler.getScopeContext(element)
        if(scopeContext == null) return
        val contextElement = element
        val gameType = definitionInfo.gameType
        val scopeContextText = buildString {
            append("<code>")
            ParadoxScopeHandler.buildScopeContextDoc(scopeContext, gameType, contextElement, this)
            append("</code>")
        }
        sections.put(PlsDocBundle.message("sectionTitle.scopeContext"), scopeContextText)
    }
    
    private fun StringBuilder.addParametersForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sections: MutableMap<String, String>?) {
        val show = getSettings().documentation.showParameters
        if(!show) return
        if(sections == null) return
        val parameterMap = element.parameters
        if(parameterMap.isEmpty()) return //ignore
        var isFirst = true
        val parametersText = buildString {
            parameterMap.entries.forEach { (name, info) ->
                if(isFirst) isFirst = false else append("<br>")
                append("<code>")
                append(name)
                if(info.optional) append("?") //optional marker
                append("</code>")
            }
        }
        sections.put(PlsDocBundle.message("sectionTitle.parameters"), parametersText)
    }
    
    private fun StringBuilder.addEventTypeForOnAction(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
        if(definitionInfo.type != "on_action") return
        //有些游戏类型直接通过CWT文件指定了事件类型，而非CSV文件，忽略这种情况
        val configGroup = definitionInfo.configGroup
        val gameType = configGroup.gameType
        val config = definitionInfo.configGroup.onActions.getByTemplate(definitionInfo.name, element, definitionInfo.configGroup)
        if(config == null) return
        val eventType = config.eventType
        appendBr()
        val typeLink = "${gameType.id}/types/event/$eventType"
        append(PlsDocBundle.message("prefix.eventType")).append(" ").appendCwtLink(eventType, typeLink)
    }
    
    private fun StringBuilder.buildDocumentationContent(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
        //尝试从类型规则或者子类型规则得到文档文本
        definitionInfo.typeConfig.let {
            val documentation = it.config.documentation?.takeIfNotEmpty()
            if(documentation != null) {
                content { append(documentation) }
            }
        }
        definitionInfo.subtypeConfigs.forEach {
            val documentation = it.config.documentation?.takeIfNotEmpty()
            if(documentation != null) {
                content { append(documentation) }
            }
        }
        
        //如果是on_action，加上从on_actions.csv中得到的文档文本（comment）
        if(definitionInfo.type == "on_action") {
            val config = definitionInfo.configGroup.onActions.getByTemplate(definitionInfo.name, element, definitionInfo.configGroup)
            val comment = config?.config?.documentation?.takeIfNotEmpty()
            if(comment != null) {
                content { append(comment) }
            }
        }
        
        //加上扩展的文档文本
        val documentation = PlsDocumentationBundle.message(definitionInfo.name, definitionInfo.type, definitionInfo.gameType)
            ?.takeIfNotEmpty()
        if(documentation != null) {
            content { append(documentation) }
        }
    }
    
    private fun StringBuilder.buildComplexEnumValueDefinition(element: PsiElement, complexEnumValueInfo: icu.windea.pls.lang.model.ParadoxComplexEnumValueInfo) {
        definition {
            //加上文件信息
            appendFileInfoHeader(element.fileInfo)
            //加上复杂枚举值信息
            val name = complexEnumValueInfo.name
            val enumName = complexEnumValueInfo.enumName
            append(PlsDocBundle.message("prefix.complexEnumValue")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
            if(enumName.isNotEmpty()) {
                val gameType = complexEnumValueInfo.gameType.orDefault()
                val configGroup = getCwtConfig(element.project).getValue(gameType)
                val complexEnumConfig = configGroup.complexEnums[enumName]
                if(complexEnumConfig != null) {
                    val typeLink = "${gameType.id}/complex_enums/${enumName}"
                    append(": ").appendCwtLink(enumName, typeLink)
                } else {
                    append(": ").append(enumName)
                }
            }
        }
    }
    
    private fun StringBuilder.buildComplexEnumValueDefinition(name: String, enumName: String, configGroup: CwtConfigGroup) {
        definition {
            //不加上文件信息
            //加上复杂枚举值信息
            append(PlsDocBundle.message("prefix.complexEnumValue")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
            if(enumName.isNotEmpty()) {
                val gameType = configGroup.gameType
                val complexEnumConfig = configGroup.complexEnums[enumName]
                if(complexEnumConfig != null) {
                    val typeLink = "${gameType.id}/complex_enums/${enumName}"
                    append(": ").appendCwtLink(enumName, typeLink)
                } else {
                    append(": ").append(enumName)
                }
            }
        }
    }
    
    private fun StringBuilder.buildLineCommentContent(element: PsiElement) {
        //加上单行注释文本
        if(!getSettings().documentation.renderLineComment) return
        val docText = getLineCommentDocText(element)
        if(docText != null && docText.isNotEmpty()) {
            content {
                append(docText)
            }
        }
    }
    
    private fun StringBuilder.buildSections(sectionsList: List<Map<String, String>>) {
        sections {
            for(sections in sectionsList) {
                for((key, value) in sections) {
                    section(key, value)
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
