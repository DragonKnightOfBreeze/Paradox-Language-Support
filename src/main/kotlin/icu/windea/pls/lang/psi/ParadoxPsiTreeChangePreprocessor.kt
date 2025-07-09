package icu.windea.pls.lang.psi

import com.intellij.openapi.project.*
import com.intellij.psi.impl.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

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
                val fileInfo = file.fileInfo ?: return
                if (!ParadoxFileManager.inLocalisationPath(fileInfo.path)) return
                ParadoxModificationTrackers.LocalisationFileTracker.incModificationCount()
            }
        }
    }
}
