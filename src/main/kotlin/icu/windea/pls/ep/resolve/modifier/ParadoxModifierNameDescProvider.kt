package icu.windea.pls.ep.resolve.modifier

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModifierInfo

/**
 * 指定修正的名字和描述的本地化键名。
 *
 * 注意：修正的名字和描述的本地化键名是**忽略大小写**的。 (#385)
 */
interface ParadoxModifierNameDescProvider {
    fun supports(gameType: ParadoxGameType) = true

    // NOTE 3.0.1 clarify: ignore case (should be and treat as) (#385)
    /** 根据传入的 [modifierInfo] 和 [element]，加入作为候选的修正名字的本地化键名到 [registry]。 */
    fun addModifierNameKey(modifierInfo: ParadoxModifierInfo, element: PsiElement, registry: MutableSet<String>)

    // NOTE 3.0.1 clarify: ignore case (should be and treat as) (#385)
    /** 根据传入的 [modifierInfo] 和 [element]，加入作为候选的修正描述的本地化键名到 [registry]。 */
    fun addModifierDescKey(modifierInfo: ParadoxModifierInfo, element: PsiElement, registry: MutableSet<String>)

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxModifierNameDescProvider>("icu.windea.pls.modifierNameDescProvider")
    }
}
