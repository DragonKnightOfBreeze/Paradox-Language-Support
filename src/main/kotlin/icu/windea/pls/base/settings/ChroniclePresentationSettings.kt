package icu.windea.pls.base.settings

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import icu.windea.pls.model.constants.ChronicleConstants

/**
 * 插件的展示层设置。统一管理快速文档和内嵌提示的文本/图片展示阈值。
 */
@Service
@State(name = "PlsPresentationSettings", storages = [Storage(ChronicleConstants.pluginSettingsFileName)])
class ChroniclePresentationSettings : SimplePersistentStateComponent<ChroniclePresentationSettings.State>(State()) {
    companion object {
        const val DEFAULT_PRESENTABLE_TEXT_LENGTH_LIMIT = 30
        const val DEFAULT_QUICK_DOC_LOCALISATION_TEXT_FONT_SIZE = 18
        const val DEFAULT_QUICK_DOC_MAX_IMAGE_DISPLAY_SIZE = 300
        const val DEFAULT_QUICK_DOC_TEXT_ICON_HEIGHT_LIMIT = 36
        const val DEFAULT_INLAY_LOCALISATION_TEXT_LENGTH_LIMIT = 60
        const val DEFAULT_INLAY_TEXT_ICON_HEIGHT_LIMIT = 36

        @JvmStatic
        fun getInstance(): ChroniclePresentationSettings = service()
    }

    /**
     * @property presentableTextLengthLimit 展示文本的长度限制。适用于字符串字面量等。
     * @property quickDocLocalisationTextFontSize 快速文档中本地化文本的字体基准大小。
     * @property quickDocMaxImageDisplaySize 快速文档中图片的最大显示尺寸。
     * @property quickDocTextIconHeightLimit 快速文档中文本图标的高度限制。
     * @property inlayLocalisationTextLengthLimit 内嵌提示中本地化文本的长度限制。
     * @property inlayTextIconHeightLimit 内嵌提示中文本图标的高度限制。
     */
    class State : BaseState() {
        var presentableTextLengthLimit by property(DEFAULT_PRESENTABLE_TEXT_LENGTH_LIMIT)
        var quickDocLocalisationTextFontSize by property(DEFAULT_QUICK_DOC_LOCALISATION_TEXT_FONT_SIZE)
        var quickDocMaxImageDisplaySize by property(DEFAULT_QUICK_DOC_MAX_IMAGE_DISPLAY_SIZE)
        var quickDocTextIconHeightLimit by property(DEFAULT_QUICK_DOC_TEXT_ICON_HEIGHT_LIMIT)
        var inlayLocalisationTextLengthLimit by property(DEFAULT_INLAY_LOCALISATION_TEXT_LENGTH_LIMIT)
        var inlayTextIconHeightLimit by property(DEFAULT_INLAY_TEXT_ICON_HEIGHT_LIMIT)
    }
}
