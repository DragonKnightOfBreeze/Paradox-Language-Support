package com.windea.plugin.idea.paradox.script.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.windea.plugin.idea.paradox.script.psi.*

abstract class ParadoxScriptNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ParadoxScriptNamedElement
