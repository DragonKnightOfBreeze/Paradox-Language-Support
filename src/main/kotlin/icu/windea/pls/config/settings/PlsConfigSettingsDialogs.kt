package icu.windea.pls.config.settings

import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.util.CwtConfigRepositoryManager
import icu.windea.pls.core.util.Entry
import icu.windea.pls.model.ParadoxGameType

class ConfigRepositoryUrlsDialog(val list: MutableList<Entry<String, String>>) : DialogWrapper(null) {
    val resultList = list.mapTo(mutableListOf()) { it.copy() }

    val graph = PropertyGraph()
    val properties = list.associateBy({ it.key }, { graph.property(it.value) })

    init {
        title = PlsBundle.message("settings.config.configRepositoryUrls.dialog.title")
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            properties.forEach f@{ (gameTypeId, configRepositoryUrlProperty) ->
                val gameType = ParadoxGameType.get(gameTypeId) ?: return@f
                row {
                    // configRepositoryUrl
                    label(gameType.title + ":").widthGroup("left")
                    textField()
                        .bindText(configRepositoryUrlProperty)
                        .columns(COLUMNS_LARGE)
                        .align(AlignX.FILL)
                        .resizableColumn()
                        .applyToComponent { setEmptyState(PlsBundle.message("not.configured")) } // If not configured, do not use default repo urls
                        .validationOnInput { CwtConfigRepositoryManager.validateUrl(this, gameType, configRepositoryUrlProperty.get()) }

                    button(PlsBundle.message("reset")) { configRepositoryUrlProperty.set(CwtConfigRepositoryManager.getDefaultUrl(gameType)) }
                        .align(AlignX.RIGHT)
                }
            }
        }
    }

    override fun doOKAction() {
        // 即使配置并未更改，也要使用 git ls-remote 检查
        val newValues = properties.map { it.value.get() }
        if (!CwtConfigRepositoryManager.validateUrlsByGit(newValues)) return // invalid

        resultList.clear()
        properties.mapTo(resultList) { (k, p) -> Entry(k, p.get()) }
        super.doOKAction()
    }
}
