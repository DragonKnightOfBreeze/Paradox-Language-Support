package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.config.cwt.CwtConfigHandler.completeLocalisationCommandField
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*

/**
 * 提供命令字段名字的代码补全。
 */
class ParadoxLocalisationCommandFieldCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val offsetInParent = parameters.offset - parameters.position.textRange.startOffset
		val keyword = parameters.position.getKeyword(offsetInParent)
		val file = parameters.originalFile
		val project = file.project
		val gameType = file.fileInfo?.rootInfo?.gameType ?: return
		val configGroup = getCwtConfig(project).get(gameType) ?: return
		
		context.put(PlsCompletionKeys.completionTypeKey, parameters.completionType)
		context.put(PlsCompletionKeys.offsetInParentKey, offsetInParent)
		context.put(PlsCompletionKeys.keywordKey, keyword)
		
		//提示command
		context.completeLocalisationCommandField(configGroup, result)
		
		//提示<scripted_loc>
		val scriptedLocSelector = definitionSelector().gameTypeFrom(file).preferRootFrom(file).distinctByName()
		val scriptedLocs = ParadoxDefinitionSearch.search("scripted_loc", project, selector = scriptedLocSelector).findAll()
		if(scriptedLocs.isEmpty()) return
		for(scriptedLoc in scriptedLocs) {
			val name = scriptedLoc.definitionInfo?.name.orEmpty() //不应该为空
			val icon = PlsIcons.LocalisationCommandField
			val tailText = " from <scripted_loc>"
			val typeFile = scriptedLoc.containingFile
			val lookupElement = LookupElementBuilder.create(scriptedLoc, name).withIcon(icon)
				.withTailText(tailText, true)
				.withTypeText(typeFile.name, typeFile.icon, true)
				.withCaseSensitivity(false) //忽略大小写
			result.addElement(lookupElement)
		}
		
		//提示value[variable]
		val variableSelector = valueSetValueSelector().gameTypeFrom(file).preferRootFrom(file).distinctByValue()
		val variableQuery = ParadoxValueSetValueSearch.search("variable", project, selector = variableSelector)
		variableQuery.processResult { variable -> 
			val value = variable.value.substringBefore('@')
			val icon = PlsIcons.Variable
			val tailText = " from value[variable]"
			val lookupElement = LookupElementBuilder.create(variable, value)
				.withIcon(icon)
				.withTailText(tailText, true)
				.withCaseSensitivity(false) //忽略大小写
			result.addElement(lookupElement)
			true
		}
	}
}

