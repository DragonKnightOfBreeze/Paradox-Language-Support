package icu.windea.pls.lang.inspections.script.complexExpression

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScriptValueReferenceExpression

/**
 * 不正确的脚本值引用表达式（[ParadoxScriptValueReferenceExpression]）的代码检查。
 */
class IncorrectScriptValueReferenceExpressionInspection : IncorrectComplexExpressionInspectionBase() {
    override fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean {
        val dataType = config.configExpression.type
        return dataType == CwtDataTypes.ScriptValueReference
    }
}
