package icu.windea.pls.model

import icu.windea.pls.lang.util.ParadoxDefineManager

/**
 * 定值的命名空间的解析信息。
 *
 * @property namespace 命名空间。
 */
data class ParadoxDefineNamespaceInfo(
    override val namespace: String,
    override val gameType: ParadoxGameType,
): ParadoxDefineInfo {
    override val expression: String get() = ParadoxDefineManager.getExpression(namespace, null)

    override fun toString(): String {
        return "ParadoxDefineNamespaceInfo(namespace=$namespace, gameType=$gameType)"
    }
}
