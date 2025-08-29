package icu.windea.pls.localisation.ui.floating

import com.intellij.ide.ui.customization.CustomizableActionGroupProvider
import icu.windea.pls.PlsBundle

//org.intellij.plugins.markdown.ui.floating.FloatingToolbarCustomizableGroupProvider

class ParadoxLocalisationFloatingToolbarCustomizableGroupProvider : CustomizableActionGroupProvider() {
    override fun registerGroups(registrar: CustomizableActionGroupRegistrar) {
        registrar.addCustomizableActionGroup(
            "Pls.ParadoxLocalisation.Toolbar.Floating",
            PlsBundle.message("localisation.floating.toolbar.customizable.group.name")
        )
    }
}
