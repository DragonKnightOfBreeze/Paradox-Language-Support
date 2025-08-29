package icu.windea.pls.images.tga

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Couple
import com.intellij.openapi.util.Pair
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.file.FileLookupInfoProvider

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

