package icu.windea.pls.base.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.registry.Registry
import icu.windea.pls.core.orNull

/**
 * 插件的全局内部设置。可以通过 Registry 页面进行调整。
 */
@Service
class ChronicleInternalSettings {
    /**
     * 默认的封装变量的名字（执行重构与生成操作时会用到）。
     */
    val defaultScriptedVariableName get() = Registry.stringValue("chronicle.settings.defaultScriptedVariableName").orNull() ?: "var"

    companion object {
        @JvmStatic
        fun getInstance(): ChronicleInternalSettings = service()
    }
}
