package icu.windea.pls.inject

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import icu.windea.pls.model.constants.ChronicleConstants

/**
 * @see CodeInjectorContext
 */
class CodeInjectorLifecycleListener : AppLifecycleListener, DynamicPluginListener {
    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        CodeInjectorContext.init()
    }

    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        if (pluginDescriptor.pluginId != ChronicleConstants.pluginId) return
        CodeInjectorContext.init()
    }

    override fun pluginUnloaded(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        if (pluginDescriptor.pluginId != ChronicleConstants.pluginId) return
        CodeInjectorContext.cleanUp()
    }
}
