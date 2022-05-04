package icu.windea.pls.localisation.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.localisation.psi.*

/**
 * 提供内置的命令字段的代码补全（非scripted_loc）。基于CWT规则文件。
 */
object CommandFieldCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val position = parameters.position //COMMAND_FIELD_ID
		val commandField = position.parent as? ParadoxLocalisationCommandField ?: return
		val project = position.project
		val gameType = parameters.originalFile.fileInfo?.gameType ?: return
		val configGroup = getCwtConfig(project).get(gameType) ?: return
		completeLocalisationCommand(configGroup, result)
		
		//TODO 补全的scope可能不正确
		result.addLookupAdvertisement(PlsBundle.message("scopeOfCompletionsMayBeIncorrect"))
	}
}