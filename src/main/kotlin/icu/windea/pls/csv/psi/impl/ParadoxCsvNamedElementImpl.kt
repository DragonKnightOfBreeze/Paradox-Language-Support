package icu.windea.pls.csv.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import icu.windea.pls.csv.psi.ParadoxCsvNamedElement

abstract class ParadoxCsvNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ParadoxCsvNamedElement
