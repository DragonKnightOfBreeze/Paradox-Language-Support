package icu.windea.pls.lang

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import kotlin.collections.mapNotNullTo

object ParadoxFilePathHandler {
    val fileExtensionsKey = Key.create<Set<String>>("paradox.filePath.fileExtensions")
    
    fun getFileExtensionOptionValues(config: CwtMemberConfig<*>) : Set<String> {
        return config.getOrPutUserData(fileExtensionsKey) {
            val option = config.options?.find { it.key == "file_extensions" }
            option?.optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue }
                ?: option?.stringValue?.toSingletonSet()
                ?: emptySet()
        }
    }
}