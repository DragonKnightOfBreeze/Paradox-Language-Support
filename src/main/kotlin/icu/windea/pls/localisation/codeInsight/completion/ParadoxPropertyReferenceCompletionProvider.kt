package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供属性引用名字的代码补全。
 */
object ParadoxPropertyReferenceCompletionProvider: CompletionProvider<CompletionParameters>(){
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
		
		val keyword = parameters.position.keyword
		val file = parameters.originalFile.castOrNull<ParadoxLocalisationFile>() ?: return
		val category = ParadoxLocalisationCategory.resolve(file) ?: return
		val project = parameters.originalFile.project
		
		//不提示predefined_variable
		
		//提示localisation或者synced_localisation
		val processor: ProcessEntry.(ParadoxLocalisationProperty) -> Boolean = {
			val name = it.name
			val icon = it.icon
			val typeText = it.containingFile.name
			val lookupElement = LookupElementBuilder.create(it, name).withIcon(icon).withTypeText(typeText, true)
			result.addElement(lookupElement)
			true
		}
		when(category){
			ParadoxLocalisationCategory.Localisation -> processLocalisationVariants(keyword, project, processor = processor)
			ParadoxLocalisationCategory.SyncedLocalisation -> processSyncedLocalisationVariants(keyword, project, processor = processor)
		}
	}
}
