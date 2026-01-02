package icu.windea.pls.lang.psi

import com.intellij.openapi.project.DumbService
import com.intellij.psi.impl.PsiModificationTrackerImpl
import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.PlsDaemonManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.script.psi.ParadoxScriptFile

// com.intellij.psi.impl.PsiModificationTrackerImpl

class ParadoxPsiTreeChangePreprocessor : PsiTreeChangePreprocessor {
    class Listener : DumbService.DumbModeListener {
        override fun enteredDumbMode() {
            PlsDaemonManager.refreshAllFileTrackers()
        }

        override fun exitDumbMode() {
            PlsDaemonManager.refreshAllFileTrackers()
        }
    }

    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        // This method should be very fast

        if (!PsiModificationTrackerImpl.canAffectPsi(event)) return

        val file = event.file ?: return
        if (file !is ParadoxFile) return
        val fileInfo = file.fileInfo ?: return
        when (file) {
            is ParadoxScriptFile -> {
                ParadoxModificationTrackers.ScriptFile.incModificationCount()
                val trackers = ParadoxModificationTrackers.ScriptFileMap.values
                for (tracker in trackers) {
                    val patterns = tracker.patterns
                    for (pattern in patterns) {
                        if (fileInfo.path.path.matchesAntPattern(pattern)) {
                            tracker.incModificationCount()
                            break
                        }
                    }
                }
            }
            is ParadoxLocalisationFile -> {
                ParadoxModificationTrackers.LocalisationFile.incModificationCount()
            }
            is ParadoxCsvFile -> {
                ParadoxModificationTrackers.CsvFile.incModificationCount()
            }
        }
    }
}
