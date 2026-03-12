package com.urdictionary.data

/**
 * The four dictionary sources — mirrors the original app's context menu.
 * [tableName] is the SQLite table name.
 * [displayTh] Thai label shown in the UI selector.
 * [isUpperCase] whether the [aa] column uses uppercase codes (ruth/rutheng/symphony)
 *               or lowercase (combine).
 */
enum class DictionarySource(
    val tableName: String,
    val displayTh: String,
    val displayEn: String,
    val isUpperCase: Boolean,
    val bgHex: String,          // color from original app
) {
    COMBINE(
        tableName = "combine",
        displayTh = "อ.ประยูร พลอารีย์",
        displayEn = "อ.ประยูร",
        isUpperCase = false,
        bgHex = "#FFD4D8D7"
    ),
    RUTH_TH(
        tableName = "ruth",
        displayTh = "Ruth Brummund (ภาษาไทย)",
        displayEn = "Ruth TH",
        isUpperCase = true,
        bgHex = "#FFD4B546"
    ),
    RUTH_EN(
        tableName = "rutheng",
        displayTh = "Ruth Brummund (English)",
        displayEn = "Ruth EN",
        isUpperCase = true,
        bgHex = "#FFD4B546"
    ),
    SYMPHONY(
        tableName = "symphony",
        displayTh = "Symphony of Planet",
        displayEn = "Symphony",
        isUpperCase = true,
        bgHex = "#99FFFF"
    );
}

/**
 * One result row ready for display.
 *
 * [displayKey] — e.g. "SU+JU-XX" (combine/ruth) or "SU/JU" (symphony)
 * [content] — the interpretation text
 * [source] — which dictionary this came from
 */
data class InterpretationRow(
    val displayKey: String,
    val content: String,
    val source: DictionarySource,
)

/**
 * The search mode for keyword search in DicSearch screen.
 * AND = all words must appear; OR = any word.
 */
enum class SearchMode { AND, OR }
