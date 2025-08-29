package icu.windea.pls.lang.references.script

import com.intellij.psi.PsiReference
import icu.windea.pls.config.config.CwtValueConfig

interface ParadoxScriptTagAwarePsiReference : PsiReference {
    val config: CwtValueConfig
}
