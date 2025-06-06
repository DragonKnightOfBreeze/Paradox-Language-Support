package icu.windea.pls.integrations.image.providers

import icu.windea.pls.core.OS
import java.io.*
import java.nio.file.*

class PlsTexconvToolProvider : PlsImageToolProvider {
    override fun supports(): Boolean {
        return OS.isWindows
    }

    override fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {
        TODO("Not yet implemented")
    }
}
