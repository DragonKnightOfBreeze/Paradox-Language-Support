package icu.windea.pls.lang.navigation

import com.intellij.navigation.*
import icu.windea.pls.lang.psi.*
import javax.swing.*

class ParadoxParameterElementPresentation(
    private val element: ParadoxParameterElement
) : ItemPresentation {
    override fun getIcon(unused: Boolean): Icon {
        return element.icon
    }

    override fun getPresentableText(): String {
        return element.name
    }
}

class ParadoxLocalisationParameterElementPresentation(
    private val element: ParadoxLocalisationParameterElement
) : ItemPresentation {
    override fun getIcon(unused: Boolean): Icon {
        return element.icon
    }

    override fun getPresentableText(): String {
        return element.name
    }
}

class ParadoxDynamicValueElementPresentation(
    private val element: ParadoxDynamicValueElement
) : ItemPresentation {
    override fun getIcon(unused: Boolean): Icon {
        return element.icon
    }

    override fun getPresentableText(): String {
        return element.name
    }
}

class ParadoxComplexEnumValueElementPresentation(
    private val element: ParadoxComplexEnumValueElement
) : ItemPresentation {
    override fun getIcon(unused: Boolean): Icon {
        return element.icon
    }

    override fun getPresentableText(): String {
        return element.name
    }
}

class ParadoxModifierElementPresentation(
    private val element: ParadoxModifierElement
) : ItemPresentation {
    override fun getIcon(unused: Boolean): Icon {
        return element.icon
    }

    override fun getPresentableText(): String {
        return element.name
    }
}

class ParadoxTemplateExpressionElementPresentation(
    private val element: ParadoxTemplateExpressionElement
) : ItemPresentation {
    override fun getIcon(unused: Boolean): Icon {
        return element.icon
    }

    override fun getPresentableText(): String {
        return element.name
    }
}
