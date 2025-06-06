package icu.windea.pls.integrations.image

import icu.windea.pls.integrations.image.providers.*
import java.io.*
import java.nio.file.*

object PlsImageManager {
    fun findTool(): PlsImageToolProvider? {
        return PlsImageToolProvider.EP_NAME.extensionList.findLast { it.supports() }
    }

    fun convertImageFormat(inputStream: InputStream, outputStream: OutputStream, sourceFormat: String, targetFormat: String): Boolean {

    }

    fun convertImageFormat(path: Path, targetPath: Path, sourceFormat: String, targetFormat: String): Boolean {

    }
}
