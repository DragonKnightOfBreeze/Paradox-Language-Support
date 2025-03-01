package icu.windea.pls.lang

import com.intellij.ide.*
import com.intellij.ide.plugins.*
import com.intellij.openapi.components.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.support.*
import kotlinx.coroutines.*
import org.apache.commons.io.file.*
import javax.imageio.spi.*
import kotlin.io.path.*

/**
 * 用于在特定生命周期执行特定的代码，例如，在IDE启动时初始化某些缓存。
 */
class ParadoxCoreLifecycleListener : AppLifecycleListener, DynamicPluginListener {
    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        IIORegistry.getDefaultInstance().registerServiceProvider(ddsImageReaderSpi)

        //init caches for specific services
        service<ParadoxDataProvider>().init()
        getDefaultProject().service<CwtConfigGroupService>().init()

        val coroutineScope = getCoroutineScope()

        //clean temp directories on application startup
        run {
            val filesToClean = PlsConstants.Paths.imagesTemp.listDirectoryEntries()
            coroutineScope.launch {
                filesToClean.forEach { PathUtils.delete(it) }
            }
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
