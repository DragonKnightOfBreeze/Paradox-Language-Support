package com.windea.plugin.idea.pls.cwt.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.windea.plugin.idea.pls.cwt.psi.*

abstract class CwtNamedElementImpl(node:ASTNode):ASTWrapperPsiElement(node),CwtNamedElement