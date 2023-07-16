package icu.windea.pls.lang.model

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*

class ParadoxParameterContextReferenceInfo(
    private val elementPointer: SmartPsiElementPointer<PsiElement>,
    val contextName: String,
    val argumentNames: Set<String>,
    val contextNameRange: TextRange,
    val gameType: ParadoxGameType,
    val project: Project
) : UserDataHolderBase() {
    val element: PsiElement? get() = elementPointer.element
    
    enum class From {
        ContextReference, InContextReference, Argument
    }
}