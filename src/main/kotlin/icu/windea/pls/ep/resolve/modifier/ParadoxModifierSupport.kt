package icu.windea.pls.ep.resolve.modifier

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModifierInfo

/**
 * 提供对修正的支持。
 *
 * 注意：修正名是**忽略大小写**的。 (#385)
 *
 * @see ParadoxModifierInfo
 * @see ParadoxModifierLightElement
 */
interface ParadoxModifierSupport {
    fun supports(gameType: ParadoxGameType) = true

    // NOTE 3.0.1 clarify: ignore case (for modifier names) (#385)
    fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean

    // TODO 3.0.1 clarify: ignore case (for modifier names) (#385)
    fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo?

    fun completeModifier(context: ParadoxCompletionContext, result: CompletionResultSet, modifierNames: MutableSet<@CaseInsensitive String>)

    /**
     * 根据指定的 [element] 和 [configGroup]，遍历所有修正（[ParadoxModifierLightElement]）。
     * 如果返回 `false`，则会终止遍历，因而也会终止遍历 EP。
     */
    fun processModifier(element: PsiElement, configGroup: CwtConfigGroup, processor: Processor<ParadoxModifierLightElement>): Boolean

    fun getModificationTracker(modifierInfo: ParadoxModifierInfo): ModificationTracker? = null

    object Keys : KeyRegistry()

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxModifierSupport>("icu.windea.pls.modifierSupport")
    }
}
