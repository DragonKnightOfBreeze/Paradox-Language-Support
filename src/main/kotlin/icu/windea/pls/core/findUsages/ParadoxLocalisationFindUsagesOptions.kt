package icu.windea.pls.core.findUsages

import com.intellij.ide.util.*
import com.intellij.openapi.project.*
import icu.windea.pls.core.*

class ParadoxLocalisationFindUsagesOptions(project: Project) : ParadoxFindUsagesOptions(project) {
    @JvmField var isSearchForAllLocales = true
    
    init {
        isSearchForTextOccurrences = false
    }
    
    override fun setDefaults(properties: PropertiesComponent, prefix: String) {
        super.setDefaults(properties, prefix)
        isSearchForAllLocales = properties.getBoolean(prefix + "isSearchForAllLocales")
    }
    
    override fun storeDefaults(properties: PropertiesComponent, prefix: String) {
        super.storeDefaults(properties, prefix)
        properties.setValue(prefix + "isSearchForAllLocales", isSearchForAllLocales)
    }
    
    override fun addUsageTypes(to: MutableList<in String?>) {
        super.addUsageTypes(to)
        //TODO
    }
    
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(!super.equals(other)) return false
        return other is ParadoxLocalisationFindUsagesOptions &&
            isSearchForAllLocales == other.isSearchForTextOccurrences
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + isSearchForAllLocales.toInt()
        return result
    }
}