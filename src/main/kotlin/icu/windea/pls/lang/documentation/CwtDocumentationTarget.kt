@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.documentation

import com.intellij.model.*
import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.platform.backend.documentation.*
import com.intellij.platform.backend.presentation.*
import com.intellij.pom.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.image.*
import icu.windea.pls.lang.util.renderer.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*

//org.jetbrains.kotlin.idea.k2.codeinsight.quickDoc.KotlinDocumentationTarget

class CwtDocumentationTarget(
    val element: PsiElement,
    val originalElement: PsiElement?
) : DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPtr = element.createPointer()
        val originalElementPtr = originalElement?.createPointer()
        return Pointer {
            val element = elementPtr.dereference() ?: return@Pointer null
            CwtDocumentationTarget(element, originalElementPtr?.dereference())
        }
    }

    override val navigatable: Navigatable?
        get() = element as? Navigatable

    override fun computePresentation(): TargetPresentation {
        return getTargetPresentation(element)
    }

    override fun computeDocumentationHint(): String? {
        return computeLocalDocumentation(element, originalElement, true)
    }

    override fun computeDocumentation(): DocumentationResult {
        return DocumentationResult.asyncDocumentation {
            val html = runReadAction { computeLocalDocumentation(element, originalElement, false) } ?: return@asyncDocumentation null
            DocumentationResult.documentation(html)
        }
    }
}

private const val SECTIONS_INFO = 0
private const val SECTIONS_IMAGES = 1
private const val SECTIONS_LOC = 2

private fun computeLocalDocumentation(element: PsiElement, originalElement: PsiElement?, quickNavigation: Boolean): String? {
    return when (element) {
        is CwtProperty -> getPropertyDoc(element, originalElement, quickNavigation)
        is CwtString -> getStringDoc(element, originalElement, quickNavigation)
        is CwtMemberConfigElement -> getMemberConfigDoc(element, originalElement, quickNavigation)
        else -> null
    }
}

private fun getPropertyDoc(element: CwtProperty, originalElement: PsiElement?, quickNavigation: Boolean): String {
    return buildDocumentation {
        val name = element.name
        val configType = CwtConfigManager.getConfigType(element)
        val project = element.project
        val configGroup = getConfigGroup(element, originalElement, project)
        if (!quickNavigation) initSections(3)
        buildPropertyOrStringDefinition(element, originalElement, name, configType, configGroup)
        if (quickNavigation) return@buildDocumentation
        buildDocumentationContent(element)
        buildSections()
    }
}

private fun getStringDoc(element: CwtString, originalElement: PsiElement?, quickNavigation: Boolean): String? {
    //only for property value or block value
    if (!element.isPropertyValue() && !element.isBlockValue()) return null

    return buildDocumentation {
        val name = element.name
        val configType = element.configType
        val project = element.project
        val configGroup = getConfigGroup(element, originalElement, project)
        if (!quickNavigation) initSections(3)
        buildPropertyOrStringDefinition(element, originalElement, name, configType, configGroup)
        if (quickNavigation) return@buildDocumentation
        buildDocumentationContent(element)
        buildSections()
    }
}

private fun getMemberConfigDoc(element: CwtMemberConfigElement, originalElement: PsiElement?, quickNavigation: Boolean): String {
    return buildDocumentation {
        val name = element.name
        val configType = null
        val project = element.project
        val configGroup = PlsFacade.getConfigGroup(project, element.gameType)
        if (!quickNavigation) initSections(3)
        buildPropertyOrStringDefinition(element, originalElement, name, configType, configGroup)
        if (quickNavigation) return@buildDocumentation
        buildDocumentationContent(element)
        buildSections()
    }
}

private fun DocumentationBuilder.buildPropertyOrStringDefinition(element: PsiElement, originalElement: PsiElement?, name: String, configType: CwtConfigType?, configGroup: CwtConfigGroup?) {
    definition {
        appendCwtConfigFileInfoHeader(element)

        val bindingConfig = element.getUserData(PlsKeys.bindingConfig)
        val tagName = bindingConfig?.castOrNull<CwtValueConfig>()?.tagType
        val referenceElement = getReferenceElement(originalElement)

        val shortName = configType?.getShortName(name) ?: name
        val byName = if (shortName == name) null else name
        val prefix = when {
            referenceElement != null && tagName != null -> "(${tagName.id})" //处理特殊标签
            configType?.isReference == true -> configType.prefix
            referenceElement is ParadoxScriptPropertyKey -> PlsConstants.Strings.definitionPropertyPrefix
            referenceElement is ParadoxScriptValue -> PlsConstants.Strings.definitionValuePrefix
            element is CwtMemberConfigElement && element.config is CwtPropertyConfig -> PlsConstants.Strings.definitionPropertyPrefix
            element is CwtMemberConfigElement && element.config is CwtValueConfig -> PlsConstants.Strings.definitionValuePrefix
            else -> configType?.prefix
        }
        val typeCategory = configType?.category

        if (prefix != null) {
            append(prefix).append(" ")
        }
        append("<b>").append(shortName.escapeXml().orAnonymous()).append("</b>")
        if (typeCategory != null) {
            val typeElement = element.parentOfType<CwtProperty>()
            val typeName = typeElement?.name?.substringIn('[', ']')?.orNull()
            if (typeName.isNotNullOrEmpty()) {
                //在脚本文件中显示为链接
                if (configGroup != null) {
                    val gameType = configGroup.gameType
                    val typeLink = "${gameType.prefix}${typeCategory}/${typeName}"
                    append(": ").appendCwtConfigLink(typeLink, typeName, typeElement)
                } else {
                    append(": ").append(typeName)
                }
            }
        }
        if (byName != null) {
            grayed {
                append(" by ").append(byName.escapeXml().orAnonymous())
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
        append(PlsConstants.Strings.relatedLocalisationPrefix).append(" ")
        append("name = ").appendLocalisationLink(gameType, nameLocalisation.name, contextElement)
    }
    run {
        if (descLocalisation == null) return@run
        appendBr()
        append(PlsConstants.Strings.relatedLocalisationPrefix).append(" ")
        append("desc = ").appendLocalisationLink(gameType, descLocalisation.name, contextElement)
    }
    run rs@{
        val sections = getSections(SECTIONS_LOC)
        if (sections == null || render) return@rs
        run {
            if (nameLocalisation == null) return@run
            val richText = ParadoxLocalisationTextHtmlRenderer.render(nameLocalisation, forDoc = true)
            sections.put("name", richText)
        }
        run {
            if (descLocalisation == null) return@run
            val richText = ParadoxLocalisationTextHtmlRenderer.render(descLocalisation, forDoc = true)
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
        append(PlsConstants.Strings.relatedImagePrefix).append(" ")
        append("icon = ").appendFilePathLink(gameType, iconPath, iconPath, contextElement)
    }
    run rs@{
        val sections = getSections(SECTIONS_IMAGES)
        if (sections == null || render) return@rs
        run {
            if (iconFile == null) return@run
            val url = ParadoxImageResolver.resolveUrlByFile(iconFile) ?: return@run
            sections.put("icon", buildDocumentation { appendImgTag(url) })
        }
    }
}

private fun DocumentationBuilder.addScope(element: PsiElement, name: String, configType: CwtConfigType?, configGroup: CwtConfigGroup) {
    //即使是在CWT文件中，如果可以推断得到CWT规则组，也显示作用域信息

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
        return CwtConfigManager.getContainingConfigGroup(element, forRepo = true)
    }
    return null
}
