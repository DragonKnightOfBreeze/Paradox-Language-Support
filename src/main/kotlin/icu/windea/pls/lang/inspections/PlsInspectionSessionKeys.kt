package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.pls.core.util.*

object PlsInspectionSessionKeys : KeyRegistry()

//var LocalInspectionToolSession.disabled: Boolean? by createKey(PlsInspectionSessionKeys)
var LocalInspectionToolSession.disabledElement: PsiElement? by createKey(PlsInspectionSessionKeys)
