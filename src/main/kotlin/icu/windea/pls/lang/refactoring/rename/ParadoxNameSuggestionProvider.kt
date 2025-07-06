package icu.windea.pls.lang.refactoring.rename

import com.intellij.psi.*
import com.intellij.psi.codeStyle.*
import com.intellij.refactoring.rename.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

//org.jetbrains.kotlin.idea.base.codeInsight.KotlinNameSuggestionProvider

class ParadoxNameSuggestionProvider : NameSuggestionProvider {
    override fun getSuggestedNames(element: PsiElement, nameSuggestionContext: PsiElement?, result: MutableSet<String>): SuggestedNameInfo? {
        if (element.language !is ParadoxBaseLanguage) return null
        val declarationInfo = ParadoxDeclarationInfo.from(element) ?: return null
        if (!isSupported(declarationInfo)) return null
        return collectSuggestedNames(declarationInfo, nameSuggestionContext, result)
    }

    private fun isSupported(declarationInfo: ParadoxDeclarationInfo): Boolean {
        return when (declarationInfo) {
            is ParadoxDeclarationInfo.Definition -> {
                when (declarationInfo.type) {
                    ParadoxDefinitionTypes.Event -> false //排除事件
                    else -> true
                }
            }
            else -> true
        }
    }

    private fun collectSuggestedNames(declarationInfo: ParadoxDeclarationInfo, nameSuggestionContext: PsiElement?, result: MutableSet<String>): SuggestedNameInfo? {
        val name = declarationInfo.name
        val suggestedNames = mutableSetOf<String>()

        val fromName = mutableSetOf<String>()
        var i = name.lastIndexOf('.')
        val prefix = if (i != -1) name.substring(0, i + 1) else ""
        i = if (i != -1) i else 0
        while (true) {
            i = name.indexOf('_', i)
            if (i == -1) break
            i++
            val n = prefix + name.substring(i)
            if (n.isEmpty()) break
            fromName.add(n)
        }
        suggestedNames.addAll(fromName)

        //parentDefinitionName作为前缀
        run {
            if (declarationInfo is ParadoxDeclarationInfo.Definition) return@run //排除本身是定义的情况
            val parentDefinition = nameSuggestionContext?.findParentDefinition() ?: return@run
            val parentDeclarationInfo = ParadoxDeclarationInfo.from(parentDefinition) ?: return@run
            if (!isSupported(parentDeclarationInfo)) return@run
            val parentDefinitionInfo = parentDefinition.definitionInfo ?: return@run
            val parentDefinitionName = parentDefinitionInfo.name
            if (parentDefinitionName.isEmpty()) return@run
            suggestedNames.add("${parentDefinitionName}_$name")
            suggestedNames.addAll(fromName.map { "${parentDefinitionName}_$it" })
        }

        if (suggestedNames.isEmpty()) return null
        result.addAll(suggestedNames)
        return SuggestedNameInfo.NULL_INFO //do not use statistics yet
    }
}
