package icu.windea.pls.localisation.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import icu.windea.pls.localisation.psi.ParadoxLocalisationNamedElement

abstract class ParadoxLocalisationNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ParadoxLocalisationNamedElement
