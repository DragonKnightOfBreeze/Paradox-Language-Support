package icu.windea.pls.lang.util

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.relatedLocalisationPatterns
import icu.windea.pls.core.annotations.Inferred
import icu.windea.pls.core.isEscapedCharAt
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextRenderer
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.greenStub
import icu.windea.pls.model.ParadoxLocalisationInfo
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object ParadoxLocalisationManager {
    object Keys : KeyRegistry() {
        val cachedLocalisationInfo by createKey<CachedValue<ParadoxLocalisationInfo>>(Keys)
        val cachedLocalizedName by createKey<CachedValue<String>>(Keys)
    }

    fun getInfo(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
        return doGetInfoFromCache(element) // 从缓存中获取
    }

    private fun doGetInfoFromCache(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedLocalisationInfo) {
            ProgressManager.checkCanceled()
            val value = runReadAction { doGetInfo(element) }
            value.withDependencyItems(element)
        }
    }

    private fun doGetInfo(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
        doGetInfoFromStub(element)?.let { return it }
        return doGetInfoFromPsi(element)
    }

    private fun doGetInfoFromStub(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
        val stub = element.greenStub ?: return null
        val name = stub.name
        val type = stub.type
        val gameType = stub.gameType
        return ParadoxLocalisationInfo(name, type, gameType)
    }

    private fun doGetInfoFromPsi(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
        val file = element.containingFile.originalFile.virtualFile ?: return null
        val name = element.name
        val type = ParadoxLocalisationType.resolve(file) ?: return null
        val gameType = selectGameType(file) ?: return null
        return ParadoxLocalisationInfo(name, type, gameType)
    }

    fun getLocalizedText(element: ParadoxLocalisationProperty): String? {
        return doGetLocalizedTextFromCache(element) // 从缓存中获取
    }

    private fun doGetLocalizedTextFromCache(element: ParadoxLocalisationProperty): String? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedLocalizedName) {
            ProgressManager.checkCanceled()
            val value = doGetLocalizedText(element)
            value.withDependencyItems(element)
        }
    }

    private fun doGetLocalizedText(element: ParadoxLocalisationProperty): String? {
        return ParadoxLocalisationTextRenderer().render(element).orNull()
    }

    fun getRelatedScriptedVariables(element: ParadoxLocalisationProperty): List<ParadoxScriptScriptedVariable> {
        return doGetRelatedScriptedVariables(element) // 直接获取
    }

    private fun doGetRelatedScriptedVariables(element: ParadoxLocalisationProperty): List<ParadoxScriptScriptedVariable> {
        val name = element.name.orNull() ?: return emptyList()
        val project = element.project
        val gameType = selectGameType(element)
        if (gameType == null) return emptyList()
        val selector = selector(project, element).scriptedVariable().contextSensitive()
        ProgressManager.checkCanceled()
        // search for all scripted variable with same name
        val result = ParadoxScriptedVariableSearch.search(name, selector).findAll().toList()
        return result
    }

    fun getRelatedDefinitions(element: ParadoxLocalisationProperty): List<ParadoxScriptDefinitionElement> {
        return doGetRelatedDefinitions(element) // 直接获取
    }

    private fun doGetRelatedDefinitions(element: ParadoxLocalisationProperty): List<ParadoxScriptDefinitionElement> {
        val name = element.name.orNull() ?: return emptyList()
        val project = element.project
        val gameType = selectGameType(element) ?: return emptyList()
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val patterns = configGroup.relatedLocalisationPatterns
        val namesToSearch = mutableSetOf<String>()
        patterns.forEach { (prefix, suffix) ->
            name.removeSurroundingOrNull(prefix, suffix)?.let { namesToSearch += it }
        }
        if (namesToSearch.isEmpty()) return emptyList()
        val selector = selector(project, element).definition().contextSensitive()
        val result = mutableListOf<ParadoxScriptDefinitionElement>()
        namesToSearch.forEach f1@{ nameToSearch ->
            ProgressManager.checkCanceled()
            // op: only search definitions declared by a property, rather than by a file, to optimize performance
            ParadoxDefinitionSearch.search(nameToSearch, null, selector, forFile = false).findAll().forEach f2@{ definition ->
                ProgressManager.checkCanceled()
                val definitionInfo = definition.definitionInfo ?: return@f2
                val definitionName = definitionInfo.name.orNull() ?: return@f2
                definitionInfo.localisations.forEach f3@{ l ->
                    val resolved = CwtLocationExpressionManager.resolvePlaceholder(l.locationExpression, definitionName) ?: return@f3
                    if (resolved != name) return@f3
                    result += definition
                    return@f2
                }
            }
        }
        return result
    }

    @Inferred
    fun isSpecialLocalisation(element: ParadoxLocalisationProperty): Boolean {
        // 存在一些特殊的本地化，不能直接用来渲染文本
        val file = element.containingFile ?: return false
        val fileName = file.name
        if (fileName.startsWith("name_system_")) return true // e.g., name_system_l_english.yml
        return false
    }

    @Inferred
    fun isRichText(text: String): Boolean {
        for ((i, c) in text.withIndex()) {
            // accept left bracket & do not check escape (`[[`)
            if (c == '[') return true
            // accept special markers && check escape
            if (c in "$£§#" && !text.isEscapedCharAt(i)) return true
        }
        return false
    }
}
