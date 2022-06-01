package icu.windea.pls.script.inspections.definition

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.openapi.observable.util.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.*
import icu.windea.pls.annotation.*
import javax.swing.*

/**
 * 无法解析的文件路径的检查。
 * 
 * @property ignoredFilePathRegex （配置项）需要忽略的文件路径的正则，忽略大小写。默认为".*\.lua"，以忽略lua文件。
 */
@UnstableInspection
class UnresolvedFilePathInspection : LocalInspectionTool(){
	@OptionTag(converter = RegexIgnoreCaseConverter::class)
	@JvmField var ignoredFilePathRegex = """.*\.lua""".toRegex(RegexOption.IGNORE_CASE)
	
	//icu.windea.pls.config.cwt.CwtConfigExtensionsKt.doResolveValue
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			row {
				label(PlsBundle.message("script.inspection.definition.inspection.unresolvedFilePath.option.ignoredFilePathRegex")).applyToComponent {
					toolTipText = PlsBundle.message("script.inspection.definition.inspection.unresolvedFilePath.option.ignoredIconNameRegex.tooltip")
				}
			}
			row {
				textField()
					.bindText({ ignoredFilePathRegex.pattern }, { ignoredFilePathRegex = it.toRegex(RegexOption.IGNORE_CASE) })
					.applyToComponent {
						whenTextChanged {
							val document = it.document
							val text = document.getText(0, document.length)
							if(text != ignoredFilePathRegex.pattern) ignoredFilePathRegex = text.toRegex(RegexOption.IGNORE_CASE)
						}
					}
					.horizontalAlign(HorizontalAlign.FILL)
					.resizableColumn()
			}
		}
	}
}