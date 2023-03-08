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
 * 提供本地化名字的代码补全。
 */
class ParadoxLocalisationNameCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		if(!getSettings().completion.completeLocalisationNames) return
		
		val element = parameters.position.parent?.parent as? ParadoxLocalisationProperty ?: return
		val offsetInParent = parameters.offset - element.textRange.startOffset
		val keyword = element.getKeyword(offsetInParent)
		val file = parameters.originalFile.castOrNull<ParadoxLocalisationFile>() ?: return
		val category = ParadoxLocalisationCategory.resolve(file) ?: return
		val project = parameters.originalFile.project
		
		//因为这里的提示结果可能有上千条，按照输入的关键字过滤结果，关键字变更时重新提示
		result.restartCompletionOnPrefixChange(StandardPatterns.string().shorterThan(keyword.length))
		
		//提示localisation或者synced_localisation
		//排除正在输入的那一个
		val selector = localisationSelector(project, file).contextSensitive()
			.preferLocale(preferredParadoxLocale())
			.notSamePosition(element)
			.distinctByName()
		val processor: ProcessEntry.(ParadoxLocalisationProperty) -> Boolean = processor@{
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
