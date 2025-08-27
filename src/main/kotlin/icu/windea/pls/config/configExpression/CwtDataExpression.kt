package icu.windea.pls.config.configExpression

import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.configExpression.impl.CwtDataExpressionResolverImpl
import icu.windea.pls.core.util.KeyRegistry

/**
 * CWT数据表达式。
 *
 * 用于描述 CWT 规则（.cwt）中“键/值”的取值形态，既可表示常量，也可表示由规则驱动的动态片段
 * （如 `value[...]`、`enum[...]`、`scope[...]`、`icon[...]`、`<definition>` 等）。
 *
 * - `isKey` 指示表达式来源于键还是值，解析与缓存会区分两类以提升命中率。
 * - `type` 为解析后的数据类型（见 `CwtDataType`）。若无法匹配任何规则，通常会回退为 `Constant`。
 * - 该类型实现了 `UserDataHolder`，可通过扩展属性（见 `_extensions.kt`）附加额外元数据
 *  （例如 `value`、`intRange`、`floatRange`、`ignoreCase`）。
 *
 * 参考：`references/cwt/guidance.md`、`docs/zh/config.md`。
 *
 * @property isKey 是否来源于“键”（true）或“值”（false）。
 * @property type 解析得到的表达式类型（即 CWT 规则中的 dataType）。
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
