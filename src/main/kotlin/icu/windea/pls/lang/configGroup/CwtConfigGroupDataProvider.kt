package icu.windea.pls.lang.configGroup

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.util.PsiModificationTracker
import icu.windea.pls.config.configGroup.*

/**
 * 用于获取CWT规则分组中的数据。
 */
interface CwtConfigGroupDataProvider {
    fun process(configGroup: CwtConfigGroup) : Boolean
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtConfigGroupDataProvider>("icu.windea.pls.configGroupDataProvider")
    }
}

