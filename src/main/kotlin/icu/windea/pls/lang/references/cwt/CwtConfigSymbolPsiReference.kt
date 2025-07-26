package icu.windea.pls.lang.references.cwt

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.model.indexInfo.*

class CwtConfigSymbolPsiReference(
    element: CwtStringExpressionElement,
    rangeInElement: TextRange,
    val info: CwtConfigSymbolIndexInfo,
) : PsiReferenceBase<CwtStringExpressionElement>(element, rangeInElement) {
    val project by lazy { element.project }

    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
    }

    override fun resolve(): PsiElement? {
        val (name, type, readWriteAccess, _, gameType) = info
        val configType = CwtConfigType.entries[type] ?: return null
        return CwtConfigSymbolElement(element, name, configType, readWriteAccess, gameType, project)
    }
}
