package icu.windea.pls.integrations.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.model.constants.*

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
     * @property enableTexconv 是否使用 <a href="https://github.com/microsoft/DirectXTex/wiki/Texconv">Texconv</a> 作为图片处理工具。
     * @property enableMagick 是否使用 <a href="https://www.imagemagick.org">Image Magick</a> 作为图片处理工具。
     * @property magickPath Image Magick 的可执行文件的路径。
     */
    @Tag("image")
    class ImageState: BaseState() {
        var enableTexconv by property(false)
        var enableMagick by property(false)
        var magickPath by string() // e.g., /path/to/magick.exe
    }

    //目前没有需要配置的项
    //@Tag("translation")
    //class TranslationState: BaseState() {
    //
    //}

    /**
     * @property enableTiger 是否启用 <a href="https://github.com/amtep/tiger">Tiger</a> 作为检查工具。
     * @property ck3TigerPath ck3-tiger 的可执行文件的路径。
     * @property irTigerPath imperator-tiger 的可执行文件的路径。
     * @property vic3TigerPath vic3-tiger 的可执行文件的路径。
     */
    @Tag("lint")
    class LintState: BaseState() {
        var enableTiger by property(false)
        var ck3TigerPath by string() //e.g., /path/to/ck3-tiger
        var irTigerPath by string() //e.g., /path/to/imperator-tiger
        var vic3TigerPath by string() //e.g., /path/to/vic3-tiger

        //TODO 2.0.0-dev 提供对conf文件的支持并在这里允许配置
    }
}
