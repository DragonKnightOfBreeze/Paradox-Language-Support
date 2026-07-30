package icu.windea.pls.lang.analysis

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import icu.windea.pls.core.util.OnceMarker

@Service
class ParadoxAnalysisLifecycleService : Disposable {
    override fun dispose() {
        // 避免内存泄露
        ParadoxAnalysisDataManager.trackedFiles.keys.forEach { file ->
            ParadoxAnalysisDataManager.Keys.clear(file)
        }
        ParadoxAnalysisDataManager.trackedFiles.clear()
        ParadoxAnalysisDataManager.markedRootInfo = null
        ParadoxAnalysisDataManager.markedFileInfo = null
        ParadoxAnalysisDataManager.markedRootPath = null
        ParadoxAnalysisDataManager.markedRootDirectory = null
        ParadoxAnalysisDataManager.markedConfigPath = null
        ParadoxAnalysisDataManager.markedConfigDirectory = null
    }

    companion object {
        private val marker = OnceMarker()

        fun ensureLoaded() {
            if (marker.mark()) getInstance()
        }

        @JvmStatic
        fun getInstance(): ParadoxAnalysisLifecycleService = service()
    }
}
