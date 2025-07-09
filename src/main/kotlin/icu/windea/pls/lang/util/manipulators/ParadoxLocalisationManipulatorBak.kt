package icu.windea.pls.lang.util.manipulators

import com.intellij.openapi.project.*
import icu.windea.pls.config.config.*
import icu.windea.pls.integrations.translation.*
import icu.windea.pls.localisation.psi.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

object ParadoxLocalisationManipulatorBak {
    /**
     * 批量收集本地化上下文。
     */
    fun collectContexts(properties: List<ParadoxLocalisationProperty>): List<Pair<ParadoxLocalisationProperty, ParadoxLocalisationContext>> {
        return properties.map { it to ParadoxLocalisationContext.from(it) }
    }

    /**
     * 过滤需要处理的本地化属性。
     */
    fun filterHandleTargets(contexts: List<Pair<ParadoxLocalisationProperty, ParadoxLocalisationContext>>): List<Pair<ParadoxLocalisationProperty, ParadoxLocalisationContext>> {
        return contexts.filter { (_, ctx) -> ctx.shouldHandle }
    }

    /**
     * 并发翻译文本，结果写入 context.newText。
     */
    suspend fun translateTexts(
        contexts: List<Pair<ParadoxLocalisationProperty, ParadoxLocalisationContext>>,
        targetLocale: CwtLocaleConfig,
        selectSourceLocale: (ParadoxLocalisationProperty) -> CwtLocaleConfig?
    ) = coroutineScope {
        contexts.map { (element, ctx) ->
            async {
                val sourceLocale = selectSourceLocale(element)
                val translated = kotlinx.coroutines.suspendCancellableCoroutine<String?> { continuation ->
                    kotlinx.coroutines.CoroutineScope(continuation.context).launch {
                        PlsTranslationManager.translate(ctx.text, sourceLocale, targetLocale) { result, e ->
                            if (e != null) continuation.resumeWith(Result.failure(e))
                            else continuation.resume(result)
                        }
                    }
                }
                if (translated != null) ctx.newText = translated
            }
        }.awaitAll()
    }

    /**
     * 批量替换本地化属性的文本内容。
     * 只会在 newText 与原内容不同的情况下进行替换。
     * 需要在 write action 中调用。
     */
    fun replaceTexts(
        contexts: List<Pair<ParadoxLocalisationProperty, ParadoxLocalisationContext>>,
        project: Project
    ) {
        contexts.forEach { (element, ctx) ->
            if (ctx.newText != ctx.text) {
                element.setValue(ctx.newText)
            }
        }
    }

    /**
     * 拼接所有上下文的文本（用于批量复制）。
     */
    fun joinTexts(contexts: List<Pair<ParadoxLocalisationProperty, ParadoxLocalisationContext>>): String {
        return contexts.joinToString("\n") { (_, ctx) -> ctx.joinWithNewText() }
    }
}
