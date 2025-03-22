package icu.windea.pls.script.references

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.references.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.psi.*

class ParadoxTypeKeyPrefixPsiReference(
    element: ParadoxScriptString,
    rangeInElement: TextRange,
    override val config: CwtValueConfig
) : PsiResolvedReference<CwtValue>(element, rangeInElement, config.pointer.element), ParadoxTagAwarePsiReference
