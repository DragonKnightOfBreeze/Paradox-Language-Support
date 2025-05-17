package icu.windea.pls.ep.data

import icu.windea.pls.lang.util.data.*
import icu.windea.pls.model.constants.*

class ParadoxSpriteData(data: ParadoxScriptData) : ParadoxDefinitionData {
    val textureFile: String? by data.get("textureFile")
    val spriteSheetSpriteType: String? by data.get("sprite_sheet_sprite_type")
    val noOfFrames: Int? by data.get("noOfFrames")
    val defaultFrame: Int? by data.get("default_frame")

    class Provider : ParadoxDefinitionDataProviderBase<ParadoxSpriteData>(ParadoxDefinitionTypes.Sprite)
}
