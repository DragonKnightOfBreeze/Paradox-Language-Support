package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.localisation.psi.*

/**
 * 提示对格式化引用的代码补全。
 */
@WithGameType(ParadoxGameType.Stellaris)
class ParadoxStellarisNamePartCompletionProvider: CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val localisationProperty = parameters.position.parentOfType<ParadoxLocalisationProperty>() ?: return
		val localisationKey = localisationProperty.name
		val originalFile = parameters.originalFile
		val project = originalFile.project
		val valueSetName = StellarisNameFormatHandler.getValueSetName(localisationKey, project) ?: return
		val gameType = selectGameType(originalFile)
		val tailText = "by value[$valueSetName]"
		val selector = valueSetValueSelector().gameType(gameType).declarationOnly().distinctByValue()
		val valueSetValueQuery = ParadoxValueSetValueSearch.search(valueSetName, project, selector = selector)
		valueSetValueQuery.processQuery { valueSetValue ->
			val value = ParadoxValueSetValueHandler.getName(valueSetValue) ?: return@processQuery true
			val icon = PlsIcons.ValueSetValue
			//不显示typeText
			val lookupElement = LookupElementBuilder.create(valueSetValue, value)
				.withIcon(icon)
				.withTailText(tailText)
				.withCaseSensitivity(false) //忽略大小写
			result.addElement(lookupElement)
			true
		}
	}
}