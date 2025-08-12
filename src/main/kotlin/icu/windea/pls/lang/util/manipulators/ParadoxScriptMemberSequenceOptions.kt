package icu.windea.pls.lang.util.manipulators

/**
 * @param conditional 如果包含参数条件块，是否需要处理其中的子节点。
 * @param inline 如果包含内联脚本使用，是否需要先进行内联。
 */
data class ParadoxScriptMemberSequenceOptions(
    val conditional: Boolean = false,
    val inline: Boolean = false,
)
