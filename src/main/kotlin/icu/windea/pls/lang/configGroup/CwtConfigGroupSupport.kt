package icu.windea.pls.lang.configGroup

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.configGroup.*

/**
 * 用于提供CWT规则分组中的数据。需要考虑导入、覆盖、刷新等情况。
 */
interface CwtConfigGroupSupport {
    fun process(configGroup: CwtConfigGroup) : Boolean
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtConfigGroupSupport>("icu.windea.pls.configGroupSupport")
    }
}