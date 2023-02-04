package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供属性引用名字的代码补全。
 */
class ParadoxLocalisationPropertyReferenceCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		//val offsetInParent = parameters.offset - parameters.position.textRange.startOffset
		//val keyword = parameters.position.getKeyword(offsetInParent)
		val file = parameters.originalFile.castOrNull<ParadoxLocalisationFile>() ?: return
		val category = ParadoxLocalisationCategory.resolve(file) ?: return
		val project = parameters.originalFile.project
		
		//不提示predefined_parameter
		
		//提示localisation或者synced_localisation
		val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale()).distinctByName()
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
			ParadoxLocalisationCategory.Localisation -> ParadoxLocalisationSearch.processVariants(project, selector = selector, processor = processor)
			ParadoxLocalisationCategory.SyncedLocalisation -> ParadoxSyncedLocalisationSearch.processVariants(project, selector = selector, processor = processor)
		}
	}
}
