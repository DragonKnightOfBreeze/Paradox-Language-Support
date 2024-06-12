package icu.windea.pls.config.configGroup

import com.intellij.ide.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icons.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.*
import icu.windea.pls.ep.configGroup.*
import javax.swing.*

/**
 * 为用于自定义CWT规则分组的CWT文件提供特殊的图标。
 */
class CwtConfigFileIconProvider: FileIconProvider {
    override fun getIcon(file: VirtualFile, flags: Int, project: Project?): Icon? {
        if(project == null) return null
        if(file.fileType != CwtFileType) return null
        
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val fileProvider = fileProviders.findFast { it.getContainingConfigGroup(file, project) != null }
        if(fileProvider == null) return null
        
        return PlsIcons.FileTypes.CwtConfig
    }
}
