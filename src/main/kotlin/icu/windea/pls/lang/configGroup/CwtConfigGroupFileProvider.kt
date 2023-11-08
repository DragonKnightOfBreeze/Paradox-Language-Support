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
    
    /**
     * @param file 用于自定义CWT规则分组的CWT文件。
     * @return 相关的CWT规则分组。
     */
    fun getConfigGroups(project: Project, file: VirtualFile): Set<CwtConfigGroup>
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtConfigGroupFileProvider>("icu.windea.pls.configGroupFileProvider")
    }
}

fun CwtConfigGroupFileProvider.isBuiltIn() = this is BuiltInCwtConfigGroupFileProvider