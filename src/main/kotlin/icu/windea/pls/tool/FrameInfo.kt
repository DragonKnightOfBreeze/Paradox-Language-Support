package icu.windea.pls.tool

/**
 * 图片的帧数信息，用于切分图片。
 * @property frame 当前帧数。如果小于等于0，或者大于总帧数，则视为未指定。
 * @property frames 总帧数。如果小于等于0，则视为未指定。
 */
data class FrameInfo(
	val frame: Int = 0,
	val frames: Int = 0
){
	fun normalize() : FrameInfo? {
		val frames = if(frames <= 0) 0 else frames
		val frame = if(frame <= 0 || frame > frames) 0 else frame
		if(frame == 0) return null
		return FrameInfo(frame, frames)
	}
}