package icu.windea.pls.lang.codeInsight

import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.cwt.psi.CwtExpressionElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.ParadoxScriptProperty

val CwtExpressionElement.type: CwtType get() = CwtTypeManager.getType(this)
val CwtExpressionElement.configType: CwtConfigType? get() = CwtTypeManager.getConfigType(this)

val ParadoxExpressionElement.type: ParadoxType get() = ParadoxTypeManager.getType(this)
val ParadoxExpressionElement.expression: String get() = ParadoxTypeManager.getExpression(this) ?: this.text
val ParadoxExpressionElement.configExpression: String? get() = ParadoxTypeManager.getConfigExpression(this)

val ParadoxScriptProperty.expression: String get() = "${propertyKey.expression} = ${propertyValue?.expression ?: PlsStringConstants.unknown}"
val ParadoxScriptProperty.configExpression: String get() = "${propertyKey.configExpression} = ${propertyValue?.configExpression ?: PlsStringConstants.unknown}"
