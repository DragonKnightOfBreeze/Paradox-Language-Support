package icu.windea.pls.localisation.navigation

import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.navigation.ParadoxItemPresentation
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import javax.swing.Icon

class ParadoxLocalisationPresentation(
    element: ParadoxLocalisationProperty
) : ParadoxItemPresentation<ParadoxLocalisationProperty>(element) {
    override fun getIcon(unused: Boolean): Icon {
        return PlsIcons.Nodes.LocalisationIcon
    }
}
