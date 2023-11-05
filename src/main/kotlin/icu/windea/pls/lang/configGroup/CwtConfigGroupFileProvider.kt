package icu.windea.pls.lang.configGroup

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.config.configGroup.*

/**
 * 用于获取CWT规则分组中的文件。
 */
interface CwtConfigGroupFileProvider {
    fun getRootDirectories(project: Project): Set<VirtualFile>
    
    fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean
    
    fun onFileChange(project: Project, file: VirtualFile): Boolean
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtConfigGroupFileProvider>("icu.windea.pls.configGroupFileProvider")
    }
}