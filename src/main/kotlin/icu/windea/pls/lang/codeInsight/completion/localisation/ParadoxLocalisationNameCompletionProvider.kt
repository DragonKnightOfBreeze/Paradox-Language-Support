package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

/**
 * 提供本地化名字的代码补全。
 */
class ParadoxLocalisationNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeLocalisationNames) return

        val position = parameters.position
        if (ParadoxPsiManager.isLocalisationLocaleLike(position)) return

        val element = position.parent?.parent as? ParadoxLocalisationProperty ?: return
        val file = parameters.originalFile.castOrNull<ParadoxLocalisationFile>() ?: return
        val type = ParadoxLocalisationType.resolve(file) ?: return
        val project = parameters.originalFile.project

        //本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnAnyPrefixChange()

        //提示localisation或者synced_localisation
        //排除正在输入的那一个
        val selector = selector(project, file).localisation()
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            .notSamePosition(element)
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
