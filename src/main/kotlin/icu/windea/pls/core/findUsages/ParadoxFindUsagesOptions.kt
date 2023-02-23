package icu.windea.pls.core.findUsages

import com.ibm.icu.text.*
import com.intellij.*
import com.intellij.analysis.*
import com.intellij.find.findUsages.*
import com.intellij.ide.util.*
import com.intellij.openapi.project.*
import org.jetbrains.annotations.*

//com.intellij.find.findUsages.JavaFindUsagesOptions

open class ParadoxFindUsagesOptions(project: Project) : PersistentFindUsagesOptions(project) {
    init {
        isUsages = true
    }
    
    override fun setDefaults(project: Project) {
        setDefaults(PropertiesComponent.getInstance(project), findPrefix())
    }
    
    protected open fun setDefaults(properties: PropertiesComponent, prefix: String) {
        isSearchForTextOccurrences = properties.getBoolean(prefix + "isSearchForTextOccurrences", true)
        isUsages = properties.getBoolean(prefix + "isUsages", true)
    }
    
    override fun storeDefaults(project: Project) {
        storeDefaults(PropertiesComponent.getInstance(project), findPrefix())
    }
    
    protected open fun storeDefaults(properties: PropertiesComponent, prefix: String) {
        properties.setValue(prefix + "isUsages", isUsages, true)
        properties.setValue(prefix + "isSearchForTextOccurrences", isSearchForTextOccurrences, true)
    }
    
    private fun findPrefix(): String {
        return javaClass.simpleName + "."
    }
    
    protected open fun addUsageTypes(to: MutableList<in String?>) {
        if(isUsages) {
            to.add(AnalysisBundle.message("find.usages.panel.title.usages"))
        }
    }
    
    override fun generateUsagesString(): String {
        val strings: MutableList<String?> = ArrayList()
        addUsageTypes(strings)
        return if(strings.isEmpty()) {
            AnalysisBundle.message("find.usages.panel.title.usages")
        } else formatOrList(strings)
    }
    
    private fun formatOrList(list: Collection<*>): @Nls String {
        return ListFormatter.getInstance(DynamicBundle.getLocale(), ListFormatter.Type.OR, ListFormatter.Width.WIDE).format(list)
    }
}