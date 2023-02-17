package icu.windea.pls.core.tool.dependencies

import com.intellij.openapi.extensions.*
import javax.swing.*

interface ParadoxModDependenciesExporter {
    val icon: Icon?
    val text: String
    
    fun execute()
    
    companion object INSTANCE {
        @JvmField
        val EP_NAME = ExtensionPointName.create<ParadoxModDependenciesExporter>("icu.windea.pls.paradoxModDependenciesExporter")
    }
}