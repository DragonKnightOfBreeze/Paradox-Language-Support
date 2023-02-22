package icu.windea.pls.core.tool.exporter

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.table.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.core.tool.*
import icu.windea.pls.lang.model.*

class ParadoxToLauncherJsonExporter : ParadoxModDependenciesExporter {
    
    var defaultSelected: VirtualFile? = null
    
    override val text: String = PlsBundle.message("mod.exporter.launcherJson")
    
    override fun execute(project: Project, tableView: TableView<ParadoxModDependencySettingsState>, tableModel: ParadoxModDependenciesTableModel) {
        val settings = tableModel.settings
        val gameType = settings.gameType.orDefault()
        setDefaultSelected(gameType)
        val descriptor = FileSaverDescriptor(
            PlsBundle.message("mod.exporter.launcherJson.title"),
            "",
            "json"
        )
            .apply { putUserData(PlsDataKeys.gameTypeKey, gameType) }
        val saved = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, tableView).save("playlist.json")
        val savedFile = saved?.getVirtualFile(true) ?: return
        val collectionName = savedFile.nameWithoutExtension
        val mods = settings.modDependencies.mapIndexedNotNull t@{ i, s ->
            val name = s.name?.takeIfNotEmpty() ?: return@t null   
            val steamId = s.remoteFileId ?: return@t null
            ParadoxLauncherJson.Mod(name, s.enabled, i, steamId)
        }
        val json = ParadoxLauncherJson(gameType.id, mods, collectionName)
        runWriteAction {
            jsonMapper.writeValue(savedFile.getOutputStream(this), json)
        }
        val count = mods.size
        notify(settings, project, PlsBundle.message("mod.exporter.launcherJson.info", collectionName, count))
    }
    
    private fun setDefaultSelected(gameType: ParadoxGameType) {
        if(defaultSelected != null) return
        val path = (getGameDataPath(gameType.name) + "/playlists").toPathOrNull() ?: return
        defaultSelected = VfsUtil.findFile(path, false)
    }
    
    fun notify(settings: ParadoxGameOrModSettingsState, project: Project, message: String) {
        NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
            settings.qualifiedName,
            message,
            NotificationType.INFORMATION
        ).notify(project)
    }
}