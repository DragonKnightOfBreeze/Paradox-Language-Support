package icu.windea.pls.dds

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.file.*

//org.intellij.images.completion.ImageLookupInfoProvider

class DdsLookupInfoProvider : FileLookupInfoProvider() {
    private val fileTypes by lazy { arrayOf(DdsFileType) }

    override fun getFileTypes(): Array<out FileType> {
        return fileTypes
    }

    override fun getLookupInfo(file: VirtualFile, project: Project?): Pair<String, String>? {
        if (project == null) return null
        val info = DdsInfoIndex.getInfo(file, project) ?: return null
        return Couple.of(file.name, "${info.width}x${info.height}")
    }
}
