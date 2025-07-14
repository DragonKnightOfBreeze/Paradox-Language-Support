package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.impl.common.*
import com.intellij.psi.*

abstract class ParadoxCsvTreeElement<T : PsiElement>(element: T) : PsiTreeElementBase<T>(element)
