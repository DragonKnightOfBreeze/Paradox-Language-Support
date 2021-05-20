package icu.windea.pls.cwt.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import icu.windea.pls.cwt.psi.*

abstract class CwtNamedElementImpl(node:ASTNode):ASTWrapperPsiElement(node),CwtNamedElement