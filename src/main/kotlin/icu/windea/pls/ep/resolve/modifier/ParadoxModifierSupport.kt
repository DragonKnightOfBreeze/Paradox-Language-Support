package icu.windea.pls.ep.resolve.modifier

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.util.KeyRegistryWithSync
import icu.windea.pls.core.util.builders.DocumentationBuilder
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.psi.mock.ParadoxModifierElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxModifierInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 提供对修正的支持。
 *
 * @see ParadoxModifierElement
 */
@WithGameTypeEP
interface ParadoxModifierSupport {
    /**
     * @param element 进行匹配时的上下文 PSI 元素。
     */
    fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean

    fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo?

    fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>)

    fun getModificationTracker(modifierInfo: ParadoxModifierInfo): ModificationTracker? = null

    fun getModifierCategories(modifierElement: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>?

    /**
     * 构建修正的快速文档中的定义部分。
     *
     * @return 此扩展点是否适用。
     */
    fun buildDocumentationDefinition(modifierElement: ParadoxModifierElement, builder: DocumentationBuilder): Boolean = false

    /**
     * 构建定义的快速文档中的定义部分中的对应的生成的修正的那一部分。
     *
     * @return 此扩展点是否适用。
     */
    fun buildDDocumentationDefinitionForDefinition(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean = false

    object Keys : KeyRegistryWithSync()

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxModifierSupport>("icu.windea.pls.modifierSupport")
    }
}
