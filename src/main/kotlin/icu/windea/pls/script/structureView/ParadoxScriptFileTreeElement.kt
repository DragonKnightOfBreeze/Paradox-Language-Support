package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.localisation.*
import javax.swing.*

class ParadoxScriptFileTreeElement(
	element: ParadoxScriptFile
) : PsiTreeElementBase<ParadoxScriptFile>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val element = element ?: return emptyList()
		val rootBlock = element.block ?: return emptyList()
		val result = SmartList<StructureViewTreeElement>()
		rootBlock.forEachChild {
			when {
				it is ParadoxScriptScriptedVariable -> result.add(ParadoxScriptVariableTreeElement(it))
				it is ParadoxScriptProperty -> result.add(ParadoxScriptPropertyTreeElement(it))
				it is ParadoxScriptValue -> result.add(ParadoxScriptValueTreeElement(it))
			}
		}
		return result
	}
	
	override fun getIcon(open: Boolean): Icon? {
		val element = element ?: return null
		//如果文件名是descriptor.mod（不区分大小写），则使用特殊图标
		val name = element.name
		if(name.equals(PlsConstants.descriptorFileName, true)) return PlsIcons.DescriptorFile
		//如果是定义，则显示定义的图标
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) return PlsIcons.Definition
		return element.icon
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		//如果文件名是descriptor.mod（不区分大小写），则直接显示该文件名
		val name = element.name
		if(name.equals(PlsConstants.descriptorFileName, true)) return name
		//如果是定义，则优先显示定义的名字
		val definitionInfo = element.definitionInfo
		if(definitionInfo != null) return definitionInfo.name.orAnonymous()
		return name
	}
	
	override fun getLocationString(): String? {
		val element = element ?: return null
		//如果文件名是descriptor.mod（不区分大小写），则忽略
		val name = element.name
		if(name.equals(PlsConstants.descriptorFileName, true)) return null
		//如果是定义，则显示定义的类型信息
		val definitionInfo = element.definitionInfo ?: return null
		val builder = StringBuilder()
		builder.append(": ").append(definitionInfo.typesText)
		//如果存在，显示定义的本地化名字（最相关的本地化文本）
		val primaryLocalisation = ParadoxDefinitionHandler.getPrimaryLocalisation(element)
		if(primaryLocalisation != null) {
			//这里需要使用移除格式后的纯文本，这里返回的字符串不是HTML
			val localizedName = ParadoxLocalisationTextExtractor.extract(primaryLocalisation)
			builder.append(" ").append(localizedName)
		}
		return builder.toString()
	}
}
