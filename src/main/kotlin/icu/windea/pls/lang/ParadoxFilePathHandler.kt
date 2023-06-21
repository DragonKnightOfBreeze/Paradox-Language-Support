package icu.windea.pls.lang

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.cwt.config.*

object ParadoxFilePathHandler {
    val fileExtensionsKey = Key.create<Set<String>>("paradox.filePath.fileExtensions")
    
    fun getFileExtensionOptionValues(config: CwtMemberConfig<*>) : Set<String> {
        return config.getOrPutUserData(fileExtensionsKey) {
            config.findOption("file_extensions")?.getOptionValueOrValues().orEmpty()
        }
    }
}