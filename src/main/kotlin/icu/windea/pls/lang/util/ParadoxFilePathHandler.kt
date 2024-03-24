package icu.windea.pls.lang.util

import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

object ParadoxFilePathHandler {
    val fileExtensionsKey = createKey<Set<String>>("paradox.filePath.fileExtensions")
    
    fun getFileExtensionOptionValues(config: CwtMemberConfig<*>) : Set<String> {
        return config.getOrPutUserData(fileExtensionsKey) {
            config.findOption("file_extensions")?.getOptionValueOrValues().orEmpty()
        }
    }
}