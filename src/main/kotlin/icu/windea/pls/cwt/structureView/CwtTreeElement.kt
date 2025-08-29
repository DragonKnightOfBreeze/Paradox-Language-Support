package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement

abstract class CwtTreeElement<T: PsiElement>(element: T): PsiTreeElementBase<T>(element)
