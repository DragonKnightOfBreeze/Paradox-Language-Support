package icu.windea.pls.ep.codeInsight.navigation

import com.intellij.codeInsight.documentation.DocumentationManagerUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtLocalisationCommandConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.config.configGroup.complexEnums
import icu.windea.pls.config.configGroup.dynamicValueTypes
import icu.windea.pls.config.configGroup.enums
import icu.windea.pls.config.configGroup.links
import icu.windea.pls.config.configGroup.localisationCommands
import icu.windea.pls.config.configGroup.localisationLinks
import icu.windea.pls.config.configGroup.modifierCategories
import icu.windea.pls.config.configGroup.modifiers
import icu.windea.pls.config.configGroup.scopeAliasMap
import icu.windea.pls.config.configGroup.systemScopes
import icu.windea.pls.config.configGroup.types
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toPsiFileSystemItem
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.localisationInfo
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withGameType
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.ReferenceLinkType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class CwtConfigLinkProvider : ReferenceLinkProvider {
    override val linkPrefix = ReferenceLinkType.CwtConfig.prefix

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType0, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val gameType = gameType0 ?: selectGameType(contextElement) ?: ParadoxGameType.Core
        val tokens = remain.split('/')
        val category = tokens.getOrNull(0) ?: return null
        val project = contextElement.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val categories = ReferenceLinkType.CwtConfig.Categories
        return when (category) {
            categories.types -> {
                if (tokens.isEmpty() || tokens.size > 3) return null
                val name = tokens.getOrNull(1)
                val subtypeName = tokens.getOrNull(2)
                val config = when {
                    name == null -> null
                    subtypeName == null -> configGroup.types[name]
                    else -> configGroup.types.getValue(name).subtypes[subtypeName]
                } ?: return null
                return config.pointer.element
            }
            categories.values -> {
                if (tokens.isEmpty() || tokens.size > 3) return null
                val name = tokens.getOrNull(1) ?: return null
                val valueName = tokens.getOrNull(2)
                val config = configGroup.dynamicValueTypes[name] ?: return null
                if (valueName == null) return config.pointer.element
                return config.valueConfigMap.get(valueName)?.pointer?.element
            }
            categories.enums -> {
                if (tokens.isEmpty() || tokens.size > 3) return null
                val name = tokens.getOrNull(1) ?: return null
                val valueName = tokens.getOrNull(2)
                val config = configGroup.enums[name] ?: return null
                if (valueName == null) return config.pointer.element
                return config.valueConfigMap.get(valueName)?.pointer?.element
            }
            categories.complexEnums -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.complexEnums[name] ?: return null
                return config.pointer.element
            }
            categories.scopes -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.scopeAliasMap[name] ?: return null
                return config.pointer.element
            }
            categories.systemScopes -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.systemScopes[name] ?: return null
                return config.pointer.element
            }
            categories.links -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.links[name] ?: return null
                return config.pointer.element
            }
            categories.localisationLinks -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.localisationLinks[name] ?: return null
                return config.pointer.element
            }
            categories.localisationCommands -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.localisationCommands[name] ?: return null
                return config.pointer.element
            }
            categories.modifierCategories -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.modifierCategories[name] ?: return null
                return config.pointer.element
            }
            categories.modifiers -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.modifiers[name] ?: return null
                return config.pointer.element
            }
            else -> null
        }
    }

    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.cwt", link)
    }

    override fun createPsiLink(element: PsiElement, plainLink: Boolean): String? {
        if (element !is CwtProperty && element !is CwtValue) return null
        val config = element.getUserData(PlsKeys.bindingConfig) ?: return null //retrieve config from user data
        //这里目前仅支持可能用到的那些
        val linkType = ReferenceLinkType.CwtConfig
        val categories = ReferenceLinkType.CwtConfig.Categories
        val builder = StringBuilder()
        when {
            config is CwtSystemScopeConfig -> {
                val gameType = config.configGroup.gameType
                val name = config.name
                val link = linkType.createLink(categories.systemScopes, name, gameType)
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
            config is CwtLinkConfig -> {
                val gameType = config.configGroup.gameType
                val name = config.name
                val category = if (config.forLocalisation) categories.localisationLinks else categories.links
                val link = linkType.createLink(category, name, gameType)
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
            config is CwtLocalisationCommandConfig -> {
                val gameType = config.configGroup.gameType
                val name = config.name
                val link = linkType.createLink(categories.localisationCommands, name, gameType)
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
        }
        return builder.toString()
    }
}

class ParadoxScriptedVariableLinkProvider : ReferenceLinkProvider {
    override val linkPrefix = ReferenceLinkType.ScriptedVariable.prefix

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val name = remain
        val project = contextElement.project
        val selector = selector(project, contextElement).scriptedVariable().contextSensitive()
            .withGameType(gameType)
        return ParadoxScriptedVariableSearch.searchGlobal(name, selector).find() //global scripted variable only
    }

    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.sv", link)
    }

    override fun createPsiLink(element: PsiElement, plainLink: Boolean): String? {
        if (element !is ParadoxScriptScriptedVariable) return null
        val gameType = selectGameType(element)
        val name = element.name?.orNull() ?: return null
        val linkType = ReferenceLinkType.ScriptedVariable
        val link = linkType.createLink(name, gameType)
        val builder = StringBuilder()
        DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
        return builder.toString()
    }
}

class ParadoxDefinitionLinkProvider : ReferenceLinkProvider {
    override val linkPrefix = ReferenceLinkType.Definition.prefix

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val tokens = remain.split('/')
        if (tokens.isEmpty() || tokens.size > 2) return null
        val typeExpression = if (tokens.size == 2) tokens.getOrNull(0) else null
        val name = if (tokens.size == 2) tokens.getOrNull(1) else tokens.getOrNull(0)
        if (name == null) return null
        val project = contextElement.project
        val selector = selector(project, contextElement).definition().contextSensitive()
            .withGameType(gameType)
        return ParadoxDefinitionSearch.search(name, typeExpression, selector).find()
    }

    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.def", link)
    }

    override fun createPsiLink(element: PsiElement, plainLink: Boolean): String? {
        if (element !is ParadoxScriptDefinitionElement) return null
        val definitionInfo = element.definitionInfo ?: return null
        if (definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
        val gameType = definitionInfo.gameType
        val name = definitionInfo.name
        val typesText = definitionInfo.types.joinToString(".")
        val linkType = ReferenceLinkType.Definition
        val link = linkType.createLink(name, typesText, gameType)
        val builder = StringBuilder()
        DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
        return builder.toString()
    }
}

class ParadoxLocalisationLinkProvider : ReferenceLinkProvider {
    override val linkPrefix = ReferenceLinkType.Localisation.prefix

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val name = remain
        val project = contextElement.project
        val selector = selector(project, contextElement).localisation().contextSensitive().preferLocale(selectLocale(contextElement))
            .withGameType(gameType)
        return ParadoxLocalisationSearch.search(name, selector).find()
    }

    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.loc", link)
    }

    override fun createPsiLink(element: PsiElement, plainLink: Boolean): String? {
        if (element !is ParadoxLocalisationProperty) return null
        val localisationInfo = element.localisationInfo ?: return null
        if (localisationInfo.type != ParadoxLocalisationType.Normal) return null
        val name = localisationInfo.name
        val gameType = localisationInfo.gameType
        val linkType = ReferenceLinkType.Localisation
        val link = linkType.createLink(name, gameType)
        val builder = StringBuilder()
        DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
        return builder.toString()
    }
}

class ParadoxFilePathLinkProvider : ReferenceLinkProvider {
    override val linkPrefix = ReferenceLinkType.FilePath.prefix

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        //can be resolved to file or directory
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val filePath = remain
        val project = contextElement.project
        val selector = selector(project, contextElement).file().contextSensitive()
            .withGameType(gameType)
        return ParadoxFilePathSearch.search(filePath, null, selector).find()?.toPsiFileSystemItem(project)
    }

    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.path", link)
    }

    override fun createPsiLink(element: PsiElement, plainLink: Boolean): String? {
        return null //unsupported since unnecessary
    }
}

class ParadoxModifierLinkProvider : ReferenceLinkProvider {
    override val linkPrefix = ReferenceLinkType.Modifier.prefix

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType0, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val gameType = gameType0 ?: selectGameType(contextElement) ?: ParadoxGameType.Core
        val name = remain
        val project = contextElement.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        return ParadoxModifierManager.resolveModifier(name, contextElement, configGroup)
    }

    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.modifier", link)
    }

    override fun createPsiLink(element: PsiElement, plainLink: Boolean): String? {
        return null //unsupported since unnecessary
    }
}

private fun getGameTypeAndRemain(shortLink: String): Tuple2<ParadoxGameType?, String> {
    val i = shortLink.indexOf(':')
    if (i == -1) return null to shortLink
    return shortLink.substring(0, i).let { ParadoxGameType.get(it) } to shortLink.substring(i + 1)
}
