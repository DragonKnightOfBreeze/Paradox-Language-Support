package icu.windea.pls.lang.model

import com.intellij.psi.*
import icu.windea.pls.script.psi.*

/**
 * 内联脚本的使用信息。
 */
data class ParadoxInlineScriptUsageInfo1(
    val pointer: SmartPsiElementPointer<ParadoxScriptProperty>,
    val hasConflict: Boolean,
    val hasRecursion: Boolean
) {
    val element get() = pointer.element
}
