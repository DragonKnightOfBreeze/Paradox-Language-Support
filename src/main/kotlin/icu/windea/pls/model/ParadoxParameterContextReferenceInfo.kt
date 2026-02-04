package icu.windea.pls.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import javax.swing.Icon

/**
 * 参数上下文引用信息。
 *
 * @param contextNameRange 表示上下文名字的那段文本在整个文件中的文本范围。
 */
@Suppress("unused")
class ParadoxParameterContextReferenceInfo(
    private val elementPointer: SmartPsiElementPointer<out PsiElement>,
    val contextName: String,
    val contextIcon: Icon?,
    val contextKey: String,
    private val contextNameElementPointer: SmartPsiElementPointer<out PsiElement>,
    val contextNameRange: TextRange,
    val arguments: List<Argument>,
    val project: Project,
    val gameType: ParadoxGameType,
) : UserDataHolderBase() {
    val element: PsiElement? get() = elementPointer.element
    val contextNameElement: PsiElement? get() = contextNameElementPointer.element

    enum class From {
        /** extraArgs: config, completionOffset? */
        Argument,
        /** extraArgs: contextConfig */
        ContextReference,
        /** extraArgs: offset? */
        InContextReference
    }

    /**
     * @param argumentNameRange 表示传入参数名的那段文本在整个文件中的文本范围。
     * @param argumentValueRange 表示传入参数值的那段文本在整个文件中的文本范围。
     */
    class Argument(
        val argumentName: String,
        val argumentValue: String?,
        private val argumentNameElementPointer: SmartPsiElementPointer<out PsiElement>,
        val argumentNameRange: TextRange,
        private val argumentValueElementPointer: SmartPsiElementPointer<out PsiElement>?,
        val argumentValueRange: TextRange?,
    ) {
        val argumentNameElement: PsiElement? get() = argumentNameElementPointer.element
        val argumentValueElement: PsiElement? get() = argumentValueElementPointer?.element
    }
}
