package icu.windea.pls.lang.references.script

import com.intellij.psi.*
import icu.windea.pls.config.config.*

interface ParadoxTagAwarePsiReference : PsiReference {
    val config: CwtValueConfig
}
