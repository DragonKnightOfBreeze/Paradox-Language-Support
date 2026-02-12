package icu.windea.pls.model.index

/**
 * 定值变量的键。
 *
 * @property namespace 命名空间。
 * @property variable 变量名。
 *
 * @see icu.windea.pls.lang.index.ParadoxDefineVariableIndex
 */
data class ParadoxDefineVariableKey(
    val namespace: String,
    val variable: String,
)
