package icu.windea.pls.lang.util

import icu.windea.pls.ep.data.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

object ParadoxSpriteManager {
    fun getFrameInfo(sprite: ParadoxScriptDefinitionElement): ImageFrameInfo? {
        val data = sprite.getData<ParadoxSpriteData>() ?: return null
        val frame = if (data.spriteSheetSpriteType != null) data.defaultFrame else null
        val frames = data.noOfFrames
        return ImageFrameInfo.of(frame, frames)
    }
}
