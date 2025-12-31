package icu.windea.pls.lang.references.script

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey

class ParadoxDefinitionInjectionModePsiReference(
    element: ParadoxScriptPropertyKey,
    rangeInElement: TextRange,
    val info: ParadoxDefinitionInjectionInfo,
) : PsiReferenceBase<ParadoxScriptPropertyKey>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement {
        throw IncorrectOperationException() // 不支持重命名
    }

    override fun resolve(): PsiElement? {
        return info.modeConfig.pointer.element
    }
}
