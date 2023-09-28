package icu.windea.pls.core.psi

import com.intellij.openapi.util.*
import com.intellij.platform.backend.navigation.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import com.intellij.psi.search.*
import icu.windea.pls.core.search.scope.*

@Suppress("UnstableApiUsage")
abstract class ParadoxFakePsiElement(parent: PsiElement): RenameableFakePsiElement(parent), PsiNameIdentifierOwner, NavigatablePsiElement {
    override fun getNameIdentifier(): PsiElement? {
        return this
    }
    
    override fun getTextRange(): TextRange? {
        return null //return null to avoid incorrect highlight at file start
    }
    
    override fun navigationRequest(): NavigationRequest? {
        return null //click to show usages
    }
    
    override fun navigate(requestFocus: Boolean) {
        //click to show usages
    }
    
    override fun canNavigate(): Boolean {
        return false //click to show usages
    }
    
    override fun getResolveScope(): GlobalSearchScope {
        return ParadoxSearchScope.fromElement(this) ?: super.getResolveScope()
    }
    
    override fun getUseScope(): SearchScope {
        return ParadoxSearchScope.fromElement(this) ?: super.getUseScope()
    }
}