package icu.windea.pls.lang.data.impl

import icu.windea.pls.lang.data.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*

class ParadoxSpriteDataProvider : ParadoxDefinitionDataProvider<ParadoxSpriteDataProvider.Data>() {
    class Data(data: ParadoxScriptData) : ParadoxDefinitionData {
        val noOfFrames: Int? by data.get("noOfFrames")
    }
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "sprite"
    }
}