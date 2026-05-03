package icu.windea.pls.model

/**
 * 定值的解析信息。
 *
 * @property namespace 命名空间。
 * @property expression 表达式。示例：`Namespace`、`Namespace.Variable`。
 */
sealed interface ParadoxDefineInfo {
    val namespace: String
    val expression: String
    val gameType: ParadoxGameType
}
