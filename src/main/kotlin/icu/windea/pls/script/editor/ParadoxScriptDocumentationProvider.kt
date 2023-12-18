@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.script.editor

import com.intellij.lang.documentation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.lang.inherit.*
import icu.windea.pls.lang.modifier.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.image.*
import icu.windea.pls.util.localisation.*

@Suppress("UnusedReceiverParameter")
class ParadoxScriptDocumentationProvider : AbstractDocumentationProvider() {
    override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?): String? {
        return when(element) {
            is ParadoxScriptScriptedVariable -> getScriptedVariableInfo(element)
            is ParadoxScriptProperty -> getPropertyInfo(element)
            is ParadoxScriptPropertyKey -> getQuickNavigateInfo(element.parent, originalElement)
            is ParadoxDefinitionNavigationElement -> getQuickNavigateInfo(element.parent, originalElement)
            else -> null
        }
    }
    
    private fun getScriptedVariableInfo(element: ParadoxScriptScriptedVariable): String? {
        val name = element.name ?: return null
        return buildString {
            buildScriptedVariableDefinition(element, name)
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
            buildDefinitionDefinition(element, definitionInfo, null)
        }
    }
    
    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        return when(element) {
            is ParadoxScriptScriptedVariable -> getScriptedVariableDoc(element)
            is ParadoxScriptProperty -> getPropertyDoc(element)
            is ParadoxScriptPropertyKey -> generateDoc(element.parent, originalElement)
            is ParadoxDefinitionNavigationElement -> generateDoc(element.parent, originalElement)
            else -> null
        }
    }
    
    private fun getScriptedVariableDoc(element: ParadoxScriptScriptedVariable): String? {
        val name = element.name ?: return null
        return buildString {
            buildScriptedVariableDefinition(element, name)
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
            //在definition部分，相关图片信息显示在相关本地化信息之后，在sections部分则显示在之前
            val sectionsList = List(4) { mutableMapOf<String, String>() }
            buildDefinitionDefinition(element, definitionInfo, sectionsList)
            buildDocumentationContent(element, definitionInfo)
            buildLineCommentContent(element)
            buildSections(sectionsList)
        }
    }
    
    private fun StringBuilder.buildScriptedVariableDefinition(element: ParadoxScriptScriptedVariable, name: String) {
        definition {
            //加上文件信息
            appendFileInfoHeader(element)
            //加上定义信息
            append(PlsBundle.message("prefix.scriptedVariable")).append(" <b>@").append(name.escapeXml().orAnonymous()).append("</b>")
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
            appendFileInfoHeader(element)
            //加上定义信息
            append(PlsBundle.message("prefix.property")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
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
            appendFileInfoHeader(element)
            
            //加上定义信息
            addDefinitionInfo(definitionInfo)
            
            //加上继承的定义信息
            val superDefinition = ParadoxDefinitionInheritSupport.getSuperDefinition(element, definitionInfo)
            val superDefinitionInfo = superDefinition?.definitionInfo
            if(superDefinitionInfo != null) {
                appendBr()
                addSuperDefinitionInfo(superDefinition, superDefinitionInfo)
            }
            
            //加上相关本地化信息：去重后的一组本地化的键名，不包括可选且没有对应的本地化的项，按解析顺序排序
            addRelatedLocalisationsForDefinition(element, definitionInfo, sectionsList?.get(2))
            
            //加上相关图片信息：去重后的一组DDS文件的filePath，或者sprite的definitionKey，不包括可选且没有对应的图片的项，按解析顺序排序
            addRelatedImagesForDefinition(element, definitionInfo, sectionsList?.get(1))
            
            //加上生成的修正的信息
            addGeneratedModifiersForDefinition(element, definitionInfo)
            
            //加上修正分类和作用域信息（如果支持）
            addModifierScopeForDefinition(element, definitionInfo, sectionsList?.get(0))
            
            //加上作用域上下文信息（如果支持）
            addScopeContextForDefinition(element, definitionInfo, sectionsList?.get(0))
            
            //加上参数信息（如果支持且存在）
            addParametersForDefinition(element, definitionInfo, sectionsList?.get(3))
            
            //加上事件类型信息（对于on_action）
            addEventTypeForOnAction(element, definitionInfo)
        }
    }
    
    private fun StringBuilder.addDefinitionInfo(definitionInfo: ParadoxDefinitionInfo) {
        val gameType = definitionInfo.gameType
        append(PlsBundle.message("prefix.definition"))
        append(" <b>")
        val name = definitionInfo.name
        append(name.orAnonymous().escapeXml())
        append("</b>: ")
        val typeConfig = definitionInfo.typeConfig
        val typeLink = "${gameType.linkToken}types/${typeConfig.name}"
        appendCwtLink(typeLink, typeConfig.name)
        val subtypeConfigs = definitionInfo.subtypeConfigs
        if(subtypeConfigs.isNotEmpty()) {
            for(subtypeConfig in subtypeConfigs) {
                append(", ")
                val subtypeLink = "$typeLink/${subtypeConfig.name}"
                appendCwtLink(subtypeLink, subtypeConfig.name)
            }
        }
    }
    
    private fun StringBuilder.addSuperDefinitionInfo(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
        val gameType = definitionInfo.gameType
        appendIndent().append(PlsBundle.message("inherits"))
        append(" ")
        val name = definitionInfo.name
        appendDefinitionLink(gameType, name, definitionInfo.type, definition, name.orAnonymous().escapeXml())
        append(": ")
        val typeConfig = definitionInfo.typeConfig
        val typeLink = "${gameType.linkToken}types/${typeConfig.name}"
        appendCwtLink(typeLink, typeConfig.name)
        val subtypeConfigs = definitionInfo.subtypeConfigs
        if(subtypeConfigs.isNotEmpty()) {
            for(subtypeConfig in subtypeConfigs) {
                append(", ")
                val subtypeLink = "$typeLink/${subtypeConfig.name}"
                appendCwtLink(subtypeLink, subtypeConfig.name)
            }
        }
    }
    
    private fun StringBuilder.addRelatedLocalisationsForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sections: MutableMap<String, String>?) {
        val localisationInfos = definitionInfo.localisations
        if(localisationInfos.isEmpty()) return
        val project = element.project
        val map = mutableMapOf<String, String>()
        val sectionKeys = mutableSetOf<String>()
        for((key, locationExpression, required) in localisationInfos) {
            if(sectionKeys.contains(key)) continue
            val selector = localisationSelector(project, element).contextSensitive().preferLocale(ParadoxLocaleHandler.getPreferredLocale())
            val resolved = locationExpression.resolve(element, definitionInfo, selector) ?: continue //发生意外，直接跳过
            if(resolved.message != null) {
                map.put(key, resolved.message)
            } else if(resolved.element != null) {
                map.put(key, buildString { appendLocalisationLink(definitionInfo.gameType, resolved.name, element) })
            } else if(required) {
                map.putIfAbsent(key, resolved.name)
            }
            if(resolved.element != null) {
                sectionKeys.add(key)
                if(sections != null && getSettings().documentation.renderRelatedLocalisationsForDefinitions) {
                    //加上渲染后的相关本地化文本
                    val richText = ParadoxLocalisationTextHtmlRenderer.render(resolved.element, forDoc = true)
                    sections.put("<code>$key</code>", richText)
                }
            }
        }
        for((key, value) in map) {
            appendBr()
            append(PlsBundle.message("prefix.relatedLocalisation")).append(" ")
            append(key).append(" = ").append(value)
        }
    }
    
    private fun StringBuilder.addRelatedImagesForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sections: MutableMap<String, String>?) {
        val render = getSettings().documentation.renderRelatedImagesForDefinitions
        val imagesInfos = definitionInfo.images
        if(imagesInfos.isEmpty()) return
        val map = mutableMapOf<String, String>()
        val sectionKeys = mutableSetOf<String>()
        for((key, locationExpression, required) in imagesInfos) {
            if(sectionKeys.contains(key)) continue
            val resolved = locationExpression.resolve(element, definitionInfo) ?: continue //发生意外，直接跳过
            if(resolved.message != null) {
                map.putIfAbsent(key, resolved.message)
            } else if(resolved.element != null) {
                val nameOrFilePath = resolved.nameOrFilePath
                val gameType = definitionInfo.gameType
                val v = when{
                    nameOrFilePath.startsWith("GFX") -> buildString { appendDefinitionLink(gameType, nameOrFilePath, "sprite", element) }
                    else -> buildString { appendFilePathLink(gameType, nameOrFilePath, nameOrFilePath, element) }
                }
                map.put(key, v)
            } else if(required) {
                map.putIfAbsent(key, resolved.nameOrFilePath)
            }
            if(resolved.element != null) {
                sectionKeys.add(key)
                if(render && sections != null) {
                    //渲染图片
                    val url = when {
                        resolved.element is ParadoxScriptDefinitionElement && resolved.element.definitionInfo != null -> {
                            ParadoxImageResolver.resolveUrlByDefinition(resolved.element, resolved.frameInfo)
                                ?: ParadoxImageResolver.getDefaultUrl()
                        }
                        resolved.element is PsiFile -> {
                            ParadoxImageResolver.resolveUrlByFile(resolved.element.virtualFile, resolved.frameInfo)
                                ?: ParadoxImageResolver.getDefaultUrl()
                        }
                        else -> continue
                    }
                    sections.put("<code>$key</code>", buildString { appendImgTag(url) })
                }
            }
        }
        for((key, value) in map) {
            appendBr()
            append(PlsBundle.message("prefix.relatedImage")).append(" ")
            append(key).append(" = ").append(value)
        }
    }
    
    private fun StringBuilder.addGeneratedModifiersForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
        ParadoxModifierSupport.buildDDocumentationDefinitionForDefinition(element, definitionInfo, this)
    }
    
    private fun StringBuilder.addModifierScopeForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sections: MutableMap<String, String>?) {
        //即使是在CWT文件中，如果可以推断得到CWT规则组，也显示作用域信息
        if(!getSettings().documentation.showScopes) return
        
        if(sections == null) return
        val gameType = definitionInfo.gameType
        val modifierCategories = ParadoxDefinitionModifierProvider.getModifierCategories(element, definitionInfo) ?: return
        val categoryNames = modifierCategories.keys
        if(categoryNames.isNotEmpty()) {
            sections.put(PlsBundle.message("sectionTitle.categories"), ParadoxDocumentationBuilder.getModifierCategoriesText(categoryNames, gameType, element))
        }
        
        val supportedScopes = modifierCategories.getSupportedScopes()
        sections.put(PlsBundle.message("sectionTitle.supportedScopes"), ParadoxDocumentationBuilder.getScopesText(supportedScopes, gameType, element))
    }
    
    private fun StringBuilder.addScopeContextForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sections: MutableMap<String, String>?) {
        //进行代码提示时也显示作用域上下文信息
        //@Suppress("DEPRECATION")
        //if(DocumentationManager.IS_FROM_LOOKUP.get(element) == true) return
        
        if(!getSettings().documentation.showScopeContext) return
        
        if(sections == null) return
        val gameType = definitionInfo.gameType
        if(!ParadoxScopeHandler.isScopeContextSupported(element, indirect = true)) return
        val scopeContext = ParadoxScopeHandler.getScopeContext(element)
        if(scopeContext == null) return
        sections.put(PlsBundle.message("sectionTitle.scopeContext"), ParadoxDocumentationBuilder.getScopeContextText(scopeContext, gameType, element))
    }
    
    private fun StringBuilder.addParametersForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sections: MutableMap<String, String>?) {
        if(!getSettings().documentation.showParameters) return
        
        if(sections == null) return
        val parameterContextInfo = ParadoxParameterSupport.getContextInfo(element) ?: return
        if(parameterContextInfo.parameters.isEmpty()) return //ignore
        val parametersText = buildString {
            var isFirst = true
            parameterContextInfo.parameters.keys.forEach { parameterName ->
                if(isFirst) isFirst = false else append("<br>")
                append("<code>")
                append(parameterName)
                //加上推断得到的规则信息
                if(ParadoxParameterHandler.isOptional(parameterContextInfo, parameterName)) append("?") //optional marker
                val inferredConfig = ParadoxParameterHandler.getInferredConfig(parameterName, parameterContextInfo)
                if(inferredConfig != null) {
                    append(": ")
                    append(inferredConfig.expression.expressionString.escapeXml())
                }
                append("</code>")
            }
        }
        sections.put(PlsBundle.message("sectionTitle.parameters"), parametersText)
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
        val typeLink = "${gameType.linkToken}types/event/$eventType"
        append(PlsBundle.message("prefix.eventType")).append(" ").appendCwtLink(typeLink, eventType)
    }
    
    private fun StringBuilder.buildDocumentationContent(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
        ParadoxDefinitionExtendedDocumentationProvider.buildDocumentation(element, definitionInfo) { documentation ->
            content { append(documentation) }
        }
    }
    
    private fun StringBuilder.buildLineCommentContent(element: PsiElement) {
        //加上单行注释文本
        if(!getSettings().documentation.renderLineComment) return
        val docText = getLineCommentDocText(element)
        if(docText.isNotNullOrEmpty()) {
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
}
