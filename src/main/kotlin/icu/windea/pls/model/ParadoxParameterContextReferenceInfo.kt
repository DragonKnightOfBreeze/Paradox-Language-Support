package icu.windea.pls.model

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import javax.swing.Icon

/**
 * @param contextNameRange 表示作用域名字的那段文本在整个文件中的文本范围。
 */
class ParadoxParameterContextReferenceInfo(
    private val elementPointer: SmartPsiElementPointer<out PsiElement>,
    val contextName: String,
    val contextIcon: Icon?,
    val contextKey: String,
    private val contextNameElementPointer: SmartPsiElementPointer<out PsiElement>,
    val contextNameRange: TextRange,
    val arguments: List<Argument>,
    val gameType: ParadoxGameType,
    val project: Project
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
        private val argumentNameElementPointer: SmartPsiElementPointer<out PsiElement>,
        val argumentNameRange: TextRange,
        private val argumentValueElementPointer: SmartPsiElementPointer<out PsiElement>?,
        val argumentValueRange: TextRange?
    ) {
        val argumentNameElement: PsiElement? get() = argumentNameElementPointer.element
        val argumentValueElement: PsiElement? get() = argumentValueElementPointer?.element
    }
}