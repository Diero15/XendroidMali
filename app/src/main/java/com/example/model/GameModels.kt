package com.example.model

data class XboxGame(
    val id: String,
    val titleId: String,
    val titleName: String,
    val releaseYear: String,
    val genre: String,
    val sizeGb: Double,
    val format: GameFormat,
    val compatibility: CompatibilityRating,
    val maliCompatibilityNotes: String,
    val recommendedFpsTarget: Int,
    val has60FpsPatch: Boolean,
    val coverAccentHex: Long
)

enum class GameFormat(val displayName: String) {
    ISO("ISO Disc"),
    XEX("Folder / XEX"),
    GOD("Games on Demand"),
    ZAR("ZArchive")
}

enum class CompatibilityRating(val label: String, val colorHex: Long) {
    PLAYABLE("Jugable (60 FPS)", 0xFF10B981),
    GOOD("Jugable (30 FPS)", 0xFF06B6D4),
    IN_GAME("En Partida", 0xFFF59E0B),
    INTRO("Intro / Menús", 0xFFEC4899),
    TESTING("En Pruebas", 0xFF94A3B8)
}

object SampleGameLibrary {
    val games = listOf(
        XboxGame(
            id = "halo3",
            titleId = "4D5307E6",
            titleName = "Halo 3",
            releaseYear = "2007",
            genre = "Shooter / Sci-Fi",
            sizeGb = 5.7,
            format = GameFormat.ISO,
            compatibility = CompatibilityRating.PLAYABLE,
            maliCompatibilityNotes = "Excelente con FBO path y FP16 shader math. Rinde a 60 FPS estables en Dimensity 8300 / 9200.",
            recommendedFpsTarget = 60,
            has60FpsPatch = true,
            coverAccentHex = 0xFF059669
        ),
        XboxGame(
            id = "gow2",
            titleId = "4D53082D",
            titleName = "Gears of War 2",
            releaseYear = "2008",
            genre = "Third-Person Shooter",
            sizeGb = 6.8,
            format = GameFormat.ISO,
            compatibility = CompatibilityRating.GOOD,
            maliCompatibilityNotes = "Requiere Scissor Bounding activado para evitar el bug de bandas negras de 8192px en Mali.",
            recommendedFpsTarget = 30,
            has60FpsPatch = false,
            coverAccentHex = 0xFFDC2626
        ),
        XboxGame(
            id = "rdr",
            titleId = "5454082B",
            titleName = "Red Dead Redemption",
            releaseYear = "2010",
            genre = "Open World / Western",
            sizeGb = 7.5,
            format = GameFormat.GOD,
            compatibility = CompatibilityRating.IN_GAME,
            maliCompatibilityNotes = "Requiere Dimensity Core Pinning y 0.75x resolution scale para mantener 30 FPS.",
            recommendedFpsTarget = 30,
            has60FpsPatch = true,
            coverAccentHex = 0xFFD97706
        ),
        XboxGame(
            id = "sonic_gen",
            titleId = "53450849",
            titleName = "Sonic Generations",
            releaseYear = "2011",
            genre = "Platformer / Speed",
            sizeGb = 7.9,
            format = GameFormat.XEX,
            compatibility = CompatibilityRating.PLAYABLE,
            maliCompatibilityNotes = "60 FPS nativo con parche activado. El compilador asíncrono elimina tirones.",
            recommendedFpsTarget = 60,
            has60FpsPatch = true,
            coverAccentHex = 0xFF2563EB
        ),
        XboxGame(
            id = "skate3",
            titleId = "454108E6",
            titleName = "Skate 3",
            releaseYear = "2010",
            genre = "Sports / Extreme",
            sizeGb = 6.2,
            format = GameFormat.ISO,
            compatibility = CompatibilityRating.PLAYABLE,
            maliCompatibilityNotes = "Perfecto en Mali G715. No presenta fallos gráficos en modo TBDR.",
            recommendedFpsTarget = 60,
            has60FpsPatch = true,
            coverAccentHex = 0xFF4F46E5
        ),
        XboxGame(
            id = "forza4",
            titleId = "4D530910",
            titleName = "Forza Motorsport 4",
            releaseYear = "2011",
            genre = "Racing Simulation",
            sizeGb = 7.8,
            format = GameFormat.ZAR,
            compatibility = CompatibilityRating.GOOD,
            maliCompatibilityNotes = "Activar resolución 1.0x y FSR CAS sharpening para máxima nitidez en pistas.",
            recommendedFpsTarget = 60,
            has60FpsPatch = true,
            coverAccentHex = 0xFFEA580C
        )
    )
}
