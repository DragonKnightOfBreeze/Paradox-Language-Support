package icu.windea.pls.lang.ui.floating

import com.intellij.ide.ui.customization.CustomizableActionGroupProvider
import icu.windea.pls.PlsBundle

// org.intellij.plugins.markdown.ui.floating.FloatingToolbarCustomizableGroupProvider

class ParadoxLocalisationFloatingToolbarCustomizableGroupProvider : com.intellij.ide.ui.customization.CustomizableActionGroupProvider() {
    override fun registerGroups(registrar: CustomizableActionGroupRegistrar) {
        registrar.addCustomizableActionGroup(
            "Pls.ParadoxLocalisation.Toolbar.Floating",
            PlsBundle.message("localisation.floating.toolbar.customizable.group.name")
        )
    }
}
