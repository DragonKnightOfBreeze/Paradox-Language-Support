package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

/**
 * 提供标签的代码补全。基于CWT规则文件。
 */
object ParadoxTagCompletionProvider: CompletionProvider<CompletionParameters>(){
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val stringElement = parameters.position.parent.castOrNull<ParadoxScriptString>() ?: return
		stringElement.completeTagConfig(result)
	}
}