package icu.windea.pls.lang.actions.cwt

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.filters.OpenFileHyperlinkInfo
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.impl.ConsoleViewUtil
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.unscramble.AnalyzeStacktraceUtil.Companion.EP_NAME
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.util.generators.CwtConfigGenerator
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.lang.errorDetails
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.BorderLayout
import javax.swing.JPanel

abstract class GenerateConfigActionBase : DumbAwareAction() {
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
        val gameType = dialog.gameType
        val inputPath = dialog.inputPath.orNull() ?: return null
        val outputPath = dialog.outputPath.orNull() ?: return null
        return Params(gameType, inputPath, outputPath)
    }

    private fun execute(project: Project, generator: CwtConfigGenerator, params: Params) {
        val coroutineScope = PlsFacade.getCoroutineScope(project)
        coroutineScope.launch {
            doExecute(project, generator, params)
        }
    }

    private suspend fun doExecute(project: Project, generator: CwtConfigGenerator, params: Params) {
        // 执行生成器，生成维护提示
        val hint = executeGenerator(project, generator, params)
        if (hint == null) return

        // 打开工具窗口，显示维护提示
        withContext(Dispatchers.EDT) {
            showHintInToolWindow(project, generator, params, hint)
        }
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

    private fun showHintInToolWindow(
        project: Project,
        generator: CwtConfigGenerator,
        params: Params,
        hint: CwtConfigGenerator.Hint
    ) {
        val builder = TextConsoleBuilderFactory.getInstance().createBuilder(project)
        builder.filters(EP_NAME.getExtensions(project))

        val consoleView = builder.console
        val toolbarActions = DefaultActionGroup()
        // 必须先创建 consoleComponent ，之后再获取 console.editor
        val consoleComponent = createConsoleComponent(consoleView, project, params, hint, toolbarActions)

        for (action in consoleView.createConsoleActions()) {
            toolbarActions.add(action)
        }
        val console = consoleView as ConsoleViewImpl
        ConsoleViewUtil.enableReplaceActionForConsoleViewEditor(console.editor!!)
        console.editor!!.settings.isCaretRowShown = true

        val tabTitle = PlsBundle.message("config.generation.console.title", generator.getName())
        val descriptor = object : RunContentDescriptor(consoleView, null, consoleComponent, tabTitle) {
            override fun isContentReuseProhibited() = true
        }
        val runContentManager = RunContentManager.getInstance(project)
        val executor = DefaultRunExecutor.getRunExecutorInstance()
        runContentManager.showRunContent(executor, descriptor)
    }

    private fun createConsoleComponent(
        consoleView: ConsoleView,
        project: Project,
        params: Params,
        hint: CwtConfigGenerator.Hint,
        toolbarActions: DefaultActionGroup
    ): JPanel {
        // 添加需要的过滤器
        consoleView.addMessageFilter(filePathFilter(project, params))
        consoleView.addMessageFilter(showDiffFilter(project, params, hint))
        // 打印文本
        val consoleText = buildConsoleText(params, hint)
        consoleView.clear()
        consoleView.print(consoleText, ConsoleViewContentType.NORMAL_OUTPUT)
        consoleView.scrollTo(0)
        // 返回默认控制台面板
        return JPanel(BorderLayout()).apply {
            val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.RUN_TOOLBAR_LEFT_SIDE, toolbarActions, false)
            toolbar.targetComponent = consoleView.component
            add(toolbar.component, BorderLayout.WEST)
            add(consoleView.component, BorderLayout.CENTER)
        }
    }

    private fun filePathFilter(project: Project, params: Params) = Filter { line, entireLength ->
        val m = PATH_REGEX.find(line) ?: return@Filter null
        val path = m.groupValues[2].trim()
        if (path != params.inputPath && path != params.outputPath) return@Filter null
        val vFile = path.toPathOrNull()?.toVirtualFile() ?: return@Filter null
        val start = entireLength - line.length + m.range.first + m.value.indexOf(path)
        val end = start + path.length
        val info = OpenFileHyperlinkInfo(project, vFile, 0, 0)
        Filter.Result(start, end, info)
    }

    private fun showDiffFilter(project: Project, params: Params, hint: CwtConfigGenerator.Hint) = Filter { line, entireLength ->
        if (line.trim() != SHOW_DIFF_MARK) return@Filter null
        val i = line.indexOf(SHOW_DIFF_MARK)
        if (i < 0) return@Filter null
        val start = entireLength - line.length + i
        val end = start + SHOW_DIFF_MARK.length
        val info = HyperlinkInfo { openDiff(project, params.outputPath, hint.fileText) }
        Filter.Result(start, end, info)
    }

    private fun openDiff(project: Project, outputPath: String, newText: String) {
        val vFile = outputPath.toPathOrNull()?.toVirtualFile()
        val contentFactory = DiffContentFactory.getInstance()
        val left = if (vFile != null) contentFactory.create(project, vFile) else contentFactory.create("")
        val right = contentFactory.create(project, newText, CwtFileType)
        val title = PlsBundle.message("config.generation.console.diff.title")
        val title1 = PlsBundle.message("config.generation.console.diff.current")
        val title2 = PlsBundle.message("config.generation.console.diff.generated")
        val request = SimpleDiffRequest(title, left, right, title1, title2)
        DiffManager.getInstance().showDiff(project, request)
    }

    private fun buildConsoleText(params: Params, hint: CwtConfigGenerator.Hint): String {
        return buildString {
            // header
            append(GAME_TYPE_PREFIX).appendLine(params.gameType.title)
            append(INPUT_PREFIX).appendLine(params.inputPath)
            append(OUTPUT_PREFIX).appendLine(params.outputPath)
            appendLine()
            appendLine(SHOW_DIFF_MARK)
            appendLine()

            // body
            if (hint.summary.isNotBlank()) {
                appendLine(SUMMARY_MARK)
                appendLine(hint.summary.trimEnd())
                appendLine()
            }
            if (hint.details.isNotBlank()) {
                appendLine(DETAILS_MARK)
                appendLine(hint.details.trimEnd())
            }
        }
    }

    data class Params(
        val gameType: ParadoxGameType,
        val inputPath: String,
        val outputPath: String,
    )

    companion object {
        private val logger = logger<GenerateConfigActionBase>()

        private const val SHOW_DIFF_MARK = "[Show DIFF]"
        private const val SUMMARY_MARK = "[Summary]"
        private const val DETAILS_MARK = "[Details]"

        private const val GAME_TYPE_PREFIX = "Game type: "
        private const val INPUT_PREFIX = "Input path: "
        private const val OUTPUT_PREFIX = "Output path: "

        private val PATH_REGEX = "^(Input|Output) path:\\s+(.+)$".toRegex()
    }
}
