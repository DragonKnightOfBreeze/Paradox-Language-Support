package icu.windea.pls.script.references

import com.intellij.psi.*
import icu.windea.pls.config.config.*

interface ParadoxTagAwarePsiReference : PsiReference {
    val config: CwtValueConfig
}
