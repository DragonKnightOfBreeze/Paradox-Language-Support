package icu.windea.pls.core.listeners.vfs

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.configGroup.*

/**
 * 监听CWT规则文件的更改以更新项目特定的本地CWT规则分组。
 * 
 * @see ProjectBasedCwtConfigGroupProvider1
 */
class ProjectBasedCwtConfigFileListener : AsyncFileListener {
    object Context {
        fun getProjectBasedStatusMap(project: Project): MutableMap<String, Boolean> {
            return project.projectBasedStatusMap
        }
    }
    
    override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
        val parents = mutableSetOf<VirtualFile>()
        events.forEachFast { event ->
            when(event) {
                is VFileCreateEvent -> parents.add(event.parent)
                is VFileCopyEvent -> parents.add(event.newParent)
                is VFileMoveEvent -> parents.add(event.newParent)
                else -> event.file?.parent?.let { parents.add(it) }
            }
        }
        if(parents.isEmpty()) return null
        
        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                ProjectManager.getInstance().openProjects.forEachFast f@{ project ->
                    val rootPath = ".config/"
                    val projectRootDir = project.guessProjectDir() ?: return@f
                    val paths = mutableSetOf<String>()
                    parents.forEach { parent ->
                        val relativePath = VfsUtil.getRelativePath(parent, projectRootDir)
                        relativePath?.removePrefixOrNull(rootPath)?.substringBefore('/')?.let { paths.add(it) }
                    }
                    val projectBasedStatusMap = project.projectBasedStatusMap
                    paths.forEach { path ->
                        projectBasedStatusMap[path] = true
                    }
                }
            }
        }
    }
}

private val PlsKeys.projectBasedStatusMap by createKey("cwt.config.projectBasedStatus") { mutableMapOf<String, Boolean>() }
private val Project.projectBasedStatusMap by PlsKeys.projectBasedStatusMap