package icu.windea.pls.config.configExpression

import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.configExpression.impl.CwtDataExpressionResolverImpl
import icu.windea.pls.core.util.KeyRegistry

/**
 * CWT数据表达式。
 *
 * @property type 表达式类型，即CWT规则中的dataType。
 */
interface CwtDataExpression : CwtConfigExpression, UserDataHolder {
    val isKey: Boolean
    val type: CwtDataType

    interface Resolver {
        fun resolveEmpty(isKey: Boolean): CwtDataExpression
        fun resolveBlock(): CwtDataExpression
        fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression
        fun resolveTemplate(expressionString: String): CwtDataExpression
        fun create(expressionString: String, isKey: Boolean, type: CwtDataType): CwtDataExpression
    }

    object Keys : KeyRegistry()

    companion object : Resolver by CwtDataExpressionResolverImpl()
}
