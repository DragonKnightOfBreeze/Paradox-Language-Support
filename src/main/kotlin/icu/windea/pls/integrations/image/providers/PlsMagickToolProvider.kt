package icu.windea.pls.integrations.image.providers

import icu.windea.pls.*
import icu.windea.pls.core.*
import java.nio.file.*

/**
 * 参见：[Image Magick](https://www.imagemagick.org)
 */
class PlsMagickToolProvider : PlsCommandBasedImageToolProvider() {
    override fun isEnabled(): Boolean {
        val settings = PlsFacade.getIntegrationsSettings().image
        return settings.enableMagick && settings.magickPath.isNotNullOrEmpty()
    }

    override fun validate(): Boolean {
        TODO("Not yet implemented")
    }

    override fun convertImageFormat(path: Path, targetDirectoryPath: Path?, targetFileName: String?, sourceFormat: String, targetFormat: String): Path {
        TODO("Not yet implemented")
    }
}
