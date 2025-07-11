package icu.windea.pls.config.configGroup

import com.intellij.openapi.editor.toolbar.floating.*
import com.intellij.psi.impl.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.configGroup.*
import icu.windea.pls.model.*

/**
 * 用于监听规则文件的PSI的更改，以便在必要时通知规则分组发生更改。
 */
class CwtConfigGroupPsiTreeChangePreprocessor : PsiTreeChangePreprocessor {
    //这个方法应当尽可能地快
    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        if (!PsiModificationTrackerImpl.canAffectPsi(event)) return

        val file = event.file ?: return
        if (file !is CwtFile) return
        val vFile = file.virtualFile ?: return
        val project = file.project
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val configGroups = mutableSetOf<CwtConfigGroup>()
        fileProviders.forEach f@{ fileProvider ->
            if (fileProvider is BuiltInCwtConfigGroupFileProvider) return@f
            val configGroup = fileProvider.getContainingConfigGroup(vFile, project) ?: return@f
            if (configGroup.gameType == null) {
                ParadoxGameType.entries.forEach { gameType ->
                    configGroups += PlsFacade.getConfigGroup(project, gameType)
                }
            } else {
                configGroups += configGroup
            }
        }
        val configGroupsToChange = configGroups.filter { !it.changed.get() }
        if (configGroupsToChange.isEmpty()) return
        configGroupsToChange.forEach { configGroup -> configGroup.changed.set(true) }
        FloatingToolbarProvider.EP_NAME.findExtensionOrFail(ConfigGroupRefreshFloatingProvider::class.java).updateToolbarComponents(project)
    }
}
