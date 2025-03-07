package icu.windea.pls.lang

import com.intellij.ide.*
import com.intellij.ide.plugins.*
import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.support.*
import kotlinx.coroutines.*
import javax.imageio.spi.*
import kotlin.io.path.*

/**
 * 用于在特定生命周期执行特定的代码，例如，在IDE启动时初始化一些缓存数据。
 */
class PlsLifecycleListener : AppLifecycleListener, DynamicPluginListener {
    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        IIORegistry.getDefaultInstance().registerServiceProvider(ddsImageReaderSpi)

        //init caches for specific services
        initCaches()

        //create necessary files and directories & clean temp directories
        handlePaths()
    }

    private fun initCaches() {
        service<PlsDataProvider>().init()
        getDefaultProject().service<CwtConfigGroupService>().init()
    }

    @Suppress("UnstableApiUsage")
    private fun handlePaths() {
        val coroutineScope = getCoroutineScope()
        coroutineScope.launch {
            runCatchingCancelable { PlsConstants.Paths.data.createDirectories() }
            runCatchingCancelable { PlsConstants.Paths.images.createDirectory() }
            runCatchingCancelable { PlsConstants.Paths.imagesTemp }
            writeAction { runCatchingCancelable { PlsConstants.Paths.unknownPngFile } }
            writeAction { runCatchingCancelable { PlsConstants.Paths.texconvExeFile } }
        }
    }

    private val ddsImageReaderSpi by lazy { DdsImageReaderSpi() }

    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        if (pluginDescriptor.pluginId.idString == PlsConstants.pluginId) {
            IIORegistry.getDefaultInstance().registerServiceProvider(ddsImageReaderSpi)
        }
    }

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        if (pluginDescriptor.pluginId.idString == PlsConstants.pluginId) {
            IIORegistry.getDefaultInstance().deregisterServiceProvider(ddsImageReaderSpi)
        }
    }
}
