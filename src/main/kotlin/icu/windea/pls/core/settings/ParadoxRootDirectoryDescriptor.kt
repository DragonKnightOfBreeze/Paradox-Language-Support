package icu.windea.pls.core.settings

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.vfs.*
import icons.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import javax.swing.*

/**
 * 打开文件选择对话框选择游戏目录和模组目录时，为模组文件夹显示特定的图标和备注（模组的名称和版本信息）
 * @see icu.windea.pls.core.ParadoxProjectViewDecorator
 */
class ParadoxRootDirectoryDescriptor : FileChooserDescriptor(false, true, false, false, false, false) {
    //这里需要先解析rootInfo
    
    override fun getIcon(file: VirtualFile): Icon {
        val rootInfo = ParadoxCoreHandler.resolveRootInfo(file)
        if(rootInfo != null && rootInfo.rootFile == file) {
            val icon = when(rootInfo) {
                is ParadoxGameRootInfo -> PlsIcons.GameDirectory
                is ParadoxModRootInfo -> PlsIcons.ModDirectory
            }
            return icon
        }
        return super.getIcon(file)
    }
    
    override fun getComment(file: VirtualFile): String? {
        //NOTE 这里显示的注释并非是灰色的，也不能使用HTML - IDEA低层代码有缺陷
        val rootInfo = ParadoxCoreHandler.resolveRootInfo(file)
        if(rootInfo != null && rootInfo.rootFile == file) {
            val comment = when(rootInfo) {
                is ParadoxGameRootInfo -> null
                is ParadoxModRootInfo -> " (" + rootInfo.descriptorInfo.qualifiedName + ")"
            }
            return comment
        }
        return super.getComment(file)
    }
}