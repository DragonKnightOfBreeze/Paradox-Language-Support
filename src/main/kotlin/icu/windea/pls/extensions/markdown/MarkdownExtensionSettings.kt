package icu.windea.pls.extensions.markdown

import icu.windea.pls.base.settings.ChronicleExtensionSettings

object MarkdownExtensionSettings {
    fun getInstance() = ChronicleExtensionSettings.getInstance().state.markdown
}
