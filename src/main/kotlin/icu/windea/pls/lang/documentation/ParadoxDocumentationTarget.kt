@file:Suppress("UnstableApiUsage", "UNUSED_PARAMETER", "UnusedReceiverParameter")

package icu.windea.pls.lang.documentation

import com.intellij.model.*
import com.intellij.platform.backend.documentation.*
import com.intellij.platform.backend.presentation.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.ep.documentation.*
import icu.windea.pls.ep.inherit.*
import icu.windea.pls.ep.modifier.*
import icu.windea.pls.ep.parameter.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.image.*
import icu.windea.pls.lang.util.renderer.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*

//org.jetbrains.kotlin.idea.k2.codeinsight.quickDoc.KotlinDocumentationTarget

class ParadoxDocumentationTarget(val element: PsiElement, val originalElement: PsiElement?) : DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPtr = element.createSmartPointer()
        val originalElementPtr = originalElement?.createSmartPointer()
        return Pointer {
            val element = elementPtr.dereference() ?: return@Pointer null
            ParadoxDocumentationTarget(element, originalElementPtr?.dereference())
        }
    }
    
    override fun computePresentation(): TargetPresentation {
        return defaultTargetPresentation(element)
    }
    
    override fun computeDocumentationHint(): String? {
        return computeLocalDocumentation(element, originalElement, true)
    }
    
    override fun computeDocumentation(): DocumentationResult {
        return DocumentationResult.asyncDocumentation {
            val html = computeLocalDocumentation(element, originalElement, false) ?: return@asyncDocumentation null
            DocumentationResult.documentation(html)
        }
    }
}

private fun computeLocalDocumentation(element: PsiElement, originalElement: PsiElement?, quickNavigation: Boolean): String? {
    return when(element) {
        is ParadoxParameterElement -> getParameterDoc(element, originalElement, quickNavigation)
        is ParadoxLocalisationParameterElement -> getLocalisationParameterDoc(element, originalElement, quickNavigation)
        is ParadoxComplexEnumValueElement -> getComplexEnumValueDoc(element, originalElement, quickNavigation)
        is ParadoxDynamicValueElement -> getDynamicValueDoc(element, originalElement, quickNavigation)
        is ParadoxModifierElement -> getModifierDoc(element, originalElement, quickNavigation)
        is ParadoxScriptScriptedVariable -> getScriptedVariableDoc(element, originalElement, quickNavigation)
        is ParadoxScriptProperty -> getPropertyDoc(element, originalElement, quickNavigation)
        is ParadoxScriptPropertyKey -> computeLocalDocumentation(element.parent, originalElement, quickNavigation)
        is ParadoxDefinitionNavigationElement -> computeLocalDocumentation(element.parent, originalElement, quickNavigation)
        is ParadoxLocalisationLocale -> getLocalisationLocaleDoc(element, originalElement, quickNavigation)
        is ParadoxLocalisationProperty -> getLocalisationPropertyDoc(element, originalElement, quickNavigation)
        is ParadoxLocalisationIcon -> getLocalisationIconDoc(element, originalElement, quickNavigation)
        is ParadoxLocalisationCommandScope -> getLocalisationCommandScopeDoc(element, originalElement, quickNavigation)
        is ParadoxLocalisationCommandField -> getLocalisationCommandFieldDoc(element, originalElement, quickNavigation)
        is ParadoxLocalisationColorfulText -> getLocalisationColorDoc(element, originalElement, quickNavigation)
        else -> null
    }
}

private fun getParameterDoc(element: ParadoxParameterElement, originalElement: PsiElement?, quickNavigation: Boolean): String {
    return buildDocumentation {
        buildParameterDefinition(element)
        if(quickNavigation) return@buildDocumentation
        buildDocumentationContent(element)
    }
}

private fun getLocalisationParameterDoc(element: ParadoxLocalisationParameterElement, originalElement: PsiElement?, quickNavigation: Boolean): String {
    return buildDocumentation {
        buildLocalisationParameterDefinition(element)
    }
}

private fun getComplexEnumValueDoc(element: ParadoxComplexEnumValueElement, originalElement: PsiElement?, quickNavigation: Boolean): String {
    return buildDocumentation {
        val sectionsList = List(1) { mutableMapOf<String, String>() }
        buildComplexEnumValueDefinition(element, sectionsList)
        if(quickNavigation) return@buildDocumentation
        buildDocumentationContent(element)
        buildSections(sectionsList)
    }
}

private fun getDynamicValueDoc(element: ParadoxDynamicValueElement, originalElement: PsiElement?, quickNavigation: Boolean): String {
    return buildDocumentation {
        val sectionsList = List(1) { mutableMapOf<String, String>() }
        buildDynamicValueDefinition(element, sectionsList)
        if(quickNavigation) return@buildDocumentation
        buildDocumentationContent(element)
        buildSections(sectionsList)
    }
}

private fun getModifierDoc(element: ParadoxModifierElement, originalElement: PsiElement?, quickNavigation: Boolean): String {
    return buildDocumentation {
        val sectionsList = List(3) { mutableMapOf<String, String>() }
        buildModifierDefinition(element, sectionsList)
        if(quickNavigation) return@buildDocumentation
        buildSections(sectionsList)
    }
}

private fun getScriptedVariableDoc(element: ParadoxScriptScriptedVariable, originalElement: PsiElement?, quickNavigation: Boolean): String? {
    val name = element.name ?: return null
    return buildDocumentation {
        buildScriptedVariableDefinition(element, name)
        if(quickNavigation) return@buildDocumentation
        buildDocumentationContent(element)
        buildLineCommentContent(element)
    }
}

private fun getPropertyDoc(element: ParadoxScriptProperty, originalElement: PsiElement?, quickNavigation: Boolean): String {
    val definitionInfo = element.definitionInfo
    if(definitionInfo != null) return getDefinitionDoc(element, definitionInfo, originalElement, quickNavigation)
    val name = element.name
    return buildDocumentation {
        buildPropertyDefinition(element, name)
        if(quickNavigation) return@buildDocumentation
        buildLineCommentContent(element)
    }
}

private fun getDefinitionDoc(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, originalElement: PsiElement?, quickNavigation: Boolean): String {
    return buildDocumentation {
        //在definition部分，相关图片信息显示在相关本地化信息之后，在sections部分则显示在之前
        val sectionsList = List(4) { mutableMapOf<String, String>() }
        buildDefinitionDefinition(element, definitionInfo, sectionsList)
        if(quickNavigation) return@buildDocumentation
        buildDocumentationContent(element, definitionInfo)
        buildLineCommentContent(element)
        buildSections(sectionsList)
    }
}

private fun getLocalisationPropertyDoc(element: ParadoxLocalisationProperty, originalElement: PsiElement?, quickNavigation: Boolean): String {
    val name = element.name
    val category = element.category
    if(category != null) return getLocalisationDoc(element, name, category, originalElement, quickNavigation)
    return buildDocumentation {
        buildLocalisationPropertyDefinition(element)
    }
}

private fun getLocalisationLocaleDoc(element: ParadoxLocalisationLocale, originalElement: PsiElement?, quickNavigation: Boolean): String {
    val name = element.name
    return buildDocumentation {
        buildLocalisationLocaleDefinition(name)
    }
}

private fun getLocalisationDoc(element: ParadoxLocalisationProperty, name: String, category: ParadoxLocalisationCategory, originalElement: PsiElement?, quickNavigation: Boolean): String {
    return buildDocumentation {
        buildLocalisationDefinition(element, category, name)
        if(quickNavigation) return@buildDocumentation
        buildLineCommentContent(element)
        buildLocalisationSections(element)
    }
}

private fun getLocalisationIconDoc(element: ParadoxLocalisationIcon, originalElement: PsiElement?, quickNavigation: Boolean): String? {
    val name = element.name ?: return null
    return buildDocumentation {
        buildLocalisationIconDefinition(name)
    }
}

private fun getLocalisationCommandScopeDoc(element: ParadoxLocalisationCommandScope, originalElement: PsiElement?, quickNavigation: Boolean): String {
    val name = element.name
    return buildDocumentation {
        buildLocalisationCommandScopeDefinition(name)
    }
}

private fun getLocalisationCommandFieldDoc(element: ParadoxLocalisationCommandField, originalElement: PsiElement?, quickNavigation: Boolean): String {
    val name = element.name
    return buildDocumentation {
        buildLocalisationCommandFieldDefinition(name)
    }
}

private fun getLocalisationColorDoc(element: ParadoxLocalisationColorfulText, originalElement: PsiElement?, quickNavigation: Boolean): String? {
    val name = element.name ?: return null
    return buildDocumentation {
        //加上元素定义信息
        buildLocalisationColorDefinition(name)
    }
}

private fun DocumentationBuilder.buildParameterDefinition(element: ParadoxParameterElement) {
    val name = element.name
    definition {
        val r = ParadoxParameterSupport.getDocumentationDefinition(element, this)
        if(!r) {
            //显示默认的快速文档
            append(PlsBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        }
    }
}

private fun DocumentationBuilder.buildLocalisationParameterDefinition(element: ParadoxLocalisationParameterElement) {
    val name = element.name
    definition {
        val r = ParadoxLocalisationParameterSupport.getDocumentationDefinition(element, this)
        if(!r) {
            //显示默认的快速文档
            append(PlsBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        }
    }
}

private fun DocumentationBuilder.buildDynamicValueDefinition(element: ParadoxDynamicValueElement, sectionsList: List<MutableMap<String, String>>?) {
    val name = element.name
    val dynamicValueTypes = element.dynamicValueTypes
    val gameType = element.gameType
    val configGroup = getConfigGroup(element.project, gameType)
    definition {
        append(PlsBundle.message("prefix.dynamicValue")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        append(": ")
        var appendSeparator = false
        for(dynamicValueType in dynamicValueTypes) {
            if(appendSeparator) append(" | ") else appendSeparator = true
            val valueConfig = configGroup.dynamicValueTypes[dynamicValueType]
            if(valueConfig != null) {
                val typeLink = "${gameType.prefix}values/${dynamicValueType}"
                appendCwtLink(typeLink, dynamicValueType)
            } else {
                append(dynamicValueType)
            }
        }
        
        addScopeContext(element, name, configGroup, sectionsList?.get(0))
    }
}

private fun DocumentationBuilder.buildComplexEnumValueDefinition(element: ParadoxComplexEnumValueElement, sectionsList: List<MutableMap<String, String>>?) {
    definition {
        val name = element.name
        val enumName = element.enumName
        val gameType = element.gameType
        val configGroup = getConfigGroup(element.project, gameType)
        append(PlsBundle.message("prefix.complexEnumValue")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        val complexEnumConfig = configGroup.complexEnums[enumName]
        if(complexEnumConfig != null) {
            val typeLink = "${gameType.prefix}complex_enums/${enumName}"
            append(": ").appendCwtLink(typeLink, enumName)
        } else {
            append(": ").append(enumName)
        }
        
        addScopeContext(element, name, configGroup, sectionsList?.get(0))
    }
}

private fun DocumentationBuilder.buildModifierDefinition(element: ParadoxModifierElement, sectionsList: List<MutableMap<String, String>>?) {
    val name = element.name
    definition {
        val r = ParadoxModifierSupport.getDocumentationDefinition(element, this)
        if(!r) {
            //显示默认的快速文档
            append(PlsBundle.message("prefix.modifier")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        }
        
        val configGroup = getConfigGroup(element.project, element.gameType)
        addModifierRelatedLocalisations(element, name, configGroup, sectionsList?.get(2))
        
        addModifierIcon(element, name, configGroup, sectionsList?.get(1))
        addModifierScope(element, name, configGroup, sectionsList?.get(0))
        addScopeContext(element, name, configGroup, sectionsList?.get(0))
    }
}

private fun DocumentationBuilder.addModifierRelatedLocalisations(element: ParadoxModifierElement, name: String, configGroup: CwtConfigGroup, sections: MutableMap<String, String>?) {
    val render = getSettings().documentation.renderNameDescForModifiers
    val gameType = configGroup.gameType ?: return
    val project = configGroup.project
    val usedLocale = ParadoxLocaleHandler.getUsedLocaleInDocumentation()
    val finalUsedLocale = usedLocale ?: ParadoxLocaleHandler.getPreferredLocale()
    val nameLocalisation = run {
        val keys = ParadoxModifierHandler.getModifierNameKeys(name, element)
        keys.firstNotNullOfOrNull { key ->
            val selector = localisationSelector(project, element).contextSensitive()
                .preferLocale(finalUsedLocale)
                .withConstraint(ParadoxLocalisationConstraint.Modifier)
            ParadoxLocalisationSearch.search(key, selector).find()
        }
    }
    val descLocalisation = run {
        val keys = ParadoxModifierHandler.getModifierDescKeys(name, element)
        keys.firstNotNullOfOrNull { key ->
            val selector = localisationSelector(project, element).contextSensitive()
                .preferLocale(finalUsedLocale)
                .withConstraint(ParadoxLocalisationConstraint.Modifier)
            ParadoxLocalisationSearch.search(key, selector).find()
        }
    }
    //如果没找到的话，不要在文档中显示相关信息
    if(nameLocalisation != null) {
        appendBr()
        append(PlsBundle.message("prefix.relatedLocalisation")).append(" ")
        append("name = ").appendLocalisationLink(gameType, nameLocalisation.name, element)
    }
    if(descLocalisation != null) {
        appendBr()
        append(PlsBundle.message("prefix.relatedLocalisation")).append(" ")
        append("desc = ").appendLocalisationLink(gameType, descLocalisation.name, element)
    }
    if(sections != null && render) {
        if(nameLocalisation != null) {
            val richText = ParadoxLocalisationTextHtmlRenderer.render(nameLocalisation, locale = usedLocale?.id, forDoc = true)
            sections.put("name", richText)
        }
        if(descLocalisation != null) {
            val richText = ParadoxLocalisationTextHtmlRenderer.render(descLocalisation, locale = usedLocale?.id, forDoc = true)
            sections.put("desc", richText)
        }
    }
}

private fun DocumentationBuilder.addModifierIcon(element: ParadoxModifierElement, name: String, configGroup: CwtConfigGroup, sections: MutableMap<String, String>?) {
    val render = getSettings().documentation.renderIconForModifiers
    val gameType = configGroup.gameType ?: return
    val project = configGroup.project
    val iconFile = run {
        val paths = ParadoxModifierHandler.getModifierIconPaths(name, element)
        paths.firstNotNullOfOrNull { path ->
            val iconSelector = fileSelector(project, element).contextSensitive()
            ParadoxFilePathSearch.searchIcon(path, iconSelector).find()
        }
    }
    //如果没找到的话，不要在文档中显示相关信息
    if(iconFile != null) {
        val iconPath = iconFile.fileInfo?.path?.path ?: return
        appendBr()
        append(PlsBundle.message("prefix.relatedImage")).append(" ")
        append("icon = ").appendFilePathLink(gameType, iconPath, iconPath, element)
    }
    if(sections != null && render) {
        if(iconFile != null) {
            val url = ParadoxImageResolver.resolveUrlByFile(iconFile) ?: ParadoxImageResolver.getDefaultUrl()
            sections.put("icon", buildDocumentation { appendImgTag(url) })
        }
    }
}

private fun DocumentationBuilder.addModifierScope(element: ParadoxModifierElement, name: String, configGroup: CwtConfigGroup, sections: MutableMap<String, String>?) {
    //即使是在CWT文件中，如果可以推断得到CWT规则组，也显示作用域信息
    if(!getSettings().documentation.showScopes) return
    
    if(sections == null) return
    val gameType = configGroup.gameType ?: return
    val modifierCategories = ParadoxModifierSupport.getModifierCategories(element) ?: return
    val contextElement = element
    val categoryNames = modifierCategories.keys
    if(categoryNames.isNotEmpty()) {
        sections.put(PlsBundle.message("sectionTitle.categories"), getModifierCategoriesText(categoryNames, gameType, contextElement))
    }
    
    val supportedScopes = ParadoxScopeHandler.getSupportedScopes(modifierCategories)
    sections.put(PlsBundle.message("sectionTitle.supportedScopes"), getScopesText(supportedScopes, gameType, contextElement))
}

private fun DocumentationBuilder.addScopeContext(element: PsiElement, name: String, configGroup: CwtConfigGroup, sections: MutableMap<String, String>?) {
    //进行代码提示时也显示作用域上下文信息
    //@Suppress("DEPRECATION")
    //if(DocumentationManager.IS_FROM_LOOKUP.get(element) == true) return
    
    if(!getSettings().documentation.showScopeContext) return
    
    if(sections == null) return
    val gameType = configGroup.gameType ?: return
    val memberElement = element.parentOfType<ParadoxScriptMemberElement>(true) ?: return
    if(!ParadoxScopeHandler.isScopeContextSupported(memberElement, indirect = true)) return
    val scopeContext = ParadoxScopeHandler.getScopeContext(memberElement)
    if(scopeContext == null) return
    //TODO 如果作用域引用位于脚本表达式中，应当使用那个位置的作用域上下文，但是目前实现不了
    // 因为这里的referenceElement是整个stringExpression，得到的作用域上下文会是脚本表达式最终的作用域上下文
    sections.put(PlsBundle.message("sectionTitle.scopeContext"), getScopeContextText(scopeContext, gameType, element))
}

private fun DocumentationBuilder.buildScriptedVariableDefinition(element: ParadoxScriptScriptedVariable, name: String) {
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

private fun DocumentationBuilder.buildPropertyDefinition(element: ParadoxScriptProperty, name: String) {
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

private fun DocumentationBuilder.buildDefinitionDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sectionsList: List<MutableMap<String, String>>?) {
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

private fun DocumentationBuilder.addDefinitionInfo(definitionInfo: ParadoxDefinitionInfo) {
    val gameType = definitionInfo.gameType
    append(PlsBundle.message("prefix.definition"))
    append(" <b>")
    val name = definitionInfo.name
    append(name.orAnonymous().escapeXml())
    append("</b>: ")
    val typeConfig = definitionInfo.typeConfig
    val typeLink = "${gameType.prefix}types/${typeConfig.name}"
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

private fun DocumentationBuilder.addSuperDefinitionInfo(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
    val gameType = definitionInfo.gameType
    appendIndent().append(PlsBundle.message("inherits"))
    append(" ")
    val name = definitionInfo.name
    appendDefinitionLink(gameType, name, definitionInfo.type, definition, name.orAnonymous().escapeXml())
    append(": ")
    val typeConfig = definitionInfo.typeConfig
    val typeLink = "${gameType.prefix}types/${typeConfig.name}"
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

private fun DocumentationBuilder.addRelatedLocalisationsForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sections: MutableMap<String, String>?) {
    val localisationInfos = definitionInfo.localisations
    if(localisationInfos.isEmpty()) return
    val project = element.project
    val usedLocale = ParadoxLocaleHandler.getUsedLocaleInDocumentation()
    val finalUsedLocale = usedLocale ?: ParadoxLocaleHandler.getPreferredLocale()
    val map = mutableMapOf<String, String>()
    val sectionKeys = mutableSetOf<String>()
    for((key, locationExpression, required) in localisationInfos) {
        if(sectionKeys.contains(key)) continue
        val selector = localisationSelector(project, element).contextSensitive().preferLocale(finalUsedLocale)
        val resolved = locationExpression.resolve(element, definitionInfo, selector) ?: continue //发生意外，直接跳过
        if(resolved.message != null) {
            map.put(key, resolved.message)
        } else if(resolved.element != null) {
            map.put(key, buildDocumentation { appendLocalisationLink(definitionInfo.gameType, resolved.name, element) })
        } else if(required) {
            map.putIfAbsent(key, resolved.name)
        }
        if(resolved.element != null) {
            sectionKeys.add(key)
            if(sections != null && getSettings().documentation.renderRelatedLocalisationsForDefinitions) {
                //加上渲染后的相关本地化文本
                val richText = ParadoxLocalisationTextHtmlRenderer.render(resolved.element, locale = usedLocale?.id, forDoc = true)
                sections.put(key, richText)
            }
        }
    }
    for((key, value) in map) {
        appendBr()
        append(PlsBundle.message("prefix.relatedLocalisation")).append(" ")
        append(key).append(" = ").append(value)
    }
}

private fun DocumentationBuilder.addRelatedImagesForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sections: MutableMap<String, String>?) {
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
            val v = when {
                nameOrFilePath.startsWith("GFX") -> buildDocumentation { appendDefinitionLink(gameType, nameOrFilePath, "sprite", element) }
                else -> buildDocumentation { appendFilePathLink(gameType, nameOrFilePath, nameOrFilePath, element) }
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
                sections.put(key, buildDocumentation { appendImgTag(url) })
            }
        }
    }
    for((key, value) in map) {
        appendBr()
        append(PlsBundle.message("prefix.relatedImage")).append(" ")
        append(key).append(" = ").append(value)
    }
}

private fun DocumentationBuilder.addGeneratedModifiersForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
    ParadoxModifierSupport.buildDDocumentationDefinitionForDefinition(element, definitionInfo, this)
}

private fun DocumentationBuilder.addModifierScopeForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sections: MutableMap<String, String>?) {
    //即使是在CWT文件中，如果可以推断得到CWT规则组，也显示作用域信息
    if(!getSettings().documentation.showScopes) return
    
    if(sections == null) return
    val gameType = definitionInfo.gameType
    val modifierCategories = ParadoxDefinitionModifierProvider.getModifierCategories(element, definitionInfo) ?: return
    val categoryNames = modifierCategories.keys
    if(categoryNames.isNotEmpty()) {
        sections.put(PlsBundle.message("sectionTitle.categories"), getModifierCategoriesText(categoryNames, gameType, element))
    }
    
    val supportedScopes = ParadoxScopeHandler.getSupportedScopes(modifierCategories)
    sections.put(PlsBundle.message("sectionTitle.supportedScopes"), getScopesText(supportedScopes, gameType, element))
}

private fun DocumentationBuilder.addScopeContextForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sections: MutableMap<String, String>?) {
    //进行代码提示时也显示作用域上下文信息
    //@Suppress("DEPRECATION")
    //if(DocumentationManager.IS_FROM_LOOKUP.get(element) == true) return
    
    if(!getSettings().documentation.showScopeContext) return
    
    if(sections == null) return
    val gameType = definitionInfo.gameType
    if(!ParadoxScopeHandler.isScopeContextSupported(element, indirect = true)) return
    val scopeContext = ParadoxScopeHandler.getScopeContext(element)
    if(scopeContext == null) return
    sections.put(PlsBundle.message("sectionTitle.scopeContext"), getScopeContextText(scopeContext, gameType, element))
}

private fun DocumentationBuilder.addParametersForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, sections: MutableMap<String, String>?) {
    if(!getSettings().documentation.showParameters) return
    
    if(sections == null) return
    val parameterContextInfo = ParadoxParameterSupport.getContextInfo(element) ?: return
    if(parameterContextInfo.parameters.isEmpty()) return //ignore
    val parametersText = buildDocumentation {
        var isFirst = true
        parameterContextInfo.parameters.forEach { (parameterName, elements) ->
            if(isFirst) isFirst = false else append("<br>")
            append("<code>")
            append(parameterName)
            //加上推断得到的规则信息
            if(ParadoxParameterHandler.isOptional(parameterContextInfo, parameterName)) append("?") //optional marker
            //加上推断得到的类型信息
            val parameterElement = elements.firstOrNull()?.parameterElement
            if(parameterElement != null) {
                val inferredType = ParadoxParameterHandler.getInferredType(parameterElement)
                if(inferredType != null) {
                    append(": ").append(inferredType.escapeXml())
                }
            }
            append("</code>")
        }
    }
    sections.put(PlsBundle.message("sectionTitle.parameters"), parametersText)
}

private fun DocumentationBuilder.addEventTypeForOnAction(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
    if(definitionInfo.type != "on_action") return
    //有些游戏类型直接通过CWT文件指定了事件类型，而非CSV文件，忽略这种情况
    val configGroup = definitionInfo.configGroup
    val gameType = configGroup.gameType
    val config = definitionInfo.configGroup.extendedOnActions.findFromPattern(definitionInfo.name, element, definitionInfo.configGroup)
    if(config == null) return
    val eventType = config.eventType
    appendBr()
    val typeLink = "${gameType.prefix}types/event/$eventType"
    append(PlsBundle.message("prefix.eventType")).append(" ").appendCwtLink(typeLink, eventType)
}

private fun DocumentationBuilder.buildLocalisationLocaleDefinition(name: String) {
    definition {
        //加上元素定义信息
        append(PlsBundle.message("prefix.localisationLocale")).append(" <b>").append(name).append("</b>")
    }
}

private fun DocumentationBuilder.buildLocalisationPropertyDefinition(element: ParadoxLocalisationProperty) {
    definition {
        //加上文件信息
        appendFileInfoHeader(element)
        //加上元素定义信息
        append(PlsBundle.message("prefix.localisationProperty")).append(" <b>").append(element.name).append("</b>")
    }
}

private fun DocumentationBuilder.buildLocalisationDefinition(element: ParadoxLocalisationProperty, category: ParadoxLocalisationCategory, name: String) {
    definition {
        //加上文件信息
        appendFileInfoHeader(element)
        //加上元素定义信息
        append(category.text).append(" <b>").append(name).append("</b>")
    }
}

private fun DocumentationBuilder.buildLocalisationSections(element: ParadoxLocalisationProperty) {
    //加上渲染后的本地化文本
    if(!getSettings().documentation.renderLocalisationForLocalisations) return
    val locale = selectLocale(element)
    val usedLocale = ParadoxLocaleHandler.getUsedLocaleInDocumentation()
    val usedElement = when {
        usedLocale == null -> element
        usedLocale == locale -> element
        else -> {
            val selector = localisationSelector(element.project, element).contextSensitive().preferLocale(usedLocale)
            val category = element.category
            when(category) {
                ParadoxLocalisationCategory.Localisation -> ParadoxLocalisationSearch.search(element.name, selector).find()
                ParadoxLocalisationCategory.SyncedLocalisation -> ParadoxSyncedLocalisationSearch.search(element.name, selector).find()
                null -> element
            }?.castOrNull<ParadoxLocalisationProperty>() ?: element
        }
    }
    val richText = ParadoxLocalisationTextHtmlRenderer.render(usedElement, locale = usedLocale?.id, forDoc = true)
    if(richText.isNotEmpty()) {
        sections {
            section(PlsBundle.message("sectionTitle.text"), richText)
        }
    }
}

private fun DocumentationBuilder.buildLocalisationIconDefinition(name: String) {
    definition {
        //加上元素定义信息
        append(PlsBundle.message("prefix.localisationIcon")).append(" <b>").append(name).append("</b>")
    }
}

private fun DocumentationBuilder.buildLocalisationCommandScopeDefinition(name: String) {
    definition {
        //加上元素定义信息
        append(PlsBundle.message("prefix.localisationCommandScope")).append(" <b>").append(name).append("</b>")
    }
}

private fun DocumentationBuilder.buildLocalisationCommandFieldDefinition(name: String) {
    definition {
        //加上元素定义信息
        append(PlsBundle.message("prefix.localisationCommandField")).append(" <b>").append(name).append("</b>")
    }
}

private fun DocumentationBuilder.buildLocalisationColorDefinition(name: String) {
    definition {
        //加上元素定义信息
        append(PlsBundle.message("prefix.localisationColor")).append(" <b>").append(name).append("</b>")
    }
}

private fun DocumentationBuilder.buildDocumentationContent(element: ParadoxParameterElement) {
    ParadoxParameterExtendedDocumentationProvider.buildDocumentationContent(element) { content ->
        content { append(content) }
    }
}

private fun DocumentationBuilder.buildDocumentationContent(element: ParadoxComplexEnumValueElement) {
    ParadoxComplexEnumValueExtendedDocumentationProvider.buildDocumentationContent(element) { content ->
        content { append(content) }
    }
}

private fun DocumentationBuilder.buildDocumentationContent(element: ParadoxDynamicValueElement) {
    ParadoxDynamicValueExtendedDocumentationProvider.buildDocumentationContent(element) { content ->
        content { append(content) }
    }
}

private fun DocumentationBuilder.buildDocumentationContent(element: ParadoxScriptScriptedVariable) {
    ParadoxScriptedVariableExtendedDocumentationProvider.buildDocumentationContent(element) { content ->
        content { append(content) }
    }
}

private fun DocumentationBuilder.buildDocumentationContent(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
    ParadoxDefinitionExtendedDocumentationProvider.buildDocumentationContent(element, definitionInfo) { content ->
        content { append(content) }
    }
}

private fun DocumentationBuilder.buildLineCommentContent(element: PsiElement) {
    //加上单行注释文本
    if(!getSettings().documentation.renderLineComment) return
    val docText = getLineCommentDocText(element)
    if(docText.isNotNullOrEmpty()) {
        content {
            append(docText)
        }
    }
}
