package icu.windea.pls.lang.references.script

import com.intellij.psi.PsiReference
import icu.windea.pls.config.config.CwtValueConfig

/**
 * @see ParadoxScriptExpressionPsiReference
 * @see ParadoxScriptTypeKeyPrefixPsiReference
 */
interface ParadoxScriptTagAwarePsiReference : PsiReference {
    val tagConfig: CwtValueConfig?
}
