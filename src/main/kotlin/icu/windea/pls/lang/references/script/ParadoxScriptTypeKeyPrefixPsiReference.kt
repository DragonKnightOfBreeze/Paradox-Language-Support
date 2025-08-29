package icu.windea.pls.lang.references.script

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.psi.PsiResolvedReference
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.script.psi.ParadoxScriptString

class ParadoxScriptTypeKeyPrefixPsiReference(
    element: ParadoxScriptString,
    rangeInElement: TextRange,
    override val config: CwtValueConfig
) : PsiResolvedReference<CwtValue>(element, rangeInElement, config.pointer.element), ParadoxScriptTagAwarePsiReference
