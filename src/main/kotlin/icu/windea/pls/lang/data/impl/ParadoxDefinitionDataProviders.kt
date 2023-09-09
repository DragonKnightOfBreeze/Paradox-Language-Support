package icu.windea.pls.lang.data.impl

import icu.windea.pls.lang.data.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*

class ParadoxSpriteDataProvider : ParadoxDefinitionDataProvider<ParadoxSpriteDataProvider.Data>() {
    class Data(data: ParadoxScriptData): ParadoxDefinitionData {
        val textureFile: String? by data.get("textureFile")
        val sprite_sheet_sprite_type: String? by data.get("sprite_sheet_sprite_type")
        val noOfFrames: Int? by data.get("noOfFrames")
        val default_frame: Int? by data.get("default_frame")
    }
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "sprite"
    }
}