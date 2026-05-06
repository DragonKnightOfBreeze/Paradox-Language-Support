package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 定值规则。
 *
 * 用于描述脚本文件中的定值命名空间和定值变量，提供快速文档文本和规则上下文。
 * 它们位于 `common/defines` 目录中的扩展名为 `.txt` 的脚本文件中。
 *
 * @property namespace 命名空间。
 *
 * @see CwtDefineNamespaceConfig
 * @see CwtDefineVariableConfig
 */
interface CwtDefineConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val namespace: String
}
