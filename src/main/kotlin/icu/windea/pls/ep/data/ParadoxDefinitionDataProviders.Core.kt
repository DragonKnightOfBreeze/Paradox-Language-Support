@file:Suppress("unused")

package icu.windea.pls.ep.data

import icu.windea.pls.lang.util.data.ParadoxScriptData
import icu.windea.pls.lang.util.data.get
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.ep.data.ParadoxDefinitionDataProviderDelegates as Delegates

class ParadoxModDescriptorData(data: ParadoxScriptData) : ParadoxDefinitionData {
    val name: String? by data.get("name")
    val version: String? by data.get("version")
    val picture: String? by data.get("picture")
    val tags: Set<String> by data.get("tags", emptySet())
    val supportedVersion: String? by data.get("supported_version")
    val remoteFileId: String? by data.get("remote_file_id")
    val path: String? by data.get("path")
}

class ParadoxSpriteData(data: ParadoxScriptData) : ParadoxDefinitionData {
    val textureFile: String? by data.get("textureFile")
    val spriteSheetSpriteType: String? by data.get("sprite_sheet_sprite_type")
    val noOfFrames: Int? by data.get("noOfFrames")
    val defaultFrame: Int? by data.get("default_frame")

    class Provider : ParadoxDefinitionDataProvider<ParadoxSpriteData> by Delegates.create(ParadoxDefinitionTypes.Sprite)
}
