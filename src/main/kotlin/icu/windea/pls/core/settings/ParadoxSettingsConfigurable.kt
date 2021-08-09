package icu.windea.pls.core.settings

import com.intellij.application.options.editor.*
import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import javax.swing.*

class ParadoxSettingsConfigurable: BoundConfigurable(message("settings"),"settings.language.pls"),SearchableConfigurable {
	companion object {
		private val _genericTitle = message("settings.generic")
		private val _defaultGameType = message("settings.generic.defaultGameType")
		private val _defaultGameTypeTooltip = message("settings.generic.defaultGameType.tooltip")
		private val _preferOverriddenName = message("settings.generic.preferOverridden")
		private val _preferOverriddenTooltip = message("settings.generic.preferOverridden.tooltip")
		private val _performanceTitle = message("settings.performance")
		private val _maxCompleteSize = message("settings.performance.maxCompleteSize")
		private val _maxCompleteSizeTooltip = message("settings.performance.maxCompleteSize.tooltip")
		private val _renderLineCommentText = message("settings.performance.renderLineCommentText")
		private val _renderLineCommentTextTooltip = message("settings.performance.renderLineCommentText.tooltip")
		private val _renderDefinitionText = message("settings.performance.renderDefinitionText")
		private val _renderDefinitionTextTooltip = message("settings.performance.renderDefinitionText.tooltip")
		private val _renderLocalisationText = message("settings.performance.renderLocalisationText")
		private val _renderLocalisationTextTooltip = message("settings.performance.renderLocalisationText.tooltip")
	}
	
	private val settings = getSettings()
	
	override fun getId() = helpTopic!!
	
	override fun createPanel(): DialogPanel {
		return panel {
			titledRow(_genericTitle) {
				row {
					cell {
						label(_defaultGameType).applyToComponent { toolTipText = _defaultGameTypeTooltip }
						comboBox(DefaultComboBoxModel(ParadoxGameType.values), settings::defaultGameType)
					}
				}
				row {
					checkBox(CheckboxDescriptor(_preferOverriddenName, settings::preferOverridden))
						.applyToComponent { toolTipText = _preferOverriddenTooltip }
				}
			}
			titledRow(_performanceTitle) {
				row {
					cell {
						label(_maxCompleteSize).applyToComponent { toolTipText = _maxCompleteSizeTooltip }
						intTextField(settings::maxCompleteSize, range = 1..1000, columns = 4)
					}
				}
				row {
					checkBox(CheckboxDescriptor(_renderLineCommentText, settings::renderLineCommentText))
						.applyToComponent { toolTipText = _renderLineCommentTextTooltip }
				}
				row {
					checkBox(CheckboxDescriptor(_renderDefinitionText, settings::renderDefinitionText))
						.applyToComponent { toolTipText = _renderDefinitionTextTooltip }
				}
				row {
					checkBox(CheckboxDescriptor(_renderLocalisationText, settings::renderLocalisationText))
						.applyToComponent { toolTipText = _renderLocalisationTextTooltip }
				}
			}
		}
	}
}
