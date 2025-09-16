package icu.windea.pls.config.configGroup

import com.intellij.psi.impl.PsiModificationTrackerImpl
import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import icu.windea.pls.PlsFacade
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.ep.configGroup.BuiltInCwtConfigGroupFileProvider
import icu.windea.pls.ep.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.model.ParadoxGameType

/**
 * 用于监听规则文件的PSI的更改，以便在必要时通知规则分组发生更改。
 */
class CwtConfigGroupPsiTreeChangePreprocessor : PsiTreeChangePreprocessor {
    // 这个方法应当尽可能地快
    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        if (!PsiModificationTrackerImpl.canAffectPsi(event)) return

        val file = event.file ?: return
        if (file !is CwtFile) return
        val vFile = file.virtualFile ?: return
        val project = file.project
        val configGroupService = PlsFacade.getConfigGroupService()
        val configGroups = mutableSetOf<CwtConfigGroup>()
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            if (fileProvider is BuiltInCwtConfigGroupFileProvider) return@f
            if (!fileProvider.isEnabled) return@f // 如果未启用则不要把规则分组标记为已更改
            val configGroup = fileProvider.getContainingConfigGroup(vFile, project) ?: return@f
            configGroups.add(configGroup)
            if (configGroup.gameType == ParadoxGameType.Core) {
                ParadoxGameType.getAll().forEach { gameType -> configGroups.add(configGroupService.getConfigGroup(project, gameType)) }
            }
        }
        val configGroupsToChange = configGroups.filter { !it.changed.get() }
        if (configGroupsToChange.isEmpty()) return
        configGroupsToChange.forEach { configGroup -> configGroup.changed.set(true) }
        configGroupService.updateRefreshFloatingToolbar(project)
    }
}
