package icu.windea.pls.lang.model

import com.intellij.psi.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.script.psi.*

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

/**
 * 内联脚本的使用信息。
 */
@WithGameType(ParadoxGameType.Stellaris)
data class ParadoxInlineScriptUsageInfo(
    val pointer: SmartPsiElementPointer<ParadoxScriptProperty>,
    val hasConflict: Boolean
)