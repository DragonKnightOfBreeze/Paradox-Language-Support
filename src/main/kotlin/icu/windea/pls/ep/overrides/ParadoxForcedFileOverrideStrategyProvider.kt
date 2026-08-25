package icu.windea.pls.ep.overrides

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileSystemItem
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.model.overrides.ParadoxOverrideStrategy

class ParadoxForcedFileOverrideStrategyProvider : ParadoxOverrideStrategyProvider {
    override fun get(target: Any): ParadoxOverrideStrategy? {
        if (target !is PsiFileSystemItem && target !is VirtualFile) return null
        return getOverrideStrategy()
    }

    override fun get(searchParameters: ParadoxSearchParameters<*>): ParadoxOverrideStrategy? {
        if (searchParameters !is ParadoxFilePathSearch.Parameters) return null
        return getOverrideStrategy()
    }

    private fun getOverrideStrategy(): ParadoxOverrideStrategy {
        // use FIOS for files and directories
        return ParadoxOverrideStrategy.FIOS
    }
}
