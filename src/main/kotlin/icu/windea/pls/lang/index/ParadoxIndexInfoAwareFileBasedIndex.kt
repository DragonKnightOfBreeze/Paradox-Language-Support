package icu.windea.pls.lang.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.lang.analysis.ParadoxAnalysisManager
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxIndexInfo

sealed class ParadoxIndexInfoAwareFileBasedIndex<V, out T : ParadoxIndexInfo> : IndexInfoAwareFileBasedIndex<V, T>() {
    override fun checkFile(file: VirtualFile, project: Project, expectGameType: ParadoxGameType?): Boolean {
        // ensure file info is resolved here
        ParadoxAnalysisManager.getFileInfo(file)
        // check game type at file level
        if (expectGameType == null) return true
        if (selectGameType(file) != expectGameType) return false
        return true
    }
}
