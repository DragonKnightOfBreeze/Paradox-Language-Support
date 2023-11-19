package icu.windea.pls.core.psi

import com.intellij.psi.impl.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.script.psi.*

class ParadoxPsiTreeChangePreprocessor: PsiTreeChangePreprocessor {
    //com.intellij.psi.impl.PsiModificationTrackerImpl.treeChanged
    
    //这个方法应当尽可能地快
    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        if(!PsiModificationTrackerImpl.canAffectPsi(event)) return
        
        val file = event.file ?: return
        if(file !is ParadoxScriptFile) return
        val fileInfo = file.fileInfo ?: return
        val filePath = fileInfo.pathToEntry.path
        val fileExtension = fileInfo.pathToEntry.fileExtension?.lowercase() //ignore case
        //注意这里需要先获取服务再获取trackers
        val trackers = ParadoxModificationTrackerProvider.getInstance(file.project).ScriptFileTrackers.values
        for(tracker in trackers) {
            val keys = tracker.keys
            val keysSize = keys.size
            for(i in 0 until keysSize) {
                val key = keys[i]
                if((filePath.isEmpty() || key.path.matchesPath(filePath)) && (key.extensions.isEmpty() || key.extensions.contains(fileExtension))) {
                    tracker.incModificationCount()
                    break
                }
            }
        }
    }
}
