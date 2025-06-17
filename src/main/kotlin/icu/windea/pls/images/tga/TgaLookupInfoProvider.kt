package icu.windea.pls.images.tga

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.file.*

//org.intellij.images.completion.ImageLookupInfoProvider

class TgaLookupInfoProvider : FileLookupInfoProvider() {
    private val fileTypes by lazy { arrayOf(TgaFileType) }

    override fun getFileTypes(): Array<out FileType> {
        return fileTypes
    }

    override fun getLookupInfo(file: VirtualFile, project: Project?): Pair<String, String>? {
        if (project == null) return null
        val metadata = runReadAction { service<TgaMetadataIndex>().getMetadata(file, project) } ?: return null
        return Couple.of(file.name, "${metadata.width}x${metadata.height}")
    }
}

