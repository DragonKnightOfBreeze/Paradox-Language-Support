package icu.windea.pls.ep.resolve

import com.intellij.codeInsight.documentation.DocumentationManagerUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.CwtConfigTypeCategories
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtLocalisationCommandConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toPsiFileSystemItem
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.search.util.withGameType
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.ReferenceLinkType
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class CwtConfigLinkProvider : ReferenceLinkProvider {
    override val linkPrefix = ReferenceLinkType.CwtConfig.prefix

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType0, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val gameType = gameType0 ?: selectGameType(contextElement) ?: ParadoxGameType.Core
        val linkPath = remain.orNull() ?: return null
        val project = contextElement.project
        val configGroup = ChronicleFacade.getConfigGroup(project, gameType)
        val config = CwtConfigManager.getConfigByLinkPath(configGroup, linkPath)
        return config?.pointer?.element
    }

    override fun getUnresolvedMessage(link: String): String {
        return ChronicleEpBundle.message("reference.link.unresolved.config", link)
    }

    override fun createPsiLink(element: PsiElement, plainLink: Boolean): String? {
        if (element !is CwtProperty && element !is CwtValue) return null
        val config = element.getUserData(CwtConfigManager.Keys.config) ?: return null // retrieve config from user data
        // 这里目前仅支持可能用到的那些
        val linkType = ReferenceLinkType.CwtConfig
        val builder = StringBuilder()
        when {
            config is CwtSystemScopeConfig -> {
                val gameType = config.configGroup.gameType
                val name = config.name
                val link = linkType.createLink(CwtConfigTypeCategories.systemScopes, name, gameType)
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
            config is CwtLinkConfig -> {
                val gameType = config.configGroup.gameType
                val name = config.name
                val category = if (config.isLocalisationLink) CwtConfigTypeCategories.localisationLinks else CwtConfigTypeCategories.links
                val link = linkType.createLink(category, name, gameType)
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
            config is CwtLocalisationCommandConfig -> {
                val gameType = config.configGroup.gameType
                val name = config.name
                val link = linkType.createLink(CwtConfigTypeCategories.localisationCommands, name, gameType)
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
        val (gameType0, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val gameType = gameType0
        val name = remain.orNull() ?: return null
        val project = contextElement.project
        val selector = ParadoxScriptedVariableSearch.selector(project, contextElement).contextSensitive()
            .withGameType(gameType)
        return ParadoxScriptedVariableSearch.searchGlobal(name, selector).find() // global scripted variable only
    }

    override fun getUnresolvedMessage(link: String): String {
        return ChronicleEpBundle.message("reference.link.unresolved.sv", link)
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
        val (gameType0, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val gameType = gameType0
        val tokens = remain.split('/')
        if (tokens.isEmpty() || tokens.size > 2) return null
        val typeExpression = if (tokens.size == 2) tokens.getOrNull(0) else null
        val name = if (tokens.size == 2) tokens.getOrNull(1) else tokens.getOrNull(0)
        if (name == null) return null
        val project = contextElement.project
        val selector = ParadoxDefinitionSearch.selector(project, contextElement).contextSensitive()
            .withGameType(gameType)
        return ParadoxDefinitionSearch.searchElement(name, typeExpression, selector).find()
    }

    override fun getUnresolvedMessage(link: String): String {
        return ChronicleEpBundle.message("reference.link.unresolved.def", link)
    }

    override fun createPsiLink(element: PsiElement, plainLink: Boolean): String? {
        if (element !is ParadoxDefinitionElement) return null
        val definitionInfo = element.definitionInfo ?: return null
        if (definitionInfo.name.isEmpty()) return null // skip anonymous definitions
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
        val (gameType0, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val gameType = gameType0
        val name = remain.orNull() ?: return null
        val project = contextElement.project
        val selector = ParadoxLocalisationSearch.selector(project, contextElement).contextSensitive().preferLocale(selectLocale(contextElement))
            .withGameType(gameType)
        return ParadoxLocalisationSearch.searchNormal(name, selector).find()
    }

    override fun getUnresolvedMessage(link: String): String {
        return ChronicleEpBundle.message("reference.link.unresolved.loc", link)
    }

    override fun createPsiLink(element: PsiElement, plainLink: Boolean): String? {
        if (element !is ParadoxLocalisationProperty) return null
        val localisationType = element.type ?: return null
        if (localisationType != ParadoxLocalisationType.Normal) return null
        val name = element.name
        val gameType = selectGameType(element)
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
        // can be resolved to file or directory
        ProgressManager.checkCanceled()
        val (gameType0, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val gameType = gameType0
        val filePath = remain.orNull() ?: return null
        val project = contextElement.project
        val selector = ParadoxFilePathSearch.selector(project, contextElement).contextSensitive()
            .withGameType(gameType)
        return ParadoxFilePathSearch.search(filePath, null, selector).find()?.toPsiFileSystemItem(project)
    }

    override fun getUnresolvedMessage(link: String): String {
        return ChronicleEpBundle.message("reference.link.unresolved.path", link)
    }

    override fun createPsiLink(element: PsiElement, plainLink: Boolean): String? {
        return null // unsupported since unnecessary
    }
}

class ParadoxModifierLinkProvider : ReferenceLinkProvider {
    override val linkPrefix = ReferenceLinkType.Modifier.prefix

    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType0, remain) = getGameTypeAndRemain(link.drop(linkPrefix.length))
        val gameType = gameType0 ?: selectGameType(contextElement) ?: ParadoxGameType.Core
        val name = remain.orNull() ?: return null
        val project = contextElement.project
        val configGroup = ChronicleFacade.getConfigGroup(project, gameType)
        return ParadoxModifierManager.resolveModifier(name, contextElement, configGroup)
    }

    override fun getUnresolvedMessage(link: String): String {
        return ChronicleEpBundle.message("reference.link.unresolved.modifier", link)
    }

    override fun createPsiLink(element: PsiElement, plainLink: Boolean): String? {
        return null // unsupported since unnecessary
    }
}

private fun getGameTypeAndRemain(shortLink: String): Tuple2<ParadoxGameType?, String> {
    val i = shortLink.indexOf(':')
    if (i == -1) return null to shortLink
    return shortLink.substring(0, i).let { ParadoxGameType.getSpecific(it) } to shortLink.substring(i + 1)
}
