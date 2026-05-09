package icu.windea.pls.model

import icu.windea.pls.config.config.delegated.CwtDefineNamespaceConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.util.ParadoxDefineManager

/**
 * 定值命名空间的解析信息。
 *
 * @property namespace 命名空间。
 *
 * @see CwtDefineNamespaceConfig
 */
data class ParadoxDefineNamespaceInfo(
    override val namespace: String,
    override val configGroup: CwtConfigGroup,
    override val config: CwtDefineNamespaceConfig?,
) : ParadoxDefineInfo {
    override val expression: String get() = ParadoxDefineManager.getExpression(namespace, null)

    override fun toString(): String {
        return "ParadoxDefineNamespaceInfo(namespace=$namespace, gameType=$gameType)"
    }
}
