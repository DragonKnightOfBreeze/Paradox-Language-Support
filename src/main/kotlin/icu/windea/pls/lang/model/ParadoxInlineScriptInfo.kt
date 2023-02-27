package icu.windea.pls.lang.model

import icu.windea.pls.core.annotations.*

/**
 * 内联脚本信息。
 * 
 * 从`inline_script = xxx`中的`inline_script`出发，获取内联脚本的路径表达式（这里即是`xxx`），以及调用位置信息。
 * @param expression 内联脚本的路径表达式。
 */
@WithGameType(ParadoxGameType.Stellaris)
data class ParadoxInlineScriptInfo(
    val expression: String,
    val gameType: ParadoxGameType?
)
