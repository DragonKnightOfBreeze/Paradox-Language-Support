package icu.windea.pls.config

import icu.windea.pls.ep.dataExpression.*

//note that CwtDataType can be extended, so that do not declare it as an enum class

/**
 * @see CwtDataTypes
 * @see CwtDataTypeGroups
 * @see CwtDataExpressionResolver
 */
data class CwtDataType(
    val name: String
)
