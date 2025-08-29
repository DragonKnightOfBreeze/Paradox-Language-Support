package icu.windea.pls.lang.findUsages

import com.intellij.find.findUsages.PersistentFindUsagesOptions
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import icu.windea.pls.PlsBundle

//com.intellij.find.findUsages.JavaFindUsagesOptions

open class ParadoxFindUsagesOptions(project: Project) : PersistentFindUsagesOptions(project) {
    init {
        isUsages = true
        isSearchForTextOccurrences = false
    }

    override fun setDefaults(project: Project) {
        setDefaults(PropertiesComponent.getInstance(project), findPrefix())
    }

    protected open fun setDefaults(properties: PropertiesComponent, prefix: String) {
        isUsages = properties.getBoolean(prefix + "isUsages", true)
        isSearchForTextOccurrences = properties.getBoolean(prefix + "isSearchForTextOccurrences", false)
    }

    override fun storeDefaults(project: Project) {
        storeDefaults(PropertiesComponent.getInstance(project), findPrefix())
    }

    protected open fun storeDefaults(properties: PropertiesComponent, prefix: String) {
        properties.setValue(prefix + "isUsages", isUsages, true)
        properties.setValue(prefix + "isSearchForTextOccurrences", isSearchForTextOccurrences, false)
    }

    private fun findPrefix(): String {
        return javaClass.simpleName + "."
    }

    override fun generateUsagesString(): String {
        return PlsBundle.message("find.usages.panel.title.usages")
    }
}
