package icu.windea.pls.model

import icu.windea.pls.lang.util.ParadoxDefineManager

/**
 * 定值变量的解析信息。
 *
 * @property namespace 命名空间。
 * @property variable 变量名。
 */
data class ParadoxDefineVariableInfo(
    override val namespace: String,
    val variable: String,
    override val gameType: ParadoxGameType,
) : ParadoxDefineInfo {
    override val expression: String get() = ParadoxDefineManager.getExpression(namespace, variable)

    override fun toString(): String {
        return "ParadoxDefineVariableInfo(namespace=$namespace, variable=$variable, gameType=$gameType)"
    }
}
