package com.magnariuk.lemonkatoolsubs.data

import com.magnariuk.lemonkatoolsubs.data.classes.*
import java.io.File

class Parser {
    fun parseAssFile(filePath: String): Ass {
        val scriptInfo = mutableMapOf<String, String>()
        val aegisubProjectGarbage = mutableMapOf<String, String>()
        val events = mutableListOf<Any>()
        val formatFields = mutableListOf<String>()

        var currentSection = ""

        File(filePath).useLines { lines ->
            lines.forEach { line ->
                when {
                    line.startsWith("[") && line.endsWith("]") -> {
                        currentSection = line.trim()
                    }
                    currentSection == "[Script Info]" -> {
                        parseKeyValue(line)?.let { (key, value) -> scriptInfo[key] = value }
                    }
                    currentSection == "[Aegisub Project Garbage]" -> {
                        parseKeyValue(line)?.let { (key, value) -> aegisubProjectGarbage[key] = value }
                    }
                    currentSection == "[Events]" -> {
                        when {
                            line.startsWith("Format:") -> {
                                formatFields.addAll(line.removePrefix("Format:").split(",").map { it.trim() })
                            }
                            line.startsWith("Comment:") -> {
                                events.add(parseEvent(line.removePrefix("Comment:"), formatFields, isComment = true))
                            }
                            line.startsWith("Dialogue:") -> {
                                events.add(parseEvent(line.removePrefix("Dialogue:"), formatFields, isComment = false))
                            }
                        }
                    }
                }
            }
        }

        return Ass(
            scriptInfo = parseScriptInfo(scriptInfo),
            aegisubProjectGarbage = parseAegisubProjectGarbage(aegisubProjectGarbage),
            events = Events(
                format = formatFields,
                comments = events.filterIsInstance<Comment>(),
                dialogues = events.filterIsInstance<Dialogue>()
            )
        )
    }

    fun parseKeyValue(line: String): Pair<String, String>? {
        val parts = line.split(":", limit = 2)
        return if (parts.size == 2) {
            parts[0].trim() to parts[1].trim()
        } else null
    }

    fun parseScriptInfo(data: Map<String, String>) = ScriptInfo(
        Title = data["Title"] ?: "",
        ScriptType = data["ScriptType"] ?: "",
        WrapStyle = data["WrapStyle"]?.toIntOrNull() ?: 0,
        ScaledBorderAndShadow = data["ScaledBorderAndShadow"] ?: "",
        PlayResX = data["PlayResX"]?.toIntOrNull() ?: 0,
        PlayResY = data["PlayResY"]?.toIntOrNull() ?: 0,
        YCbCrMatrix = data["YCbCrMatrix"] ?: ""
    )

    fun parseAegisubProjectGarbage(data: Map<String, String>) = AegisubProjectGarbage(
        LastStyleStorage = data["Last Style Storage"] ?: "",
        AudioFile = data["Audio File"] ?: "",
        VideoFile = data["Video File"] ?: "",
        VideoARMode = data["Video AR Mode"] ?: "",
        VideoARValue = data["Video AR Value"]?.toDoubleOrNull() ?: 0.0,
        VideoZoomPercent = data["Video Zoom Percent"]?.toDoubleOrNull() ?: 0.0,
        ScrollPosition = data["Scroll Position"]?.toIntOrNull() ?: 0,
        ActiveLine = data["Active Line"]?.toIntOrNull() ?: 0,
        VideoPosition = data["Video Position"]?.toIntOrNull() ?: 0
    )

    fun parseEvent(line: String, format: List<String>, isComment: Boolean): Any {
        val values = line.split(",", limit = 10).map { it.trim() }

        val eventData = format.zip(values).toMap()

        return if (isComment) {
            Comment(
                layer = eventData["Layer"]?.toIntOrNull() ?: 0,
                startTime = eventData["Start"] ?: "",
                endTime = eventData["End"] ?: "",
                style = eventData["Style"] ?: "",
                actor = eventData["Name"] ?: "",
                marginL = eventData["MarginL"]?.toIntOrNull() ?: 0,
                marginR = eventData["MarginR"]?.toIntOrNull() ?: 0,
                marginV = eventData["MarginV"]?.toIntOrNull() ?: 0,
                effect = eventData["Effect"] ?: "",
                text = eventData["Text"] ?: ""
            )
        } else {
                Dialogue(
                    layer = eventData["Layer"]?.toIntOrNull() ?: 0,
                    startTime = eventData["Start"] ?: "",
                    endTime = eventData["End"] ?: "",
                    style = eventData["Style"] ?: "",
                    actor = eventData["Name"] ?: "",
                    marginL = eventData["MarginL"]?.toIntOrNull() ?: 0,
                    marginR = eventData["MarginR"]?.toIntOrNull() ?: 0,
                    marginV = eventData["MarginV"]?.toIntOrNull() ?: 0,
                    effect = eventData["Effect"] ?: "",
                    text = eventData["Text"] ?: ""
                )
        }
    }

    fun writeToFile(filename: String, lines: MutableList<String>) {
        val file = File(filename)
        file.printWriter().use { writer ->
            lines.forEach { line ->
                writer.println(line)
            }
        }
    }

    fun createSubRip(filename: String, ass: Ass, characters: List<String>){
        val lines: MutableList<String> = mutableListOf()
        var counter = 2
        println(filename)
        println(characters.toString())
        if (ass.events.dialogues.any { it.actor in characters }) {
            lines.add("1")
            lines.add("00:00:00,00 --> 00:00:00,1")
            lines.add("   ")
            lines.add("")


            ass.events.dialogues.forEach { dialogue ->
                if(dialogue.actor in characters && !lines.contains("${dialogue.startTime} --> ${dialogue.endTime}")){
                    lines.add(counter.toString())
                    lines.add("0${dialogue.startTime.replace(".", ",")} --> 0${dialogue.endTime.replace(".", ",")}")
                    lines.add("[${dialogue.actor}]: ${dialogue.text.replace(Regex("\\{.*?}"), "")}") //.replace(Regex("\\{.*?}"), "")
                    lines.add("")
                    counter++
                }
            }
            writeToFile(filename, lines)
        }
    }
}

