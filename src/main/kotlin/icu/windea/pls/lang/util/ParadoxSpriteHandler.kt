package icu.windea.pls.lang.util

import icu.windea.pls.ep.data.*
import icu.windea.pls.lang.util.image.*
import icu.windea.pls.script.psi.*

object ParadoxSpriteHandler {
    fun getFrameInfo(sprite: ParadoxScriptDefinitionElement): FrameInfo? {
        val data = sprite.getData<ParadoxSpriteDataProvider.Data>() ?: return null
        val frame = if(data.sprite_sheet_sprite_type != null ) data.default_frame else null
        val frames = data.noOfFrames
        return FrameInfo.of(frame, frames)
    }
}