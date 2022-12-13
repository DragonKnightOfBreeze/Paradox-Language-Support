package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供属性引用名字的代码补全。
 */
class ParadoxLocalisationPropertyReferenceCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
		
		val caretOffset = parameters.offset - parameters.position.textRange.startOffset
		val keyword = parameters.position.getKeyword(caretOffset)
		val file = parameters.originalFile.castOrNull<ParadoxLocalisationFile>() ?: return
		val category = ParadoxLocalisationCategory.resolve(file) ?: return
		val project = parameters.originalFile.project
		
		//不提示predefined_parameter
		
		//提示localisation或者synced_localisation
		val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
		val processor: ProcessEntry.(ParadoxLocalisationProperty) -> Boolean = {
			val name = it.name
			val icon = it.icon
			val typeFile = it.containingFile
			val lookupElement = LookupElementBuilder.create(it, name).withIcon(icon)
				.withTypeText(typeFile.name, typeFile.icon, true)
			result.addElement(lookupElement)
			true
		}
		when(category) {
			ParadoxLocalisationCategory.Localisation -> processLocalisationVariants(keyword, project, selector = selector, processor = processor)
			ParadoxLocalisationCategory.SyncedLocalisation -> processSyncedLocalisationVariants(keyword, project, selector = selector, processor = processor)
		}
	}
}
