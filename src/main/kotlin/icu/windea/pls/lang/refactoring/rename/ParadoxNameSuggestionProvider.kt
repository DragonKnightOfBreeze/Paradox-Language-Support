package icu.windea.pls.lang.refactoring.rename

import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.SuggestedNameInfo
import com.intellij.refactoring.rename.NameSuggestionProvider
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.ParadoxLanguage
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.psi.findParentDefinition
import icu.windea.pls.lang.psi.findParentDefinitionInjection
import icu.windea.pls.model.codeInsight.ParadoxTargetInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

// org.jetbrains.kotlin.idea.base.codeInsight.KotlinNameSuggestionProvider

class ParadoxNameSuggestionProvider : NameSuggestionProvider {
    override fun getSuggestedNames(element: PsiElement, nameSuggestionContext: PsiElement?, result: MutableSet<String>): SuggestedNameInfo? {
        if (element.language !is ParadoxLanguage) return null
        val declarationInfo = ParadoxTargetInfo.from(element) ?: return null
        if (!isSupported(declarationInfo)) return null
        return collectSuggestedNames(declarationInfo, nameSuggestionContext, result)
    }

    private fun isSupported(declarationInfo: ParadoxTargetInfo): Boolean {
        return when (declarationInfo) {
            is ParadoxTargetInfo.Definition -> {
                when (declarationInfo.type) {
                    ParadoxDefinitionTypes.event -> false // 排除事件
                    else -> true
                }
            }
            else -> true
        }
    }

    private fun collectSuggestedNames(declarationInfo: ParadoxTargetInfo, nameSuggestionContext: PsiElement?, result: MutableSet<String>): SuggestedNameInfo? {
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

        // parentDefinitionName 作为前缀
        run {
            if (declarationInfo is ParadoxTargetInfo.Definition) return@run // 排除本身是定义的情况
            val parentDefinition = nameSuggestionContext?.findParentDefinition() ?: return@run
            val parentDeclarationInfo = ParadoxTargetInfo.from(parentDefinition) ?: return@run
            if (!isSupported(parentDeclarationInfo)) return@run
            val parentDefinitionInfo = parentDefinition.definitionInfo ?: return@run
            val parentDefinitionName = parentDefinitionInfo.name.orNull() ?: return@run
            suggestedNames.add("${parentDefinitionName}_$name")
            suggestedNames.addAll(fromName.map { "${parentDefinitionName}_$it" })
        }

        // 兼容定义注入
        run {
            if (declarationInfo is ParadoxTargetInfo.DefinitionInjection) return@run // 排除本身是定义注入的情况
            val parentDefinitionInjection = nameSuggestionContext?.findParentDefinitionInjection() ?: return@run
            val parentDeclarationInfo = ParadoxTargetInfo.from(parentDefinitionInjection) ?: return@run
            if (!isSupported(parentDeclarationInfo)) return@run
            val parentDefinitionInjectionInfo = parentDefinitionInjection.definitionInjectionInfo ?: return@run
            val parentDefinitionName = parentDefinitionInjectionInfo.target?.orNull() ?: return@run
            if (parentDefinitionName.isEmpty()) return@run
            suggestedNames.add("${parentDefinitionName}_$name")
            suggestedNames.addAll(fromName.map { "${parentDefinitionName}_$it" })
        }

        if (suggestedNames.isEmpty()) return null
        result.addAll(suggestedNames)
        return SuggestedNameInfo.NULL_INFO // do not use statistics yet
    }
}
