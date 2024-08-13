package icu.windea.pls.ep.configGroup

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.config.configGroup.*

/**
 * 用于获取规则分组中的文件。
 */
interface CwtConfigGroupFileProvider {
    fun getRootDirectory(project: Project): VirtualFile?
    
    fun processFiles(configGroup: CwtConfigGroup, consumer: (String, VirtualFile) -> Boolean): Boolean
    
    /**
     * @return 规则文件[file]所在的规则分组。
     */
    fun getContainingConfigGroup(file: VirtualFile, project: Project): CwtConfigGroup?
    
    fun getHintMessage(): String
    
    fun getNotificationMessage(): String
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtConfigGroupFileProvider>("icu.windea.pls.configGroupFileProvider")
    }
}
