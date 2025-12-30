package icu.windea.pls.lang

import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.StatefulValue
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.paths.ParadoxElementPath

object PlsKeys : KeyRegistry() {
    /** 用于在根目录级别保存根信息（[ParadoxRootInfo]）。 */
    val cachedRootInfo by createKey<StatefulValue<ParadoxRootInfo>>(this)
    /** 用于在文件级别保存文件信息（[ParadoxFileInfo]）。 */
    val cachedFileInfo by createKey<StatefulValue<ParadoxFileInfo>>(this)
    /** 用于在文件级别保存语言环境规则（[CwtLocaleConfig]）。 */
    val cachedLocaleConfig by createKey<StatefulValue<CwtLocaleConfig>>(this)

    /** 用于为临时文件注入根信息（[ParadoxRootInfo]）。 */
    val injectedRootInfo by createKey<ParadoxRootInfo>(this)
    /** 用于为临时文件注入文件信息（[ParadoxFileInfo]）。 */
    val injectedFileInfo by createKey<ParadoxFileInfo>(this)
    /** 用于为临时文件注入游戏类型（[ParadoxGameType]）。 */
    val injectedGameType by createKey<ParadoxGameType>(this)
    /** 用于为临时文件注入语言环境规则（[CwtLocaleConfig]）。 */
    val injectedLocaleConfig by createKey<CwtLocaleConfig>(this)
    /** 用于为临时脚本文件注入成员路径前缀 */
    val injectedElementPathPrefix by createKey<ParadoxElementPath>(this)

    /** 用于在解析引用时，将规则临时写入到对应的PSI的用户数据中。 */
    val bindingConfig by createKey<CwtConfig<*>>(this)
    /** 用于标记快速文档使用的本地化语言环境。 */
    val documentationLocale by createKey<String>(this)
    /** 用于标记图片的帧数信息以便后续进行切分。 */
    val imageFrameInfo by createKey<ImageFrameInfo>(this)
}
