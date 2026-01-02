package icu.windea.pls.ep.analyze

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType

/**
 * 提供推断的游戏类型。
 *
 * @see ParadoxGameType
 */
interface ParadoxInferredGameTypeProvider {
    fun get(rootFile: VirtualFile): ParadoxGameType?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxInferredGameTypeProvider>("icu.windea.pls.inferredGameTypeProvider")
    }
}
