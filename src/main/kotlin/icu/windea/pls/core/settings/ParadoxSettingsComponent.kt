package icu.windea.pls.core.settings

import com.intellij.ui.components.*
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import javax.swing.*

class ParadoxSettingsComponent {
	companion object {
		private val _genericTitle = message("pls.settings.generic")
		private val _defaultGameType = message("pls.settings.generic.defaultGameType")
		private val _defaultGameTypeTooltip = message("pls.settings.generic.defaultGameType.tooltip")
		private val _preferOverriddenName = message("pls.settings.generic.preferOverridden")
		private val _preferOverriddenTooltip = message("pls.settings.generic.preferOverridden.tooltip")
		private val _performanceTitle = message("pls.settings.performance")
		private val _maxCompleteSize = message("pls.settings.performance.maxCompleteSize")
		private val _maxCompleteSizeTooltip = message("pls.settings.performance.maxCompleteSize.tooltip")
		private val _renderLineCommentText = message("pls.settings.performance.renderLineCommentText")
		private val _renderLineCommentTextTooltip = message("pls.settings.performance.renderLineCommentText.tooltip")
		private val _renderDefinitionText = message("pls.settings.performance.renderDefinitionText")
		private val _renderDefinitionTextTooltip = message("pls.settings.performance.renderDefinitionText.tooltip")
		private val _renderLocalisationText = message("pls.settings.performance.renderLocalisationText")
		private val _renderLocalisationTextTooltip = message("pls.settings.performance.renderLocalisationText.tooltip")
	}
	
	lateinit var defaultGameTypeComboBox :JComboBox<ParadoxGameType>
	lateinit var preferOverriddenCheckBox: JCheckBox
	lateinit var maxCompleteSizeTextField:JBTextField
	lateinit var renderLineCommentTextCheckBox: JCheckBox
	lateinit var renderDefinitionTextCheckBox: JCheckBox
	lateinit var renderLocalisationTextCheckBox: JCheckBox
	
	val panel = panel {
		titledRow(_genericTitle) {
			row{
				label(_defaultGameType).applyToComponent {
					toolTipText = _defaultGameTypeTooltip
				}
				comboBox(DefaultComboBoxModel(ParadoxGameType.values), getSettings()::defaultGameType).applyToComponent {
					defaultGameTypeComboBox = this
				}
			}
			row {
				checkBox(_preferOverriddenName, false).applyToComponent {
					toolTipText = _preferOverriddenTooltip 
					preferOverriddenCheckBox = this 
				}
			}
		}
		titledRow(_performanceTitle){
			row {
				label(_maxCompleteSize).applyToComponent {
					toolTipText = _maxCompleteSizeTooltip
				}
				intTextField(getSettings()::maxCompleteSize, range=0..1000).applyToComponent {
					maxCompleteSizeTextField = this
				}
			}
			row {
				checkBox(_renderLineCommentText, false).applyToComponent {
					toolTipText = _renderLineCommentTextTooltip
					renderLineCommentTextCheckBox = this
				}
			}
			row {
				checkBox(_renderDefinitionText, true).applyToComponent {
					toolTipText = _renderDefinitionTextTooltip
					renderDefinitionTextCheckBox = this
				}
			}
			row {
				checkBox(_renderLocalisationText, true).applyToComponent {
					toolTipText = _renderLocalisationTextTooltip
					renderLocalisationTextCheckBox = this
				}
			}
		}
	}
}