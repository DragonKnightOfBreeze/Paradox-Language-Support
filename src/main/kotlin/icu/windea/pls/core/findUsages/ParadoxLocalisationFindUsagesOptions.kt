package icu.windea.pls.core.findUsages

import com.intellij.ide.util.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.*

class ParadoxLocalisationFindUsagesOptions(project: Project) : ParadoxFindUsagesOptions(project) {
    @JvmField var isCrossLocales = true
    
    init {
        isSearchForTextOccurrences = false
    }
    
    override fun setDefaults(properties: PropertiesComponent, prefix: String) {
        super.setDefaults(properties, prefix)
        isCrossLocales = properties.getBoolean(prefix + "isCrossLocales")
    }
    
    override fun storeDefaults(properties: PropertiesComponent, prefix: String) {
        super.storeDefaults(properties, prefix)
        properties.setValue(prefix + "isCrossLocales", isCrossLocales)
    }
    
    override fun generateUsagesString(): String {
        if(isCrossLocales) {
            return PlsBundle.message("find.usages.panel.title.usages.crossLocales")
        } else {
            return super.generateUsagesString()
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(!super.equals(other)) return false
        return other is ParadoxLocalisationFindUsagesOptions &&
            isCrossLocales == other.isCrossLocales
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + isCrossLocales.toInt()
        return result
    }
}