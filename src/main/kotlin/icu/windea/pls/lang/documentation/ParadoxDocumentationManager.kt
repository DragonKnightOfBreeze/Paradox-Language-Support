@file:Suppress("unused")

package icu.windea.pls.lang.documentation

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.ep.documentation.*
import icu.windea.pls.ep.inherit.*
import icu.windea.pls.ep.modifier.*
import icu.windea.pls.ep.parameter.*
import icu.windea.pls.ep.reference.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*

object ParadoxDocumentationManager {
    private const val SECTIONS_INFO = 0
    private const val SECTIONS_IMAGES = 1
    private const val SECTIONS_LOC = 2

    fun computeLocalDocumentation(element: PsiElement, originalElement: PsiElement?, hint: Boolean): String? {
        return when (element) {
            is ParadoxParameterElement -> getParameterDoc(element, originalElement, hint)
            is ParadoxLocalisationParameterElement -> getLocalisationParameterDoc(element, originalElement, hint)
            is ParadoxComplexEnumValueElement -> getComplexEnumValueDoc(element, originalElement, hint)
            is ParadoxDynamicValueElement -> getDynamicValueDoc(element, originalElement, hint)
            is ParadoxModifierElement -> getModifierDoc(element, originalElement, hint)
            is ParadoxScriptScriptedVariable -> getScriptedVariableDoc(element, originalElement, hint)
            is ParadoxScriptProperty -> getPropertyDoc(element, originalElement, hint)
            is ParadoxScriptPropertyKey -> computeLocalDocumentation(element.parent, originalElement, hint)
            is ParadoxDefinitionNavigationElement -> computeLocalDocumentation(element.parent, originalElement, hint)
            is ParadoxLocalisationLocale -> getLocalisationLocaleDoc(element, originalElement, hint)
            is ParadoxLocalisationProperty -> getLocalisationPropertyDoc(element, originalElement, hint)
            is ParadoxLocalisationIcon -> getLocalisationIconDoc(element, originalElement, hint)
            is ParadoxLocalisationColorfulText -> getLocalisationColorDoc(element, originalElement, hint)
            is ParadoxLocalisationArgument -> getLocalisationArgumentDoc(element, originalElement, hint)
            else -> null
        }
    }

    private fun getParameterDoc(element: ParadoxParameterElement, originalElement: PsiElement?, hint: Boolean): String {
        return buildDocumentation {
            buildParameterDefinition(element)
            if (hint) return@buildDocumentation
            buildDocumentationContent(element)
        }
    }

    private fun getLocalisationParameterDoc(element: ParadoxLocalisationParameterElement, originalElement: PsiElement?, hint: Boolean): String {
        return buildDocumentation {
            buildLocalisationParameterDefinition(element)
        }
    }

    private fun getComplexEnumValueDoc(element: ParadoxComplexEnumValueElement, originalElement: PsiElement?, hint: Boolean): String {
        return buildDocumentation {
            if (!hint) initSections(3)
            buildComplexEnumValueDefinition(element)
            if (hint) return@buildDocumentation
            buildDocumentationContent(element)
            buildSections()
        }
    }

    private fun getDynamicValueDoc(element: ParadoxDynamicValueElement, originalElement: PsiElement?, hint: Boolean): String {
        return buildDocumentation {
            if (!hint) initSections(3)
            buildDynamicValueDefinition(element)
            if (hint) return@buildDocumentation
            buildDocumentationContent(element)
            buildSections()
        }
    }

    private fun getModifierDoc(element: ParadoxModifierElement, originalElement: PsiElement?, hint: Boolean): String {
        return buildDocumentation {
            if (!hint) initSections(3)
            buildModifierDefinition(element)
            if (hint) return@buildDocumentation
            buildSections()
        }
    }

    private fun getScriptedVariableDoc(element: ParadoxScriptScriptedVariable, originalElement: PsiElement?, hint: Boolean): String? {
        val name = element.name ?: return null
        return buildDocumentation {
            buildScriptedVariableDefinition(element, name)
            if (hint) return@buildDocumentation
            buildDocumentationContent(element)
            buildLineCommentContent(element)
        }
    }

    private fun getPropertyDoc(element: ParadoxScriptProperty, originalElement: PsiElement?, hint: Boolean): String {
        val definitionInfo = element.definitionInfo
        if (definitionInfo != null) return getDefinitionDoc(element, definitionInfo, originalElement, hint)

        val name = element.name
        return buildDocumentation {
            buildPropertyDefinition(element, name)
            if (hint) return@buildDocumentation
            buildLineCommentContent(element)
        }
    }

    private fun getDefinitionDoc(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, originalElement: PsiElement?, hint: Boolean): String {
        return buildDocumentation {
            //对于相关图片信息，在definition部分显示在相关本地化信息之后，在sections部分则显示在之前
            if (!hint) initSections(3)
            buildDefinitionDefinition(element, definitionInfo)
            if (hint) return@buildDocumentation
            buildDocumentationContent(element, definitionInfo)
            buildLineCommentContent(element)
            buildSections()
        }
    }

    private fun getLocalisationPropertyDoc(element: ParadoxLocalisationProperty, originalElement: PsiElement?, hint: Boolean): String {
        val localisationInfo = element.localisationInfo
        if (localisationInfo != null) return getLocalisationDoc(element, localisationInfo, originalElement, hint)

        return buildDocumentation {
            buildLocalisationPropertyDefinition(element)
        }
    }

    private fun getLocalisationLocaleDoc(element: ParadoxLocalisationLocale, originalElement: PsiElement?, hint: Boolean): String {
        val name = element.name
        return buildDocumentation {
            buildLocalisationLocaleDefinition(name)
        }
    }

    private fun getLocalisationDoc(element: ParadoxLocalisationProperty, localisationInfo: ParadoxLocalisationInfo, originalElement: PsiElement?, hint: Boolean): String {
        return buildDocumentation {
            buildLocalisationDefinition(element, localisationInfo)
            if (hint) return@buildDocumentation
            buildLineCommentContent(element)
            buildLocalisationSections(element)
        }
    }

    private fun getLocalisationIconDoc(element: ParadoxLocalisationIcon, originalElement: PsiElement?, hint: Boolean): String? {
        val name = element.name ?: return null
        return buildDocumentation {
            buildLocalisationIconDefinition(name)
        }
    }

    private fun getLocalisationColorDoc(element: ParadoxLocalisationColorfulText, originalElement: PsiElement?, hint: Boolean): String? {
        val name = element.name ?: return null
        return buildDocumentation {
            //加上元素定义信息
            buildLocalisationColorDefinition(name)
        }
    }

    private fun getLocalisationArgumentDoc(element: ParadoxLocalisationArgument, originalElement: PsiElement?, hint: Boolean): String? {
        if (hint) return null
        if (element is ParadoxLocalisationIconArgument) return null
        return buildDocumentation {
            initSections(1)
            buildLocalisationArgumentInfo(element)
            buildSections()
        }
    }

    private fun DocumentationBuilder.buildParameterDefinition(element: ParadoxParameterElement) {
        val name = element.name
        definition {
            val r = ParadoxParameterSupport.getDocumentationDefinition(element, this)
            if (!r) {
                //显示默认的快速文档
                append(PlsStringConstants.parameterPrefix).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
            }
        }
    }

    private fun DocumentationBuilder.buildLocalisationParameterDefinition(element: ParadoxLocalisationParameterElement) {
        val name = element.name
        definition {
            val r = ParadoxLocalisationParameterSupport.getDocumentationDefinition(element, this)
            if (!r) {
                //显示默认的快速文档
                append(PlsStringConstants.parameterPrefix).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
            }
        }
    }

    private fun DocumentationBuilder.buildComplexEnumValueDefinition(element: ParadoxComplexEnumValueElement) {
        definition {
            val name = element.name
            val enumName = element.enumName
            val gameType = element.gameType
            val configGroup = PlsFacade.getConfigGroup(element.project, gameType)
            append(PlsStringConstants.complexEnumValuePrefix).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
            val complexEnumConfig = configGroup.complexEnums[enumName]
            if (complexEnumConfig != null) {
                val typeLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "complex_enums", enumName)
                append(": ").appendPsiLinkOrUnresolved(typeLink.escapeXml(), enumName.escapeXml())
            } else {
                append(": ").append(enumName)
            }

            //加上相关本地化信息：同名的本地化
            addRelatedLocalisationsForComplexEnumValue(element)

            //加上作用域上下文信息
            addScopeContext(element, name, configGroup)
        }
    }

    private fun DocumentationBuilder.addRelatedLocalisationsForComplexEnumValue(element: ParadoxComplexEnumValueElement) {
        val render = PlsFacade.getSettings().documentation.renderRelatedLocalisationsForComplexEnumValues
        val gameType = element.gameType
        val usedLocale = ParadoxLocaleManager.getResolvedLocaleConfigInDocumentation(element)
        val nameLocalisation = ParadoxComplexEnumValueManager.getNameLocalisation(element.name, element, usedLocale)
        //如果没找到的话，不要在文档中显示相关信息
        run {
            if (nameLocalisation == null) return@run
            appendBr()
            append(PlsStringConstants.relatedLocalisationPrefix).append(" ")
            val link = ParadoxReferenceLinkType.Localisation.createLink(gameType, nameLocalisation.name)
            append("name = ").appendPsiLinkOrUnresolved(link.escapeXml(), nameLocalisation.name.escapeXml(), context = element)
        }
        run rs@{
            val sections = getSections(SECTIONS_LOC)
            if (sections == null || !render) return@rs
            run {
                if (nameLocalisation == null) return@run
                val richText = ParadoxLocalisationTextHtmlRenderer(forDoc = true).render(nameLocalisation)
                sections.put("name", richText)
            }
        }
    }

    private fun DocumentationBuilder.buildDynamicValueDefinition(element: ParadoxDynamicValueElement) {
        val name = element.name
        val dynamicValueTypes = element.dynamicValueTypes
        val gameType = element.gameType
        val configGroup = PlsFacade.getConfigGroup(element.project, gameType)
        definition {
            append(PlsStringConstants.dynamicValuePrefix).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
            append(": ")
            var appendSeparator = false
            for (dynamicValueType in dynamicValueTypes) {
                if (appendSeparator) append(" | ") else appendSeparator = true
                val valueConfig = configGroup.dynamicValueTypes[dynamicValueType]
                if (valueConfig != null) {
                    val typeLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "values", dynamicValueType)
                    appendPsiLinkOrUnresolved(typeLink.escapeXml(), dynamicValueType.escapeXml())
                } else {
                    append(dynamicValueType)
                }
            }

            //加上相关本地化信息：同名的本地化
            addRelatedLocalisationsForDynamicValue(element)

            //加上作用域上下文信息
            addScopeContext(element, name, configGroup)
        }
    }

    private fun DocumentationBuilder.addRelatedLocalisationsForDynamicValue(element: ParadoxDynamicValueElement) {
        val render = PlsFacade.getSettings().documentation.renderRelatedLocalisationsForDynamicValues
        val gameType = element.gameType
        val usedLocale = ParadoxLocaleManager.getResolvedLocaleConfigInDocumentation(element)
        val nameLocalisation = ParadoxDynamicValueManager.getNameLocalisation(element.name, element, usedLocale)
        //如果没找到的话，不要在文档中显示相关信息
        run {
            if (nameLocalisation == null) return@run
            appendBr()
            append(PlsStringConstants.relatedLocalisationPrefix).append(" ")
            val link = ParadoxReferenceLinkType.Localisation.createLink(gameType, nameLocalisation.name)
            append("name = ").appendPsiLinkOrUnresolved(link.escapeXml(), nameLocalisation.name.escapeXml(), context = element)
        }
        run rs@{
            val sections = getSections(SECTIONS_LOC)
            if (sections == null || !render) return@rs
            run {
                if (nameLocalisation == null) return@run
                val richText = ParadoxLocalisationTextHtmlRenderer(forDoc = true).render(nameLocalisation)
                sections.put("name", richText)
            }
        }
    }

    private fun DocumentationBuilder.buildModifierDefinition(element: ParadoxModifierElement) {
        val name = element.name
        definition {
            val r = ParadoxModifierSupport.getDocumentationDefinition(element, this)
            if (!r) {
                //显示默认的快速文档
                append(PlsStringConstants.modifierPrefix).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
            }

            val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
            addModifierRelatedLocalisations(element, name, configGroup)

            addModifierIcon(element, name, configGroup)
            addModifierScope(element, name, configGroup)
            addScopeContext(element, name, configGroup)
        }
    }

    private fun DocumentationBuilder.addModifierRelatedLocalisations(element: ParadoxModifierElement, name: String, configGroup: CwtConfigGroup) {
        val render = PlsFacade.getSettings().documentation.renderNameDescForModifiers
        val gameType = configGroup.gameType ?: return
        val project = configGroup.project
        val usedLocale = ParadoxLocaleManager.getResolvedLocaleConfigInDocumentation(element)
        val nameLocalisation = run {
            val keys = ParadoxModifierManager.getModifierNameKeys(name, element)
            keys.firstNotNullOfOrNull { key ->
                val selector = selector(project, element).localisation().contextSensitive()
                    .preferLocale(usedLocale)
                    .withConstraint(ParadoxIndexConstraint.Localisation.Modifier)
                ParadoxLocalisationSearch.search(key, selector).find()
            }
        }
        val descLocalisation = run {
            val keys = ParadoxModifierManager.getModifierDescKeys(name, element)
            keys.firstNotNullOfOrNull { key ->
                val selector = selector(project, element).localisation().contextSensitive()
                    .preferLocale(usedLocale)
                    .withConstraint(ParadoxIndexConstraint.Localisation.Modifier)
                ParadoxLocalisationSearch.search(key, selector).find()
            }
        }
        //如果没找到的话，不要在文档中显示相关信息
        run {
            if (nameLocalisation == null) return@run
            appendBr()
            append(PlsStringConstants.relatedLocalisationPrefix).append(" ")
            val link = ParadoxReferenceLinkType.Localisation.createLink(gameType, nameLocalisation.name)
            append("name = ").appendPsiLinkOrUnresolved(link.escapeXml(), nameLocalisation.name.escapeXml(), context = element)
        }
        run {
            if (descLocalisation == null) return@run
            appendBr()
            append(PlsStringConstants.relatedLocalisationPrefix).append(" ")
            val link = ParadoxReferenceLinkType.Localisation.createLink(gameType, descLocalisation.name)
            append("desc = ").appendPsiLinkOrUnresolved(link.escapeXml(), descLocalisation.name.escapeXml(), context = element)
        }
        run rs@{
            val sections = getSections(SECTIONS_LOC)
            if (sections == null || !render) return@rs
            run {
                if (nameLocalisation == null) return@run
                val richText = ParadoxLocalisationTextHtmlRenderer(forDoc = true).render(nameLocalisation)
                sections.put("name", richText)
            }
            run {
                if (descLocalisation == null) return@run
                val richText = ParadoxLocalisationTextHtmlRenderer(forDoc = true).render(descLocalisation)
                sections.put("desc", richText)
            }
        }
    }

    private fun DocumentationBuilder.addModifierIcon(element: ParadoxModifierElement, name: String, configGroup: CwtConfigGroup) {
        val render = PlsFacade.getSettings().documentation.renderIconForModifiers
        val gameType = configGroup.gameType ?: return
        val project = configGroup.project
        val iconFile = run {
            val paths = ParadoxModifierManager.getModifierIconPaths(name, element)
            paths.firstNotNullOfOrNull { path ->
                val iconSelector = selector(project, element).file().contextSensitive()
                ParadoxFilePathSearch.searchIcon(path, iconSelector).find()
            }
        }
        //如果没找到的话，不要在文档中显示相关信息
        run {
            if (iconFile == null) return@run
            val iconPath = iconFile.fileInfo?.path?.path ?: return@run
            appendBr()
            append(PlsStringConstants.relatedImagePrefix).append(" ")
            val link = ParadoxReferenceLinkType.FilePath.createLink(gameType, iconPath)
            append("icon = ").appendPsiLinkOrUnresolved(link.escapeXml(), iconPath.escapeXml(), context = element)
        }
        run rs@{
            val sections = getSections(SECTIONS_IMAGES)
            if (sections == null || !render) return@rs
            run {
                if (iconFile == null) return@run
                val url = ParadoxImageManager.resolveUrlByFile(iconFile, project) ?: return@run
                sections.put("icon", buildDocumentation { appendImgTag(url) })
            }
        }
    }

    private fun DocumentationBuilder.addModifierScope(element: ParadoxModifierElement, name: String, configGroup: CwtConfigGroup) {
        //即使是在CWT文件中，如果可以推断得到CWT规则组，也显示作用域信息
        if (!PlsFacade.getSettings().documentation.showScopes) return

        val sections = getSections(SECTIONS_INFO) ?: return
        val gameType = configGroup.gameType ?: return
        val modifierCategories = ParadoxModifierSupport.getModifierCategories(element) ?: return
        val contextElement = element
        val categoryNames = modifierCategories.keys
        if (categoryNames.isNotEmpty()) {
            sections.put(PlsBundle.message("sectionTitle.categories"), getModifierCategoriesText(categoryNames, gameType, contextElement))
        }

        val supportedScopes = ParadoxScopeManager.getSupportedScopes(modifierCategories)
        sections.put(PlsBundle.message("sectionTitle.supportedScopes"), getScopesText(supportedScopes, gameType, contextElement))
    }

    private fun DocumentationBuilder.addScopeContext(element: PsiElement, name: String, configGroup: CwtConfigGroup) {
        //进行代码提示时也显示作用域上下文信息
        //@Suppress("DEPRECATION")
        //if(DocumentationManager.IS_FROM_LOOKUP.get(element) == true) return

        if (!PlsFacade.getSettings().documentation.showScopeContext) return

        val sections = getSections(SECTIONS_INFO) ?: return
        val gameType = configGroup.gameType ?: return
        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(true) ?: return
        if (!ParadoxScopeManager.isScopeContextSupported(memberElement, indirect = true)) return
        val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(memberElement)
        if (scopeContext == null) return
        //TODO 如果作用域引用位于脚本表达式中，应当使用那个位置的作用域上下文，但是目前实现不了
        // 因为这里的referenceElement是整个stringExpression，得到的作用域上下文会是脚本表达式最终的作用域上下文
        sections.put(PlsBundle.message("sectionTitle.scopeContext"), getScopeContextText(scopeContext, gameType, element))
    }

    private fun DocumentationBuilder.buildScriptedVariableDefinition(element: ParadoxScriptScriptedVariable, name: String) {
        definition {
            //加上文件信息
            appendFileInfoHeader(element)
            //加上定义信息
            append(PlsStringConstants.scriptedVariablePrefix).append(" <b>@").append(name.escapeXml().orAnonymous()).append("</b>")
            val valueElement = element.scriptedVariableValue
            when (valueElement) {
                is ParadoxScriptString -> append(" = ").append(valueElement.text.escapeXml())
                is ParadoxScriptValue -> append(" = ").append(valueElement.value.escapeXml())
            }

            //加上相关本地化信息：同名的本地化
            addRelatedLocalisationsForScriptedVariable(element, name)
        }
    }

    private fun DocumentationBuilder.addRelatedLocalisationsForScriptedVariable(element: ParadoxScriptScriptedVariable, name: String) {
        val render = PlsFacade.getSettings().documentation.renderRelatedLocalisationsForScriptedVariables
        val gameType = selectGameType(element) ?: return
        val usedLocale = ParadoxLocaleManager.getResolvedLocaleConfigInDocumentation(element)
        val nameLocalisation = ParadoxScriptedVariableManager.getNameLocalisation(name, element, usedLocale)
        //如果没找到的话，不要在文档中显示相关信息
        run {
            if (nameLocalisation == null) return@run
            appendBr()
            append(PlsStringConstants.relatedLocalisationPrefix).append(" ")
            val link = ParadoxReferenceLinkType.Localisation.createLink(gameType, nameLocalisation.name)
            append("name = ").appendPsiLinkOrUnresolved(link.escapeXml(), nameLocalisation.name.escapeXml(), context = element)
        }
        run rs@{
            val sections = getSections(SECTIONS_LOC)
            if (sections == null || !render) return@rs
            run {
                if (nameLocalisation == null) return@run
                val richText = ParadoxLocalisationTextHtmlRenderer(forDoc = true).render(nameLocalisation)
                sections.put("name", richText)
            }
        }
    }

    private fun DocumentationBuilder.buildPropertyDefinition(element: ParadoxScriptProperty, name: String) {
        definition {
            //加上文件信息
            appendFileInfoHeader(element)
            //加上定义信息
            append(PlsStringConstants.propertyPrefix).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
            val valueElement = element.propertyValue
            when (valueElement) {
                is ParadoxScriptString -> append(" = ").append(valueElement.text.escapeXml())
                is ParadoxScriptValue -> append(" = ").append(valueElement.value.escapeXml())
            }
        }
    }

    private fun DocumentationBuilder.buildDefinitionDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
        definition {
            //加上文件信息
            appendFileInfoHeader(element)

            //加上定义信息
            addDefinitionInfo(element, definitionInfo)

            //加上继承的定义信息
            val superDefinition = ParadoxDefinitionInheritSupport.getSuperDefinition(element, definitionInfo)
            val superDefinitionInfo = superDefinition?.definitionInfo
            if (superDefinitionInfo != null) {
                appendBr()
                addSuperDefinitionInfo(superDefinition, superDefinitionInfo)
            }

            //加上相关本地化信息：去重后的一组本地化的键名，不包括可选且没有对应的本地化的项，按解析顺序排序
            addRelatedLocalisationsForDefinition(element, definitionInfo)

            //加上相关图片信息：去重后的一组DDS文件的filePath，或者sprite的definitionKey，不包括可选且没有对应的图片的项，按解析顺序排序
            addRelatedImagesForDefinition(element, definitionInfo)

            //加上生成的修正的信息
            addGeneratedModifiersForDefinition(element, definitionInfo)

            //加上修正分类和作用域信息（如果支持）
            addModifierScopeForDefinition(element, definitionInfo)

            //加上作用域上下文信息（如果支持）
            addScopeContextForDefinition(element, definitionInfo)

            //加上参数信息（如果支持且存在）
            addParametersForDefinition(element, definitionInfo)

            //加上事件类型信息（对于on_action）
            addEventTypeForOnAction(element, definitionInfo)
        }
    }

    private fun DocumentationBuilder.addDefinitionInfo(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, usePrefix: String? = null) {
        val gameType = definitionInfo.gameType
        val prefix = usePrefix ?: PlsStringConstants.definitionPrefix
        append(prefix).append(" ")
        val name = definitionInfo.name
        if (usePrefix == null) {
            append("<b>").append(name.escapeXml().orAnonymous()).append("</b>")
        } else {
            val link = ParadoxReferenceLinkType.Definition.createLink(gameType, name, definitionInfo.type)
            appendPsiLinkOrUnresolved(link.escapeXml(), name.escapeXml().orAnonymous(), context = definition)
        }
        append(": ")
        val typeConfig = definitionInfo.typeConfig
        val typeLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "types", typeConfig.name)
        appendPsiLinkOrUnresolved(typeLink.escapeXml(), typeConfig.name.escapeXml())
        val subtypeConfigs = definitionInfo.subtypeConfigs
        if (subtypeConfigs.isNotEmpty()) {
            for (subtypeConfig in subtypeConfigs) {
                val subtypeLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "types", typeConfig.name, subtypeConfig.name)
                append(", ").appendPsiLinkOrUnresolved(subtypeLink.escapeXml(), subtypeConfig.name.escapeXml())
            }
        }
    }

    private fun DocumentationBuilder.addSuperDefinitionInfo(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
        val gameType = definitionInfo.gameType
        appendIndent().append(PlsBundle.message("inherits")).append(" ")
        val name = definitionInfo.name
        val link = ParadoxReferenceLinkType.Definition.createLink(gameType, name, definitionInfo.type)
        appendPsiLinkOrUnresolved(link.escapeXml(), name.escapeXml().orAnonymous(), context = definition)
        append(": ")
        val typeConfig = definitionInfo.typeConfig
        val typeLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "types", typeConfig.name)
        appendPsiLinkOrUnresolved(typeLink.escapeXml(), typeConfig.name.escapeXml())
        val subtypeConfigs = definitionInfo.subtypeConfigs
        if (subtypeConfigs.isNotEmpty()) {
            for (subtypeConfig in subtypeConfigs) {
                val subtypeLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "types", typeConfig.name, subtypeConfig.name)
                append(", ").appendPsiLinkOrUnresolved(subtypeLink.escapeXml(), subtypeConfig.name.escapeXml())
            }
        }
    }

    private fun DocumentationBuilder.addRelatedLocalisationsForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
        val localisationInfos = definitionInfo.localisations
        if (localisationInfos.isEmpty()) return
        val usedLocale = ParadoxLocaleManager.getResolvedLocaleConfigInDocumentation(element)
        val map = mutableMapOf<String, String>()
        val sections = getSections(SECTIONS_LOC)
        val sectionKeys = mutableSetOf<String>()
        for ((key, locationExpression, required) in localisationInfos) {
            if (sectionKeys.contains(key)) continue
            val resolveResult = CwtLocationExpressionManager.resolve(locationExpression, element, definitionInfo) { preferLocale(usedLocale) } ?: continue //发生意外，直接跳过
            if (resolveResult.message != null) {
                map.put(key, resolveResult.message)
            } else if (resolveResult.element != null) {
                map.put(key, buildDocumentation {
                    val link = ParadoxReferenceLinkType.Localisation.createLink(definitionInfo.gameType, resolveResult.name)
                    appendPsiLinkOrUnresolved(link.escapeXml(), resolveResult.name.escapeXml(), context = element)
                })
            } else if (required) {
                map.putIfAbsent(key, resolveResult.name)
            }
            val resolvedElement = resolveResult.element
            if (resolvedElement != null) {
                sectionKeys.add(key)
                if (sections != null && PlsFacade.getSettings().documentation.renderRelatedLocalisationsForDefinitions) {
                    //加上渲染后的相关本地化文本
                    val richText = ParadoxLocalisationTextHtmlRenderer(forDoc = true).render(resolvedElement)
                    sections.put(key, richText)
                }
            }
        }
        for ((key, value) in map) {
            appendBr()
            append(PlsStringConstants.relatedLocalisationPrefix).append(" ")
            append(key).append(" = ").append(value)
        }
    }

    private fun DocumentationBuilder.addRelatedImagesForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
        val render = PlsFacade.getSettings().documentation.renderRelatedImagesForDefinitions
        val imagesInfos = definitionInfo.images
        if (imagesInfos.isEmpty()) return
        val map = mutableMapOf<String, String>()
        val sections = getSections(SECTIONS_IMAGES)
        val sectionKeys = mutableSetOf<String>()
        for ((key, locationExpression, required) in imagesInfos) {
            if (sectionKeys.contains(key)) continue
            val resolveResult = CwtLocationExpressionManager.resolve(locationExpression, element, definitionInfo) ?: continue //发生意外，直接跳过
            if (resolveResult.message != null) {
                map.put(key, resolveResult.message)
            } else if (resolveResult.element != null) {
                val nameOrFilePath = resolveResult.nameOrFilePath
                val gameType = definitionInfo.gameType
                val v = when {
                    nameOrFilePath.startsWith("GFX") -> buildDocumentation {
                        val link = ParadoxReferenceLinkType.Definition.createLink(gameType, nameOrFilePath, ParadoxDefinitionTypes.Sprite)
                        appendPsiLinkOrUnresolved(link.escapeXml(), nameOrFilePath.escapeXml(), context = element)
                    }
                    else -> buildDocumentation {
                        val link = ParadoxReferenceLinkType.FilePath.createLink(gameType, nameOrFilePath)
                        appendPsiLinkOrUnresolved(link.escapeXml(), nameOrFilePath.escapeXml(), context = element)
                    }
                }
                map.put(key, v)
            } else if (required) {
                map.putIfAbsent(key, resolveResult.nameOrFilePath)
            }
            val resolveElement = resolveResult.element
            if (resolveElement != null) {
                sectionKeys.add(key)
                if (render && sections != null) {
                    //渲染图片
                    val url = when {
                        resolveElement is ParadoxScriptDefinitionElement && resolveElement.definitionInfo != null -> {
                            ParadoxImageManager.resolveUrlByDefinition(resolveElement, resolveResult.frameInfo)
                        }
                        resolveElement is PsiFile -> {
                            ParadoxImageManager.resolveUrlByFile(resolveElement.virtualFile, resolveElement.project, resolveResult.frameInfo)
                        }
                        else -> null
                    }
                    if (url != null) {
                        sections.put(key, buildDocumentation { appendImgTag(url) })
                    }
                }
            }
        }
        for ((key, value) in map) {
            appendBr()
            append(PlsStringConstants.relatedImagePrefix).append(" ")
            append(key).append(" = ").append(value)
        }
    }

    private fun DocumentationBuilder.addGeneratedModifiersForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
        if (!PlsFacade.getSettings().documentation.showGeneratedModifiers) return

        ParadoxModifierSupport.buildDDocumentationDefinitionForDefinition(element, definitionInfo, this)
    }

    private fun DocumentationBuilder.addModifierScopeForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
        //即使是在CWT文件中，如果可以推断得到CWT规则组，也显示作用域信息
        if (!PlsFacade.getSettings().documentation.showScopes) return

        val sections = getSections(SECTIONS_INFO) ?: return
        val gameType = definitionInfo.gameType
        val modifierCategories = ParadoxDefinitionModifierProvider.getModifierCategories(element, definitionInfo) ?: return
        val categoryNames = modifierCategories.keys
        if (categoryNames.isNotEmpty()) {
            sections.put(PlsBundle.message("sectionTitle.categories"), getModifierCategoriesText(categoryNames, gameType, element))
        }

        val supportedScopes = ParadoxScopeManager.getSupportedScopes(modifierCategories)
        sections.put(PlsBundle.message("sectionTitle.supportedScopes"), getScopesText(supportedScopes, gameType, element))
    }

    private fun DocumentationBuilder.addScopeContextForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
        //进行代码提示时也显示作用域上下文信息
        //@Suppress("DEPRECATION")
        //if(DocumentationManager.IS_FROM_LOOKUP.get(element) == true) return

        if (!PlsFacade.getSettings().documentation.showScopeContext) return

        val sections = getSections(SECTIONS_INFO) ?: return
        val gameType = definitionInfo.gameType
        if (!ParadoxScopeManager.isScopeContextSupported(element, indirect = true)) return
        val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(element)
        if (scopeContext == null) return
        sections.put(PlsBundle.message("sectionTitle.scopeContext"), getScopeContextText(scopeContext, gameType, element))
    }

    private fun DocumentationBuilder.addParametersForDefinition(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
        if (!PlsFacade.getSettings().documentation.showParameters) return

        val sections = getSections(SECTIONS_INFO) ?: return
        val parameterContextInfo = ParadoxParameterSupport.getContextInfo(element) ?: return
        if (parameterContextInfo.parameters.isEmpty()) return //ignore
        val parametersText = buildDocumentation {
            var isFirst = true
            parameterContextInfo.parameters.forEach { (parameterName, elements) ->
                if (isFirst) isFirst = false else append("<br>")
                append("<pre>")
                append(parameterName)
                //加上推断得到的规则信息
                if (ParadoxParameterManager.isOptional(parameterContextInfo, parameterName)) append("?") //optional marker
                //加上推断得到的类型信息
                val parameterElement = elements.firstOrNull()?.parameterElement
                if (parameterElement != null) {
                    val inferredType = ParadoxParameterManager.getInferredType(parameterElement)
                    if (inferredType != null) {
                        append(": ").append(inferredType.escapeXml())
                    }
                }
                append("</pre>")
            }
        }
        sections.put(PlsBundle.message("sectionTitle.parameters"), parametersText)
    }

    private fun DocumentationBuilder.addEventTypeForOnAction(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
        if (definitionInfo.type != ParadoxDefinitionTypes.OnAction) return
        //有些游戏类型直接通过CWT文件指定了事件类型，而非CSV文件，忽略这种情况
        val configGroup = definitionInfo.configGroup
        val gameType = configGroup.gameType
        val config = definitionInfo.configGroup.extendedOnActions.findFromPattern(definitionInfo.name, element, definitionInfo.configGroup)
        if (config == null) return
        val eventType = config.eventType
        appendBr()
        val typeLink = ParadoxReferenceLinkType.CwtConfig.createLink(gameType, "types", "event", eventType)
        append(PlsStringConstants.eventTypePrefix).append(" ").appendPsiLinkOrUnresolved(typeLink.escapeXml(), eventType.escapeXml())
    }

    private fun DocumentationBuilder.buildLocalisationLocaleDefinition(name: String) {
        definition {
            //加上元素定义信息
            append(PlsStringConstants.localePrefix).append(" <b>").append(name).append("</b>")
        }
    }

    private fun DocumentationBuilder.buildLocalisationPropertyDefinition(element: ParadoxLocalisationProperty) {
        definition {
            //加上文件信息
            appendFileInfoHeader(element)
            //加上元素定义信息
            append(PlsStringConstants.localisationPropertyPrefix).append(" <b>").append(element.name).append("</b>")
        }
    }

    private fun DocumentationBuilder.buildLocalisationDefinition(element: ParadoxLocalisationProperty, localisationInfo: ParadoxLocalisationInfo) {
        definition {
            //加上文件信息
            appendFileInfoHeader(element)

            //加上定义信息
            addLocalisationInfo(localisationInfo)

            //加上相关定义信息
            addRelatedDefinitionsForLocalisation(element, localisationInfo)
        }
    }

    private fun DocumentationBuilder.addLocalisationInfo(localisationInfo: ParadoxLocalisationInfo) {
        val prefix = when (localisationInfo.category) {
            ParadoxLocalisationCategory.Localisation -> PlsStringConstants.localisationPrefix
            ParadoxLocalisationCategory.SyncedLocalisation -> PlsStringConstants.localisationSyncedPrefix
        }
        append(prefix).append(" ")
        append("<b>").append(localisationInfo.name.orUnresolved()).append("</b>")
    }

    private fun DocumentationBuilder.addRelatedDefinitionsForLocalisation(element: ParadoxLocalisationProperty, localisationInfo: ParadoxLocalisationInfo) {
        val relatedDefinitions = ParadoxLocalisationManager.getRelatedDefinitions(element)
        if (relatedDefinitions.isEmpty()) return
        for (relatedDefinition in relatedDefinitions) {
            val relatedDefinitionInfo = relatedDefinition.definitionInfo ?: continue
            appendBr()
            addDefinitionInfo(relatedDefinition, relatedDefinitionInfo, usePrefix = PlsStringConstants.relatedDefinitionPrefix)
        }
    }

    private fun DocumentationBuilder.buildLocalisationSections(element: ParadoxLocalisationProperty) {
        //加上渲染后的本地化文本
        if (!PlsFacade.getSettings().documentation.renderLocalisationForLocalisations) return
        val locale = selectLocale(element)
        val usedLocale = ParadoxLocaleManager.getResolvedLocaleConfigInDocumentation(element, locale)
        val usedElement = when {
            usedLocale == locale -> element
            else -> {
                val selector = selector(element.project, element).localisation().contextSensitive().preferLocale(usedLocale)
                val category = element.category
                when (category) {
                    ParadoxLocalisationCategory.Localisation -> ParadoxLocalisationSearch.search(element.name, selector).find()
                    ParadoxLocalisationCategory.SyncedLocalisation -> ParadoxSyncedLocalisationSearch.search(element.name, selector).find()
                    null -> element
                }?.castOrNull<ParadoxLocalisationProperty>() ?: element
            }
        }
        val richText = ParadoxLocalisationTextHtmlRenderer(forDoc = true).render(usedElement)
        if (richText.isNotEmpty()) {
            sections {
                section(PlsBundle.message("sectionTitle.text"), richText)
            }
        }
    }

    private fun DocumentationBuilder.buildLocalisationIconDefinition(name: String) {
        definition {
            //加上元素定义信息
            append(PlsStringConstants.localisationIconPrefix).append(" <b>").append(name).append("</b>")
        }
    }

    private fun DocumentationBuilder.buildLocalisationColorDefinition(name: String) {
        definition {
            //加上元素定义信息
            append(PlsStringConstants.localisationColorPrefix).append(" <b>").append(name).append("</b>")
        }
    }

    private fun DocumentationBuilder.buildLocalisationArgumentInfo(element: ParadoxLocalisationArgument) {
        val sections = getSections(SECTIONS_INFO) ?: return
        ParadoxLocalisationArgumentManager.getInfo(element)?.let {
            sections.put(PlsBundle.message("sectionTitle.formattingTags"), it)
        }
    }

    private fun DocumentationBuilder.buildDocumentationContent(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) {
        ParadoxDefinitionExtendedDocumentationProvider.getAllDocumentationContent(element, definitionInfo).forEach { content { append(it) } }
    }

    private fun DocumentationBuilder.buildDocumentationContent(element: PsiElement) {
        val contents = when (element) {
            is ParadoxParameterElement -> ParadoxParameterExtendedDocumentationProvider.getAllDocumentationContent(element)
            is ParadoxComplexEnumValueElement -> ParadoxComplexEnumValueExtendedDocumentationProvider.getAllDocumentationContent(element)
            is ParadoxDynamicValueElement -> ParadoxDynamicValueExtendedDocumentationProvider.getAllDocumentationContent(element)
            is ParadoxScriptScriptedVariable -> ParadoxScriptedVariableExtendedDocumentationProvider.getAllDocumentationContent(element)
            else -> emptyList()
        }
        if (contents.isEmpty()) return
        contents.forEach { content { append(it) } }
    }

    private fun DocumentationBuilder.buildLineCommentContent(element: PsiElement) {
        //加上单行注释文本
        if (!PlsFacade.getSettings().documentation.renderLineComment) return
        val docText = ParadoxPsiManager.getLineCommentText(element, "<br>")
        if (docText.isNotNullOrEmpty()) {
            content {
                append(docText)
            }
        }
    }
}
