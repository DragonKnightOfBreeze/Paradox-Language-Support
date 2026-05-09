package icu.windea.pls.model

import com.intellij.openapi.project.Project
import icu.windea.pls.config.config.delegated.CwtDefineConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 定值的解析信息。
 *
 * @property namespace 命名空间。
 * @property expression 表达式。示例：`Namespace`、`Namespace.Variable`。
 *
 * @see CwtDefineConfig
 */
sealed interface ParadoxDefineInfo {
    val namespace: String
    val expression: String
    val config: CwtDefineConfig?
    val configGroup: CwtConfigGroup

    val project: Project get() = configGroup.project
    val gameType: ParadoxGameType get() = configGroup.gameType
}
