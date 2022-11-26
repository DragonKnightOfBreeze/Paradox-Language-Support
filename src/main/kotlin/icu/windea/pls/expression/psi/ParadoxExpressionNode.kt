package icu.windea.pls.expression.psi

import com.intellij.openapi.editor.colors.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.expression.*
import icu.windea.pls.script.psi.*

interface ParadoxExpressionNode: PsiElement {
	val configGroup: CwtConfigGroup get() = parent.getUserData(ParadoxExpressionKeys.configGroupKey)!!
	
	fun getAttributesKey(): TextAttributesKey? = null
	
	fun getAttributesKeyExpression(element: ParadoxScriptExpressionElement, config: CwtDataConfig<*>): CwtDataExpression? = null
	
	fun getReference(element: ParadoxScriptExpressionElement): PsiReference? = null
	
	fun getUnresolvedError(element: ParadoxScriptExpressionElement): ParadoxExpressionError? = null
}
