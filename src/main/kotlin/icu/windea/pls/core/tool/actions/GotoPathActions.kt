@file:Suppress("ComponentNotRegistered", "UnstableApiUsage")

package icu.windea.pls.core.tool.actions

import com.intellij.ide.lightEdit.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.fileChooser.actions.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.lang.model.*
import java.nio.file.*

/**
 * 用于在文件选择页面中跳转到一个路径。
 */
abstract class GotoPathAction : FileChooserAction(), LightEditCompatible {
    abstract val targetPath: Path?
    
    open val expand: Boolean = false
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    protected abstract fun setVisible(e: AnActionEvent): Boolean
    
    override fun update(panel: FileChooserPanel, e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isVisible = setVisible(e)
        presentation.isEnabled = presentation.isVisible && targetPath != null
    }
    
    override fun update(fileChooser: FileSystemTree, e: AnActionEvent) {
        val presentation = e.presentation
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
            val file = VfsUtil.findFile(targetPath, true) ?: return
            fileChooser.select(file, if(expand) Runnable { fileChooser.expand(file, null) } else null)
        }
    }
}

class GotoSteamPathAction : GotoPathAction() {
    override var targetPath: Path? = null
    
    override fun setVisible(e: AnActionEvent): Boolean {
        val gameType = e.gameTypeProperty?.get() ?: e.gameType
        if(gameType == null) return false
        if(targetPath == null) {
            targetPath = getSteamPath()?.toPathOrNull()
        }
        return true
    }
}

class GotoSteamGamePathAction : GotoPathAction() {
    private var gameType: ParadoxGameType? = null
    
    override var targetPath: Path? = null
    
    override fun setVisible(e: AnActionEvent): Boolean {
        val gameType = e.gameTypeProperty?.get() ?: e.gameType
        if(gameType == null) return false
        if(this.targetPath == null || this.gameType != gameType) {
            this.gameType = gameType
            this.targetPath = getSteamGamePath(gameType.gameSteamId, gameType.gameName)?.toPathOrNull()
        }
        return true
    }
}

class GotoSteamWorkshopPathAction : GotoPathAction() {
    private var gameType: ParadoxGameType? = null
    
    override var targetPath: Path? = null
    override val expand: Boolean = true
    
    override fun setVisible(e: AnActionEvent): Boolean {
        val gameType = e.gameTypeProperty?.get() ?: e.gameType
        if(gameType == null) return false
        if(this.targetPath == null || this.gameType != gameType) {
            this.gameType = gameType
            this.targetPath = getSteamWorkshopPath(gameType.gameSteamId)?.toPathOrNull()
        }
        return true
    }
}


class GotoGameDataPathAction : GotoPathAction() {
    private var gameType: ParadoxGameType? = null
    
    override var targetPath: Path? = null
    override val expand: Boolean = true
    
    override fun setVisible(e: AnActionEvent): Boolean {
        val gameType = e.gameTypeProperty?.get() ?: e.gameType
        if(gameType == null) return false
        if(this.targetPath == null || this.gameType != gameType) {
            this.gameType = gameType
            this.targetPath = getGameDataPath(gameType.gameName)?.toPathOrNull()
        }
        return true
    }
}
