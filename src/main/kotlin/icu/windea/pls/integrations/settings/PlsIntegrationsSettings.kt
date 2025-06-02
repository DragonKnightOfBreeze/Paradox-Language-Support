package icu.windea.pls.integrations.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.Property
import com.intellij.util.xmlb.annotations.Tag
import icu.windea.pls.*

/**
 * PLS集成设置。可以在插件的对应设置页面中进行配置。
 */
@Service(Service.Level.APP)
@State(name = "PlsIntegrationsSettings", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class PlsIntegrationsSettings : SimplePersistentStateComponent<PlsIntegrationsSettingsState>(PlsIntegrationsSettingsState())

class PlsIntegrationsSettingsState : BaseState() {
    @get:Property(surroundWithTag = false)
    var image by property(ImageState())
    //@get:Property(surroundWithTag = false)
    //var translation by property(TranslationState())
    @get:Property(surroundWithTag = false)
    var lint by property(LintState())

    /**
     * @property enableMagick 是否使用 <a href="https://www.imagemagick.org">Image Magick</a> 作为图片处理工具。
     * @see enablePaintNet 是否使用 <a href="https://www.paint.net">Paint.NET</a> 作为图片处理工具。
     */
    @Tag("image")
    class ImageState: BaseState() {
        var enableMagick by property(false)
        var enablePaintNet by property(false)
    }

    //目前没有需要配置的项
    //@Tag("translation")
    //class TranslationState: BaseState() {
    //
    //}

    /**
     * @property enableTiger 是否启用 <a href="https://github.com/amtep/tiger">Tiger</a> 作为检查工具。
     */
    @Tag("lint")
    class LintState: BaseState() {
        var enableTiger by property(false)
    }
}
