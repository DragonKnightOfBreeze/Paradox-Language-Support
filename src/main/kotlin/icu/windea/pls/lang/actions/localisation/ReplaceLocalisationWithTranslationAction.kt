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
import icu.windea.pls.integrations.translation.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.manipulators.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.*

class ReplaceLocalisationWithTranslationAction : ManipulateLocalisationActionBase.WithLocalePopup(), DumbAware {
    override fun isAvailable(e: AnActionEvent, project: Project): Boolean {
        return super.isAvailable(e, project) && PlsTranslationManager.findTool() != null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("UnstableApiUsage")
    override suspend fun doHandleAll(e: AnActionEvent, project: Project, context: Context) {
        //进度条 - 显示的进度文本不会变化，按已处理的文件来显示当前进度
        //并发性 - 文件级别+本地化级别

        val (files, selectedLocale) = context
        withBackgroundProgress(project, PlsBundle.message("action.replaceLocalisationWithTranslation.progress.title", selectedLocale.text)) action@{
            val total = files.size
            val allContexts = mutableListOf<ParadoxLocalisationContext>().synced()
            val processedRef = AtomicInteger()
            val errorRef = AtomicReference<Throwable>()

            reportRawProgress { rawReporter ->
                val stepText = PlsBundle.message("manipulation.localisation.translate.replace.progress.filesStep", total)
                rawReporter.text(stepText)

                files.forEachConcurrent { file ->
                    val elements = ParadoxLocalisationManipulator.buildFlow(file)
                    val contextsToHandle = elements.map { readAction { ParadoxLocalisationContext.from(it) } }.filter { it.shouldHandle }

                    contextsToHandle.flatMapMerge { context ->
                        flow {
                            runCatchingCancelable { handleText(context, selectedLocale) }.onFailure { errorRef.compareAndSet(null, it) }.getOrThrow()
                            runCatchingCancelable { replaceText(context, project) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()
                            emit(context)
                        }
                    }.collect { context -> allContexts += context }

                    val processed = processedRef.incrementAndGet()
                    rawReporter.fraction(processed / total.toDouble())
                    rawReporter.text("$stepText ($processed / $total)")
                }
            }

            createNotification(selectedLocale, processedRef.get(), errorRef.get())
                .addAction(ParadoxLocalisationManipulator.createRevertAction(allContexts))
                .addAction(ParadoxLocalisationManipulator.createReapplyAction(allContexts))
                .notify(project)
        }
    }

    private suspend fun handleText(context: ParadoxLocalisationContext, selectedLocale: CwtLocaleConfig) {
        return ParadoxLocalisationManipulator.handleTextWithTranslation(context, selectedLocale)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("manipulation.localisation.command.ai.translate.replace")
        return ParadoxLocalisationManipulator.replaceText(context, project, commandName)
    }

    private fun createNotification(selectedLocale: CwtLocaleConfig, processed: Int, error: Throwable?): Notification {
        if (error == null) {
            val content = PlsBundle.message("action.replaceLocalisationWithTranslation.notification", selectedLocale.text, Messages.success(processed))
            return createNotification(content, NotificationType.INFORMATION)
        }

        thisLogger().warn(error)
        val errorDetails = error.message?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("action.replaceLocalisationWithTranslation.notification", selectedLocale.text, Messages.failed(processed)) + errorDetails
        return createNotification(content, NotificationType.WARNING)
    }
}
