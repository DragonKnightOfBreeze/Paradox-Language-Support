package icu.windea.pls.localisation.navigation

import icu.windea.pls.*
import icu.windea.pls.lang.navigation.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

class ParadoxLocalisationFilePresentation(
    element: ParadoxLocalisationFile
) : ParadoxItemPresentation<ParadoxLocalisationFile>(element)

class ParadoxLocalisationPresentation(
    element: ParadoxLocalisationProperty
) : ParadoxItemPresentation<ParadoxLocalisationProperty>(element) {
    override fun getIcon(unused: Boolean): Icon {
        return PlsIcons.LocalisationNodes.Icon
    }
}
