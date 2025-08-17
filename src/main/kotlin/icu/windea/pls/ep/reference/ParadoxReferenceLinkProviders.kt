package icu.windea.pls.ep.reference

import com.intellij.codeInsight.documentation.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class CwtConfigLinkProvider : ParadoxReferenceLinkProvider {
    override val linkPrefix = ParadoxReferenceLinkType.CwtConfig.prefix

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val tokens = remain.split('/')
        val category = tokens.getOrNull(0) ?: return null
        val project = contextElement.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        return when (category) {
            "types" -> {
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
            "values" -> {
                if (tokens.isEmpty() || tokens.size > 3) return null
                val name = tokens.getOrNull(1) ?: return null
                val valueName = tokens.getOrNull(2)
                val config = configGroup.dynamicValueTypes[name] ?: return null
                if (valueName == null) return config.pointer.element
                return config.valueConfigMap.get(valueName)?.pointer?.element
            }
            "enums" -> {
                if (tokens.isEmpty() || tokens.size > 3) return null
                val name = tokens.getOrNull(1) ?: return null
                val valueName = tokens.getOrNull(2)
                val config = configGroup.enums[name] ?: return null
                if (valueName == null) return config.pointer.element
                return config.valueConfigMap.get(valueName)?.pointer?.element
            }
            "complex_enums" -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.complexEnums[name] ?: return null
                return config.pointer.element
            }
            "scopes" -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.scopeAliasMap[name] ?: return null
                return config.pointer.element
            }
            "system_scopes" -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.systemScopes[name] ?: return null
                return config.pointer.element
            }
            "links" -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.links[name] ?: return null
                return config.pointer.element
            }
            "localisation_links" -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.localisationLinks[name] ?: return null
                return config.pointer.element
            }
            "localisation_commands" -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.localisationCommands[name] ?: return null
                return config.pointer.element
            }
            "modifier_categories" -> {
                if (tokens.isEmpty() || tokens.size > 2) return null
                val name = tokens.getOrNull(1) ?: return null
                val config = configGroup.modifierCategories[name] ?: return null
                return config.pointer.element
            }
            "modifiers" -> {
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
        val linkType = ParadoxReferenceLinkType.CwtConfig
        val builder = StringBuilder()
        when {
            config is CwtSystemScopeConfig -> {
                val gameType = config.configGroup.gameType
                val name = config.name
                val link = linkType.createLink(gameType, "system_scopes", name)
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
            config is CwtLinkConfig -> {
                val gameType = config.configGroup.gameType
                val name = config.name
                val category = if (config.forLocalisation) "localisation_links" else "links"
                val link = linkType.createLink(gameType, category, name)
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
            config is CwtLocalisationCommandConfig -> {
                val gameType = config.configGroup.gameType
                val name = config.name
                val link = linkType.createLink(gameType, "localisation_commands", name)
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
        }
        return builder.toString()
    }
}

class ParadoxScriptedVariableLinkProvider : ParadoxReferenceLinkProvider {
    override val linkPrefix = ParadoxReferenceLinkType.ScriptedVariable.prefix

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val name = remain
        val project = contextElement.project
        val selector = selector(project, contextElement).scriptedVariable().contextSensitive()
            .withGameType(gameType)
        return ParadoxGlobalScriptedVariableSearch.search(name, selector).find() //global scripted variable only
    }

    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.sv", link)
    }

    override fun createPsiLink(element: PsiElement, plainLink: Boolean): String? {
        if (element !is ParadoxScriptScriptedVariable) return null
        val gameType = selectGameType(element)
        val name = element.name?.orNull() ?: return null
        val linkType = ParadoxReferenceLinkType.ScriptedVariable
        val link = linkType.createLink(gameType, name)
        val builder = StringBuilder()
        DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
        return builder.toString()
    }
}

class ParadoxDefinitionLinkProvider : ParadoxReferenceLinkProvider {
    override val linkPrefix = ParadoxReferenceLinkType.Definition.prefix

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
        val linkType = ParadoxReferenceLinkType.Definition
        val link = linkType.createLink(gameType, name, typesText)
        val builder = StringBuilder()
        DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
        return builder.toString()
    }
}

class ParadoxLocalisationLinkProvider : ParadoxReferenceLinkProvider {
    override val linkPrefix = ParadoxReferenceLinkType.Localisation.prefix

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
        if (localisationInfo.category != ParadoxLocalisationCategory.Normal) return null
        val name = localisationInfo.name
        val gameType = localisationInfo.gameType
        val linkType = ParadoxReferenceLinkType.Localisation
        val link = linkType.createLink(gameType, name)
        val builder = StringBuilder()
        DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
        return builder.toString()
    }
}

class ParadoxFilePathLinkProvider : ParadoxReferenceLinkProvider {
    override val linkPrefix = ParadoxReferenceLinkType.FilePath.prefix

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

class ParadoxModifierLinkProvider : ParadoxReferenceLinkProvider {
    override val linkPrefix = ParadoxReferenceLinkType.Modifier.prefix

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType0, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val gameType = gameType0 ?: selectGameType(contextElement)
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
    return shortLink.substring(0, i).let { ParadoxGameType.resolve(it) } to shortLink.substring(i + 1)
}
