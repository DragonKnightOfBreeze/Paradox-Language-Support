package icu.windea.pls.model

import java.util.*

/**
 * 图片的帧数信息，用于切分图片。
 * @property frame 当前帧数。如果小于等于0，或者大于总帧数，则视为未指定。
 * @property frames 总帧数。如果小于等于0，则视为未指定。
 */
class ImageFrameInfo private constructor(
    val frame: Int,
    val frames: Int
) {
    fun canApply() : Boolean {
        return frame > 0 && frames > 1 && frame <= frames
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is ImageFrameInfo && this.frame == other.frame && this.frames == other.frames
    }

    override fun hashCode(): Int {
        return Objects.hash(frame, frames)
    }

    override fun toString(): String {
        return "ImageFrameInfo(frame=$frame, frames=$frames)"
    }

    companion object {
        @JvmStatic
        fun of(frame: Int? = null, frames: Int? = null): ImageFrameInfo {
            val frames0 = if (frames == null || frames <= 0) 0 else frames
            val frame0 = if (frame == null || frame <= 0 || (frames0 != 0 && frame > frames0)) 0 else frame
            return ImageFrameInfo(frame0, frames0)
        }
    }
}

infix fun ImageFrameInfo?.merge(other: ImageFrameInfo?): ImageFrameInfo? {
    if (this == null && other == null) return null
    val frame0 = if (other == null || other.frame == 0) this?.frame else other.frame
    val frames0 = if (other == null || other.frames == 0) this?.frames else other.frames
    return ImageFrameInfo.of(frame0, frames0)
}

