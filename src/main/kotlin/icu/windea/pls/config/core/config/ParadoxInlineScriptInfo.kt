package icu.windea.pls.config.core.config

/**
 * 内联脚本信息。
 * 
 * 从`inline_script = xxx`中的`inline_script`出发，获取内联脚本的路径表达式（这里即是`xxx`），以及调用位置信息。
 * @param expression 内联脚本的路径表达式。
 */
data class ParadoxInlineScriptInfo(
    val expression: String,
    val gameType: ParadoxGameType?
)

//* @param params 传入的参数信息.
//* @param config 内联脚本的调用位置对应的CWT规则。（从`inline_script`的父节点出发）
//* @param elementPath 内联脚本的调用位置对应的元素路径。（从`inline_script`的父节点出发）