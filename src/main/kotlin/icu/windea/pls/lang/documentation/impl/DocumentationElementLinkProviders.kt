package icu.windea.pls.lang.documentation.impl

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.documentation.*

class CwtConfigLinkProvider : DocumentationElementLinkProvider {
    //e.g. #cwt/stellaris/types/civic_or_origin/civic
    
    companion object {
        const val LINK_PREFIX = "#cwt/"
    }
    
    override val linkPrefix = LINK_PREFIX
    
    override fun resolveLink(shortLink: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val tokens = shortLink.split('/')
        if(tokens.size > 4) return null
        val gameType = tokens.getOrNull(0) ?: return null
        val category = tokens.getOrNull(1) ?: return null
        val project = contextElement.project
        return when(category) {
            "types" -> {
                val name = tokens.getOrNull(2)
                val subtypeName = tokens.getOrNull(3)
                val config = when {
                    name == null -> null
                    subtypeName == null -> getCwtConfig(project).get(gameType).types[name]
                    else -> getCwtConfig(project).get(gameType).types.getValue(name).subtypes[subtypeName]
                } ?: return null
                return config.pointer.element
            }
            "enums" -> {
                val name = tokens.getOrNull(2) ?: return null
                val valueName = tokens.getOrNull(3)
                val config = getCwtConfig(project).get(gameType).enums[name] ?: return null
                if(valueName == null) return config.pointer.element
                return config.valueConfigMap.get(valueName)?.pointer?.element
            }
            "complex_enums" -> {
                val name = tokens.getOrNull(2) ?: return null
                val config = getCwtConfig(project).get(gameType).complexEnums[name] ?: return null
                return config.pointer.element
            }
            "values" -> {
                val name = tokens.getOrNull(2) ?: return null
                val valueName = tokens.getOrNull(3)
                val config = getCwtConfig(project).get(gameType).values[name] ?: return null
                if(valueName == null) return config.pointer.element
                return config.valueConfigMap.get(valueName)?.pointer?.element
            }
            "scopes" -> {
                val name = tokens.getOrNull(2) ?: return null
                val config = getCwtConfig(project).get(gameType).scopeAliasMap[name] ?: return null
                return config.pointer.element
            }
            "system_links" -> {
                val name = tokens.getOrNull(2) ?: return null
                val config = getCwtConfig(project).get(gameType).systemLinks[name] ?: return null
                return config.pointer.element
            }
            "links" -> {
                val name = tokens.getOrNull(2) ?: return null
                val config = getCwtConfig(project).get(gameType).links[name] ?: return null
                return config.pointer.element
            }
            "modifier_categories" -> {
                val name = tokens.getOrNull(2) ?: return null
                val config = getCwtConfig(project).get(gameType).modifierCategories[name] ?: return null
                return config.pointer.element
            }
            "modifiers" -> {
                val name = tokens.getOrNull(2) ?: return null
                val config = getCwtConfig(project).get(gameType).modifiers[name] ?: return null
                return config.pointer.element
            }
            else -> null
        }
    }
}

class ParadoxDefinitionLinkProvider : DocumentationElementLinkProvider {
    //e.g. #definition/stellaris/civic_or_origin.origin/origin_default
    
    companion object {
        const val LINK_PREFIX = "#definition/"
    }
    
    override val linkPrefix = LINK_PREFIX
    
    override fun resolveLink(shortLink: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val tokens = shortLink.split('/')
        if(tokens.size > 3) return null
        val typeExpression = tokens.getOrNull(1) ?: return null
        val name = tokens.getOrNull(2) ?: return null
        val project = contextElement.project
        val selector = definitionSelector(project, contextElement).contextSensitive()
        return ParadoxDefinitionSearch.search(name, typeExpression, selector).find()
    }
}

class ParadoxLocalisationLinkProvider : DocumentationElementLinkProvider {
    //e.g. #localisation/stellaris/KEY
    
    companion object {
        const val LINK_PREFIX = "#localisation/"
    }
    
    override val linkPrefix = LINK_PREFIX
    
    override fun resolveLink(shortLink: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val tokens = shortLink.split('/')
        if(tokens.size > 2) return null
        val name = tokens.getOrNull(1) ?: return null
        val project = contextElement.project
        val selector = localisationSelector(project, contextElement).contextSensitive().preferLocale(contextElement.localeConfig)
        return ParadoxLocalisationSearch.search(name, selector).find()
    }
}

class ParadoxFilePathLinkProvider: DocumentationElementLinkProvider {
    //e.g. #path/stellaris/path
    
    companion object {
        const val LINK_PREFIX = "#path/"
    }
    
    override val linkPrefix = LINK_PREFIX
    
    override fun resolveLink(shortLink: String, contextElement: PsiElement): PsiElement? {
        ProgressManager.checkCanceled()
        val tokens = shortLink.split('/', limit = 2)
        val filePath = tokens.getOrNull(1) ?: return null
        val project = contextElement.project
        val selector = fileSelector(project, contextElement).contextSensitive()
        return ParadoxFilePathSearch.search(filePath, null, selector).find()
            ?.toPsiFile(project)
    }
}
