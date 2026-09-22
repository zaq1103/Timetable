package com.example.teachertimetable.ocr

data class ParsedCourse(
    val rawLine: String,
    val dayOfWeek: Int?,
    val periodIndex: Int?,
    val startMinute: Int?,
    val endMinute: Int?,
    val className: String,
    val subject: String,
    val location: String,
    val confidence: Float
)

object TimetableOcrParser {

    private val dayMap = mapOf(
        "周一" to 1, "星期一" to 1, "礼拜一" to 1,
        "周二" to 2, "星期二" to 2, "礼拜二" to 2,
        "周三" to 3, "星期三" to 3, "礼拜三" to 3,
        "周四" to 4, "星期四" to 4, "礼拜四" to 4,
        "周五" to 5, "星期五" to 5, "礼拜五" to 5,
        "周六" to 6, "星期六" to 6, "礼拜六" to 6,
        "周日" to 7, "星期日" to 7, "星期天" to 7, "周天" to 7
    )

    private val periodRegex = Regex("""第\s*([0-9一二三四五六七八九十]+)\s*节""")
    private val timeRangeRegex = Regex("""(\d{1,2})[:：](\d{2})\s*[-~—到至]\s*(\d{1,2})[:：](\d{2})""")
    private val classFallbackRegex = Regex("""[\u4e00-\u9fa5]{1,4}\d{1,2}班|[\u4e00-\u9fa5]{1,4}\(\d{1,2}\)班""")
    private val commonSubjects = listOf(
        "语文", "数学", "英语", "物理", "化学", "生物",
        "政治", "历史", "地理", "体育", "音乐", "美术",
        "信息技术", "通用技术", "科学", "道法", "道德与法治"
    )

    private val cnDigits = mapOf(
        '一' to 1, '二' to 2, '三' to 3, '四' to 4, '五' to 5,
        '六' to 6, '七' to 7, '八' to 8, '九' to 9, '十' to 10
    )

    fun parse(rawText: String, myClasses: List<String>, mySubjects: List<String>): List<ParsedCourse> {
        return rawText.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { parseLine(it, myClasses, mySubjects) }
    }

    private fun parseLine(line: String, myClasses: List<String>, mySubjects: List<String>): ParsedCourse? {
        var day: Int? = null
        var period: Int? = null
        var startMin: Int? = null
        var endMin: Int? = null
        var confidence = 0f
        var work = line

        // 1. 匹配星期
        for ((k, v) in dayMap) {
            if (work.contains(k)) {
                day = v
                work = work.replace(k, " ")
                confidence += 0.3f
                break
            }
        }

        // 2. 匹配时间范围
        timeRangeRegex.find(work)?.let { m ->
            val h1 = m.groupValues[1].toInt(); val m1 = m.groupValues[2].toInt()
            val h2 = m.groupValues[3].toInt(); val m2 = m.groupValues[4].toInt()
            startMin = h1 * 60 + m1
            endMin = h2 * 60 + m2
            work = work.replace(m.value, " ")
            confidence += 0.3f
        }

        // 3. 匹配"第N节"
        if (startMin == null) {
            periodRegex.find(work)?.let { m ->
                val s = m.groupValues[1]
                period = s.toIntOrNull() ?: cnToInt(s)
                work = work.replace(m.value, " ")
                confidence += 0.2f
            }
        }

        // 4. 匹配预设班级（优先），否则用通用正则兜底
        var matchedClass = ""
        for (cls in myClasses) {
            if (cls.isNotBlank() && work.contains(cls)) {
                matchedClass = cls
                work = work.replace(cls, " ")
                confidence += 0.2f
                break
            }
        }
        if (matchedClass.isBlank()) {
            classFallbackRegex.find(work)?.let {
                matchedClass = it.value
                work = work.replace(it.value, " ")
                confidence += 0.1f
            }
        }

        // 5. 匹配预设学科（优先），否则用常见学科兜底
        var matchedSubject = ""
        for (sub in mySubjects) {
            if (sub.isNotBlank() && work.contains(sub)) {
                matchedSubject = sub
                work = work.replace(sub, " ")
                confidence += 0.2f
                break
            }
        }
        if (matchedSubject.isBlank()) {
            for (sub in commonSubjects) {
                if (work.contains(sub)) {
                    matchedSubject = sub
                    work = work.replace(sub, " ")
                    break
                }
            }
        }

        val remainder = work.replace(Regex("""\s+"""), " ").trim()

        if (day == null && startMin == null && period == null &&
            matchedClass.isBlank() && matchedSubject.isBlank() && remainder.isBlank()
        ) return null

        return ParsedCourse(
            rawLine = line,
            dayOfWeek = day,
            periodIndex = period,
            startMinute = startMin,
            endMinute = endMin,
            className = matchedClass,
            subject = matchedSubject,
            location = remainder,
            confidence = confidence.coerceIn(0f, 1f)
        )
    }

    private fun cnToInt(s: String): Int? {
        if (s.length == 1) return cnDigits[s[0]]
        if (s.length == 2 && s[0] == '十') return 10 + (cnDigits[s[1]] ?: 0)
        if (s.length == 2 && s[1] == '十') return (cnDigits[s[0]] ?: 0) * 10
        if (s.length == 3 && s[1] == '十')
            return (cnDigits[s[0]] ?: 0) * 10 + (cnDigits[s[2]] ?: 0)
        return null
    }

    /** "第N节" → 默认时间。可在设置里扩展成可配作息表 */
    fun periodToTime(period: Int): Pair<Int, Int> {
        val baseStart = 8 * 60
        val slot = 55
        val start = baseStart + (period - 1).coerceAtLeast(0) * slot
        return start to (start + 45)
    }
}
