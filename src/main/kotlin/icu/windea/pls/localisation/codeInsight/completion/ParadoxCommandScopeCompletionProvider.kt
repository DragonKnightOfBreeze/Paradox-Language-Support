package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.CwtConfigHandler.completeLocalisationCommandScope
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供命令字段作用域的代码补全。
 */
class ParadoxCommandScopeCompletionProvider : CompletionProvider<CompletionParameters>() {
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
		
		val prevScope = parameters.position.parent?.siblings(forward = false, withSelf = false)
			?.find { it is ParadoxLocalisationCommandScope }
			?.text
		if(prevScope != null) context.put(PlsCompletionKeys.prevScopeKey, prevScope)
		
		//提示scope
		context.completeLocalisationCommandScope(configGroup, result)
	}
}