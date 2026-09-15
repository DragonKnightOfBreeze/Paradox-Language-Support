package icu.windea.pls.cwt.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiNamedElement

interface CwtNamedElement : PsiNamedElement, PsiNameIdentifierOwner, NavigatablePsiElement
