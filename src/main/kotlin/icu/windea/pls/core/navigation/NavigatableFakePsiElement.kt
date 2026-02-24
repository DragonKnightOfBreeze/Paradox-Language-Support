package icu.windea.pls.core.navigation

import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import java.util.*
import javax.swing.Icon

class NavigatableFakePsiElement(
    private val parent: NavigatablePsiElement
) : FakePsiElement(), NavigatablePsiElement {
    // 用于绕过以下位置的内部检查：com.intellij.ide.impl.DataValidators.isPsiElementProvided

    override fun getParent(): PsiElement {
        return parent
    }

    override fun getNavigationElement(): PsiElement {
        return parent
    }

    override fun getName(): String? {
        return parent.name
    }

    override fun getPresentableText(): String? {
        return parent.presentation?.presentableText
    }

    override fun getLocationString(): @NlsSafe String? {
        return parent.presentation?.locationString
    }

    override fun getIcon(open: Boolean): Icon? {
        return parent.presentation?.getIcon(open)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is NavigatableFakePsiElement && parent == other.parent
    }

    override fun hashCode(): Int {
        return Objects.hash(parent)
    }
}
