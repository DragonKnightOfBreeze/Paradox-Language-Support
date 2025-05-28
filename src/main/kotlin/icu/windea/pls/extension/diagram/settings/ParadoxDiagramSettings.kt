package icu.windea.pls.extension.diagram.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import kotlinx.coroutines.*

abstract class ParadoxDiagramSettings<T : ParadoxDiagramSettings.State>(
    val project: Project,
    initialState: T,
    val gameType: ParadoxGameType?
) : SimplePersistentStateComponent<T>(initialState) {
    abstract val id: String

    abstract class State : BaseState() {
        abstract var scopeType: String?

        fun updateSettings() = incrementModificationCount()
    }

    abstract val groupName: String

    abstract val groupBuilder: Panel.() -> Unit

    protected fun MutableMap<String, Boolean>.retainSettings(keys: Collection<String>) {
        val settings = keys.associateWith { this[it] ?: true }
        this.clear()
        this.putAll(settings)
    }

    protected fun <T> MutableMap<String, Boolean>.retainSettings(collection: Collection<T>, keySelector: (T) -> String) {
        val keys = collection.mapTo(mutableSetOf()) { keySelector(it) }
        val settings = keys.associateWith { this[it] ?: true }
        this.clear()
        this.putAll(settings)
    }

    protected fun Panel.checkBoxGroup(
        map: MutableMap<String, Boolean>,
        groupText: String,
        optionTextProvider: (key: String) -> String?,
        optionLabelProvider: ((key: String) -> String?)? = null
    ) {
        if (map.isEmpty()) return

        val coroutineScope = getCoroutineScope()
        lateinit var cb: Cell<ThreeStateCheckBox>
        row {
            cell(ThreeStateCheckBox(groupText))
                .applyToComponent { isThirdStateEnabled = false }
                .smaller()
                .also { cb = it }
        }
        indent {
            map.keys.forEach { key ->
                val optionText = optionTextProvider(key) ?: return@forEach
                row {
                    checkBox(optionText)
                        .bindSelected(map.toMutableProperty(key, true))
                        .threeStateCheckBox(cb)
                        .smaller()

                    if (optionLabelProvider != null) {
                        //add related label (e.g., localized name) as comment lazily
                        comment("").customize(UnscaledGaps(3, 16, 3, 0)).applyToComponent t@{
                            coroutineScope.launch {
                                val p = readAction { optionLabelProvider(key) }
                                if (p.isNotNullOrEmpty()) text = p
                            }
                        }
                    }
                }
            }
        }
    }
}
