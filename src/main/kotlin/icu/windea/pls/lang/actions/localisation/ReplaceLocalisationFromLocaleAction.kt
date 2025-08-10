package icu.windea.pls.lang.actions.localisation

import com.intellij.notification.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.project.*
import com.intellij.platform.ide.progress.*
import com.intellij.platform.util.coroutines.*
import com.intellij.platform.util.progress.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.manipulators.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.*

class ReplaceLocalisationFromLocaleAction : ManipulateLocalisationActionBase.WithLocalePopup() {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("UnstableApiUsage")
    override suspend fun doHandleAll(e: AnActionEvent, project: Project, context: Context) {
        //进度条 - 显示的进度文本不会变化，按已处理的文件来显示当前进度
        //并发性 - 文件级别+本地化级别

        val (files, selectedLocale) = context
        withBackgroundProgress(project, PlsBundle.message("action.replaceLocalisationFromLocale.progress.title", selectedLocale.text)) action@{
            val total = files.size
            val contexts = mutableListOf<ParadoxLocalisationContext>().synced()
            val processedRef = AtomicInteger()
            val errorRef = AtomicReference<Throwable>()

            reportRawProgress { rawReporter ->
                val stepText = PlsBundle.message("manipulation.localisation.search.replace.progress.filesStep", total)
                rawReporter.text(stepText)

                files.forEachConcurrent { file ->
                    val elements = ParadoxLocalisationManipulator.buildFlow(file)
                    elements.transform { element ->
                        val context = readAction { ParadoxLocalisationContext.from(element) }
                        if (!context.shouldHandle) return@transform
                        emit(context)
                    }.flatMapMerge { context ->
                        flow {
                            runCatchingCancelable { handleText(context, project, selectedLocale) }.onFailure { errorRef.compareAndSet(null, it) }.getOrThrow()
                            runCatchingCancelable { replaceText(context, project) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()
                            emit(context)
                        }
                    }.collect { context -> contexts += context }

                    val processed = processedRef.incrementAndGet()
                    rawReporter.fraction(processed / total.toDouble())
                    rawReporter.text("$stepText ($processed / $total)")
                }
            }

            createNotification(selectedLocale, processedRef.get(), errorRef.get())
                .addAction(ParadoxLocalisationManipulator.createRevertAction(contexts))
                .addAction(ParadoxLocalisationManipulator.createReapplyAction(contexts))
                .notify(project)
        }
    }

    private suspend fun handleText(context: ParadoxLocalisationContext, project: Project, selectedLocale: CwtLocaleConfig) {
        return ParadoxLocalisationManipulator.handleTextFromLocale(context, project, selectedLocale)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("manipulation.localisation.command.replace")
        return ParadoxLocalisationManipulator.replaceText(context, project, commandName)
    }

    private fun createNotification(selectedLocale: CwtLocaleConfig, processed: Int, error: Throwable?): Notification {
        if (error == null) {
            val content = PlsBundle.message("action.replaceLocalisationFromLocale.notification", selectedLocale.text, Messages.success(processed))
            return createNotification(content, NotificationType.INFORMATION)
        }

        thisLogger().warn(error)
        val errorDetails = error.message?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("action.replaceLocalisationFromLocale.notification", selectedLocale.text, Messages.failed(processed)) + errorDetails
        return createNotification(content, NotificationType.WARNING)
    }
}
