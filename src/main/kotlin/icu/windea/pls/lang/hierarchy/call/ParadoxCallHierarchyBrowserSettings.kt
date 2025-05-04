package icu.windea.pls.lang.hierarchy.call

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.util.xmlb.*
import icu.windea.pls.lang.hierarchy.*

@Service(Service.Level.PROJECT)
@State(name = "ParadoxCallHierarchyBrowserSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class ParadoxCallHierarchyBrowserSettings : PersistentStateComponent<ParadoxCallHierarchyBrowserSettings>, ParadoxHierarchyBrowserSettings {
    override var scopeType: String = "all"

    override fun getState() = this

    override fun loadState(state: ParadoxCallHierarchyBrowserSettings) = XmlSerializerUtil.copyBean(state, this)

    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<ParadoxCallHierarchyBrowserSettings>()
    }
}
