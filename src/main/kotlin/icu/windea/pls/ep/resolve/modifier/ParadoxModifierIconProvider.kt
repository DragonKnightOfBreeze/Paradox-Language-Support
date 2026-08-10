package icu.windea.pls.ep.resolve.modifier

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModifierInfo

/**
 * 用于为修正提供图标的文件路径。
 *
 * 注意：修正的图标对应的文件名是**忽略大小写**的。 (#385)
 */
interface ParadoxModifierIconProvider {
    fun supports(gameType: ParadoxGameType) = true

    // TODO 3.0.1 clarify: ignore case (for file names only) (#385)
    /** 根据传入的 [modifierInfo] 和 [element]，加入作为候选的修正图标的文件路径（不包含形如 `.dds` 的扩展名）到 [registry]。 */
    fun addModifierIconBaseName(modifierInfo: ParadoxModifierInfo, element: PsiElement, registry: MutableSet<String>)

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxModifierIconProvider>("icu.windea.pls.modifierIconProvider")
    }
}
