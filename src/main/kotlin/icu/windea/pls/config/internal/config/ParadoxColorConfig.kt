package icu.windea.pls.config.internal.config

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*
import java.awt.*
import java.util.*

class ParadoxColorConfig(
	override val id: String,
	override val description: String,
	val r: Int,
	val g: Int,
	val b: Int,
	val pointer: SmartPsiElementPointer<out PsiElement>
) : IdAware, DescriptionAware, IconAware {
	val color: Color = Color(r, g, b)
	override val icon = ColorIcon(16, color)
	
	val rgbExpression = "{ $r $g $b }"
	val documentation = buildString {
		append(id).append(" = ").append(rgbExpression)
		if(description.isNotEmpty()) append(" (").append(description).append(")")
	}
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxColorConfig && id == other.id && color == other.color
	}
	
	override fun hashCode(): Int {
		return Objects.hash(id, color)
	}
	
	override fun toString(): String {
		return description
	}
	
	companion object {
		//如果可以定位到font.gfx#textcolors中的定义，则基于此，否则基于内置规则文件
		//除了大写A有特殊用途其他字母都能自定义rgb颜色值
		
		/**
		 * 从`interface/fonts.gfx#bitmapfonts.textcolors`中得到指定ID的游戏目录中的或者自定义的颜色设置。或者从内置规则文件中得到。
		 */
		fun find(colorId: String, project: Project? = null): ParadoxColorConfig? {
			if(project != null) runReadAction { doFind(colorId, project) }?.also { return it }
			return getInternalConfig(project).colorMap[colorId]
		}
		
		private fun doFind(colorId: String, project: Project): ParadoxColorConfig? {
			if(colorId.singleOrNull()?.isExactLetter() != true) return null
			val definitions = findDefinitionsByType("textcolors", project)
			if(definitions.isEmpty()) return null
			var color: ParadoxColorConfig? = null
			for(definition in definitions) {
				definition.block?.processProperty { prop ->
					val id = prop.name
					if(id.singleOrNull()?.isExactLetter() != true) return@processProperty true
					if(id != colorId) return@processProperty true
					val rgbList = prop.valueList.mapNotNull { it.castOrNull<ParadoxScriptInt>()?.intValue }
					if(rgbList.size != 3) return@processProperty true
					val description = getInternalConfig(project).colorMap[id]?.description.orEmpty() //来自内置规则文件
					val colorConfig = ParadoxColorConfig(id, description, rgbList[0], rgbList[1], rgbList[2], prop.createPointer())
					color = colorConfig
					true
				}
			}
			return color //需要得到的是重载后的颜色设置
		}
		
		/**
		 * 从`interface/fonts.gfx#bitmapfonts.textcolors`中得到所有游戏目录中的或者自定义的颜色设置。或者从内置规则文件中得到。
		 */
		fun findAll(project: Project? = null): Map<String, ParadoxColorConfig> {
			if(project != null) runReadAction { doFindAll(project) }.takeIf { it.isNotEmpty() }?.also { return it }
			return getInternalConfig(project).colorMap
		}
		
		private fun doFindAll(project: Project): Map<String, ParadoxColorConfig> {
			val definitions = findDefinitionsByType("textcolors", project)
			if(definitions.isEmpty()) return emptyMap()
			val configMap = mutableMapOf<String, ParadoxColorConfig>()
			for(definition in definitions) {
				definition.block?.processProperty { prop ->
					val id = prop.name
					if(id.singleOrNull()?.isExactLetter() != true) return@processProperty true
					val rgbList = prop.valueList.mapNotNull { it.castOrNull<ParadoxScriptInt>()?.intValue }
					if(rgbList.size != 3) return@processProperty true
					val description = getInternalConfig(project).colorMap[id]?.description.orEmpty() //来自内置规则文件
					val colorConfig = ParadoxColorConfig(id, description, rgbList[0], rgbList[1], rgbList[2], prop.createPointer())
					configMap[id] = colorConfig
					true
				}
			}
			return configMap
		}
		
		/**
		 * 从`interface/fonts.gfx#bitmapfonts.textcolors`中得到所有游戏目录中的或者自定义的颜色设置。或者从内置规则文件中得到。
		 */
		fun findAllAsArray(project: Project? = null): Array<ParadoxColorConfig> {
			if(project != null) runReadAction { doFindAll(project) }.takeIf { it.isNotEmpty() }?.also { return it.values.toTypedArray() }
			return getInternalConfig(project).colors
		}
	}
}