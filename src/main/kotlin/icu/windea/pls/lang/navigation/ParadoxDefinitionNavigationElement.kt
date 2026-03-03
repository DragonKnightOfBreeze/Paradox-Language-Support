package icu.windea.pls.lang.navigation

import icu.windea.pls.PlsIcons
import icu.windea.pls.core.navigation.NavigationElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement

class ParadoxDefinitionNavigationElement(
    parent: ParadoxDefinitionElement,
    private val definitionInfo: ParadoxDefinitionInfo
) : NavigationElement(parent, parent) {
    override fun getName() = definitionInfo.name

    override fun getPresentableText() = name

    override fun getLocationString() = super.locationString

    override fun getIcon(open: Boolean) = PlsIcons.Nodes.Definition(definitionInfo.type)
}
