package icu.windea.pls.lang.util

import icu.windea.pls.ep.util.data.ParadoxSpriteData
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

object ParadoxSpriteManager {
    fun getFrameInfo(element: ParadoxScriptDefinitionElement): ImageFrameInfo? {
        val data = element.getDefinitionData<ParadoxSpriteData>() ?: return null
        val frame = if (data.spriteSheetSpriteType != null) data.defaultFrame else null
        val frames = data.noOfFrames
        return ImageFrameInfo.of(frame, frames)
    }

    fun getFrameInfo(element: ParadoxScriptDefinitionElement, oldFrameInfo: ImageFrameInfo): ImageFrameInfo {
        val data = element.getDefinitionData<ParadoxSpriteData>() ?: return oldFrameInfo
        val noOfFrames = data.noOfFrames ?: return oldFrameInfo
        return ImageFrameInfo.of(oldFrameInfo.frame, noOfFrames)
    }
}
