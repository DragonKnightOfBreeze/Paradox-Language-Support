package icu.windea.pls.lang.util

import icu.windea.pls.ep.data.ParadoxSpriteData
import icu.windea.pls.lang.getData
import icu.windea.pls.model.ImageFrameInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

object ParadoxSpriteManager {
    fun getFrameInfo(element: ParadoxScriptDefinitionElement): ImageFrameInfo? {
        val data = element.getData<ParadoxSpriteData>() ?: return null
        val frame = if (data.spriteSheetSpriteType != null) data.defaultFrame else null
        val frames = data.noOfFrames
        return ImageFrameInfo.of(frame, frames)
    }

    fun getFrameInfo(element: ParadoxScriptDefinitionElement, oldFrameInfo: ImageFrameInfo): ImageFrameInfo {
        val data = element.getData<ParadoxSpriteData>() ?: return oldFrameInfo
        val noOfFrames = data.noOfFrames ?: return oldFrameInfo
        return ImageFrameInfo.of(oldFrameInfo.frame, noOfFrames)
    }
}
