package icu.windea.pls.lang.model

import com.intellij.psi.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.script.psi.*

/**
 * 内联脚本信息。
 * @param expression 内联脚本的路径表达式。
 * @param offset 对应的[ParadoxScriptPropertyKey]在文件中的起始位置。
 */
@WithGameType(ParadoxGameType.Stellaris)
data class ParadoxInlineScriptInfo(
    val expression: String,
    val offset: Int,
    override val gameType: ParadoxGameType,
): ParadoxScriptExpressionInfo {
    override var file: PsiFile? = null
}
