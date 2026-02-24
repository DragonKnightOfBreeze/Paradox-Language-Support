package icu.windea.pls.lang.psi.light

import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import icu.windea.pls.lang.psi.ParadoxPsiManager
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.model.ParadoxGameType

abstract class ParadoxMockPsiElement(parent: PsiElement) : MockPsiElement(parent) {
    abstract val gameType: ParadoxGameType

    override fun getResolveScope(): GlobalSearchScope {
        return ParadoxSearchScope.fromElement(this) ?: super.getResolveScope()
    }

    override fun getUseScope(): SearchScope {
        return ParadoxSearchScope.fromElement(this) ?: super.getUseScope()
    }

    override fun getPresentableText(): String? {
        return name
    }

    override fun getLocationString(): @NlsSafe String? {
        val parent = parent
        ParadoxPsiManager.getFileInfoText(parent)?.let { return it }
        return parent.containingFile?.name
    }
}
