package icu.windea.pls.lang.psi.light

import com.intellij.openapi.project.Project
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.core.ReadWriteAccess
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import java.util.*
import icu.windea.pls.model.ParadoxGameType

class CwtConfigSymbolLightElement(
    parent: CwtStringExpressionElement,
    private val name: String,
    val configType: CwtConfigType,
    val readWriteAccess: ReadWriteAccess,
    override val gameType: ParadoxGameType,
    private val project: Project
) : CwtConfigLightElementBase(parent) {
    override fun getIcon(flags: Int) = configType.icon

    override fun getName() = name

    override fun getText() = name

    override fun getProject() = project

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtConfigSymbolLightElement
            && name == other.name
            && configType == other.configType
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(name, configType, gameType, project)
    }
}

