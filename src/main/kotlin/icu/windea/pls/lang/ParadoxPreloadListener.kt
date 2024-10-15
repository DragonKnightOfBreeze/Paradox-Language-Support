package icu.windea.pls.lang

import com.intellij.ide.*
import com.intellij.openapi.components.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*

/**
 * 用于在IDE启动时预加载一些服务。（例如，用于初始化缓存）
 */
class ParadoxPreloadListener : AppLifecycleListener {
    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        service<ParadoxDataProvider>().init()
        getDefaultProject().service<CwtConfigGroupService>().init()
    }
}
