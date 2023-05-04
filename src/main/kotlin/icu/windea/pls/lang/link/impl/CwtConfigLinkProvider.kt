package icu.windea.pls.lang.link.impl

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.link.*

class CwtConfigLinkProvider : PsiElementLinkProvider {
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
