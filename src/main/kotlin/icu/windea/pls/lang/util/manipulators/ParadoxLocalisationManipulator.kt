@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.util.manipulators

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts.*
import com.intellij.platform.ide.progress.withBackgroundProgress
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.integrations.translation.PlsTranslationManager
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.locale
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.selector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ParadoxLocalisationManipulator {
    suspend fun searchTextFromLocale(context: ParadoxLocalisationContext, project: Project, locale: CwtLocaleConfig) {
        val newText = readAction {
            val type = context.element?.type ?: return@readAction null
            val selector = selector(project, context.element).localisation().contextSensitive().locale(locale)
            val e = ParadoxLocalisationSearch.search(context.key, type, selector).find()
            e?.value
        }
        if (newText == null) return
        context.newText = newText
    }

    suspend fun handleTextWithTranslation(context: ParadoxLocalisationContext, sourceLocale: CwtLocaleConfig, targetLocale: CwtLocaleConfig) {
        val newText = suspendCancellableCoroutine { continuation ->
            CoroutineScope(continuation.context).launch {
                PlsTranslationManager.translate(context.newText, sourceLocale, targetLocale) { translated, e ->
                    if (e != null) {
                        continuation.resumeWithException(e)
                    } else {
                        continuation.resume(translated)
                    }
                }
            }
        }
        if (newText == null) return
        context.newText = newText
    }

    suspend fun replaceText(context: ParadoxLocalisationContext, project: Project, @Command commandName: String) {
        if (context.newText == context.text) return
        writeCommandAction(project, commandName) {
            // 注意这里 context.element 可能已经不合法
            context.element?.setValue(context.newText)
        }
    }

    fun joinText(contexts: List<ParadoxLocalisationContext>): String {
        return contexts.joinToString("\n") { context -> "${context.prefix}\"${context.newText}\"" }
    }

    fun createRevertAction(contexts: List<ParadoxLocalisationContext>): AnAction {
        return object : AnAction(PlsBundle.message("manipulation.localisation.revert")) {
            override fun actionPerformed(e: AnActionEvent) {
                val project = e.project ?: return
                val coroutineScope = PlsFacade.getCoroutineScope(project)
                coroutineScope.launch {
                    withBackgroundProgress(project, PlsBundle.message("manipulation.localisation.revert.progress.title")) {
                        writeCommandAction(project, PlsBundle.message("manipulation.localisation.revert.command")) {
                            for (context in contexts) {
                                if (context.text == context.newText) continue
                                // 注意这里 context.element 可能已经不合法
                                context.element?.setValue(context.text)
                            }
                        }
                    }
                }
            }
        }
    }

    fun createReapplyAction(contexts: List<ParadoxLocalisationContext>): AnAction {
        return object : AnAction(PlsBundle.message("manipulation.localisation.reapply")) {
            override fun actionPerformed(e: AnActionEvent) {
                val project = e.project ?: return
                val coroutineScope = PlsFacade.getCoroutineScope(project)
                coroutineScope.launch {
                    withBackgroundProgress(project, PlsBundle.message("manipulation.localisation.reapply.progress.title")) {
                        writeCommandAction(project, PlsBundle.message("manipulation.localisation.reapply.command")) {
                            for (context in contexts) {
                                if (context.text == context.newText) continue
                                // 注意这里 context.element 可能已经不合法
                                context.element?.setValue(context.newText)
                            }
                        }
                    }
                }
            }
        }
    }
}
