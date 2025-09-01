package icu.windea.pls.lang.actions.localisation

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.coroutines.forEachConcurrent
import com.intellij.platform.util.progress.reportRawProgress
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.collections.synced
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.integrations.translation.PlsTranslationManager
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationManipulator
import icu.windea.pls.lang.withErrorRef
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class ReplaceLocalisationWithTranslationFromLocaleAction : ManipulateLocalisationActionBase.WithLocalePopup() {
    override fun isAvailable(e: AnActionEvent, project: Project): Boolean {
        return super.isAvailable(e, project) && PlsTranslationManager.findTool() != null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("UnstableApiUsage")
    override suspend fun doHandleAll(e: AnActionEvent, project: Project, context: Context) {
        //进度条 - 显示的进度文本不会变化，按已处理的文件来显示当前进度
        //并发性 - 文件级别+本地化级别

        val (files, selectedLocale) = context
        withBackgroundProgress(project, PlsBundle.message("action.replaceLocalisationWithTranslationFromLocale.progress.title", selectedLocale.text)) action@{
            val total = files.size
            val allContexts = mutableListOf<ParadoxLocalisationContext>().synced()
            val processedRef = AtomicInteger()
            val errorRef = AtomicReference<Throwable>()

            reportRawProgress { rawReporter ->
                val stepText = PlsBundle.message("manipulation.localisation.search.translate.replace.progress.filesStep", total)
                rawReporter.text(stepText)

                files.forEachConcurrent { file ->
                    val elements = findElements(e, file)
                    val contexts = readAction { elements.map { ParadoxLocalisationContext.from(it) }.toList() }
                    val contextsToHandle = contexts.filter { context -> context.shouldHandle }
                    allContexts.addAll(contextsToHandle)

                    runCatchingCancelable r@{
                        if(contextsToHandle.isEmpty()) return@r
                        val locale = selectLocale(file)
                        contextsToHandle.asFlow().flatMapMerge { context ->
                            flow {
                                withErrorRef(errorRef) { handleText(context, project, selectedLocale, locale) }.getOrThrow()
                                withErrorRef(errorRef) { replaceText(context, project) }.getOrNull()
                                emit(context)
                            }
                        }.collect()
                    }

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

    private suspend fun handleText(context: ParadoxLocalisationContext, project: Project, selectedLocale: CwtLocaleConfig, locale: CwtLocaleConfig?) {
        ParadoxLocalisationManipulator.searchTextFromLocale(context, project, selectedLocale)
        if (locale != null) ParadoxLocalisationManipulator.handleTextWithTranslation(context, locale)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("manipulation.localisation.command.ai.translate.replace")
        ParadoxLocalisationManipulator.replaceText(context, project, commandName)
    }

    private fun createNotification(selectedLocale: CwtLocaleConfig, processed: Int, error: Throwable?): Notification {
        if (error == null) {
            val content = PlsBundle.message("action.replaceLocalisationWithTranslationFromLocale.notification", selectedLocale.text, Messages.success(processed))
            return PlsCoreManager.createNotification(NotificationType.INFORMATION, content)
        }

        thisLogger().warn(error)
        val errorDetails = error.message?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("action.replaceLocalisationWithTranslationFromLocale.notification", selectedLocale.text, Messages.failed(processed)) + errorDetails
        return PlsCoreManager.createNotification(NotificationType.WARNING, content)
    }
}
