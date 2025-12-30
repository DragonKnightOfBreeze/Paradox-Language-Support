package icu.windea.pls.lang.util.renderers

import icu.windea.pls.lang.settings.PlsSettings

abstract class ParadoxLocalisationRenderer: ParadoxRenderer {
    protected val renderColorfulText: Boolean
        get() = PlsSettings.getInstance().state.others.renderLocalisationColorfulText
}
