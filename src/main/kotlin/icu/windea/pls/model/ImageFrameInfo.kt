package icu.windea.pls.model

import java.awt.image.*

/**
 * 图片的帧数信息，用于切分图片。
 * @property frame 当前帧数。如果小于等于0，或者大于总帧数，则视为未指定。
 * @property frames 总帧数。如果小于等于0，则视为未指定。
 */
data class ImageFrameInfo(
    val frame: Int,
    val frames: Int
) {
    companion object {
        fun of(frame: Int? = null, frames: Int? = null): ImageFrameInfo? {
            val frames0 = if (frames == null || frames <= 0) 0 else frames
            val frame0 = if (frame == null || frame <= 0 || (frames0 != 0 && frame > frames0)) 0 else frame
            return ImageFrameInfo(frame0, frames0)
        }
    }
}

infix fun ImageFrameInfo?.merge(other: ImageFrameInfo?): ImageFrameInfo? {
    val frame0 = if (other == null || other.frame == 0) this?.frame else other.frame
    val frames0 = if (other == null || other.frames == 0) this?.frames else other.frames
    return ImageFrameInfo.of(frame0, frames0)
}

fun BufferedImage.sliceBy(frameInfo: ImageFrameInfo): BufferedImage? {
    if (frameInfo.frame <= 0 || frameInfo.frames == 1) return null
    val width = width
    val height = height
    val finalFrames = if (frameInfo.frames > 0) frameInfo.frames else width / height
    val finalFrame = if (frameInfo.frame > finalFrames) finalFrames else frameInfo.frame
    val frameWidth = width / finalFrames
    val startX = (finalFrame - 1) * frameWidth
    return getSubimage(startX, 0, frameWidth, height)
}
