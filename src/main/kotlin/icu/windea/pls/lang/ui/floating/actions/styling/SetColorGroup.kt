package icu.windea.pls.lang.ui.floating.actions.styling

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.lang.util.ParadoxTextColorManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.ParadoxTextColorInfo

// 动态获取可用的 SetColorAction
private val setColorActionCache = CacheBuilder().weakKeys().build<ParadoxTextColorInfo, SetColorAction> { key -> SetColorAction(key) }

class SetColorGroup : DefaultActionGroup() {
    private val actionBaseName get() = ChronicleBundle.message("action.Pls.Localisation.Styling.SetColorGroup.text")

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        if (editor == null) return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return
        if (file !is ParadoxLocalisationFile) return
        val colorInfos = ParadoxTextColorManager.getInfos(file.project, file)
        if (colorInfos.isEmpty()) return
        e.presentation.text = actionBaseName
        val actions = colorInfos.map { setColorActionCache.get(it) }
        synchronized(this) {
            removeAll()
            addAll(actions)
        }
    }
}
