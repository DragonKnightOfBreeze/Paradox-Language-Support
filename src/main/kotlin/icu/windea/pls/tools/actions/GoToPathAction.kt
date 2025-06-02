package icu.windea.pls.tools.actions

import com.intellij.ide.lightEdit.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.fileChooser.actions.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.model.*
import java.nio.file.*

/**
 * 用于在文件选择页面中跳转到一个路径。
 */
@Suppress("UnstableApiUsage")
abstract class GoToPathAction : FileChooserAction(), LightEditCompatible {
    abstract val targetPath: Path?

    open val expand: Boolean = false

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    protected abstract fun setVisible(e: AnActionEvent): Boolean

    override fun update(panel: FileChooserPanel, e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isVisible = setVisible(e)
        presentation.isEnabled = presentation.isVisible && targetPath != null
    }

    override fun update(fileChooser: FileSystemTree, e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isVisible = setVisible(e)
        if (presentation.isEnabled) {
            presentation.isEnabled = presentation.isVisible && runCatchingCancelable {
                val targetPath = targetPath ?: return@runCatchingCancelable false
                val file = VfsUtil.findFile(targetPath, false) ?: return@runCatchingCancelable false
                fileChooser.isUnderRoots(file)
            }.getOrElse { false }
        }
    }

    override fun actionPerformed(panel: FileChooserPanel, e: AnActionEvent) {
        runCatchingCancelable {
            val targetPath = targetPath ?: return
            panel.load(targetPath)
        }
    }

    override fun actionPerformed(fileChooser: FileSystemTree, e: AnActionEvent) {
        runCatchingCancelable {
            val targetPath = targetPath ?: return
            val file = VfsUtil.findFile(targetPath, true) ?: return
            fileChooser.select(file, if (expand) Runnable { fileChooser.expand(file, null) } else null)
        }
    }

    class Steam : GoToPathAction() {
        override var targetPath: Path? = null

        override fun setVisible(e: AnActionEvent): Boolean {
            val gameType = e.gameTypeProperty?.get() ?: e.gameType
            if (gameType == null) return false
            if (targetPath == null) {
                targetPath = PlsFacade.getDataProvider().getSteamPath()?.toPathOrNull()
            }
            return true
        }
    }

    class SteamGame : GoToPathAction() {
        private var gameType: ParadoxGameType? = null

        override var targetPath: Path? = null

        override fun setVisible(e: AnActionEvent): Boolean {
            val gameType = e.gameTypeProperty?.get() ?: e.gameType
            if (gameType == null) return false
            if (this.targetPath == null || this.gameType != gameType) {
                this.gameType = gameType
                this.targetPath = PlsFacade.getDataProvider().getSteamGamePath(gameType.steamId, gameType.title)?.toPathOrNull()
            }
            return true
        }
    }

    class SteamWorkshop : GoToPathAction() {
        private var gameType: ParadoxGameType? = null

        override var targetPath: Path? = null
        override val expand: Boolean = true

        override fun setVisible(e: AnActionEvent): Boolean {
            val gameType = e.gameTypeProperty?.get() ?: e.gameType
            if (gameType == null) return false
            if (this.targetPath == null || this.gameType != gameType) {
                this.gameType = gameType
                this.targetPath = PlsFacade.getDataProvider().getSteamWorkshopPath(gameType.steamId)?.toPathOrNull()
            }
            return true
        }
    }

    class GameData : GoToPathAction() {
        private var gameType: ParadoxGameType? = null

        override var targetPath: Path? = null
        override val expand: Boolean = true

        override fun setVisible(e: AnActionEvent): Boolean {
            val gameType = e.gameTypeProperty?.get() ?: e.gameType
            if (gameType == null) return false
            if (this.targetPath == null || this.gameType != gameType) {
                this.gameType = gameType
                this.targetPath = PlsFacade.getDataProvider().getGameDataPath(gameType.title)?.toPathOrNull()
            }
            return true
        }
    }
}
