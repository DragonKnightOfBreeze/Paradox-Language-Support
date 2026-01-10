package icu.windea.pls.config.configExpression

import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.configExpression.impl.CwtDataExpressionResolverImpl
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.ep.configExpression.CwtDataExpressionResolver

/**
 * 数据表达式。
 *
 * 用于描述脚本文件中的表达式（键或值）的匹配模式，基于数据类型以及数种元数据。
 *
 * 说明：
 * - 对应的数据类型可通过 [type] 获取。
 * - 主要的元数据可通过 [value] 获取。
 * - 额外的元数据会存储到 [UserDataHolder] 中，可通过扩展属性获取。
 *
 * 适用对象：定义成员对应的规则的键或值。
 *
 * CWTools 兼容性：兼容，但存在较多扩展与改进。
 *
 * @property isKey 是否来源于作为键的表达式。
 * @property type 解析得到的数据类型。
 * @property value 主要的元数据。可用于存储定义类型、枚举名等信息。
 *
 * @see CwtDataType
 * @see CwtDataExpressionResolver
 */
interface CwtDataExpression : CwtConfigExpression, UserDataHolder {
    val isKey: Boolean
    val type: CwtDataType
    var value: String?

    interface Resolver {
        fun create(expressionString: String, isKey: Boolean, type: CwtDataType): CwtDataExpression
        fun resolveEmpty(isKey: Boolean): CwtDataExpression
        fun resolveBlock(): CwtDataExpression
        fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression
        fun resolveTemplate(expressionString: String): CwtDataExpression
    }

    object Keys : KeyRegistry()

    companion object : Resolver by CwtDataExpressionResolverImpl()
}
