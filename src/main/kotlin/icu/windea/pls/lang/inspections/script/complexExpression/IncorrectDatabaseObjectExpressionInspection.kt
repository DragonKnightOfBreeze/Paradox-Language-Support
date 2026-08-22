package icu.windea.pls.lang.inspections.script.complexExpression

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression

/**
 * 检查是否存在不正确的数据库对象表达式（[ParadoxDatabaseObjectExpression]）。不适用于嵌套的此类复杂表达式。
 */
class IncorrectDatabaseObjectExpressionInspection : IncorrectComplexExpressionInspectionBase() {
    override fun isAvailableForConfig(config: CwtMemberConfig<*>): Boolean {
        val dataType = config.configExpression.type
        return dataType == CwtDataTypes.DatabaseObject
    }
}
