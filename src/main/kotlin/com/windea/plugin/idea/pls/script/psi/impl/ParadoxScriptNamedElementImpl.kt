package com.windea.plugin.idea.pls.script.psi.impl

import com.intellij.extapi.psi.*
import com.intellij.lang.*
import com.windea.plugin.idea.pls.script.psi.*

abstract class ParadoxScriptNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ParadoxScriptNamedElement
