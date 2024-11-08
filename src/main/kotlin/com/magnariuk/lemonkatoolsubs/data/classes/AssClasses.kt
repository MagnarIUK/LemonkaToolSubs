package com.magnariuk.lemonkatoolsubs.data.classes


data class Ass(
    var scriptInfo: ScriptInfo,
    var aegisubProjectGarbage: AegisubProjectGarbage,
    var events: Events
) {
    fun getAllActors(): List<String> {
        return events.dialogues.map { it.actor }
    }
}
data class Comment(
    val layer: Int,
    val startTime: String,
    val endTime: String,
    val style: String,
    val actor: String,
    val marginL: Int,
    val marginR: Int,
    val marginV: Int,
    val effect: String,
    val text: String
)
data class Dialogue(
    val layer: Int,
    val startTime: String,
    val endTime: String,
    val style: String,
    val actor: String,
    val marginL: Int,
    val marginR: Int,
    val marginV: Int,
    val effect: String,
    val text: String
)
data class ScriptInfo(
    val Title: String,
    val ScriptType: String,
    val WrapStyle: Int,
    val ScaledBorderAndShadow: String,
    val PlayResX: Int,
    val PlayResY: Int,
    val YCbCrMatrix: String
)

data class AegisubProjectGarbage(
    val LastStyleStorage: String,
    val AudioFile: String,
    val VideoFile: String,
    val VideoARMode: String,
    val VideoARValue: Double,
    val VideoZoomPercent: Double,
    val ScrollPosition: Int,
    val ActiveLine: Int,
    val VideoPosition: Int
)

data class Events(
    var format: List<String>,
    var comments: List<Comment>,
    var dialogues: List<Dialogue>,
)



