package icu.windea.pls.lang.model

import com.intellij.psi.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.script.psi.*

/**
 * 内联脚本的使用信息。
 */
@WithGameType(ParadoxGameType.Stellaris)
data class ParadoxInlineScriptUsageInfo(
    val pointer: SmartPsiElementPointer<ParadoxScriptProperty>,
    val hasConflict: Boolean
)
