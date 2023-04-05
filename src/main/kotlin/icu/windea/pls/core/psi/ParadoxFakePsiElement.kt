package icu.windea.pls.core.psi

import com.intellij.psi.*
import com.intellij.psi.impl.*
import com.intellij.psi.search.*
import icu.windea.pls.core.search.scope.*

abstract class ParadoxFakePsiElement(parent: PsiElement): RenameableFakePsiElement(parent), PsiNameIdentifierOwner, NavigatablePsiElement {
    override fun getResolveScope(): GlobalSearchScope {
        return ParadoxSearchScope.fromElement(this) ?: super.getResolveScope()
    }
    
    override fun getUseScope(): SearchScope {
        return ParadoxSearchScope.fromElement(this) ?: super.getUseScope()
    }
}