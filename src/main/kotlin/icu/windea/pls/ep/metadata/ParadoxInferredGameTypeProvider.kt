package icu.windea.pls.ep.metadata

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.model.ParadoxGameType

interface ParadoxInferredGameTypeProvider {
    fun getGameType(rootFile: VirtualFile): ParadoxGameType?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxInferredGameTypeProvider>("icu.windea.pls.inferredGameTypeProvider")

        fun getGameType(rootFile: VirtualFile): ParadoxGameType? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.getGameType(rootFile) }
        }
    }
}
