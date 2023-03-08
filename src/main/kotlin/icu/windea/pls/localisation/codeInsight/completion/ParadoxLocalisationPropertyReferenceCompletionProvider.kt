package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.patterns.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供属性引用名字的代码补全。
 */
class ParadoxLocalisationPropertyReferenceCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val offsetInParent = parameters.offset - parameters.position.textRange.startOffset
        val keyword = parameters.position.getKeyword(offsetInParent)
        val file = parameters.originalFile.castOrNull<ParadoxLocalisationFile>() ?: return
        val category = ParadoxLocalisationCategory.resolve(file) ?: return
        val project = parameters.originalFile.project
        
        //不提示predefined_parameter
        
        //因为这里的提示结果可能有上千条，按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(keyword.length))
        
        //提示localisation或者synced_localisation
        val selector = localisationSelector(project, file)
            .contextSensitive()
            .preferLocale(preferredParadoxLocale())
            .distinctByName()
        val processor: ProcessEntry.(ParadoxLocalisationProperty) -> Boolean = {
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
