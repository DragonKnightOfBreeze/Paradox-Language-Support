package icu.windea.pls.core.settings

import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import icu.windea.pls.*
import icu.windea.pls.core.*
import io.ktor.utils.io.*

class ParadoxSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings"), "settings.language.pls"), SearchableConfigurable {
	override fun getId() = helpTopic!!
	
	override fun createPanel(): DialogPanel {
		//TODO 完善通用设置
		val settings = getSettings()
		return panel {
			group(PlsBundle.message("settings.generic")) {
				row {
					label(PlsBundle.message("settings.generic.defaultGameType")).applyToComponent {
						toolTipText = PlsBundle.message("settings.generic.defaultGameType.tooltip")
					}
					comboBox(ParadoxGameType.values.toList()).bindItem(settings::defaultGameType.toNullableProperty())
				}
				row{
					label(PlsBundle.message("settings.generic.maxCompleteSize")).applyToComponent { 
						toolTipText = PlsBundle.message("settings.generic.maxCompleteSize.tooltip")
					}
					this.intTextField(0..1000).bindIntText(settings::maxCompleteSize)
				}
				row {
					checkBox(PlsBundle.message("settings.generic.preferOverridden"))
						.bindSelected(settings::preferOverridden)
						.applyToComponent { toolTipText = PlsBundle.message("settings.generic.preferOverridden.tooltip") }
				}
			}
			group(PlsBundle.message("settings.script")) {
				buttonsGroup(PlsBundle.message("settings.script.doc")) {
					row {
						checkBox(PlsBundle.message("settings.script.doc.renderLineComment"))
							.bindSelected(settings::scriptRenderLineComment)
							.applyToComponent { toolTipText = PlsBundle.message("settings.script.doc.renderLineComment.tooltip") }
					}
					row {
						checkBox(PlsBundle.message("settings.script.doc.renderRelatedLocalisation"))
							.bindSelected(settings::scriptRenderRelatedLocalisation)
							.applyToComponent { toolTipText = PlsBundle.message("settings.script.doc.renderRelatedLocalisation.tooltip") }
					}
					row {
						checkBox(PlsBundle.message("settings.script.doc.renderRelatedPictures"))
							.bindSelected(settings::scriptRenderRelatedPictures)
							.applyToComponent { toolTipText = PlsBundle.message("settings.script.doc.renderRelatedPictures.tooltip") }
					}
				}
			}
			group(PlsBundle.message("settings.localisation")) {
				buttonsGroup(PlsBundle.message("settings.localisation.doc")) {
					row {
						checkBox(PlsBundle.message("settings.localisation.doc.renderLineComment"))
							.bindSelected(settings::localisationRenderLineComment)
							.applyToComponent { toolTipText = PlsBundle.message("settings.localisation.doc.renderLineComment.tooltip") }
					}
					row {
						checkBox(PlsBundle.message("settings.localisation.doc.renderLocalisation"))
							.bindSelected(settings::localisationRenderLocalisation)
							.applyToComponent { toolTipText = PlsBundle.message("settings.localisation.doc.renderLocalisation.tooltip") }
					}
				}
			}
		}
	}
}
