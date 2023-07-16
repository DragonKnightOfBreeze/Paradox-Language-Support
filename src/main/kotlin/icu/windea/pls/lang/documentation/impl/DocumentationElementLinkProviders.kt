package icu.windea.pls.lang.documentation.impl

import com.intellij.codeInsight.documentation.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class CwtConfigLinkProvider : DocumentationElementLinkProvider {
    // e.g.
    // cwt:stellaris:types/civic_or_origin/civic
    
    companion object {
        const val LINK_PREFIX = "cwt:"
    }
    
    override val linkPrefix = LINK_PREFIX
    
    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(LINK_PREFIX.length))
        val tokens = remain.split('/')
        val category = tokens.getOrNull(0) ?: return null
        return when(category) {
            "types" -> {
                if(tokens.isEmpty() || tokens.size > 3) return null
                val project = contextElement.project
                val name = tokens.getOrNull(1)
                val subtypeName = tokens.getOrNull(2)
                val config = when {
                    name == null -> null
                    subtypeName == null -> getCwtConfig(project).get(gameType).types[name]
                    else -> getCwtConfig(project).get(gameType).types.getValue(name).subtypes[subtypeName]
                } ?: return null
                return config.pointer.element
            }
            "values" -> {
                if(tokens.isEmpty() || tokens.size > 3) return null
                val project = contextElement.project
                val name = tokens.getOrNull(1) ?: return null
                val valueName = tokens.getOrNull(2)
                val config = getCwtConfig(project).get(gameType).values[name] ?: return null
                if(valueName == null) return config.pointer.element
                return config.valueConfigMap.get(valueName)?.pointer?.element
            }
            "enums" -> {
                if(tokens.isEmpty() || tokens.size > 3) return null
                val project = contextElement.project
                val name = tokens.getOrNull(1) ?: return null
                val valueName = tokens.getOrNull(2)
                val config = getCwtConfig(project).get(gameType).enums[name] ?: return null
                if(valueName == null) return config.pointer.element
                return config.valueConfigMap.get(valueName)?.pointer?.element
            }
            "complex_enums" -> {
                if(tokens.isEmpty() || tokens.size > 2) return null
                val project = contextElement.project
                val name = tokens.getOrNull(1) ?: return null
                val config = getCwtConfig(project).get(gameType).complexEnums[name] ?: return null
                return config.pointer.element
            }
            "scopes" -> {
                if(tokens.isEmpty() || tokens.size > 2) return null
                val project = contextElement.project
                val name = tokens.getOrNull(1) ?: return null
                val config = getCwtConfig(project).get(gameType).scopeAliasMap[name] ?: return null
                return config.pointer.element
            }
            "system_links" -> {
                if(tokens.isEmpty() || tokens.size > 2) return null
                val project = contextElement.project
                val name = tokens.getOrNull(1) ?: return null
                val config = getCwtConfig(project).get(gameType).systemLinks[name] ?: return null
                return config.pointer.element
            }
            "links" -> {
                if(tokens.isEmpty() || tokens.size > 2) return null
                val project = contextElement.project
                val name = tokens.getOrNull(1) ?: return null
                val config = getCwtConfig(project).get(gameType).links[name] ?: return null
                return config.pointer.element
            }
            "localisation_links" -> {
                if(tokens.isEmpty() || tokens.size > 2) return null
                val project = contextElement.project
                val name = tokens.getOrNull(1) ?: return null
                val config = getCwtConfig(project).get(gameType).localisationLinks[name] ?: return null
                return config.pointer.element
            }
            "localisation_commands" -> {
                if(tokens.isEmpty() || tokens.size > 2) return null
                val project = contextElement.project
                val name = tokens.getOrNull(1) ?: return null
                val config = getCwtConfig(project).get(gameType).localisationCommands[name] ?: return null
                return config.pointer.element
            }
            "modifier_categories" -> {
                if(tokens.isEmpty() || tokens.size > 2) return null
                val project = contextElement.project
                val name = tokens.getOrNull(1) ?: return null
                val config = getCwtConfig(project).get(gameType).modifierCategories[name] ?: return null
                return config.pointer.element
            }
            "modifiers" -> {
                if(tokens.isEmpty() || tokens.size > 2) return null
                val project = contextElement.project
                val name = tokens.getOrNull(1) ?: return null
                val config = getCwtConfig(project).get(gameType).modifiers[name] ?: return null
                return config.pointer.element
            }
            else -> null
        }
    }
    
    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.cwt", link)
    }
    
    override fun create(element: PsiElement, plainLink: Boolean): String? {
        if(element !is CwtProperty && element !is CwtValue) return null
        val config = element.getUserData(PlsKeys.cwtConfig) ?: return null //retrieve config from user data
        //这里目前仅支持可能用到的那些
        val builder = StringBuilder()
        when {
            config is CwtSystemLinkConfig -> {
                val gameType = config.info.configGroup.gameType
                val name = config.name
                val link = "${linkPrefix}${gameType.linkToken}system_links/${name}"
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
            config is CwtLinkConfig -> {
                val gameType = config.info.configGroup.gameType
                val name = config.name
                val link = "${linkPrefix}${gameType.linkToken}links/${name}"
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
            config is CwtLocalisationLinkConfig -> {
                val gameType = config.info.configGroup.gameType
                val name = config.name
                val link = "${linkPrefix}${gameType.linkToken}localisation_links/${name}"
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
            config is CwtLocalisationCommandConfig -> {
                val gameType = config.info.configGroup.gameType
                val name = config.name
                val link = "${linkPrefix}${gameType.linkToken}localisation_commands/${name}"
                DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
            }
        }
        return builder.toString()
    }
}

class ParadoxScriptedVariableLinkProvider : DocumentationElementLinkProvider {
    // e.g.
    // pdx-sv:some_sv
    // pdx-sv:stellaris:some_sv
    
    companion object {
        const val LINK_PREFIX = "pdx-sv:"
    }
    
    override val linkPrefix = LINK_PREFIX
    
    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(LINK_PREFIX.length))
        val name = remain
        val project = contextElement.project
        val selector = scriptedVariableSelector(project, contextElement).contextSensitive()
            .withGameType(gameType)
        return ParadoxGlobalScriptedVariableSearch.search(name, selector).find() //global scripted variable only
    }
    
    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.sv", link)
    }
    
    override fun create(element: PsiElement, plainLink: Boolean): String? {
        if(element !is ParadoxScriptScriptedVariable) return null
        val gameType = selectGameType(element)
        val name = element.name
        val link = "${linkPrefix}${gameType.linkToken}${name}"
        val builder = StringBuilder()
        DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
        return builder.toString()
    }
}

class ParadoxDefinitionLinkProvider : DocumentationElementLinkProvider {
    // e.g.
    // pdx-def:origin_default
    // pdx-def:civic_or_origin.origin/origin_default
    // pdx-def:stellaris:civic_or_origin.origin/origin_default
    
    companion object {
        const val LINK_PREFIX = "pdx-def:"
    }
    
    override val linkPrefix = LINK_PREFIX
    
    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(LINK_PREFIX.length))
        val tokens = remain.split('/')
        if(tokens.isEmpty() || tokens.size > 2) return null
        val typeExpression = if(tokens.size == 2) tokens.getOrNull(0) else null
        val name = if(tokens.size == 2) tokens.getOrNull(1) else tokens.getOrNull(0)
        if(name == null) return null
        val project = contextElement.project
        val selector = definitionSelector(project, contextElement).contextSensitive()
            .withGameType(gameType)
        return ParadoxDefinitionSearch.search(name, typeExpression, selector).find()
    }
    
    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.def", link)
    }
    
    override fun create(element: PsiElement, plainLink: Boolean): String? {
        if(element !is ParadoxScriptDefinitionElement) return null
        val definitionInfo = element.definitionInfo ?: return null
        if(definitionInfo.name.isEmpty()) return null //ignore anonymous definitions
        val gameType = definitionInfo.gameType
        val name = definitionInfo.name
        val typesText = definitionInfo.types.joinToString(".")
        val link = "${linkPrefix}${gameType.linkToken}${typesText}/${name}"
        val builder = StringBuilder()
        DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
        return builder.toString()
    }
}

class ParadoxLocalisationLinkProvider : DocumentationElementLinkProvider {
    // e.g.
    // pdx-loc:KEY
    // pdx-loc:stellaris:KEY
    
    companion object {
        const val LINK_PREFIX = "pdx-loc:"
    }
    
    override val linkPrefix = LINK_PREFIX
    
    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(LINK_PREFIX.length))
        val name = remain
        val project = contextElement.project
        val selector = localisationSelector(project, contextElement).contextSensitive().preferLocale(contextElement.localeConfig)
            .withGameType(gameType)
        return ParadoxLocalisationSearch.search(name, selector).find()
    }
    
    override fun getUnresolvedMessage(link: String): String {
        return PlsBundle.message("path.reference.unresolved.loc", link)
    }
    
    override fun create(element: PsiElement, plainLink: Boolean): String? {
        if(element !is ParadoxLocalisationProperty) return null
        val localisationInfo = element.localisationInfo ?: return null
        if(localisationInfo.category != ParadoxLocalisationCategory.Localisation) return null
        val name = localisationInfo.name
        val gameType = localisationInfo.gameType
        val link = "${linkPrefix}${gameType.linkToken}${name}"
        val builder = StringBuilder()
        DocumentationManagerUtil.createHyperlink(builder, link, name, plainLink)
        return builder.toString()
    }
}

class ParadoxFilePathLinkProvider: DocumentationElementLinkProvider {
    // e.g. 
    // pdx-path:path
    // pdx-path:stellaris:path
    
    companion object {
        const val LINK_PREFIX = "pdx-path:"
    }
    
    override val linkPrefix = LINK_PREFIX
    
    override fun resolve(link: String, contextElement: PsiElement): PsiElement? {
        //can be resolved to file or directory
        ProgressManager.checkCanceled()
        val (gameType, remain) = getGameTypeAndRemain(link.drop(LINK_PREFIX.length))
        val filePath = remain
        val project = contextElement.project
        val selector = fileSelector(project, contextElement).contextSensitive()
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

private fun getGameTypeAndRemain(shortLink: String): Tuple2<ParadoxGameType?, String> {
    val i = shortLink.indexOf(':')
    if(i == -1) return null to shortLink
    return shortLink.substring(0, i).let { ParadoxGameType.resolve(it) } to shortLink.substring(i + 1)
}