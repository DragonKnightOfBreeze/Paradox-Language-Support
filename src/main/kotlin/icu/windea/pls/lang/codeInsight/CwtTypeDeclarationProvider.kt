package icu.windea.pls.lang.codeInsight

import com.intellij.codeInsight.navigation.actions.*
import com.intellij.psi.*

/**
 * 用于导航到类型声明（`Navigate > Type Declaration`）。
 */
class CwtTypeDeclarationProvider : TypeDeclarationProvider {
    override fun getSymbolTypeDeclarations(symbol: PsiElement): Array<PsiElement>? {
        val result = CwtTypeManager.findTypeDeclarations(symbol)
        if (result.isEmpty()) return null
        return result.toTypedArray()
    }
}
