package icu.windea.pls.lang.psi

import com.intellij.psi.impl.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

//com.intellij.psi.impl.PsiModificationTrackerImpl

class ParadoxPsiTreeChangePreprocessor : PsiTreeChangePreprocessor {
    //这个方法应当尽可能地快
    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        if(!PsiModificationTrackerImpl.canAffectPsi(event)) return
        
        val file = event.file ?: return
        when {
            file is ParadoxScriptFile -> {
                ParadoxModificationTrackers.LocalisationFileTracker.incModificationCount()
            }
            file is ParadoxLocalisationFile -> {
                ParadoxModificationTrackers.ScriptFileTracker.incModificationCount()
                
                val fileInfo = file.fileInfo ?: return
                val filePath = fileInfo.pathToEntry.path
                //注意这里需要先获取服务再获取trackers
                val trackers = ParadoxModificationTrackers.ScriptFileTrackers.values
                for(tracker in trackers) {
                    val patterns = tracker.patterns
                    for(pattern in patterns) {
                        if(filePath.matchesAntPattern(pattern, ignoreCase = true)) {
                            tracker.incModificationCount()
                            break
                        }
                    }
                }
            }
        }
    }
}
