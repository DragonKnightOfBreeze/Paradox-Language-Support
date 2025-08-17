package icu.windea.pls.lang

import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*

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
