package com.kaze.player.data.lyrics

data class LyricLine(
    val time: Long, // milliseconds
    val text: String
)

object LyricsParser {

    private val timeRegex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})]""")

    /**
     * Parse LRC format lyrics into a list of timed lyric lines.
     */
    fun parseLrc(content: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        for (rawLine in content.lines()) {
            val matches = timeRegex.findAll(rawLine).toList()
            if (matches.isEmpty()) continue

            // Extract lyric text after all timestamps
            val lastMatch = matches.last()
            val text = rawLine.substring(lastMatch.range.last + 1).trim()

            for (match in matches) {
                val min = match.groupValues[1].toLong()
                val sec = match.groupValues[2].toLong()
                val msStr = match.groupValues[3]
                val ms = if (msStr.length == 2) msStr.toLong() * 10 else msStr.toLong()
                val time = min * 60_000 + sec * 1000 + ms
                lines.add(LyricLine(time, text))
            }
        }
        return lines.sortedBy { it.time }
    }

    /**
     * Find the current lyric line index for a given position.
     */
    fun findCurrentLine(lines: List<LyricLine>, position: Long): Int {
        if (lines.isEmpty()) return -1
        var result = 0
        for (i in lines.indices) {
            if (lines[i].time <= position) {
                result = i
            } else {
                break
            }
        }
        return result
    }
}
