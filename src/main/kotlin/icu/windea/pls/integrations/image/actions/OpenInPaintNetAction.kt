package icu.windea.pls.integrations.image.actions

import icu.windea.pls.integrations.image.providers.PlsImageToolProvider
import icu.windea.pls.integrations.image.providers.PlsPaintNetToolProvider

class OpenInPaintNetAction : OpenInImageToolAction() {
    override fun getTool(): PlsImageToolProvider? {
        return PlsImageToolProvider.INSTANCE.EP_NAME.findExtension(PlsPaintNetToolProvider::class.java)
    }
}
