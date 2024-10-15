package icu.windea.pls.lang.util.image

/**
 * 图片的帧数信息，用于切分图片。
 * @property frame 当前帧数。如果小于等于0，或者大于总帧数，则视为未指定。
 * @property frames 总帧数。如果小于等于0，则视为未指定。
 */
data class FrameInfo(
    val frame: Int,
    val frames: Int
) {

    companion object {
        fun of(frame: Int? = null, frames: Int? = null): FrameInfo? {
            val frames0 = if (frames == null || frames <= 0) 0 else frames
            val frame0 = if (frame == null || frame <= 0 || (frames0 != 0 && frame > frames0)) 0 else frame
            if (frame0 == 0 && frames0 == 0) return null
            return FrameInfo(frame0, frames0)
        }
    }
}

fun FrameInfo?.merge(other: FrameInfo?): FrameInfo? {
    val frame0 = if (other == null || other.frame == 0) this?.frame else other.frame
    val frames0 = if (other == null || other.frames == 0) this?.frames else other.frames
    return FrameInfo.of(frame0, frames0)
}
