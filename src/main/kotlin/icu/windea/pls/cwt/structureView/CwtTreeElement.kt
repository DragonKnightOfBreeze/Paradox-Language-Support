package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.impl.common.*
import com.intellij.psi.*

abstract class CwtTreeElement<T: PsiElement>(element: T): PsiTreeElementBase<T>(element)
