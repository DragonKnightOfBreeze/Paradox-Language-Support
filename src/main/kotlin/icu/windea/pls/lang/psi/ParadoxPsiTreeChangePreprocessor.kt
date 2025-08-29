package icu.windea.pls.lang.psi

import com.intellij.openapi.project.DumbService
import com.intellij.psi.impl.PsiModificationTrackerImpl
import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.script.psi.ParadoxScriptFile

//com.intellij.psi.impl.PsiModificationTrackerImpl

class ParadoxPsiTreeChangePreprocessor : PsiTreeChangePreprocessor {
    class Listener : DumbService.DumbModeListener {
        override fun enteredDumbMode() {
            ParadoxModificationTrackers.refreshPsi()
        }

        override fun exitDumbMode() {
            ParadoxModificationTrackers.refreshPsi()
        }
    }

    //这个方法应当尽可能地快
    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        if (!PsiModificationTrackerImpl.canAffectPsi(event)) return

        val file = event.file ?: return
        when {
            file is ParadoxScriptFile -> {
                val fileInfo = file.fileInfo ?: return
                ParadoxModificationTrackers.ScriptFileTracker.incModificationCount()

                val trackers = ParadoxModificationTrackers.ScriptFileTrackers.values
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
            file is ParadoxLocalisationFile -> {
                if(file.fileInfo == null) return
                ParadoxModificationTrackers.LocalisationFileTracker.incModificationCount()
            }
            file is ParadoxCsvFile -> {
                if(file.fileInfo == null) return
                ParadoxModificationTrackers.CsvFileTracker.incModificationCount()
            }
        }
    }
}
