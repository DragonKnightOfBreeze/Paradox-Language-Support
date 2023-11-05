package icu.windea.pls.lang.configGroup

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.config.configGroup.*

/**
 * 用于获取CWT规则分组中的文件。
 */
interface CwtConfigGroupFileProvider {
    fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtConfigGroupFileProvider>("icu.windea.pls.configGroupFileProvider")
    }
}