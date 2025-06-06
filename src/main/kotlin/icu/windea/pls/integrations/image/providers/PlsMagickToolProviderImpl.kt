package icu.windea.pls.integrations.image.providers

import icu.windea.pls.*
import icu.windea.pls.core.*
import java.io.*
import java.nio.file.*

class PlsMagickToolProviderImpl : PlsImageToolProvider {
    override fun supports(): Boolean {
        val settings = PlsFacade.getIntegrationsSettings().image
        return settings.enableMagick && settings.magickPath.isNotNullOrEmpty()
    }

    override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        TODO("Not yet implemented")
    }
}
