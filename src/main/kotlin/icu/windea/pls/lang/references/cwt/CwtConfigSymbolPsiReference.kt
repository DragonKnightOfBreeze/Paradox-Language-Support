package icu.windea.pls.lang.references.cwt

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.core.unquote
import icu.windea.pls.cwt.psi.CwtPropertyKey
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import icu.windea.pls.lang.psi.mock.CwtConfigSymbolElement
import icu.windea.pls.model.indexInfo.CwtConfigSymbolIndexInfo

class CwtConfigSymbolPsiReference(
    element: CwtStringExpressionElement,
    rangeInElement: TextRange,
    val info: CwtConfigSymbolIndexInfo,
) : PsiReferenceBase<CwtStringExpressionElement>(element, rangeInElement) {
    val project by lazy { element.project }

    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        //兼容性处理（property VS propertyKey）
        if (element is CwtPropertyKey && isReferenceTo(element.parent)) return true
        return super.isReferenceTo(element)
    }

    override fun resolve(): PsiElement? {
        val (name, type, readWriteAccess, _, _, gameType) = info
        val configType = CwtConfigType.entries[type] ?: return null
        return CwtConfigSymbolElement(element, name, configType, readWriteAccess, gameType, project)
    }
}
