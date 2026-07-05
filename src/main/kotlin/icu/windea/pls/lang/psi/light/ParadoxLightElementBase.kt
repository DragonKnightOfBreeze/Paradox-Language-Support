package icu.windea.pls.lang.psi.light

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.core.psi.light.LightElementBase
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.model.ParadoxGameType
import javax.swing.Icon

abstract class ParadoxLightElementBase(parent: PsiElement) : LightElementBase(parent), ItemPresentation {
    abstract val gameType: ParadoxGameType

    final override fun getResolveScope(): GlobalSearchScope {
        return ParadoxSearchScope.fromElement(this) ?: ResolveScopeManager.getElementResolveScope(this)
    }

    final override fun getUseScope(): SearchScope {
        return ParadoxSearchScope.fromElement(this) ?: ResolveScopeManager.getElementUseScope(this)
    }

    final override fun getPresentation(): ItemPresentation {
        return this
    }

    final override fun getIcon(unused: Boolean): Icon? {
        return getIcon(0)
    }

    final override fun getPresentableText(): String? {
        return name
    }

    final override fun getLocationString(): @NlsSafe String? {
        val element = parent
        ParadoxPsiPresentationService.getFileInfoText(element)?.let { return it }
        return element.containingFile?.name
    }

    override fun toString(): String {
        return PsiService.toPresentableString(this)
    }
}
