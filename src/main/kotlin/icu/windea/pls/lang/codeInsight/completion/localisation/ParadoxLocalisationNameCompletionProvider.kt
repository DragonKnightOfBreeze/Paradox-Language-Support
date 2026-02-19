package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.LimitedCompletionProcessor
import icu.windea.pls.core.icon
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.filterBy
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiUtil
import icu.windea.pls.model.ParadoxLocalisationType
import java.util.concurrent.Callable

/**
 * 提供本地化名称的代码补全。
 */
class ParadoxLocalisationNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsSettings.getInstance().state.completion.completeLocalisationNames) return

        val position = parameters.position
        if (ParadoxLocalisationPsiUtil.isLocalisationLocaleLike(position)) return

        val element = position.parent?.parent as? ParadoxLocalisationProperty ?: return
        val file = parameters.originalFile.castOrNull<ParadoxLocalisationFile>() ?: return
        val type = ParadoxLocalisationType.resolve(file) ?: return
        val project = parameters.originalFile.project

        // 本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnAnyPrefixChange()

        // 提示 `localisation` 或者 `synced_localisation`
        val selector = selector(project, file).localisation()
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            .filterBy { it.name != element.name } // 排除与正在输入的同名的
        val processor = LimitedCompletionProcessor<ParadoxLocalisationProperty> {
            ProgressManager.checkCanceled()
            val name = it.name
            val icon = it.icon
            val typeFile = it.containingFile
            val lookupElement = LookupElementBuilder.create(it, name)
                .withIcon(icon)
                .withTypeText(typeFile.name, typeFile.icon, true)
            result.addElement(lookupElement)
            true
        }
        // 保证索引在此 readAction 中可用
        val task = Callable {
            ParadoxLocalisationSearch.processVariants(type, result.prefixMatcher, selector, processor)
        }
        ReadAction.nonBlocking(task).inSmartMode(project).executeSynchronously()
    }
}
