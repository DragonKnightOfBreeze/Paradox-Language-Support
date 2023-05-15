package icu.windea.pls.localisation.ui.floating

import com.intellij.ide.ui.customization.*
import icu.windea.pls.*

class FloatingToolbarCustomizableGroupProvider: CustomizableActionGroupProvider() {
    override fun registerGroups(registrar: CustomizableActionGroupRegistrar) {
        registrar.addCustomizableActionGroup(
            "Pls.ParadoxLocalisation.Toolbar.Floating",
            PlsBundle.message("localisation.floating.toolbar.customizable.group.name")
        )
    }
}