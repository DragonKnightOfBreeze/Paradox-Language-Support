package icu.windea.pls.images

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import icu.windea.pls.model.constants.ChronicleConstants

internal class ImageLifecycleListener : AppLifecycleListener, DynamicPluginListener {
    override fun appFrameCreated(commandLineArgs: List<String?>) {
        ImageContext.init()
    }

    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        if (pluginDescriptor.pluginId != ChronicleConstants.pluginId) return
        ImageContext.init()
    }

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        if (pluginDescriptor.pluginId != ChronicleConstants.pluginId) return
        ImageContext.cleanUp()
    }
}
