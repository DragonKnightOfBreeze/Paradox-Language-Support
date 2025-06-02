package icu.windea.pls.extension.translation

import cn.yiiguxing.plugin.translate.trans.*

data class TranslatableStringSnippet(
    var text: String,
    val shouldTranslate: Boolean,
    val lang: Lang,
)
