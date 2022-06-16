package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.*

class ParadoxScriptFileTreeElement(
	element: ParadoxScriptFile
) : PsiTreeElementBase<ParadoxScriptFile>(element) {
	override fun getChildrenBase(): Collection<StructureViewTreeElement> {
		val element = element ?: return emptyList()
		val rootBlock = element.block ?: return emptyList()
		val result = SmartList<StructureViewTreeElement>()
		rootBlock.forEachChild {
			when {
				it is ParadoxScriptVariable -> result.add(ParadoxScriptVariableTreeElement(it))
				it is ParadoxScriptProperty -> result.add(ParadoxScriptPropertyTreeElement(it))
				it is ParadoxScriptValue -> result.add(ParadoxScriptValueTreeElement(it))
			}
		}
		return result
	}
	
	override fun getPresentableText(): String? {
		val element = element ?: return null
		return element.name
	}
	
	override fun getLocationString(): String? {
		val element = element ?: return null
		//如果文件名是descriptor.mod（不区分大小写），不显示定义信息
		if(element.name.equals(descriptorFileName, true)) return null
		val definitionInfo = element.definitionInfo ?: return null
		val name = definitionInfo.name
		val typesText = definitionInfo.typeText
		//如果definitionName和rootKey相同，则省略definitionName
		val builder = StringBuilder()
		if(!name.equals(definitionInfo.rootKey, true)) {
			builder.append(name)
		}
		builder.append(": ").append(typesText)
		//如果存在，显示定义的本地化名字（最相关的本地化文本）
		val primaryLocalisation = definitionInfo.resolvePrimaryLocalisation(element)
		if(primaryLocalisation != null) {
			val localizedName = ParadoxLocalisationTextRenderer.render(primaryLocalisation)
			builder.append(" ").append(localizedName)
		}
		return builder.toString()
	}
}
