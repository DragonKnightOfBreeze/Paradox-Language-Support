package icu.windea.pls.lang.model

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*

/**
 * @param contextNameRange 表示作用域名字的那段文本在整个文件中的文本范围。
 */
class ParadoxParameterContextReferenceInfo(
    private val elementPointer: SmartPsiElementPointer<PsiElement>,
    val contextName: String,
    val argumentNames: Set<String>,
    val contextNameRange: TextRange,
    val gameType: ParadoxGameType,
    val project: Project,
    val arguments: List<ParadoxParameterReferenceInfo>
) : UserDataHolderBase() {
    val element: PsiElement? get() = elementPointer.element
    
    enum class From {
        /** extraArgs: config, completionOffset? */
        Argument,
        /** extraArgs: contextConfig */
        ContextReference,
        /** extraArgs: offset? */
        InContextReference
    }
}

/**
 * @param argumentNameRange 表示传入参数名的那段文本在整个文件中的文本范围。
 * @param argumentValueRange 表示传入参数值的那段文本在整个文件中的文本范围。
 */
class ParadoxParameterReferenceInfo(
    val argumentName: String,
    val argumentNameRange: TextRange,
    val argumentValueRange: TextRange?
)