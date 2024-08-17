package icu.windea.pls.localisation.ui.actions.styling

import com.google.common.cache.*
import com.intellij.openapi.actionSystem.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

//这里actions是基于project动态获取的，需要特殊处理

class SetColorGroup : DefaultActionGroup() {
    companion object {
        private val setColorActionCache = CacheBuilder.newBuilder()
            .weakKeys()
            .build(CacheLoader.from<ParadoxTextColorInfo, SetColorAction> { SetColorAction(it) })
    }
    
    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        if(editor == null) return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if(file !is ParadoxLocalisationFile) return
        val colorConfigs = ParadoxTextColorManager.getInfos(file.project, file)
        if(colorConfigs.isEmpty()) return
        val actions = colorConfigs.map { setColorActionCache.get(it) }
        synchronized(this) {
            removeAll()
            addAll(actions)
        }
    }
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
