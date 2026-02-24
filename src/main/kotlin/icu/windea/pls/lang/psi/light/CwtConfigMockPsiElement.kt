package icu.windea.pls.lang.psi.light

import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import icu.windea.pls.model.ParadoxGameType

abstract class CwtConfigMockPsiElement(parent: PsiElement) : MockPsiElement(parent) {
    abstract val gameType: ParadoxGameType

    override fun getResolveScope(): GlobalSearchScope {
        return ResolveScopeManager.getElementResolveScope(this)
    }

    override fun getUseScope(): SearchScope {
        return GlobalSearchScope.allScope(project)
    }

    override fun getPresentableText(): String? {
        return name
    }

    override fun getLocationString(): @NlsSafe String? {
        val parent = parent
        return parent.containingFile?.name
    }
}
