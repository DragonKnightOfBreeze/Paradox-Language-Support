package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.cwt.psi.*

/**
 * @property path (property*) path: string 相对于游戏或模组根路径的路径。
 * @property pathFile (property) path_file: string 路径下的文件名。
 * @property pathStrict (property) path_strict: boolean
 * @property startFromRoot (property) start_from_root: boolean
 * @property searchScopeType (property) search_scope_type: string 查询作用域，认为仅该作用域下的复杂枚举值是等同的。（目前支持：definition）
 * @property nameConfig (property) name: block 描述如何获取枚举名。将`enum_name`对应的key/value作为枚举名。
 */
data class CwtComplexEnumConfig(
    override val pointer: SmartPsiElementPointer<CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val name: String,
    val path: Set<String>,
    val pathFile: String?,
    val pathStrict: Boolean,
    val startFromRoot: Boolean,
    val searchScopeType: String?,
    val nameConfig: CwtPropertyConfig
) : CwtConfig<CwtProperty> {
    /**
     * [nameConfig]中作为锚点的`enum_name`对应的CWT规则。
     */
    val enumNameConfigs: List<CwtMemberConfig<*>> by lazy {
        buildList {
            nameConfig.processDescendants {
                when {
                    it is CwtPropertyConfig -> {
                        if(it.key == "enum_name" || it.stringValue == "enum_name") {
                            add(it)
                        }
                    }
                    it is CwtValueConfig -> {
                        if(it.stringValue == "enum_name") {
                            add(it)
                        }
                    }
                }
                true
            }
        }
    }
}