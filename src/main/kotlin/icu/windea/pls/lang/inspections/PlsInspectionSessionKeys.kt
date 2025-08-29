package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.psi.PsiElement
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue

object PlsInspectionSessionKeys : KeyRegistry()

//var LocalInspectionToolSession.disabled: Boolean? by createKey(PlsInspectionSessionKeys)
var LocalInspectionToolSession.disabledElement: PsiElement? by createKey(PlsInspectionSessionKeys)
