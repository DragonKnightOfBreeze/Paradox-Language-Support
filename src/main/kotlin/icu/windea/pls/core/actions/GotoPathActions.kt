package icu.windea.pls.core.actions

import com.intellij.ide.lightEdit.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.fileChooser.actions.*
import com.intellij.openapi.vfs.*
import icons.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.*
import java.nio.file.*
import javax.swing.*

@Suppress("UnstableApiUsage")
abstract class GotoPathAction(private val icon: Icon) : FileChooserAction(), LightEditCompatible {
    abstract val targetPath: Path?
    open val expand: Boolean = false
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    protected abstract fun setVisible(e: AnActionEvent): Boolean
    
    override fun update(panel: FileChooserPanel, e: AnActionEvent) {
        val presentation = e.presentation
        presentation.icon = icon
        presentation.isVisible = setVisible(e)
        presentation.isEnabled = presentation.isVisible && targetPath != null
    }
    
    override fun update(fileChooser: FileSystemTree, e: AnActionEvent) {
        val presentation = e.presentation
        presentation.icon = icon
        presentation.isVisible = setVisible(e)
        if(presentation.isEnabled) {
            presentation.isEnabled = presentation.isVisible && runCatching {
                val targetPath = targetPath ?: return@runCatching false
                val file = VfsUtil.findFile(targetPath, false) ?: return@runCatching false
                fileChooser.isUnderRoots(file)
            }.getOrElse { false }
        }
    }
    
    override fun actionPerformed(panel: FileChooserPanel, e: AnActionEvent) {
        runCatching {
            val targetPath = targetPath ?: return
            panel.load(targetPath)
        }
    }
    
    override fun actionPerformed(fileChooser: FileSystemTree, e: AnActionEvent) {
        runCatching {
            val targetPath = targetPath ?: return
            val file = VfsUtil.findFile(targetPath, false) ?: return
            fileChooser.select(file, if(expand) Runnable { fileChooser.expand(file, null) } else null)
        }
    }
}

@Suppress("ComponentNotRegistered")
class GotoSteamPathAction : GotoPathAction(PlsIcons.Actions.SteamDirectory) {
    override var targetPath: Path? = null
    
    override fun setVisible(e: AnActionEvent): Boolean {
        if(e.gameTypeProperty == null || e.rootTypeProperty == null) return false
        if(targetPath == null) {
            targetPath = getSteamPath()?.toPathOrNull()
        }
        return true
    }
}

@Suppress("ComponentNotRegistered")
class GotoSteamGamePathAction : GotoPathAction(PlsIcons.Actions.SteamGameDirectory) {
    private var gameType: ParadoxGameType? = null
    private var rootType: ParadoxRootType? = null
    
    override var targetPath: Path? = null
    
    override fun setVisible(e: AnActionEvent): Boolean {
        val gameType = e.gameTypeProperty?.get()
        val rootType = e.rootTypeProperty?.get()
        if(gameType == null || rootType == null) return false
        //if(gameType == null || rootType == null || rootType != ParadoxRootType.Game) return false
        if(this.targetPath == null || this.gameType != gameType || this.rootType != rootType) {
            this.gameType = gameType
            this.rootType = rootType
            this.targetPath = getSteamGamePath(gameType.gameSteamId, gameType.gameName)?.toPathOrNull()
        }
        return true
    }
}

@Suppress("ComponentNotRegistered")
class GotoSteamWorkshopPathAction : GotoPathAction(PlsIcons.Actions.SteamWorkshopDirectory) {
    private var gameType: ParadoxGameType? = null
    private var rootType: ParadoxRootType? = null
    
    override var targetPath: Path? = null
    override val expand: Boolean = true
    
    override fun setVisible(e: AnActionEvent): Boolean {
        val gameType = e.gameTypeProperty?.get()
        val rootType = e.rootTypeProperty?.get()
        if(gameType == null || rootType == null) return false
        //if(gameType == null || rootType == null || rootType != ParadoxRootType.Mod) return false
        if(this.targetPath == null || this.gameType != gameType || this.rootType != rootType) {
            this.gameType = gameType
            this.rootType = rootType
            this.targetPath = getSteamWorkshopPath(gameType.gameSteamId)?.toPathOrNull()
        }
        return true
    }
}


@Suppress("ComponentNotRegistered")
class GotoGameModPathAction : GotoPathAction(PlsIcons.Actions.GameModDirectory) {
    private var gameType: ParadoxGameType? = null
    private var rootType: ParadoxRootType? = null
    
    override var targetPath: Path? = null
    override val expand: Boolean = true
    
    override fun setVisible(e: AnActionEvent): Boolean {
        val gameType = e.gameTypeProperty?.get()
        val rootType = e.rootTypeProperty?.get()
        if(gameType == null || rootType == null) return false
        //if(gameType == null || rootType == null || rootType != ParadoxRootType.Mod) return false
        if(this.targetPath == null || this.gameType != gameType || this.rootType != rootType) {
            this.gameType = gameType
            this.rootType = rootType
            this.targetPath = getGameModPath(gameType.gameName)?.toPathOrNull()
        }
        return true
    }
}
