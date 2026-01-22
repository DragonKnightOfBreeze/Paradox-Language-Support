package icu.windea.pls.config.configGroup

import com.intellij.psi.impl.PsiModificationTrackerImpl
import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastSet
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.ep.config.configGroup.CwtBuiltInConfigGroupFileProvider
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.model.ParadoxGameType

/**
 * 用于监听规则文件的PSI的更改，以便在必要时通知规则分组发生更改。
 */
@Optimized
class CwtConfigGroupPsiTreeChangePreprocessor : PsiTreeChangePreprocessor {
    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        // This method should be very fast

        if (!PsiModificationTrackerImpl.canAffectPsi(event)) return

        val file = event.file ?: return
        if (file !is CwtFile) return
        val vFile = file.virtualFile ?: return
        val project = file.project
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val configGroupService = CwtConfigGroupService.getInstance(project)
        val configGroupsToChange = FastSet<CwtConfigGroup>()
        fileProviders.forEach f@{ fileProvider ->
            if (fileProvider is CwtBuiltInConfigGroupFileProvider) return@f
            if (!fileProvider.isEnabled) return@f // 如果未启用则不要把规则分组标记为已更改
            val configGroup = fileProvider.getContainingConfigGroup(vFile, project) ?: return@f
            if (!configGroup.changed) configGroupsToChange += configGroup
            if (configGroup.gameType != ParadoxGameType.Core) return@f
            ParadoxGameType.getAll().forEachFast { gameType ->
                val extraConfigGroup = configGroupService.getConfigGroup(gameType)
                if (!extraConfigGroup.changed) configGroupsToChange += extraConfigGroup
            }
        }
        if (configGroupsToChange.isEmpty()) return
        configGroupsToChange.forEach { configGroup -> configGroup.changed = true }
        configGroupService.updateRefreshFloatingToolbar()
    }
}
