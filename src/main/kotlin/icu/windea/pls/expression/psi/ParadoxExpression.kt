package icu.windea.pls.expression.psi

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.expression.*

interface ParadoxExpression : ParadoxExpressionNode {
	val configGroup: CwtConfigGroup get() = getUserData(ParadoxExpressionKeys.configGroupKey)!!
	
	fun complete(context: ProcessingContext, result: CompletionResultSet) = pass()
}
