package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*

/**
 * 提供命令字段作用域的代码补全。
 */
@Suppress("UnstableApiUsage")
class ParadoxCommandScopeCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val file = parameters.originalFile
		val project = file.project
		
		//提示scope
		val gameType = file.fileInfo?.gameType ?: return
		val configGroup = getCwtConfig(project).get(gameType) ?: return
		CwtConfigHandler.completeLocalisationCommandScope(configGroup, result)
	}
}