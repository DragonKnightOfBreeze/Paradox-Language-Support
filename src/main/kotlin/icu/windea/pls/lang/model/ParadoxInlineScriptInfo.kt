package icu.windea.pls.lang.model

import com.intellij.psi.*
import icu.windea.pls.core.annotations.*

/**
 * 内联脚本信息。
 * @param expression 内联脚本的路径表达式。
 */
@WithGameType(ParadoxGameType.Stellaris)
data class ParadoxInlineScriptInfo(
    val expression: String,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType,
): ParadoxScriptExpressionInfo {
    @Volatile override var file: PsiFile? = null
}
