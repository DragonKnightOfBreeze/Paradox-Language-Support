package com.windea.plugin.idea.paradox.localisation.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.windea.plugin.idea.paradox.localisation.psi.*

abstract class ParadoxLocalisationNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ParadoxLocalisationNamedElement
