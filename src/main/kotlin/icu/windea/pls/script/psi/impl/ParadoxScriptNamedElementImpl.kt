package icu.windea.pls.script.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import icu.windea.pls.script.psi.ParadoxScriptNamedElement

abstract class ParadoxScriptNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ParadoxScriptNamedElement
