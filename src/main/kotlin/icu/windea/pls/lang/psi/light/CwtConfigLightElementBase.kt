package icu.windea.pls.lang.psi.light

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.core.psi.light.LightElementBase
import icu.windea.pls.model.ParadoxGameType
import javax.swing.Icon

/**
 * 来自规则文件的 [LightElementBase]。
 */
abstract class CwtConfigLightElementBase(parent: PsiElement) : LightElementBase(parent), ItemPresentation {
    abstract val gameType: ParadoxGameType

    final override fun getResolveScope(): GlobalSearchScope {
        return ResolveScopeManager.getElementResolveScope(this)
    }

    final override fun getUseScope(): SearchScope {
        return GlobalSearchScope.allScope(project)
    }

    final override fun getPresentation(): ItemPresentation {
        return this
    }

    override fun toString(): String {
        return PsiService.toPresentableString(this)
    }

    final override fun getIcon(unused: Boolean): Icon? {
        return getIcon(0)
    }

    final override fun getPresentableText(): String? {
        return name
    }

    final override fun getLocationString(): @NlsSafe String? {
        val element = parent
        return element.containingFile?.name
    }
}
