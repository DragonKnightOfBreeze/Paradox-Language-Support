package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.impl.common.*
import com.intellij.psi.*

abstract class ParadoxLocalisationTreeElement<T: PsiElement>(element: T): PsiTreeElementBase<T>(element)
