package icu.windea.pls.lang.codeInsight.type

import com.intellij.codeInsight.navigation.actions.TypeDeclarationProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.type.ParadoxTypeService

/**
 * 用于导航到类型声明（`Navigate > Type Declaration`）。
 */
class ParadoxTypeDeclarationProvider : TypeDeclarationProvider {
    override fun getSymbolTypeDeclarations(symbol: PsiElement): Array<PsiElement>? {
        val result = ParadoxTypeService.findTypeDeclarations(symbol)
        if (result.isEmpty()) return null
        return result.toTypedArray()
    }
}
