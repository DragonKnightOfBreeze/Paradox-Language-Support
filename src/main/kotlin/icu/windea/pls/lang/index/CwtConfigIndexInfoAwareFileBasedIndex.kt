package icu.windea.pls.lang.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.CwtConfigIndexInfo

sealed class CwtConfigIndexInfoAwareFileBasedIndex<V, out T : CwtConfigIndexInfo> : IndexInfoAwareFileBasedIndex<V, T>() {
    override fun checkFile(file: VirtualFile, project: Project, expectGameType: ParadoxGameType?): Boolean {
        // check game type at file level
        if (expectGameType == null) return true
        val configGroup = CwtConfigManager.getContainingConfigGroup(file, project) ?: return false
        if (configGroup.gameType != ParadoxGameType.Core && configGroup.gameType != expectGameType) return false
        return true
    }
}
