@file:Suppress("unused")

package icu.windea.pls.lang.documentation

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.aliasGroups
import icu.windea.pls.config.configGroup.aliasNamesSupportScope
import icu.windea.pls.config.configGroup.links
import icu.windea.pls.config.configGroup.localisationCommands
import icu.windea.pls.config.configGroup.localisationLinks
import icu.windea.pls.config.configGroup.localisationPromotions
import icu.windea.pls.config.configGroup.modifierCategories
import icu.windea.pls.config.configGroup.modifiers
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.documentation.DocumentationBuilder
import icu.windea.pls.core.documentation.buildDocumentation
import icu.windea.pls.core.documentation.buildSections
import icu.windea.pls.core.documentation.content
import icu.windea.pls.core.documentation.definition
import icu.windea.pls.core.documentation.getSections
import icu.windea.pls.core.documentation.grayed
import icu.windea.pls.core.documentation.initSections
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.isSamePosition
import icu.windea.pls.core.orNull
import icu.windea.pls.core.pass
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.substringIn
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtElementTypes
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.cwt.psi.isExpression
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.codeInsight.configType
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.mock.CwtConfigSymbolElement
import icu.windea.pls.lang.psi.mock.CwtMemberConfigElement
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withConstraint
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.psi.ParadoxPsiManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.lang.util.psi.PlsPsiManager
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextHtmlRenderer
import icu.windea.pls.model.ReferenceLinkType
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
import icu.windea.pls.script.psi.ParadoxScriptMemberElement
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptValue

object CwtDocumentationManager {
    private const val SECTIONS_INFO = 0
    private const val SECTIONS_IMAGES = 1
    private const val SECTIONS_LOC = 2

    fun computeLocalDocumentation(element: PsiElement, originalElement: PsiElement?, hint: Boolean): String? {
        return when (element) {
            is CwtConfigSymbolElement -> getConfigSymbolDoc(element, originalElement, hint)
            is CwtMemberConfigElement -> getMemberConfigDoc(element, originalElement, hint)
            is CwtProperty -> getPropertyDoc(element, originalElement, hint)
            is CwtString -> getStringDoc(element, originalElement, hint)
            else -> null
        }
    }

    private fun getConfigSymbolDoc(element: CwtConfigSymbolElement, originalElement: PsiElement?, hint: Boolean): String {
        return buildDocumentation {
            val name = element.name
            val configType = element.configType
            val project = element.project
            val configGroup = PlsFacade.getConfigGroup(project, element.gameType)
            buildConfigSymbolDefinition(element, originalElement, name, configType, configGroup)
        }
    }

    private fun getMemberConfigDoc(element: CwtMemberConfigElement, originalElement: PsiElement?, hint: Boolean): String {
        return buildDocumentation {
            val name = element.name
            val configType = null
            val project = element.project
            val configGroup = PlsFacade.getConfigGroup(project, element.gameType)
            if (!hint) initSections(3)
            buildPropertyOrStringDefinition(element, originalElement, name, configType, configGroup)
            if (hint) return@buildDocumentation
            buildDocumentationContent(element)
            buildSections()
        }
    }

    private fun getPropertyDoc(element: CwtProperty, originalElement: PsiElement?, hint: Boolean): String {
        return buildDocumentation {
            val name = element.name
            val configType = CwtConfigManager.getConfigType(element)
            val project = element.project
            val configGroup = getConfigGroup(element, originalElement, project)
            if (!hint) initSections(3)
            buildPropertyOrStringDefinition(element, originalElement, name, configType, configGroup)
            if (hint) return@buildDocumentation
            buildDocumentationContent(element)
            buildSections()
        }
    }

    private fun getStringDoc(element: CwtString, originalElement: PsiElement?, hint: Boolean): String? {
        //only for property value or block value
        if (!element.isExpression()) return null

        return buildDocumentation {
            val name = element.name
            val configType = element.configType
            val project = element.project
            val configGroup = getConfigGroup(element, originalElement, project)
            if (!hint) initSections(3)
            buildPropertyOrStringDefinition(element, originalElement, name, configType, configGroup)
            if (hint) return@buildDocumentation
            buildDocumentationContent(element)
            buildSections()
        }
    }

    private fun DocumentationBuilder.buildConfigSymbolDefinition(element: PsiElement, originalElement: PsiElement?, name: String, configType: CwtConfigType, configGroup: CwtConfigGroup) {
        definition {
            appendCwtConfigFileInfoHeader(element)

            val prefix = configType.prefix

            if (prefix != null) {
                append(prefix).append(" ")
            }
            append("<b>").append(name.escapeXml().or.anonymous()).append("</b>")
        }
    }

    private fun DocumentationBuilder.buildPropertyOrStringDefinition(element: PsiElement, originalElement: PsiElement?, name: String, configType: CwtConfigType?, configGroup: CwtConfigGroup?) {
        definition {
            appendCwtConfigFileInfoHeader(element)

            val bindingConfig = element.getUserData(PlsKeys.bindingConfig)
            val tagName = bindingConfig?.castOrNull<CwtValueConfig>()?.tagType
            val referenceElement = PlsPsiManager.getReferenceElement(originalElement)

            val shortName = configType?.let { CwtConfigManager.getNameByConfigType(name, it) } ?: name
            val byName = if (shortName == name) null else name
            val prefix = when {
                referenceElement != null && tagName != null -> "(${tagName.id})" //处理特殊标签
                configType?.isReference == true -> configType.prefix
                referenceElement is ParadoxScriptPropertyKey -> PlsStringConstants.definitionPropertyPrefix
                referenceElement is ParadoxScriptValue -> PlsStringConstants.definitionValuePrefix
                element is CwtMemberConfigElement && element.config is CwtPropertyConfig -> PlsStringConstants.definitionPropertyPrefix
                element is CwtMemberConfigElement && element.config is CwtValueConfig -> PlsStringConstants.definitionValuePrefix
                else -> configType?.prefix
            }
            val finalPrefix = when {
                prefix != null -> prefix
                element is CwtProperty -> PlsStringConstants.propertyPrefix
                element is CwtString -> PlsStringConstants.stringPrefix
                else -> null
            }

            if (finalPrefix != null) {
                append(finalPrefix).append(" ")
            }
            append("<b>").append(shortName.escapeXml().or.anonymous()).append("</b>")
            if (configType?.category != null) {
                val typeElement = element.parentOfType<CwtProperty>()
                val typeName = typeElement?.name?.substringIn('[', ']')?.orNull()
                if (typeName.isNotNullOrEmpty()) {
                    //在脚本文件中显示为链接
                    if (configGroup != null) {
                        val gameType = configGroup.gameType
                        val typeLink = ReferenceLinkType.CwtConfig.createLink(configType.category, typeName, gameType)
                        append(": ").appendPsiLinkOrUnresolved(typeLink.escapeXml(), typeName.escapeXml(), context = typeElement)
                    } else {
                        append(": ").append(typeName)
                    }
                }
            }
            if (byName != null) {
                grayed {
                    append(" by ").append(byName.escapeXml().or.anonymous())
                }
            }

            if (configGroup != null) {
                if (referenceElement != null && configType == CwtConfigTypes.Modifier) {
                    addModifierRelatedLocalisations(element, referenceElement, name, configGroup)
                    addModifierIcon(element, referenceElement, name, configGroup)
                }
                if (element is CwtProperty || (element is CwtMemberConfigElement && element.config is CwtPropertyConfig)) {
                    addScope(element, name, configType, configGroup)
                }
                if (referenceElement != null) {
                    addScopeContext(element, referenceElement, configGroup)
                }
            }
        }
    }

    private fun DocumentationBuilder.addModifierRelatedLocalisations(element: PsiElement, referenceElement: PsiElement, name: String, configGroup: CwtConfigGroup) {
        val render = PlsFacade.getSettings().documentation.renderNameDescForModifiers
        val contextElement = referenceElement
        val gameType = configGroup.gameType ?: return
        val project = configGroup.project
        val usedLocale = ParadoxLocaleManager.getResolvedLocaleConfigInDocumentation(element)
        val nameLocalisation = run {
            val keys = ParadoxModifierManager.getModifierNameKeys(name, contextElement)
            keys.firstNotNullOfOrNull { key ->
                val selector = selector(project, contextElement).localisation().contextSensitive()
                    .preferLocale(usedLocale)
                    .withConstraint(ParadoxIndexConstraint.Localisation.Modifier)
                ParadoxLocalisationSearch.search(key, selector).find()
            }
        }
        val descLocalisation = run {
            val keys = ParadoxModifierManager.getModifierDescKeys(name, contextElement)
            keys.firstNotNullOfOrNull { key ->
                val selector = selector(project, contextElement).localisation().contextSensitive()
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
            val link = ReferenceLinkType.Localisation.createLink(nameLocalisation.name, gameType)
            append("name = ").appendPsiLinkOrUnresolved(link.escapeXml(), nameLocalisation.name.escapeXml(), context = contextElement)
        }
        run {
            if (descLocalisation == null) return@run
            appendBr()
            append(PlsStringConstants.relatedLocalisationPrefix).append(" ")
            val link = ReferenceLinkType.Localisation.createLink(descLocalisation.name, gameType)
            append("desc = ").appendPsiLinkOrUnresolved(link.escapeXml(), descLocalisation.name.escapeXml(), context = contextElement)
        }
        run rs@{
            val sections = getSections(SECTIONS_LOC)
            if (sections == null || render) return@rs
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

    private fun DocumentationBuilder.addModifierIcon(element: PsiElement, referenceElement: PsiElement, name: String, configGroup: CwtConfigGroup) {
        val render = PlsFacade.getSettings().documentation.renderIconForModifiers
        val contextElement = referenceElement
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
            val link = ReferenceLinkType.FilePath.createLink(iconPath, gameType)
            append("icon = ").appendPsiLinkOrUnresolved(link.escapeXml(), iconPath.escapeXml(), context = contextElement)
        }
        run rs@{
            val sections = getSections(SECTIONS_IMAGES)
            if (sections == null || render) return@rs
            run {
                if (iconFile == null) return@run
                val url = ParadoxImageManager.resolveUrlByFile(iconFile, project) ?: return@run
                sections.put("icon", buildDocumentation { appendImgTag(url) })
            }
        }
    }

    private fun DocumentationBuilder.addScope(element: PsiElement, name: String, configType: CwtConfigType?, configGroup: CwtConfigGroup) {
        //即使是在CWT文件中，如果可以推断得到CWT规则分组，也显示作用域信息

        if (!PlsFacade.getSettings().documentation.showScopes) return

        //为link提示名字、描述、输入作用域、输出作用域的文档注释
        //为alias modifier localisation_command等提供分类、支持的作用域的文档注释
        //仅为脚本文件和本地化文件中的引用提供
        val sections = getSections(SECTIONS_INFO)
        val gameType = configGroup.gameType ?: return
        val contextElement = element
        when (configType) {
            CwtConfigTypes.Link -> {
                val linkConfig = configGroup.links[name] ?: return
                if (sections != null) {
                    val inputScopes = linkConfig.inputScopes
                    sections.put(PlsBundle.message("sectionTitle.inputScopes"), getScopesText(inputScopes, gameType, contextElement))

                    val outputScope = linkConfig.outputScope ?: ParadoxScopeManager.anyScopeId
                    sections.put(PlsBundle.message("sectionTitle.outputScope"), getScopeText(outputScope, gameType, contextElement))
                }
            }
            CwtConfigTypes.LocalisationLink -> {
                val linkConfig = configGroup.localisationLinks[name] ?: return
                if (sections != null) {
                    val inputScopes = linkConfig.inputScopes
                    sections.put(PlsBundle.message("sectionTitle.inputScopes"), getScopesText(inputScopes, gameType, contextElement))

                    val outputScope = linkConfig.outputScope ?: ParadoxScopeManager.anyScopeId
                    sections.put(PlsBundle.message("sectionTitle.outputScope"), getScopeText(outputScope, gameType, contextElement))
                }
            }
            CwtConfigTypes.Modifier -> {
                val modifierConfig = configGroup.modifiers[name] ?: return
                if (sections != null) {
                    val categoryNames = modifierConfig.categoryConfigMap.keys
                    if (categoryNames.isNotEmpty()) {
                        sections.put(PlsBundle.message("sectionTitle.categories"), getModifierCategoriesText(categoryNames, gameType, contextElement))
                    }

                    val supportedScopes = modifierConfig.supportedScopes
                    sections.put(PlsBundle.message("sectionTitle.supportedScopes"), getScopesText(supportedScopes, gameType, contextElement))
                }
            }
            CwtConfigTypes.ModifierCategory -> {
                val modifierCategoryConfig = configGroup.modifierCategories[name] ?: return
                if (sections != null) {
                    val supportedScopes = modifierCategoryConfig.supportedScopes
                    sections.put(PlsBundle.message("sectionTitle.supportedScopes"), getScopesText(supportedScopes, gameType, contextElement))
                }
            }
            CwtConfigTypes.LocalisationPromotion -> {
                val localisationPromotionConfig = configGroup.localisationPromotions[name] ?: return
                if (sections != null) {
                    val supportedScopes = localisationPromotionConfig.supportedScopes
                    sections.put(PlsBundle.message("sectionTitle.supportedScopes"), getScopesText(supportedScopes, gameType, contextElement))
                }
            }
            CwtConfigTypes.LocalisationCommand -> {
                val localisationCommandConfig = configGroup.localisationCommands[name] ?: return
                if (sections != null) {
                    val supportedScopes = localisationCommandConfig.supportedScopes
                    sections.put(PlsBundle.message("sectionTitle.supportedScopes"), getScopesText(supportedScopes, gameType, contextElement))
                }
            }
            CwtConfigTypes.Alias, CwtConfigTypes.Trigger, CwtConfigTypes.Effect -> {
                val (aliasName, aliasSubName) = name.removeSurroundingOrNull("alias[", "]")?.split(':') ?: return
                val aliasConfigGroup = configGroup.aliasGroups[aliasName] ?: return
                val aliasConfigs = aliasConfigGroup[aliasSubName] ?: return
                val aliasConfig = aliasConfigs.singleOrNull()
                    ?: aliasConfigs.find { element.isSamePosition(it.pointer.element) }
                    ?: return
                if (aliasConfig.name !in configGroup.aliasNamesSupportScope) return
                if (sections != null) {
                    val supportedScopes = aliasConfig.supportedScopes
                    sections.put(PlsBundle.message("sectionTitle.supportedScopes"), getScopesText(supportedScopes, gameType, contextElement))

                    val outputScope = aliasConfig.outputScope
                    if (outputScope != null) {
                        sections.put(PlsBundle.message("sectionTitle.outputScope"), getScopeText(outputScope, gameType, contextElement))
                    }
                }
            }
            else -> pass()
        }
    }

    private fun DocumentationBuilder.addScopeContext(element: PsiElement, referenceElement: PsiElement, configGroup: CwtConfigGroup) {
        //进行代码提示时也显示作用域上下文信息
        //@Suppress("DEPRECATION")
        //if(DocumentationManager.IS_FROM_LOOKUP.get(element) == true) return

        if (!PlsFacade.getSettings().documentation.showScopeContext) return

        val sections = getSections(0) ?: return
        val gameType = configGroup.gameType ?: return
        val memberElement = referenceElement.parentOfType<ParadoxScriptMemberElement>(true) ?: return
        if (!ParadoxScopeManager.isScopeContextSupported(memberElement, indirect = true)) return
        val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(memberElement)
        if (scopeContext == null) return
        //TODO 如果作用域引用位于脚本表达式中，应当使用那个位置的作用域上下文，但是目前实现不了
        // 因为这里的referenceElement是整个stringExpression，得到的作用域上下文会是脚本表达式最终的作用域上下文
        sections.put(PlsBundle.message("sectionTitle.scopeContext"), getScopeContextText(scopeContext, gameType, element))
    }

    private fun DocumentationBuilder.buildDocumentationContent(element: PsiElement) {
        val documentation = ParadoxPsiManager.getDocCommentText(element, CwtElementTypes.DOC_COMMENT, "<br>")
        if (documentation.isNullOrEmpty()) return
        content {
            append(documentation)
        }
    }

    private fun getConfigGroup(element: PsiElement, originalElement: PsiElement?, project: Project): CwtConfigGroup? {
        if (originalElement != null && originalElement.language is ParadoxBaseLanguage) {
            val gameType = selectGameType(originalElement)
            if (gameType != null) return PlsFacade.getConfigGroup(project, gameType)
        }
        if (element.language is CwtLanguage) {
            return CwtConfigManager.getContainingConfigGroup(element)
        }
        return null
    }
}
