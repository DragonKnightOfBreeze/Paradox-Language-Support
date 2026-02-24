package icu.windea.pls.lang.navigation

import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.core.navigation.NavigationElement
import icu.windea.pls.cwt.psi.CwtStringExpressionElement

class CwtConfigNavigationElement(
    parent: CwtStringExpressionElement,
    private val name: String,
    private val configType: CwtConfigType
) : NavigationElement(parent, parent) {
    override fun getName() = name

    override fun getPresentableText() = name

    override fun getLocationString() = super.locationString

    override fun getIcon(open: Boolean) = configType.icon
}

