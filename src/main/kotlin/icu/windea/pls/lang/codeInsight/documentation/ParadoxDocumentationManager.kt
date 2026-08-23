package icu.windea.pls.lang.codeInsight.documentation

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.base.settings.ChronicleSettings
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.util.OnceMarker
import icu.windea.pls.core.util.builders.DocumentationBuilder
import icu.windea.pls.core.util.builders.buildDocumentation
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.defineInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.index.constraints.ParadoxLocalisationIndexConstraint
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxPsiService
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.lang.psi.light.ParadoxMeshLocatorLightElement
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.psi.light.ParadoxShaderEffectLightElement
import icu.windea.pls.lang.resolve.CwtImageLocationResolveResult
import icu.windea.pls.lang.resolve.CwtLocalisationLocationResolveResult
import icu.windea.pls.lang.resolve.ParadoxDefinitionService
import icu.windea.pls.lang.resolve.ParadoxLocationExpressionService
import icu.windea.pls.lang.resolve.ParadoxModifierCategoryService
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.search.util.withConstraint
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxDynamicValueManager
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxLocalisationArgumentManager
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextQuickDocRenderer
import icu.windea.pls.localisation.psi.ParadoxLocalisationArgument
import icu.windea.pls.localisation.psi.ParadoxLocalisationIconArgument
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefineInfo
import icu.windea.pls.model.ParadoxDefineNamespaceInfo
import icu.windea.pls.model.ParadoxDefineVariableInfo
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.ReferenceLinkType
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

@Suppress("unused")
object ParadoxDocumentationManager {
    private const val SECTIONS_INFO = 0
    private const val SECTIONS_IMAGES = 1
    private const val SECTIONS_LOC = 2
    private const val SECTIONS_EXTRA = 3

    // region Entry Documentation Methods

    fun compute(element: PsiElement, originalElement: PsiElement?, hint: Boolean): DocumentationBuilder? {
        return buildDocumentation(hint) { build(element, originalElement) }.takeUnless { it.isEmpty() }
    }

    private fun DocumentationBuilder.build(element: PsiElement, originalElement: PsiElement?) {
        when (element) {
            is ParadoxComplexEnumValueLightElement -> buildForComplexEnumValue(element, originalElement)
            is ParadoxDynamicValueLightElement -> buildForDynamicValue(element, originalElement)
            is ParadoxParameterLightElement -> buildForParameter(element, originalElement)
            is ParadoxLocalisationParameterLightElement -> buildForLocalisationParameter(element, originalElement)
            is ParadoxModifierLightElement -> buildForModifier(element, originalElement)
            is ParadoxShaderEffectLightElement -> buildForShaderEffect(element, originalElement)
            is ParadoxMeshLocatorLightElement -> buildForMeshLocator(element, originalElement)
            is ParadoxScriptScriptedVariable -> buildForScriptedVariable(element, originalElement)
            is ParadoxScriptProperty -> buildForScriptProperty(element, originalElement)
            is ParadoxScriptFile -> buildForScriptFile(element, originalElement)
            is ParadoxScriptPropertyKey -> build(element.parent, originalElement)
            is ParadoxLocalisationLocale -> buildForLocalisationLocale(element, originalElement)
            is ParadoxLocalisationProperty -> buildForLocalisationProperty(element, originalElement)
            is ParadoxLocalisationArgument -> buildForLocalisationArgument(element, originalElement)
        }
    }

    // endregion

    // region Dispatch Documentation Methods

    private fun DocumentationBuilder.buildForComplexEnumValue(element: ParadoxComplexEnumValueLightElement, originalElement: PsiElement?) {
        buildDefinitionPartForComplexEnumValue(element)
        if (hint) return
        buildDocumentationContent(element)
        buildSections()
    }

    private fun DocumentationBuilder.buildForDynamicValue(element: ParadoxDynamicValueLightElement, originalElement: PsiElement?) {
        buildDefinitionPartForDynamicValue(element)
        if (hint) return
        buildDocumentationContent(element)
        buildSections()
    }

    private fun DocumentationBuilder.buildForParameter(element: ParadoxParameterLightElement, originalElement: PsiElement?) {
        buildDefinitionPartForParameter(element)
        if (hint) return
        buildDocumentationContent(element)
    }

    private fun DocumentationBuilder.buildForLocalisationParameter(element: ParadoxLocalisationParameterLightElement, originalElement: PsiElement?) {
        buildDefinitionPartForLocalisationParameter(element)
    }

    private fun DocumentationBuilder.buildForModifier(element: ParadoxModifierLightElement, originalElement: PsiElement?) {
        buildDefinitionPartForModifier(element)
        if (hint) return
        buildSections()
    }

    private fun DocumentationBuilder.buildForShaderEffect(element: ParadoxShaderEffectLightElement, originalElement: PsiElement?) {
        buildDefinitionPartForShaderEffect(element)
    }

    private fun DocumentationBuilder.buildForMeshLocator(element: ParadoxMeshLocatorLightElement, originalElement: PsiElement?) {
        buildDefinitionPartForMeshLocator(element)
    }

    private fun DocumentationBuilder.buildForScriptedVariable(element: ParadoxScriptScriptedVariable, originalElement: PsiElement?) {
        val name = element.name ?: return
        buildDefinitionPartForScriptedVariable(element, name)
        if (hint) return
        buildDocumentationContent(element)
        buildLineCommentContent(element)
        addOverrideStrategy(element)
        buildSections()
    }

    private fun DocumentationBuilder.buildForScriptProperty(element: ParadoxScriptProperty, originalElement: PsiElement?) {
        run {
            val definitionInfo = element.definitionInfo ?: return@run
            return buildForDefinition(element, definitionInfo, originalElement)
        }
        run {
            val defineInfo = element.defineInfo ?: return@run
            return buildForDefine(element, defineInfo, originalElement)
        }

        val name = element.name
        buildDefinitionPartForProperty(element, name)
        if (hint) return
        buildLineCommentContent(element)
    }

    private fun DocumentationBuilder.buildForScriptFile(element: ParadoxScriptFile, originalElement: PsiElement?) {
        run {
            val definitionInfo = element.definitionInfo ?: return@run
            return buildForDefinition(element, definitionInfo, originalElement)
        }
        run {
            val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(element) ?: return@run
            return buildForInlineScript(element, inlineScriptExpression, originalElement)
        }
        // nothing now
    }

    private fun DocumentationBuilder.buildForDefinition(element: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo, originalElement: PsiElement?) {
        // 忽略内联或注入的定义
        if (definitionInfo.source == ParadoxDefinitionSource.Inline || definitionInfo.source == ParadoxDefinitionSource.Injection) return

        // 注意：对于相关图片的信息，在 definition 部分显示在相关本地化的信息之后，在 sections 部分则显示在之前
        buildDefinitionPartForDefinition(element, definitionInfo)
        if (hint) return
        buildDocumentationContent(element)
        buildLineCommentContent(element)
        addOverrideStrategy(element)
        buildSections()
    }

    private fun DocumentationBuilder.buildForDefine(element: ParadoxScriptProperty, defineInfo: ParadoxDefineInfo, originalElement: PsiElement?) {
        buildDefinitionPartForDefine(element, defineInfo)
        if (hint) return
        buildDocumentationContent(element)
        buildLineCommentContent(element)
        addOverrideStrategy(element)
        buildSections()
    }

    private fun DocumentationBuilder.buildForInlineScript(element: ParadoxScriptFile, expression: String, originalElement: PsiElement?) {
        buildDefinitionPartForInlineScript(element, expression)
        if (hint) return
        buildDocumentationContent(element)
        buildSections()
    }

    private fun DocumentationBuilder.buildForLocalisationProperty(element: ParadoxLocalisationProperty, originalElement: PsiElement?) {
        run {
            val localisationType = element.type ?: return@run
            return buildForLocalisation(element, localisationType, originalElement)
        }
        buildDefinitionPartForLocalisationProperty(element)
    }

    private fun DocumentationBuilder.buildForLocalisationLocale(element: ParadoxLocalisationLocale, originalElement: PsiElement?) {
        val name = element.name
        buildDefinitionPartForLocalisationLocale(name)
    }

    private fun DocumentationBuilder.buildForLocalisation(element: ParadoxLocalisationProperty, localisationType: ParadoxLocalisationType, originalElement: PsiElement?) {
        buildDefinitionPartForLocalisation(element, localisationType)
        if (hint) return
        buildLineCommentContent(element)
        addTextForLocalisation(element)
        addOverrideStrategy(element)
        buildSections()
    }

    private fun DocumentationBuilder.buildForLocalisationArgument(element: ParadoxLocalisationArgument, originalElement: PsiElement?) {
        if (hint) return // skip for hint
        if (element is ParadoxLocalisationIconArgument) return // skip for icon frames
        buildLocalisationArgumentInfo(element)
        buildSections()
    }

    // endregion

    // region Implementation Documentation Methods

    private fun DocumentationBuilder.buildDefinitionPartForComplexEnumValue(element: ParadoxComplexEnumValueLightElement) {
        definition {
            val name = element.name
            val enumName = element.enumName
            val gameType = element.gameType
            val configGroup = ChronicleFacade.getConfigGroup(element.project, gameType)
            append(ChronicleStrings.complexEnumValuePrefix).append(" <b>").append(name.escapeXml().or.anonymous()).append("</b>")
            val complexEnumConfig = configGroup.complexEnums[enumName]
            if (complexEnumConfig != null) {
                val category = ReferenceLinkType.CwtConfig.Categories.complexEnums
                val typeLink = ReferenceLinkType.CwtConfig.createLink(category, enumName, gameType)
                append(": ").psiLinkOrUnresolved(typeLink.escapeXml(), enumName.escapeXml())
            } else {
                append(": ").append(enumName)
            }

            // 加上相关本地化的信息：同名的本地化
            addRelatedLocalisationsForComplexEnumValue(element)

            // 加上作用域上下文信息
            addScopeContext(element, name, configGroup)
        }
    }

    private fun DocumentationBuilder.addRelatedLocalisationsForComplexEnumValue(element: ParadoxComplexEnumValueLightElement) {
        val gameType = element.gameType
        val usedLocale = ParadoxLocaleManager.getResolvedLocaleConfigInDocumentation(element)
        val nameLocalisation = ParadoxComplexEnumValueManager.getNameLocalisation(element.name, element, usedLocale)
        // 如果没找到的话，不要在文档中显示相关信息
        run {
            if (nameLocalisation == null) return@run
            br()
            append(ChronicleStrings.relatedLocalisationPrefix).append(" ")
            val link = ReferenceLinkType.Localisation.createLink(nameLocalisation.name, gameType)
            append("name = ").psiLinkOrUnresolved(link.escapeXml(), nameLocalisation.name.escapeXml(), context = element)
        }
        if (hint) return
        if (!ChronicleSettings.getInstance().state.documentation.renderRelatedLocalisationsForComplexEnumValues) return
        val sections = getSections(SECTIONS_LOC)
        run {
            if (nameLocalisation == null) return@run
            sections["name"] = ParadoxLocalisationTextQuickDocRenderer().render(nameLocalisation)
        }
    }

    private fun DocumentationBuilder.buildDefinitionPartForDynamicValue(element: ParadoxDynamicValueLightElement) {
        val name = element.name
        val dynamicValueTypes = element.types
        val gameType = element.gameType
        val configGroup = ChronicleFacade.getConfigGroup(element.project, gameType)
        definition {
            append(ChronicleStrings.dynamicValuePrefix).append(" <b>").append(name.escapeXml().or.anonymous()).append("</b>")
            append(": ")
            val m = OnceMarker()
            for (dynamicValueType in dynamicValueTypes) {
                if (m.mark()) append(" | ")
                val valueConfig = configGroup.dynamicValueTypes[dynamicValueType]
                if (valueConfig != null) {
                    val category = ReferenceLinkType.CwtConfig.Categories.values
                    val typeLink = ReferenceLinkType.CwtConfig.createLink(category, dynamicValueType, gameType)
                    psiLinkOrUnresolved(typeLink.escapeXml(), dynamicValueType.escapeXml())
                } else {
                    append(dynamicValueType)
                }
            }

            // 加上相关本地化的信息：同名的本地化
            addRelatedLocalisationsForDynamicValue(element)

            // 加上作用域上下文信息
            addScopeContext(element, name, configGroup)
        }
    }

    private fun DocumentationBuilder.addRelatedLocalisationsForDynamicValue(element: ParadoxDynamicValueLightElement) {
        val gameType = element.gameType
        val usedLocale = ParadoxLocaleManager.getResolvedLocaleConfigInDocumentation(element)
        val nameLocalisation = ParadoxDynamicValueManager.getNameLocalisation(element.name, element, usedLocale)
        // 如果没找到的话，不要在文档中显示相关信息
        run {
            if (nameLocalisation == null) return@run
            br()
            append(ChronicleStrings.relatedLocalisationPrefix).append(" ")
            val link = ReferenceLinkType.Localisation.createLink(nameLocalisation.name, gameType)
            append("name = ").psiLinkOrUnresolved(link.escapeXml(), nameLocalisation.name.escapeXml(), context = element)
        }
        if (hint) return
        if (!ChronicleSettings.getInstance().state.documentation.renderRelatedLocalisationsForDynamicValues) return
        val sections = getSections(SECTIONS_LOC)
        run {
            if (nameLocalisation == null) return@run
            sections["name"] = ParadoxLocalisationTextQuickDocRenderer().render(nameLocalisation)
        }
    }

    private fun DocumentationBuilder.buildDefinitionPartForParameter(element: ParadoxParameterLightElement) {
        val name = element.name
        definition {
            run {
                // 显示来自 EP 的快速文档
                val r = ParadoxDocumentationService.buildDefinitionPart(element, this)
                if (r) return@run
                // 显示默认的快速文档
                append(ChronicleStrings.parameterPrefix).append(" <b>").append(name.escapeXml().or.anonymous()).append("</b>")
            }
        }
    }

    private fun DocumentationBuilder.buildDefinitionPartForLocalisationParameter(element: ParadoxLocalisationParameterLightElement) {
        val name = element.name
        definition {
            run {
                // 显示来自 EP 的快速文档
                val r = ParadoxDocumentationService.buildDefinitionPart(element, this)
                if (r) return@run
                // 显示默认的快速文档
                append(ChronicleStrings.parameterPrefix).append(" <b>").append(name.escapeXml().or.anonymous()).append("</b>")
            }
        }
    }

    private fun DocumentationBuilder.buildDefinitionPartForModifier(element: ParadoxModifierLightElement) {
        val name = element.name
        definition {
            run {
                // 显示来自 EP 的快速文档
                val r = ParadoxDocumentationService.buildDefinitionPart(element, this)
                if (r) return@run
                // 显示默认的快速文档
                append(ChronicleStrings.modifierPrefix).append(" <b>").append(name.escapeXml().or.anonymous()).append("</b>")
            }

            val configGroup = ChronicleFacade.getConfigGroup(element.project, element.gameType)
            addModifierRelatedLocalisations(element, name, configGroup)
            addModifierIcon(element, name, configGroup)
            addModifierScope(element, name, configGroup)
            addScopeContext(element, name, configGroup)
        }
    }

    private fun DocumentationBuilder.addModifierRelatedLocalisations(element: ParadoxModifierLightElement, name: String, configGroup: CwtConfigGroup) {
        val gameType = configGroup.gameType
        val project = configGroup.project
        val usedLocale = ParadoxLocaleManager.getResolvedLocaleConfigInDocumentation(element)
        val nameLocalisation = run {
            val keys = ParadoxModifierManager.getModifierNameKeys(name, element)
            keys.firstNotNullOfOrNull { key ->
                val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive()
                    .preferLocale(usedLocale)
                    .withConstraint(ParadoxLocalisationIndexConstraint.Modifier) // so ignore case
                ParadoxLocalisationSearch.searchNormal(key, selector).find()
            }
        }
        val descLocalisation = run {
            val keys = ParadoxModifierManager.getModifierDescKeys(name, element)
            keys.firstNotNullOfOrNull { key ->
                val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive()
                    .preferLocale(usedLocale)
                    .withConstraint(ParadoxLocalisationIndexConstraint.Modifier) // so ignore case
                ParadoxLocalisationSearch.searchNormal(key, selector).find()
            }
        }
        // 如果没找到的话，不要在文档中显示相关信息
        run {
            if (nameLocalisation == null) return@run
            br()
            append(ChronicleStrings.relatedLocalisationPrefix).append(" ")
            val link = ReferenceLinkType.Localisation.createLink(nameLocalisation.name, gameType)
            append("name = ").psiLinkOrUnresolved(link.escapeXml(), nameLocalisation.name.escapeXml(), context = element)
        }
        run {
            if (descLocalisation == null) return@run
            br()
            append(ChronicleStrings.relatedLocalisationPrefix).append(" ")
            val link = ReferenceLinkType.Localisation.createLink(descLocalisation.name, gameType)
            append("desc = ").psiLinkOrUnresolved(link.escapeXml(), descLocalisation.name.escapeXml(), context = element)
        }
        if (hint) return
        if (!ChronicleSettings.getInstance().state.documentation.renderNameDescForModifiers) return
        val sections = getSections(SECTIONS_LOC)
        run {
            if (nameLocalisation == null) return@run
            sections["name"] = ParadoxLocalisationTextQuickDocRenderer().render(nameLocalisation)
        }
        run {
            if (descLocalisation == null) return@run
            sections["desc"] = ParadoxLocalisationTextQuickDocRenderer().render(descLocalisation)
        }
    }

    private fun DocumentationBuilder.addModifierIcon(element: ParadoxModifierLightElement, name: String, configGroup: CwtConfigGroup) {
        val gameType = configGroup.gameType
        val project = configGroup.project
        val iconFile = run {
            val paths = ParadoxModifierManager.getModifierIconPaths(name, element)
            paths.firstNotNullOfOrNull { path ->
                val iconSelector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
                ParadoxFilePathSearch.searchModifierIcon(path, iconSelector).find()
            }
        }
        // 如果没找到的话，不要在文档中显示相关信息
        run {
            if (iconFile == null) return@run
            val iconPath = iconFile.fileInfo?.path?.path ?: return@run
            br()
            append(ChronicleStrings.relatedImagePrefix).append(" ")
            val link = ReferenceLinkType.FilePath.createLink(iconPath, gameType)
            append("icon = ").psiLinkOrUnresolved(link.escapeXml(), iconPath.escapeXml(), context = element)
        }
        if (hint) return
        if (!ChronicleSettings.getInstance().state.documentation.renderIconForModifiers) return
        val sections = getSections(SECTIONS_IMAGES)
        run {
            if (iconFile == null) return@run
            val url = ParadoxImageManager.resolveUrlByFile(iconFile, project) ?: return@run
            sections["icon"] = buildDocumentation { image(url) }.toString()
        }
    }

    private fun DocumentationBuilder.addModifierScope(element: ParadoxModifierLightElement, name: String, configGroup: CwtConfigGroup) {
        if (hint) return
        if (!ChronicleSettings.getInstance().state.documentation.showScopes) return

        // 即使是在 CWT 文件中，如果可以推断得到规则分组，也显示作用域信息

        val sections = getSections(SECTIONS_INFO)
        val gameType = configGroup.gameType
        val modifierCategories = ParadoxModifierCategoryService.getModifierCategories(element) ?: return
        val contextElement = element
        val categoryNames = modifierCategories.keys
        if (categoryNames.isNotEmpty()) {
            sections[ChronicleBundle.message("doc.sectionTitle.categories")] = getModifierCategoriesText(categoryNames, gameType, contextElement)
        }

        val supportedScopes = ParadoxScopeManager.getSupportedScopes(modifierCategories)
        sections[ChronicleBundle.message("doc.sectionTitle.supportedScopes")] = getScopesText(supportedScopes, gameType, contextElement)
    }

    private fun DocumentationBuilder.buildDefinitionPartForShaderEffect(element: ParadoxShaderEffectLightElement) {
        definition {
            append(ChronicleStrings.shaderEffectPrefix).append(" <b>").append(element.name.escapeXml().or.anonymous()).append("</b>")
        }
    }

    private fun DocumentationBuilder.buildDefinitionPartForMeshLocator(element: ParadoxMeshLocatorLightElement) {
        definition {
            append(ChronicleStrings.meshLocatorPrefix).append(" <b>").append(element.name.escapeXml().or.anonymous()).append("</b>")
        }
    }

    private fun DocumentationBuilder.buildDefinitionPartForScriptedVariable(element: ParadoxScriptScriptedVariable, name: String) {
        definition {
            // 加上文件信息
            fileInfoHeader(element)
            // 加上基本信息
            append(ChronicleStrings.scriptedVariablePrefix).append(" <b>@").append(name.escapeXml().or.anonymous()).append("</b>")
            val valueElement = element.scriptedVariableValue
            if (valueElement != null) append(" = ").append(valueElement.presentableText.escapeXml())

            // 加上相关本地化的信息：同名的本地化
            addRelatedLocalisationsForScriptedVariable(element, name)
        }
    }

    private fun DocumentationBuilder.addRelatedLocalisationsForScriptedVariable(element: ParadoxScriptScriptedVariable, name: String) {
        val gameType = selectGameType(element) ?: return
        val usedLocale = ParadoxLocaleManager.getResolvedLocaleConfigInDocumentation(element)
        val nameLocalisation = ParadoxScriptedVariableManager.getNameLocalisation(name, element, usedLocale)
        // 如果没找到的话，不要在文档中显示相关信息
        run {
            if (nameLocalisation == null) return@run
            br()
            append(ChronicleStrings.relatedLocalisationPrefix).append(" ")
            val link = ReferenceLinkType.Localisation.createLink(nameLocalisation.name, gameType)
            append("name = ").psiLinkOrUnresolved(link.escapeXml(), nameLocalisation.name.escapeXml(), context = element)
        }
        if (hint) return
        if (!ChronicleSettings.getInstance().state.documentation.renderRelatedLocalisationsForScriptedVariables) return
        val sections = getSections(SECTIONS_LOC)
        run {
            if (nameLocalisation == null) return@run
            sections["name"] = ParadoxLocalisationTextQuickDocRenderer().render(nameLocalisation)
        }
    }

    private fun DocumentationBuilder.buildDefinitionPartForProperty(element: ParadoxScriptProperty, name: String) {
        definition {
            // 加上文件信息
            fileInfoHeader(element)
            // 加上基本信息
            append(ChronicleStrings.propertyPrefix).append(" <b>").append(name.escapeXml().or.anonymous()).append("</b>")
            val valueElement = element.propertyValue
            if (valueElement != null) append(" = ").append(valueElement.presentableText.escapeXml())
        }
    }

    private fun DocumentationBuilder.buildDefinitionPartForDefinition(element: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
        definition {
            // 加上文件信息
            fileInfoHeader(element)
            // 加上定义信息
            addDefinitionInfo(element, definitionInfo)

            // 加上继承的定义信息
            val superDefinition = ParadoxDefinitionService.getSuperDefinition(definitionInfo)
            val superDefinitionInfo = superDefinition?.definitionInfo
            if (superDefinitionInfo != null) {
                br()
                addSuperDefinitionInfo(superDefinition, superDefinitionInfo)
            }

            // 加上相关本地化的信息：去重后的一组本地化的键名，不包括可选且没有对应的本地化的项，按解析顺序排序
            addRelatedLocalisationsForDefinition(element, definitionInfo)
            // 加上相关图片的信息：去重后的一组图片的 ID（定义名或文件路径），不包括可选且没有对应的图片的项，按解析顺序排序
            addRelatedImagesForDefinition(element, definitionInfo)

            // 加上生成的修正的信息
            addGeneratedModifiersForDefinition(element, definitionInfo)

            // 加上修正分类和作用域信息（如果支持）
            addModifierScopeForDefinition(element, definitionInfo)
            // 加上作用域上下文信息（如果支持）
            addScopeContextForDefinition(element, definitionInfo)

            // 加上参数信息（如果支持且存在）
            addParameters(element)

            // 加上事件类型信息（对于on_action）
            addEventTypeForOnAction(element, definitionInfo)
        }
    }

    private fun DocumentationBuilder.addDefinitionInfo(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo, usePrefix: String? = null) {
        val gameType = definitionInfo.gameType
        val categories = ReferenceLinkType.CwtConfig.Categories
        val prefix = usePrefix ?: ChronicleStrings.definitionPrefix
        append(prefix).append(" ")
        val name = definitionInfo.name
        if (usePrefix == null) {
            append("<b>").append(name.escapeXml().or.anonymous()).append("</b>")
        } else {
            val link = ReferenceLinkType.Definition.createLink(name, definitionInfo.type, gameType)
            psiLinkOrUnresolved(link.escapeXml(), name.escapeXml().or.anonymous(), context = definition)
        }
        append(": ")
        val typeConfig = definitionInfo.typeConfig
        val typeLink = ReferenceLinkType.CwtConfig.createLink(categories.types, typeConfig.name, gameType)
        psiLinkOrUnresolved(typeLink.escapeXml(), typeConfig.name.escapeXml())
        val subtypeConfigs = definitionInfo.subtypeConfigs
        if (subtypeConfigs.isNotEmpty()) {
            for (subtypeConfig in subtypeConfigs) {
                val subtypeLink = ReferenceLinkType.CwtConfig.createLink(categories.types, "${typeConfig.name}/${subtypeConfig.name}", gameType)
                append(", ").psiLinkOrUnresolved(subtypeLink.escapeXml(), subtypeConfig.name.escapeXml())
            }
        }
    }

    private fun DocumentationBuilder.addSuperDefinitionInfo(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
        val gameType = definitionInfo.gameType
        val categories = ReferenceLinkType.CwtConfig.Categories
        indent().append(ChronicleBundle.message("doc.text.inherits")).append(" ")
        val name = definitionInfo.name
        val link = ReferenceLinkType.Definition.createLink(name, definitionInfo.type, gameType)
        psiLinkOrUnresolved(link.escapeXml(), name.escapeXml().or.anonymous(), context = definition)
        append(": ")
        val typeConfig = definitionInfo.typeConfig
        val typeLink = ReferenceLinkType.CwtConfig.createLink(categories.types, typeConfig.name, gameType)
        psiLinkOrUnresolved(typeLink.escapeXml(), typeConfig.name.escapeXml())
        val subtypeConfigs = definitionInfo.subtypeConfigs
        if (subtypeConfigs.isNotEmpty()) {
            for (subtypeConfig in subtypeConfigs) {
                val subtypeLink = ReferenceLinkType.CwtConfig.createLink(categories.types, "${typeConfig.name}/${subtypeConfig.name}", gameType)
                append(", ").psiLinkOrUnresolved(subtypeLink.escapeXml(), subtypeConfig.name.escapeXml())
            }
        }
    }

    private fun DocumentationBuilder.addRelatedLocalisationsForDefinition(element: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
        val render = ChronicleSettings.getInstance().state.documentation.renderRelatedLocalisationsForDefinitions
        val localisationInfos = definitionInfo.localisations
        if (localisationInfos.isEmpty()) return
        val usedLocale = ParadoxLocaleManager.getResolvedLocaleConfigInDocumentation(element)
        val map = mutableMapOf<String, String>()
        val sections = getSections(SECTIONS_LOC)
        val sectionKeys = mutableSetOf<String>()
        for ((key, locationExpression, required) in localisationInfos) {
            if (sectionKeys.contains(key)) continue
            ProgressManager.checkCanceled()
            val resolveResult = ParadoxLocationExpressionService.resolve(locationExpression, element, definitionInfo) { preferLocale(usedLocale) } ?: continue // 发生意外，直接跳过
            when (resolveResult) {
                is CwtLocalisationLocationResolveResult.Static -> {
                    val resolvedElement = resolveResult.element
                    if (resolvedElement != null) {
                        map[key] = buildDocumentation {
                            val link = ReferenceLinkType.Localisation.createLink(resolveResult.name, definitionInfo.gameType)
                            psiLinkOrUnresolved(link.escapeXml(), resolveResult.name.escapeXml(), context = element)
                        }.toString()
                        sectionKeys.add(key)
                        if (!hint && render) {
                            // 加上渲染后的相关本地化文本
                            sections[key] = ParadoxLocalisationTextQuickDocRenderer().render(resolvedElement)
                        }
                    } else if (required) {
                        map.putIfAbsent(key, resolveResult.name.escapeXml())
                    }
                }
                is CwtLocalisationLocationResolveResult.Dynamic -> {
                    map[key] = resolveResult.message.escapeXml()
                }
            }
        }
        for ((key, value) in map) {
            br()
            append(ChronicleStrings.relatedLocalisationPrefix).append(" ").append(key).append(" = ").append(value)
        }
    }

    private fun DocumentationBuilder.addRelatedImagesForDefinition(element: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
        val render = ChronicleSettings.getInstance().state.documentation.renderRelatedImagesForDefinitions
        val imagesInfos = definitionInfo.images
        if (imagesInfos.isEmpty()) return
        val map = mutableMapOf<String, String>()
        val sections = getSections(SECTIONS_IMAGES)
        val sectionKeys = mutableSetOf<String>()
        for ((key, locationExpression, required) in imagesInfos) {
            if (sectionKeys.contains(key)) continue
            ProgressManager.checkCanceled()
            val resolveResult = ParadoxLocationExpressionService.resolve(locationExpression, element, definitionInfo) ?: continue // 发生意外，直接跳过
            when (resolveResult) {
                is CwtImageLocationResolveResult.Static -> {
                    val resolvedElement = resolveResult.element
                    if (resolvedElement != null) {
                        val name = resolveResult.name
                        val gameType = definitionInfo.gameType
                        map[key] = when {
                            name.startsWith("GFX") -> buildDocumentation {
                                val link = ReferenceLinkType.Definition.createLink(name, ParadoxDefinitionTypes.sprite, gameType)
                                psiLinkOrUnresolved(link.escapeXml(), name.escapeXml(), context = element)
                            }.toString()
                            else -> buildDocumentation {
                                val link = ReferenceLinkType.FilePath.createLink(name, gameType)
                                psiLinkOrUnresolved(link.escapeXml(), name.escapeXml(), context = element)
                            }.toString()
                        }
                        sectionKeys.add(key)
                        if (!hint && render) {
                            // 渲染图片
                            val url = when {
                                resolvedElement is ParadoxDefinitionElement && resolvedElement.definitionInfo != null -> {
                                    ParadoxImageManager.resolveUrlByDefinition(resolvedElement, resolveResult.frameInfo)
                                }
                                resolvedElement is PsiFile -> {
                                    ParadoxImageManager.resolveUrlByFile(resolvedElement.virtualFile, resolvedElement.project, resolveResult.frameInfo)
                                }
                                else -> null
                            }
                            if (url != null) {
                                sections[key] = buildDocumentation { image(url) }.toString()
                            }
                        }
                    } else if (required) {
                        map.putIfAbsent(key, resolveResult.name.escapeXml())
                    }
                }
                is CwtImageLocationResolveResult.Dynamic -> {
                    map[key] = resolveResult.message.escapeXml()
                }
            }
        }
        for ((key, value) in map) {
            br()
            append(ChronicleStrings.relatedImagePrefix).append(" ").append(key).append(" = ").append(value)
        }
    }

    private fun DocumentationBuilder.addGeneratedModifiersForDefinition(element: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
        if (!ChronicleSettings.getInstance().state.documentation.showGeneratedModifiers) return

        ParadoxDocumentationService.buildDefinitionPartForDefinition(element, definitionInfo, this)
    }

    private fun DocumentationBuilder.addModifierScopeForDefinition(element: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
        if (hint) return
        if (!ChronicleSettings.getInstance().state.documentation.showScopes) return

        // 即使是在 CWT 文件中，如果可以推断得到规则分组，也显示作用域信息

        val sections = getSections(SECTIONS_INFO)
        val gameType = definitionInfo.gameType
        val modifierCategories = ParadoxModifierCategoryService.getModifierCategories(definitionInfo) ?: return
        val categoryNames = modifierCategories.keys
        if (categoryNames.isNotEmpty()) {
            sections[ChronicleBundle.message("doc.sectionTitle.categories")] = getModifierCategoriesText(categoryNames, gameType, element)
        }

        val supportedScopes = ParadoxScopeManager.getSupportedScopes(modifierCategories)
        sections[ChronicleBundle.message("doc.sectionTitle.supportedScopes")] = getScopesText(supportedScopes, gameType, element)
    }

    private fun DocumentationBuilder.addScopeContextForDefinition(element: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
        if (element !is ParadoxScriptProperty) return

        // 进行代码提示时也显示作用域上下文信息
        // @Suppress("DEPRECATION")
        // if (DocumentationManager.IS_FROM_LOOKUP.get(element) == true) return

        if (hint) return
        if (!ChronicleSettings.getInstance().state.documentation.showScopeContext) return

        val sections = getSections(SECTIONS_INFO)
        val gameType = definitionInfo.gameType
        if (!ParadoxScopeManager.isScopeContextSupported(element, indirect = true)) return
        val scopeContext = ParadoxScopeManager.getScopeContext(element)
        if (scopeContext == null) return
        sections[ChronicleBundle.message("doc.sectionTitle.scopeContext")] = getScopeContextText(scopeContext, gameType, element)
    }

    private fun DocumentationBuilder.addEventTypeForOnAction(element: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
        if (element !is ParadoxScriptProperty) return
        if (definitionInfo.type != ParadoxDefinitionTypes.onAction) return
        // 有些游戏类型直接通过 CWT 文件指定了事件类型，而非 CSV 文件，忽略这种情况
        val configGroup = definitionInfo.configGroup
        val gameType = configGroup.gameType
        val config = definitionInfo.configGroup.extendedOnActions.findByPattern(definitionInfo.name, element, configGroup)
        if (config == null) return
        val eventType = config.eventType
        br()
        val categories = ReferenceLinkType.CwtConfig.Categories
        val typeLink = ReferenceLinkType.CwtConfig.createLink(categories.types, "event/$eventType", gameType)
        append(ChronicleStrings.eventTypePrefix).append(" ").psiLinkOrUnresolved(typeLink.escapeXml(), eventType.escapeXml())
    }

    private fun DocumentationBuilder.buildDefinitionPartForDefine(element: ParadoxScriptProperty, defineInfo: ParadoxDefineInfo) {
        definition {
            // 加上文件信息
            fileInfoHeader(element)
            // 加上基本信息
            when (defineInfo) {
                is ParadoxDefineNamespaceInfo -> {
                    append(ChronicleStrings.defineNamespacePrefix)
                    append(" <b>").append(defineInfo.namespace.escapeXml()).append("</b>")
                }
                is ParadoxDefineVariableInfo -> {
                    append(ChronicleStrings.defineVariablePrefix)
                    append(" <b>").append(defineInfo.namespace.escapeXml()).append(".").append(defineInfo.variable.escapeXml()).append("</b>")
                }
            }
        }
    }

    private fun DocumentationBuilder.buildDefinitionPartForInlineScript(element: ParadoxScriptFile, expression: String) {
        definition {
            // 加上文件信息
            fileInfoHeader(element)
            // 加上基本信息
            append(ChronicleStrings.inlineScriptPrefix).append(" <b>").append(expression.escapeXml()).append("</b>")

            // 加上参数信息（如果存在）
            addParameters(element)
        }
    }

    private fun DocumentationBuilder.buildDefinitionPartForLocalisationLocale(name: String) {
        definition {
            // 加上元素定义信息
            append(ChronicleStrings.localePrefix).append(" <b>").append(name).append("</b>")
        }
    }

    private fun DocumentationBuilder.buildDefinitionPartForLocalisationProperty(element: ParadoxLocalisationProperty) {
        definition {
            // 加上文件信息
            fileInfoHeader(element)
            // 加上元素定义信息
            append(ChronicleStrings.localisationPropertyPrefix).append(" <b>").append(element.name).append("</b>")
        }
    }

    private fun DocumentationBuilder.buildDefinitionPartForLocalisation(element: ParadoxLocalisationProperty, localisationType: ParadoxLocalisationType) {
        definition {
            // 加上文件信息
            fileInfoHeader(element)
            // 加上定义信息
            addLocalisationInfo(element, localisationType)

            // 加上相关定义信息
            addRelatedDefinitionsForLocalisation(element, localisationType)
        }
    }

    private fun DocumentationBuilder.addLocalisationInfo(element: ParadoxLocalisationProperty, localisationType: ParadoxLocalisationType) {
        val prefix = when (localisationType) {
            ParadoxLocalisationType.Normal -> ChronicleStrings.localisationPrefix
            ParadoxLocalisationType.Synced -> ChronicleStrings.localisationSyncedPrefix
        }
        append(prefix).append(" ")
        append("<b>").append(element.name.escapeXml()).append("</b>")
    }

    private fun DocumentationBuilder.addRelatedDefinitionsForLocalisation(element: ParadoxLocalisationProperty, localisationType: ParadoxLocalisationType) {
        val relatedDefinitions = ParadoxLocalisationManager.getRelatedDefinitions(element)
        if (relatedDefinitions.isEmpty()) return
        for (relatedDefinition in relatedDefinitions) {
            val relatedDefinitionInfo = relatedDefinition.definitionInfo ?: continue
            br()
            addDefinitionInfo(relatedDefinition, relatedDefinitionInfo, usePrefix = ChronicleStrings.relatedDefinitionPrefix)
        }
    }

    private fun DocumentationBuilder.addTextForLocalisation(element: ParadoxLocalisationProperty) {
        if(hint) return
        if (!ChronicleSettings.getInstance().state.documentation.renderLocalisationForLocalisations) return

        // 加上渲染后的本地化文本
        val sections = getSections(SECTIONS_LOC)
        val locale = selectLocale(element)
        val usedLocale = ParadoxLocaleManager.getResolvedLocaleConfigInDocumentation(element, locale)
        val usedElement = when {
            usedLocale == locale -> element
            else -> {
                val selector = ParadoxLocalisationSearch.selector(element.project, element).contextSensitive().preferLocale(usedLocale)
                val type = element.type
                val found = type?.let { type -> ParadoxLocalisationSearch.search(element.name, type, selector).find() }
                found ?: element
            }
        }
        val richText = ParadoxLocalisationTextQuickDocRenderer().render(usedElement)
        if (richText.isEmpty()) return
        sections[ChronicleBundle.message("doc.sectionTitle.text")] = richText
    }

    private fun DocumentationBuilder.buildLocalisationArgumentInfo(element: ParadoxLocalisationArgument) {
        if(hint) return
        val sections = getSections(SECTIONS_INFO)
        ParadoxLocalisationArgumentManager.getInfo(element).let {
            sections.put(ChronicleBundle.message("doc.sectionTitle.formattingTags"), it)
        }
    }

    private fun DocumentationBuilder.addParameters(element: ParadoxDefinitionElement) {
        if(hint) return
        if (!ChronicleSettings.getInstance().state.documentation.showParameters) return

        val sections = getSections(SECTIONS_INFO)
        val parameterContextInfo = ParadoxParameterManager.getContextInfo(element) ?: return
        if (parameterContextInfo.parameters.isEmpty()) return // ignore
        val parametersText = buildString {
            append("<pre>")
            val m = OnceMarker()
            parameterContextInfo.parameters.forEach f@{ (parameterName, elements) ->
                if (m.mark()) append("<br>")
                append(parameterName)
                val optional = ParadoxParameterManager.isOptional(parameterContextInfo, parameterName)
                if (optional) append("?") // optional marker
                // 加上推断得到的类型信息
                val parameterElement = elements.firstOrNull()?.parameterElement
                val inferredType = parameterElement?.let { ParadoxParameterManager.getInferredType(it) }
                if (inferredType.isNotNullOrEmpty()) append(": ").append(inferredType.escapeXml())
            }
            append("</pre>")
        }
        sections[ChronicleBundle.message("doc.sectionTitle.parameters")] = parametersText
    }

    private fun DocumentationBuilder.addScopeContext(element: PsiElement, name: String, configGroup: CwtConfigGroup) {
        // 进行代码提示时也显示作用域上下文信息
        // @Suppress("DEPRECATION")
        // if (DocumentationManager.IS_FROM_LOOKUP.get(element) == true) return

        if (hint) return
        if (!ChronicleSettings.getInstance().state.documentation.showScopeContext) return

        val sections = getSections(SECTIONS_INFO)
        val gameType = configGroup.gameType
        val memberElement = element.parentOfType<ParadoxScriptMember>(true) ?: return
        if (!ParadoxScopeManager.isScopeContextSupported(memberElement, indirect = true)) return
        val scopeContext = ParadoxScopeManager.getScopeContext(memberElement)
        if (scopeContext == null) return
        // TODO 如果作用域引用位于脚本表达式中，应当使用那个位置的作用域上下文，但是目前实现不了
        //  因为这里的 `referenceElement` 是整个 `stringExpression`，得到的作用域上下文会是脚本表达式最终的作用域上下文
        sections[ChronicleBundle.message("doc.sectionTitle.scopeContext")] = getScopeContextText(scopeContext, gameType, element)
    }

    private fun DocumentationBuilder.buildDocumentationContent(element: PsiElement) {
        val contents = ParadoxDocumentationService.listQuickDocText(element)
        if (contents.isEmpty()) return
        contents.forEach { content { append(it) } }
    }

    private fun DocumentationBuilder.buildLineCommentContent(element: PsiElement) {
        // 加上单行注释文本
        if (!ChronicleSettings.getInstance().state.documentation.renderLineComment) return
        val ownedComments = ParadoxPsiService.getOwnedComments(element)
        val commentText = ParadoxPsiService.getLineCommentText(ownedComments)
        if (commentText.isNullOrEmpty()) return
        content { append(commentText) }
    }

    private fun DocumentationBuilder.addOverrideStrategy(element: PsiElement) {
        if (hint) return
        if (!ChronicleSettings.getInstance().state.documentation.showOverrideStrategy) return

        val sections = getSections(SECTIONS_EXTRA)
        val overrideStrategy = ParadoxOverrideService.getOverrideStrategy(element) ?: return
        sections[ChronicleBundle.message("doc.sectionTitle.overrideStrategy")] = overrideStrategy.id + " - " + overrideStrategy.text
    }

    // endregion
}
