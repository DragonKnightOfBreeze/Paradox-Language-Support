package icu.windea.pls.ep.documentation

import com.intellij.codeInsight.documentation.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class CwtConfigLinkProvider : ParadoxDocumentationLinkProvider {
    // e.g.,
    // cwt:stellaris:types/civic_or_origin/civic

    // limited support only

    companion object {
        const val LINK_PREFIX = "cwt:"
    }

    override val linkPrefix = LINK_PREFIX

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(LINK_PREFIX.length))
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

    override fun create(element: PsiElement, plainLink: Boolean): String? {
        if (element !is CwtProperty && element !is CwtValue) return null
        val config = element.getUserData(PlsKeys.bindingConfig) ?: return null //retrieve config from user data
        //这里目前仅支持可能用到的那些
        val builder = StringBuilder()
        when {
            config is CwtSystemScopeConfig -> {
                val gameType = config.configGroup.gameType
                val name = config.name
                val link = "${linkPrefix}${gameType.prefix}system_scopes/${name}"
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
            config is CwtLinkConfig -> {
                val gameType = config.configGroup.gameType
                val name = config.name
                val category = if(config.forLocalisation) "localisation_links" else "links"
                val link = "${linkPrefix}${gameType.prefix}${category}/${name}"
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
            config is CwtLocalisationCommandConfig -> {
                val gameType = config.configGroup.gameType
                val name = config.name
                val link = "${linkPrefix}${gameType.prefix}localisation_commands/${name}"
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
        }
        return builder.toString()
    }
}

class ParadoxScriptedVariableLinkProvider : ParadoxDocumentationLinkProvider {
    // e.g.,
    // pdx.sv:some_sv
    // pdx.sv:stellaris:some_sv

    companion object {
        const val LINK_PREFIX = "pdx.sv:"
    }

    override val linkPrefix = LINK_PREFIX

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(LINK_PREFIX.length))
        val name = remain
        val project = contextElement.project
        val selector = selector(project, contextElement).scriptedVariable().contextSensitive()
            .withGameType(gameType)
        return ParadoxGlobalScriptedVariableSearch.search(name, selector).find() //global scripted variable only
    }

    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.sv", link)
    }

    override fun create(element: PsiElement, plainLink: Boolean): String? {
        if (element !is ParadoxScriptScriptedVariable) return null
        val gameType = selectGameType(element)
        val name = element.name
        val link = "${linkPrefix}${gameType.prefix}${name}"
        val builder = StringBuilder()
        DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
        return builder.toString()
    }
}

class ParadoxDefinitionLinkProvider : ParadoxDocumentationLinkProvider {
    // e.g.,
    // pdx.d:origin_default
    // pdx.d:civic_or_origin.origin/origin_default
    // pdx.d:stellaris:civic_or_origin.origin/origin_default

    companion object {
        const val LINK_PREFIX = "pdx.d:"
    }

    override val linkPrefix = LINK_PREFIX

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(LINK_PREFIX.length))
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

    override fun create(element: PsiElement, plainLink: Boolean): String? {
        if (element !is ParadoxScriptDefinitionElement) return null
        val definitionInfo = element.definitionInfo ?: return null
        if (definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
        val gameType = definitionInfo.gameType
        val name = definitionInfo.name
        val typesText = definitionInfo.types.joinToString(".")
        val link = "${linkPrefix}${gameType.prefix}${typesText}/${name}"
        val builder = StringBuilder()
        DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
        return builder.toString()
    }
}

class ParadoxLocalisationLinkProvider : ParadoxDocumentationLinkProvider {
    // e.g.,
    // pdx.l:KEY
    // pdx.l:stellaris:KEY

    companion object {
        const val LINK_PREFIX = "pdx.l:"
    }

    override val linkPrefix = LINK_PREFIX

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(LINK_PREFIX.length))
        val name = remain
        val project = contextElement.project
        val selector = selector(project, contextElement).localisation().contextSensitive().preferLocale(selectLocale(contextElement))
            .withGameType(gameType)
        return ParadoxLocalisationSearch.search(name, selector).find()
    }

    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.loc", link)
    }

    override fun create(element: PsiElement, plainLink: Boolean): String? {
        if (element !is ParadoxLocalisationProperty) return null
        val localisationInfo = element.localisationInfo ?: return null
        if (localisationInfo.category != ParadoxLocalisationCategory.Localisation) return null
        val name = localisationInfo.name
        val gameType = localisationInfo.gameType
        val link = "${linkPrefix}${gameType.prefix}${name}"
        val builder = StringBuilder()
        DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
        return builder.toString()
    }
}

class ParadoxFilePathLinkProvider : ParadoxDocumentationLinkProvider {
    // e.g.,
    // pdx.p:path
    // pdx.p:stellaris:path

    companion object {
        const val LINK_PREFIX = "pdx.p:"
    }

    override val linkPrefix = LINK_PREFIX

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        //can be resolved to file or directory
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(LINK_PREFIX.length))
        val filePath = remain
        val project = contextElement.project
        val selector = selector(project, contextElement).file().contextSensitive()
            .withGameType(gameType)
        return ParadoxFilePathSearch.search(filePath, null, selector).find()?.toPsiFileSystemItem(project)
    }

    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.path", link)
    }

    override fun create(element: PsiElement, plainLink: Boolean): String? {
        return null //unsupported since unnecessary
    }
}

class ParadoxModifierLinkProvider : ParadoxDocumentationLinkProvider {
    // e.g.,
    // pdx.m:job_researcher_add

    companion object {
        const val LINK_PREFIX = "pdx.m:"
    }

    override val linkPrefix = LINK_PREFIX

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val name = link.drop(LINK_PREFIX.length)
        return ParadoxModifierManager.resolveModifier(name, contextElement)
    }

    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.modifier", link)
    }

    override fun create(element: PsiElement, plainLink: Boolean): String? {
        return null //unsupported since unnecessary
    }
}

private fun getGameTypeAndRemain(shortLink: String): Tuple2<ParadoxGameType?, String> {
    val i = shortLink.indexOf(':')
    if (i == -1) return null to shortLink
    return shortLink.substring(0, i).let { ParadoxGameType.resolve(it) } to shortLink.substring(i + 1)
}
