package icu.windea.pls.integrations.image.actions

import icu.windea.pls.integrations.image.providers.*

class OpenInMagickAction : OpenInImageToolAction() {
    override fun getTool(): PlsImageToolProvider? {
        return PlsImageToolProvider.EP_NAME.findExtension(PlsMagickToolProvider::class.java)
    }
}
