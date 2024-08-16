package icu.windea.pls.lang.util

import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*

object ParadoxFilePathHandler {
    object Keys: KeyRegistry() {
        val fileExtensions by createKey<Set<String>>(this)
    }
    
    fun getFileExtensionOptionValues(config: CwtMemberConfig<*>) : Set<String> {
        return config.getOrPutUserData(Keys.fileExtensions) {
            config.findOption("file_extensions")?.getOptionValueOrValues().orEmpty()
        }
    }
}
