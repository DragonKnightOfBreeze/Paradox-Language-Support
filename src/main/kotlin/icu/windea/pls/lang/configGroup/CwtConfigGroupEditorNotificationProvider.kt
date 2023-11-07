package icu.windea.pls.lang.configGroup

import com.intellij.ide.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.cwt.*
import java.util.function.Function
import javax.swing.*

/**
 * 当用户打开一个可用于自定义CWT规则分组的CWT文件时，给出提示以及一些参考信息。
 */
class CwtConfigGroupEditorNotificationProvider : EditorNotificationProvider{
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        if(file.fileType != CwtFileType) return null
        
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val configGroups = mutableSetOf<CwtConfigGroup>()
        fileProviders.forEach { fileProvider ->
            configGroups += fileProvider.getConfigGroups(project, file)
        }
        if(configGroups.isEmpty()) return null
        
        return Function { fileEditor ->
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info)
                .text(PlsBundle.message("configGroup.config.file.message"))
            panel.createActionLabel(PlsBundle.message("configGroup.config.file.guidance")) {
                val url = "https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md"
                //val url = "https://github.com/cwtools/cwtools/wiki/.cwt-config-file-guidance"
                BrowserUtil.browse(url)
            }
            panel.createActionLabel(PlsBundle.message("configGroup.config.file.repositories")) {
                val url = "https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/tree/master/src/main/resources/config"
                BrowserUtil.browse(url)
            }
            panel
        }
    }
}

