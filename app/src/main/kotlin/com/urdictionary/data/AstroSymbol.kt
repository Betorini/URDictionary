package com.urdictionary.data

/**
 * One Uranian astrological symbol.
 *
 * [code] — 2-letter code used in the DB (e.g. "SU", "JU").
 *           Exactly matches the original app's button codes.
 * [unicode] — Unicode glyph shown large in the grid.
 * [nameTh] — Short Thai name shown below the glyph.
 * [nameEn] — Short English name.
 * [category] — Grouping for potential sectioned display.
 */
data class AstroSymbol(
    val code: String,
    val unicode: String,
    val nameTh: String,
    val nameEn: String,
    val category: SymbolCategory,
)

enum class SymbolCategory { AXIS, LUMINARY, CLASSIC, OUTER, TRANSNEPTUNIAN }

/**
 * Complete list in the exact visual order shown in the original app screenshot:
 *   Row 1 (red):  AR  AS  MC  ☉  ☽  ♏ (Ω)
 *   Row 2 (dark): ☿  ♀  ♂  ♃  ♄  ♅  ♆  ♇  (Nodes)
 *   Row 3 (blue): CU HA ZE KR AP AD VU PO  (TNPs)
 */
val ALL_SYMBOLS: List<AstroSymbol> = listOf(
    // ── Row 1: Axes & Luminaries ──────────────────────────────────────────
    AstroSymbol("AR", "♈",  "ลาภ/จุดชะตา", "Aries/Fortune",  SymbolCategory.AXIS),
    AstroSymbol("AS", "AC",  "แอสเซนแดนท์",  "Ascendant",      SymbolCategory.AXIS),
    AstroSymbol("MC", "MC",  "มิดเฮฟเว่น",   "Midheaven",      SymbolCategory.AXIS),
    AstroSymbol("SU", "☉",  "ดวงอาทิตย์",   "Sun",            SymbolCategory.LUMINARY),
    AstroSymbol("MO", "☽",  "ดวงจันทร์",    "Moon",           SymbolCategory.LUMINARY),
    AstroSymbol("NO", "☊",  "นอร์ธโนด",     "North Node",     SymbolCategory.LUMINARY),

    // ── Row 2: Classic & Outer planets ────────────────────────────────────
    AstroSymbol("ME", "☿",  "ดาวพุธ",       "Mercury",        SymbolCategory.CLASSIC),
    AstroSymbol("VE", "♀",  "ดาวศุกร์",     "Venus",          SymbolCategory.CLASSIC),
    AstroSymbol("MA", "♂",  "ดาวอังคาร",    "Mars",           SymbolCategory.CLASSIC),
    AstroSymbol("JU", "♃",  "ดาวพฤหัส",     "Jupiter",        SymbolCategory.CLASSIC),
    AstroSymbol("SA", "♄",  "ดาวเสาร์",     "Saturn",         SymbolCategory.CLASSIC),
    AstroSymbol("UR", "♅",  "ดาวยูเรนัส",   "Uranus",         SymbolCategory.OUTER),
    AstroSymbol("NE", "♆",  "ดาวเนปจูน",    "Neptune",        SymbolCategory.OUTER),
    AstroSymbol("PL", "♇",  "ดาวพลูโต",     "Pluto",          SymbolCategory.OUTER),

    // ── Row 3: Hamburg Transneptunians ─────────────────────────────────────
    AstroSymbol("CU", "⚵",  "คิวพิโด",      "Cupido",         SymbolCategory.TRANSNEPTUNIAN),
    AstroSymbol("HA", "⚶",  "ฮาเดส",        "Hades",          SymbolCategory.TRANSNEPTUNIAN),
    AstroSymbol("ZE", "⚷",  "ซุส",          "Zeus",           SymbolCategory.TRANSNEPTUNIAN),
    AstroSymbol("KR", "⚸",  "โครนอส",       "Kronos",         SymbolCategory.TRANSNEPTUNIAN),
    AstroSymbol("AP", "⚹",  "อพอลโล",       "Apollon",        SymbolCategory.TRANSNEPTUNIAN),
    AstroSymbol("AD", "⚺",  "อดมีโตส",      "Admetos",        SymbolCategory.TRANSNEPTUNIAN),
    AstroSymbol("VU", "⚻",  "วัลคานัส",     "Vulcanus",       SymbolCategory.TRANSNEPTUNIAN),
    AstroSymbol("PO", "⚼",  "โพไซดอน",      "Poseidon",       SymbolCategory.TRANSNEPTUNIAN),
)

val SYMBOL_MAP: Map<String, AstroSymbol> = ALL_SYMBOLS.associateBy { it.code }
