package icu.windea.pls.ep.configGroup

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.config.configGroup.*

/**
 * 用于获取CWT规则分组中的文件。
 */
interface CwtConfigGroupFileProvider {
    fun isBuiltIn(): Boolean = false
    
    fun getRootDirectory(project: Project): VirtualFile?
    
    fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean
    
    /**
     * @return 最相关的CWT规则分组。
     */
    fun getContainingConfigGroup(file: VirtualFile, project: Project): CwtConfigGroup?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtConfigGroupFileProvider>("icu.windea.pls.configGroupFileProvider")
    }
}