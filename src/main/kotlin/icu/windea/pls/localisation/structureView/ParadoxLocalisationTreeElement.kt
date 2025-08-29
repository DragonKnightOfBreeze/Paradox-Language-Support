package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement

abstract class ParadoxLocalisationTreeElement<T: PsiElement>(element: T): PsiTreeElementBase<T>(element)
