package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

/**
 * 提供本地化名字的代码补全。
 */
class ParadoxLocalisationNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if(!getSettings().completion.completeLocalisationNames) return
        
        val position = parameters.position
        if(ParadoxPsiManager.isLocalisationLocaleLike(position)) return
        
        val element = position.parent?.parent as? ParadoxLocalisationProperty ?: return
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = position.getKeyword(offsetInParent)
        val file = parameters.originalFile.castOrNull<ParadoxLocalisationFile>() ?: return
        val category = ParadoxLocalisationCategory.resolve(file) ?: return
        val project = parameters.originalFile.project
        
        //因为这里的提示结果可能有上千条，按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnAnyPrefixChange()
        
        //提示localisation或者synced_localisation
        //排除正在输入的那一个
        val selector = localisationSelector(project, file)
            .contextSensitive()
            .preferLocale(ParadoxLocaleHandler.getPreferredLocale())
            .notSamePosition(element)
            //.distinctByName() //这里selector不需要指定去重
        val processor: (ParadoxLocalisationProperty) -> Boolean = processor@{
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
        when(category) {
            ParadoxLocalisationCategory.Localisation -> ParadoxLocalisationSearch.processVariants(keyword, selector, processor)
            ParadoxLocalisationCategory.SyncedLocalisation -> ParadoxSyncedLocalisationSearch.processVariants(keyword, selector, processor)
        }
    }
}
