package icu.windea.pls.lang

import icu.windea.pls.lang.data.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*

object ParadoxSpriteHandler {
    fun getFrameInfo(sprite: ParadoxScriptDefinitionElement): FrameInfo? {
        val data = sprite.getData<ParadoxSpriteDataProvider.Data>() ?: return null
        val frame = if(data.sprite_sheet_sprite_type != null ) data.default_frame else null
        val frames = data.noOfFrames
        return FrameInfo.of(frame, frames)
    }
}