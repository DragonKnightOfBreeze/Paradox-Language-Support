package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.LimitedCompletionProcessor
import icu.windea.pls.core.icon
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxSyncedLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxLocalisationParameterManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType

/**
 * 提供参数名字的代码补全。
 */
class ParadoxLocalisationParameterCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val file = parameters.originalFile.castOrNull<ParadoxLocalisationFile>() ?: return
        val type = ParadoxLocalisationType.resolve(file) ?: return
        val project = parameters.originalFile.project

        //提示parameter
        val localisation = parameters.position.parentOfType<ParadoxLocalisationProperty>()
        if (localisation != null) {
            ParadoxLocalisationParameterManager.completeParameters(localisation, result)
        }

        //本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnAnyPrefixChange()

        //提示localisation或者synced_localisation
        val selector = selector(project, file).localisation()
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        //.distinctByName() //这里selector不需要指定去重
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
        //保证索引在此readAction中可用

        ReadAction.nonBlocking<Unit> {
            when (type) {
                ParadoxLocalisationType.Normal -> ParadoxLocalisationSearch.processVariants(result.prefixMatcher, selector, processor)
                ParadoxLocalisationType.Synced -> ParadoxSyncedLocalisationSearch.processVariants(result.prefixMatcher, selector, processor)
            }
        }.inSmartMode(project).executeSynchronously()
    }
}
