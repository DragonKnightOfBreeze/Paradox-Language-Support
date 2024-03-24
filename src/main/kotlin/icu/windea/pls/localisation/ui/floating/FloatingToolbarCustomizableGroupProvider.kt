package icu.windea.pls.localisation.ui.floating

import com.intellij.ide.ui.customization.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

class FloatingToolbarCustomizableGroupProvider: CustomizableActionGroupProvider() {
    override fun registerGroups(registrar: CustomizableActionGroupRegistrar) {
        registrar.addCustomizableActionGroup(
            "Pls.ParadoxLocalisation.Toolbar.Floating",
            PlsBundle.message("localisation.floating.toolbar.customizable.group.name")
        )
    }
}