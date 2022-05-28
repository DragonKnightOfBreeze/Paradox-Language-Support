package icu.windea.pls.localisation.ui.floating

import com.intellij.ide.ui.customization.*
import icu.windea.pls.*

//org.intellij.plugins.markdown.ui.floating.FloatingToolbarCustomizableGroupProvider

class FloatingToolbarCustomizableGroupProvider: CustomizableActionGroupProvider() {
	override fun registerGroups(registrar: CustomizableActionGroupRegistrar) {
		registrar.addCustomizableActionGroup(
			"ParadoxLocalisation.Toolbar.Floating",
			PlsBundle.message("localisation.floatingToolbar.customizableGroupName")
		)
	}
}