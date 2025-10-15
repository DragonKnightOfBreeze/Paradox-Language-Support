package icu.windea.pls.lang.actions.cwt

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.filters.OpenFileHyperlinkInfo
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.unscramble.AnalyzeStacktraceUtil
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.util.generators.CwtConfigGenerator
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.errorDetails
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

abstract class GenerateConfigActionBase : DumbAwareAction() {
    companion object {
        private val logger = logger<GenerateConfigActionBase>()
        private const val SHOW_DIFF_MARK = "[Show DIFF]"
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val generator = createGenerator(project)
        val params = createParams(project, generator) ?: return // return -> cancelled
        execute(project, generator, params)
    }

    protected abstract fun createGenerator(project: Project): CwtConfigGenerator

    private fun createParams(project: Project, generator: CwtConfigGenerator): Params? {
        val dialog = GenerateConfigDialog(project, generator)
        if (!dialog.showAndGet()) return null
        val gameType = dialog.gameType ?: return null
        val inputPath = dialog.inputPath?.orNull() ?: return null
        val outputPath = dialog.outputPath?.orNull() ?: return null
        return Params(gameType, inputPath, outputPath)
    }

    private fun execute(project: Project, generator: CwtConfigGenerator, params: Params) {
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            doExecute(project, generator, params)
        }
    }

    private suspend fun doExecute(project: Project, generator: CwtConfigGenerator, params: Params) {
        // 在后台异步且可取消地执行生成器，生成维护提示
        val hint = executeGenerator(project, generator, params)
        if (hint == null) return

        // 打开工具窗口，显示维护提示
        showConsole(project, generator, params, hint)
    }

    private suspend fun executeGenerator(project: Project, generator: CwtConfigGenerator, params: Params): CwtConfigGenerator.Hint? {
        return try {
            withBackgroundProgress(project, PlsBundle.message("config.generation.progress.title", generator.getName())) {
                val (gameType, inputPath, outputPath) = params
                generator.generate(gameType, inputPath, outputPath)
            }
        } catch (e: Exception) {
            if (e is ProcessCanceledException || e is CancellationException) throw e
            logger.warn(e)
            val content = PlsBundle.message("config.generation.notification.failed") + e.message.errorDetails
            PlsCoreManager.createNotification(NotificationType.WARNING, generator.getName(), content).notify(project)
            null
        }
    }

    private fun showConsole(project: Project, generator: CwtConfigGenerator, params: Params, hint: CwtConfigGenerator.Hint): RunContentDescriptor {
        val tabTitle = PlsBundle.message("config.generation.console.title", generator.getName())
        val text = buildConsoleText(project, params, hint)
        // 使用 ConsoleFactory 以插入自定义过滤器和自定义打印流程
        val consoleFactory = object : AnalyzeStacktraceUtil.ConsoleFactory {
            override fun createConsoleComponent(consoleView: ConsoleView?, toolbarActions: DefaultActionGroup?): JComponent? {
                if (consoleView == null || toolbarActions == null) return null
                // 1) 添加文件/目录超链接过滤器与自定义“Show DIFF”过滤器
                consoleView.addMessageFilter(filePathFilter(project))
                consoleView.addMessageFilter(showDiffFilter(project, params.outputPath, hint.fileText))
                // 2) 打印文本
                consoleView.print(text, com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT)
                // 返回默认控制台面板
                return JPanel(BorderLayout()).apply {
                    val toolbar = ActionManager.getInstance().createActionToolbar(
                        ActionPlaces.ANALYZE_STACKTRACE_PANEL_TOOLBAR, toolbarActions, false
                    )
                    toolbar.targetComponent = consoleView.component
                    add(toolbar.component, BorderLayout.WEST)
                    add(consoleView.component, BorderLayout.CENTER)
                }
            }
        }
        return AnalyzeStacktraceUtil.addConsole(project, consoleFactory, tabTitle, text, null)
    }

    private fun buildConsoleText(project: Project, params: Params, hint: CwtConfigGenerator.Hint): String {
        val header = buildString {
            appendLine(PlsBundle.message("config.generation.console.header.gameType", params.gameType.title))
            appendLine("Input: ${params.inputPath}")
            appendLine("Output: ${params.outputPath}")
            appendLine()
            appendLine(SHOW_DIFF_MARK)
            appendLine()
        }
        val body = buildString {
            if (hint.summary.isNotBlank()) {
                appendLine(hint.summary.trimEnd())
                appendLine()
            }
            if (hint.details.isNotBlank()) {
                appendLine(hint.details.trimEnd())
            }
        }
        return header + body
    }

    private fun filePathFilter(project: Project): Filter = Filter { line, entireLength ->
        val m = Regex("^(Input|Output):\\s+(.+)$").find(line) ?: return@Filter null
        val path = m.groupValues[2].trim()
        val vFile = LocalFileSystem.getInstance().findFileByPath(path) ?: return@Filter null
        val lineStart = entireLength - line.length
        val start = lineStart + m.range.first + m.value.indexOf(path)
        val end = start + path.length
        val info: HyperlinkInfo = if (vFile.isDirectory) HyperlinkInfo {
            // 暂不处理目录导航
        } else OpenFileHyperlinkInfo(project, vFile, 0, 0)
        Filter.Result(start, end, info)
    }

    private fun showDiffFilter(project: Project, outputPath: String, newText: String): Filter = Filter { line, entireLength ->
        val idx = line.indexOf(SHOW_DIFF_MARK)
        if (idx < 0) return@Filter null
        val start = entireLength - line.length + idx
        val end = start + SHOW_DIFF_MARK.length
        val info = HyperlinkInfo { openDiff(project, outputPath, newText) }
        Filter.Result(start, end, info)
    }

    private fun openDiff(project: Project, outputPath: String, newText: String) {
        val vFile = LocalFileSystem.getInstance().findFileByPath(outputPath)
        val contentFactory = DiffContentFactory.getInstance()
        val left = if (vFile != null) contentFactory.create(project, vFile) else contentFactory.create("")
        val right = contentFactory.create(newText)
        val title = PlsBundle.message("config.generation.console.diff.title")
        val request = SimpleDiffRequest(title, left, right, "Current", "Generated")
        DiffManager.getInstance().showDiff(project, request)
    }

    data class Params(
        val gameType: ParadoxGameType,
        val inputPath: String,
        val outputPath: String,
    )
}
