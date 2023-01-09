package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供本地化名字的代码补全。
 */
class ParadoxLocalisationNameCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val element = parameters.position.parent?.parent as? ParadoxLocalisationProperty ?: return
		//val offsetInParent = parameters.offset - element.textRange.startOffset
		//val keyword = element.getKeyword(offsetInParent)
		val file = parameters.originalFile.castOrNull<ParadoxLocalisationFile>() ?: return
		val category = ParadoxLocalisationCategory.resolve(file) ?: return
		val project = parameters.originalFile.project
		
		//提示localisation或者synced_localisation
		//排除正在输入的那一个
		val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
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
			ParadoxLocalisationCategory.Localisation -> ParadoxLocalisationSearch.processVariants(project, selector = selector, processor = processor)
			ParadoxLocalisationCategory.SyncedLocalisation -> ParadoxSyncedLocalisationSearch.processVariants(project, selector = selector, processor = processor)
		}
	}
}
