package icu.windea.pls.localisation.navigation

import icons.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

class ParadoxLocalisationFilePresentation(
	element: ParadoxLocalisationFile
): ParadoxItemPresentation<ParadoxLocalisationFile>(element)

class ParadoxLocalisationPresentation(
	element: ParadoxLocalisationProperty
): ParadoxItemPresentation<ParadoxLocalisationProperty>(element) {
	override fun getIcon(unused: Boolean): Icon {
		return PlsIcons.LocalisationIcon
	}
}