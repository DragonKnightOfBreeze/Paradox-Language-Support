package icu.windea.pls.lang.resolve

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.ep.resolve.localisation.ParadoxCompositeLocalisationIconSupport
import icu.windea.pls.ep.resolve.localisation.ParadoxDefinitionBasedLocalisationIconSupport
import icu.windea.pls.ep.resolve.localisation.ParadoxLocalisationIconSupport
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.orSpecific

@Optimized
object ParadoxLocalisationIconService {
    /**
     * @see ParadoxLocalisationIconSupport.resolve
     */
    fun resolve(name: String, element: ParadoxLocalisationIcon, project: Project): PsiElement? {
        val gameType = selectGameType(element)
        val supports = ParadoxLocalisationIconSupport.EP_NAME.extensionList
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            ProgressManager.checkCanceled() // 3.0.1 optimize: check cancellation immediately before applying logic
            support.resolve(name, element, project)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxLocalisationIconSupport.resolveAll
     */
    fun resolveAll(name: String, element: ParadoxLocalisationIcon, project: Project): Collection<PsiElement> {
        val gameType = selectGameType(element)
        val supports = ParadoxLocalisationIconSupport.EP_NAME.extensionList
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            ProgressManager.checkCanceled() // 3.0.1 optimize: check cancellation immediately before applying logic
            support.resolveAll(name, element, project).orNull()?.let { return it }
        }
        return emptyList()
    }

    /**
     * @see ParadoxLocalisationIconSupport.complete
     */
    fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val gameType = context.gameType
        val supports = ParadoxLocalisationIconSupport.EP_NAME.extensionList
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            ProgressManager.checkCanceled() // 3.0.1 optimize: check cancellation immediately before applying logic
            support.complete(context, result)
        }
    }

    fun getDefinitionTypes(gameType: ParadoxGameType): Set<String> {
        val result = mutableSetOf<String>()
        val supports = ParadoxLocalisationIconSupport.EP_NAME.extensionList
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            collectDefinitionTypes(support, result)
        }
        if (result.isEmpty()) return emptySet()
        return result
    }

    private fun collectDefinitionTypes(support: ParadoxLocalisationIconSupport, result: MutableSet<String>) {
        when (support) {
            is ParadoxCompositeLocalisationIconSupport -> support.supports.forEachFast { doCollectDefinitionTypes(it, result) }
            else -> doCollectDefinitionTypes(support, result)
        }
    }

    private fun doCollectDefinitionTypes(support: ParadoxLocalisationIconSupport, result: MutableSet<String>) {
        if (support is ParadoxDefinitionBasedLocalisationIconSupport) {
            result += support.definitionType
        }
    }

    fun getNameGetters(gameType: ParadoxGameType, definitionInfo: ParadoxDefinitionInfo): Set<(String) -> String?> {
        val result = mutableSetOf<(String) -> String?>()
        val supports = ParadoxLocalisationIconSupport.EP_NAME.extensionList
        supports.forEachFast f@{ support ->
            if (gameType.orSpecific() != null && !support.supports(gameType)) return@f // check game type first
            collectNameGetters(definitionInfo, support, result)
        }
        if (result.isEmpty()) return emptySet()
        return result
    }

    private fun collectNameGetters(definitionInfo: ParadoxDefinitionInfo, support: ParadoxLocalisationIconSupport, result: MutableSet<(String) -> String?>) {
        when (support) {
            is ParadoxCompositeLocalisationIconSupport -> support.supports.forEachFast { doCollectNameGetters(definitionInfo, it, result) }
            else -> doCollectNameGetters(definitionInfo, support, result)
        }
    }

    private fun doCollectNameGetters(definitionInfo: ParadoxDefinitionInfo, support: ParadoxLocalisationIconSupport, result: MutableSet<(String) -> String?>) {
        if (support is ParadoxDefinitionBasedLocalisationIconSupport) {
            if (ParadoxDefinitionTypeExpression.resolve(support.definitionType).matches(definitionInfo)) {
                result.add(support.nameGetter)
            }
        }
    }
}

