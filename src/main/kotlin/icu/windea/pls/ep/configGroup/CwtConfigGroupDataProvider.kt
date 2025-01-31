package icu.windea.pls.ep.configGroup

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.configGroup.*

/**
 * 用于获取规则分组中的数据。
 */
interface CwtConfigGroupDataProvider {
    fun process(configGroup: CwtConfigGroup): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtConfigGroupDataProvider>("icu.windea.pls.configGroupDataProvider")
    }
}

