package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement

abstract class ParadoxCsvTreeElement<T : PsiElement>(element: T) : PsiTreeElementBase<T>(element)
