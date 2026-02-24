package icu.windea.pls.core.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import java.util.*
import javax.swing.Icon

class NavigationAwarePsiElement(
    private val parent: NavigatablePsiElement,
    private val navigationElement: PsiElement? = null,
) : FakePsiElement(), NavigatablePsiElement {
    // 用于绕过以下位置的内部检查：com.intellij.ide.impl.DataValidators.isPsiElementProvided

    override fun getParent(): PsiElement {
        return parent
    }

    override fun getNavigationElement(): PsiElement {
        return navigationElement ?: this
    }

    override fun getName(): String? {
        return parent.name
    }

    override fun getPresentableText(): String? {
        return parent.presentation?.presentableText
    }

    override fun getLocationString(): String? {
        return parent.presentation?.locationString
    }

    override fun getIcon(open: Boolean): Icon? {
        return parent.presentation?.getIcon(open)
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is NavigationAwarePsiElement && parent == other.parent
    }

    override fun hashCode(): Int {
        return Objects.hash(parent)
    }
}
