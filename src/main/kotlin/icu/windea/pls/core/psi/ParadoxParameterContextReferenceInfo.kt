package icu.windea.pls.core.psi

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.lang.model.*

class ParadoxParameterContextReferenceInfo(
    private val elementPointer: SmartPsiElementPointer<PsiElement>,
    val rangeInElement: TextRange,
    val contextName: String,
    val argumentNames: Set<String>,
    val gameType: ParadoxGameType,
    val project: Project
) : UserDataHolderBase() {
    val element: PsiElement? get() = elementPointer.element
    
    enum class From {
        ContextReference, InContextReference, Argument
    }
}