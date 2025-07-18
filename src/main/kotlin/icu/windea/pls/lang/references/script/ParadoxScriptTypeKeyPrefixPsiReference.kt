package icu.windea.pls.lang.references.script

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.psi.*

class ParadoxScriptTypeKeyPrefixPsiReference(
    element: ParadoxScriptString,
    rangeInElement: TextRange,
    override val config: CwtValueConfig
) : PsiResolvedReference<CwtValue>(element, rangeInElement, config.pointer.element), ParadoxScriptTagAwarePsiReference
