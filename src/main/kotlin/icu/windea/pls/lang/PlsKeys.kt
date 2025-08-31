package icu.windea.pls.lang

import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.configGroup.CwtConfigGroupLibrary
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.model.ImageFrameInfo
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.paths.ParadoxExpressionPath

object PlsKeys : KeyRegistry() {
    val library by createKey<ParadoxLibrary>(this)
    val configGroupLibrary by createKey<CwtConfigGroupLibrary>(this)

    val rootInfo by createKey<Any>(this)
    val fileInfo by createKey<Any>(this)
    val localeConfig by createKey<Any>(this)

    //用于为临时文件注入根目录信息
    val injectedRootInfo by createKey<ParadoxRootInfo>(this)
    //用于为临时文件注入文件信息
    val injectedFileInfo by createKey<ParadoxFileInfo>(this)
    //用于为临时文件注入语言区域
    val injectedGameType by createKey<ParadoxGameType>(this)
    //用于为临时文件注入语言区域
    val injectedLocaleConfig by createKey<CwtLocaleConfig>(this)
    //用于为临时脚本文件注入表达式路径前缀
    val injectedElementPathPrefix by createKey<ParadoxExpressionPath>(this)

    //用于将CWT规则临时写入到CWT元素的userData中（例如，解析引用为枚举值后，将会是对应的CwtEnumConfig）
    val bindingConfig by createKey<CwtConfig<*>>(this)
    //用于标记快速文档使用的本地化语言区域
    val documentationLocale by createKey<String>(this)
    //用于标记图片的帧数信息以便后续进行切分
    val imageFrameInfo by createKey<ImageFrameInfo>(this)
}
