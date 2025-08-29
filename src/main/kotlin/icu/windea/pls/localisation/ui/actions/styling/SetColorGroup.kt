package icu.windea.pls.localisation.ui.actions.styling

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import icu.windea.pls.lang.util.ParadoxTextColorManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.ParadoxTextColorInfo

//这里actions是基于project动态获取的，需要特殊处理

class SetColorGroup : DefaultActionGroup() {
    companion object {
        private val setColorActionCache = CacheBuilder.newBuilder()
            .weakKeys()
            .build(CacheLoader.from<ParadoxTextColorInfo, SetColorAction> { SetColorAction(it) })
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        if (editor == null) return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (file !is ParadoxLocalisationFile) return
        val colorConfigs = ParadoxTextColorManager.getInfos(file.project, file)
        if (colorConfigs.isEmpty()) return
        val actions = colorConfigs.map { setColorActionCache.get(it) }
        synchronized(this) {
            removeAll()
            addAll(actions)
        }
    }
}
