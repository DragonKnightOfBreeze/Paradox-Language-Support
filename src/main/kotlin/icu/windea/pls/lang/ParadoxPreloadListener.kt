package icu.windea.pls.lang

import com.intellij.ide.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.project.impl.ProjectLifecycleListener
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.util.io.*

/**
 * 用于在IDE启动时预加载一些服务。（例如，用于初始化缓存）
 */
class ParadoxPreloadListener: AppLifecycleListener {
    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        service<ParadoxPathProvider>().init()
        getDefaultProject().service<CwtConfigGroupService>().init()
    }
}