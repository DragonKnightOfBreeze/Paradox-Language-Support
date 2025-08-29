package icu.windea.pls.cwt.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import icu.windea.pls.cwt.psi.CwtNamedElement

abstract class CwtNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), CwtNamedElement
