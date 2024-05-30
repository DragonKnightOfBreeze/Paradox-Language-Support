package icu.windea.pls.config.configGroup

import com.intellij.ide.*
import com.intellij.openapi.application.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.cwt.*
import icu.windea.pls.ep.configGroup.*
import java.util.function.Function
import javax.swing.*

/**
 * 当用户打开一个可用于自定义CWT规则分组的CWT文件时，给出提示以及一些参考信息。
 */
class CwtConfigGroupEditorNotificationProvider : EditorNotificationProvider, DumbAware {
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        if(file.fileType != CwtFileType) return null
        
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val fileProvider = fileProviders.find { it.getContainingConfigGroup(file, project) != null }
        if(fileProvider == null) return null
        
        return Function { fileEditor ->
            val builtIn = fileProvider.isBuiltIn()
            val message = when {
                builtIn -> PlsBundle.message("configGroup.config.file.message.1")
                else -> PlsBundle.message("configGroup.config.file.message.2")
            }
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info).text(message)
            run {
                if(!builtIn) return@run
                val rootDirectory = fileProvider.getRootDirectory(project) ?: return@run
                val relPath = VfsUtil.getRelativePath(file, rootDirectory) ?: return@run
                val projectLocalRootDirectory = fileProviders.find { it is ProjectCwtConfigGroupFileProvider }
                    ?.getRootDirectory(project) ?: return@run
                val destFile = VfsUtil.findRelativeFile(relPath, projectLocalRootDirectory)
                panel.createActionLabel(PlsBundle.message("configGroup.config.file.customize")) action@{
                    if(destFile != null && destFile.exists()) {
                        destFile.toPsiFile(project)?.navigate(true)
                    } else {
                        runWriteAction {
                            val newFile = projectLocalRootDirectory.findOrCreateFile(relPath)
                            newFile.writeText(file.readText())
                            newFile.toPsiFile(project)?.navigate(true)
                        }
                    }
                }
                
            }
            panel.createActionLabel(PlsBundle.message("configGroup.config.file.guidance")) {
                val url = "https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md"
                //val url = "https://github.com/cwtools/cwtools/wiki/.cwt-config-file-guidance"
                BrowserUtil.browse(url)
            }
            panel.createActionLabel(PlsBundle.message("configGroup.config.file.repositories")) {
                val url = "https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/cwt/README.md#repositories"
                BrowserUtil.browse(url)
            }
            panel
        }
    }
}

