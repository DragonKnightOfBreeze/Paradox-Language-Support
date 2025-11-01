package icu.windea.pls.ep.modifier

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.documentation.DocumentationBuilder
import icu.windea.pls.core.util.SyncedKeyRegistry
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.codeInsight.completion.gameType
import icu.windea.pls.lang.psi.mock.ParadoxModifierElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxModifierInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 提供对修正的支持。
 *
 * @see ParadoxModifierElement
 */
@WithGameTypeEP
interface ParadoxModifierSupport {
    /**
     * @param element 进行匹配时的上下文PSI元素。
     */
    fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean

    fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo?

    fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>)

    fun getModificationTracker(modifierInfo: ParadoxModifierInfo): ModificationTracker? = null

    fun getModifierCategories(modifierElement: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>?

    /**
     * 构建修正的快速文档中的定义部分。
     * @return 此解析器是否适用。
     */
    fun buildDocumentationDefinition(modifierElement: ParadoxModifierElement, builder: DocumentationBuilder): Boolean = false

    /**
     * 构建定义的快速文档中的定义部分中的对应的生成的修正的那一部分。
     * @return 此解析器是否适用。
     */
    fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean = false

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxModifierSupport>("icu.windea.pls.modifierSupport")

        fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
            val gameType = configGroup.gameType
            return EP_NAME.extensionList.any f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f false
                ep.matchModifier(name, element, configGroup)
            }
        }

        fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
            val gameType = configGroup.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f null
                ep.resolveModifier(name, element, configGroup)
                    ?.also { it.support = ep }
            }
        }

        fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
            val gameType = context.gameType ?: return
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f
                ep.completeModifier(context, result, modifierNames)
            }
        }

        fun getModifierCategories(element: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
            val gameType = element.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f null
                ep.getModifierCategories(element)
            }
        }

        fun getDocumentationDefinition(element: ParadoxModifierElement, builder: DocumentationBuilder): Boolean {
            val gameType = element.gameType
            return EP_NAME.extensionList.any f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f false
                ep.buildDocumentationDefinition(element, builder)
            }
        }

        fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.any f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f false
                ep.buildDDocumentationDefinitionForDefinition(definition, definitionInfo, builder)
            }
        }
    }

    object Keys : SyncedKeyRegistry()
}
