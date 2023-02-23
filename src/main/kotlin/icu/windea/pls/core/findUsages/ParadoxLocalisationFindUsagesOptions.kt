package icu.windea.pls.core.findUsages

import com.intellij.openapi.project.*

// isCrossLocales - 是否查找不同的语言区域的本地化文件中的引用，不便实现，忽略

class ParadoxLocalisationFindUsagesOptions(project: Project) : ParadoxFindUsagesOptions(project) {
    //@JvmField var isCrossLocales = true
    //
    //override fun setDefaults(properties: PropertiesComponent, prefix: String) {
    //    super.setDefaults(properties, prefix)
    //    isCrossLocales = properties.getBoolean(prefix + "isCrossLocales", true)
    //}
    //
    //override fun storeDefaults(properties: PropertiesComponent, prefix: String) {
    //    super.storeDefaults(properties, prefix)
    //    properties.setValue(prefix + "isCrossLocales", isCrossLocales, true)
    //}
    //
    //override fun generateUsagesString(): String {
    //    if(isCrossLocales) {
    //        return PlsBundle.message("find.usages.panel.title.usages.crossLocales")
    //    } else {
    //        return super.generateUsagesString()
    //    }
    //}
    //
    //override fun equals(other: Any?): Boolean {
    //    if(this === other) return true
    //    if(!super.equals(other)) return false
    //    return other is ParadoxLocalisationFindUsagesOptions &&
    //        isCrossLocales == other.isCrossLocales
    //}
    //
    //override fun hashCode(): Int {
    //    var result = super.hashCode()
    //    result = 31 * result + isCrossLocales.toInt()
    //    return result
    //}
}