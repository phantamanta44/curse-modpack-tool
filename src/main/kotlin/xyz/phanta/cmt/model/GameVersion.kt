package xyz.phanta.cmt.model

enum class MajorGameVersion {
    V1_14, V1_13, V1_12, V1_11, V1_10, V1_9, V1_8, V1_7, V1_6, V1_5, V1_4, V1_3, V1_2, V1_1, V1_0
}

enum class GameVersion(val majorVersion: MajorGameVersion, val displayName: String) {
    V1_14_4(MajorGameVersion.V1_14, "1.14.4"),
    V1_14_3(MajorGameVersion.V1_14, "1.14.3"),
    V1_14_2(MajorGameVersion.V1_14, "1.14.2"),
    V1_14_1(MajorGameVersion.V1_14, "1.14.1"),
    V1_14(MajorGameVersion.V1_14, "1.14"),
    V1_14_SNAPSHOT(MajorGameVersion.V1_14, "1.14-Snapshot"),
    V1_13_2(MajorGameVersion.V1_13, "1.13.2"),
    V1_13_1(MajorGameVersion.V1_13, "1.13.1"),
    V1_13(MajorGameVersion.V1_13, "1.13"),
    V1_13_SNAPSHOT(MajorGameVersion.V1_13, "1.13-Snapshot"),
    V1_12_2(MajorGameVersion.V1_12, "1.12.2"),
    V1_12_1(MajorGameVersion.V1_12, "1.12.1"),
    V1_12(MajorGameVersion.V1_12, "1.12"),
    V1_12_SNAPSHOT(MajorGameVersion.V1_12, "1.12-Snapshot"),
    V1_11_2(MajorGameVersion.V1_11, "1.11.2"),
    V1_11_1(MajorGameVersion.V1_11, "1.11.1"),
    V1_11(MajorGameVersion.V1_11, "1.11"),
    V1_11_SNAPSHOT(MajorGameVersion.V1_11, "1.11-Snapshot"),
    V1_10_2(MajorGameVersion.V1_10, "1.10.2"),
    V1_10_1(MajorGameVersion.V1_10, "1.10.1"),
    V1_10(MajorGameVersion.V1_10, "1.10"),
    V1_10_SNAPSHOT(MajorGameVersion.V1_10, "1.10-Snapshot"),
    V1_9_4(MajorGameVersion.V1_9, "1.9.4"),
    V1_9_3(MajorGameVersion.V1_9, "1.9.3"),
    V1_9_2(MajorGameVersion.V1_9, "1.9.2"),
    V1_9_1(MajorGameVersion.V1_9, "1.9.1"),
    V1_9(MajorGameVersion.V1_9, "1.9"),
    V1_9_SNAPSHOT(MajorGameVersion.V1_9, "1.9-Snapshot"),
    V1_8_9(MajorGameVersion.V1_8, "1.8.9"),
    V1_8_8(MajorGameVersion.V1_8, "1.8.8"),
    V1_8_7(MajorGameVersion.V1_8, "1.8.7"),
    V1_8_6(MajorGameVersion.V1_8, "1.8.6"),
    V1_8_5(MajorGameVersion.V1_8, "1.8.5"),
    V1_8_4(MajorGameVersion.V1_8, "1.8.4"),
    V1_8_3(MajorGameVersion.V1_8, "1.8.3"),
    V1_8_2(MajorGameVersion.V1_8, "1.8.2"),
    V1_8_1(MajorGameVersion.V1_8, "1.8.1"),
    V1_8(MajorGameVersion.V1_8, "1.8"),
    V1_8_SNAPSHOT(MajorGameVersion.V1_8, "1.8-Snapshot"),
    V1_7_10(MajorGameVersion.V1_7, "1.7.10"),
    V1_7_9(MajorGameVersion.V1_7, "1.7.9"),
    V1_7_8(MajorGameVersion.V1_7, "1.7.8"),
    V1_7_7(MajorGameVersion.V1_7, "1.7.7"),
    V1_7_6(MajorGameVersion.V1_7, "1.7.6"),
    V1_7_5(MajorGameVersion.V1_7, "1.7.5"),
    V1_7_4(MajorGameVersion.V1_7, "1.7.4"),
    V1_7_2(MajorGameVersion.V1_7, "1.7.2"),
    V1_6_4(MajorGameVersion.V1_6, "1.6.4"),
    V1_6_2(MajorGameVersion.V1_6, "1.6.2"),
    V1_6_1(MajorGameVersion.V1_6, "1.6.1"),
    V1_5_2(MajorGameVersion.V1_5, "1.5.2"),
    V1_5_1(MajorGameVersion.V1_5, "1.5.1"),
    V1_5_0(MajorGameVersion.V1_5, "1.5.0"),
    V1_4_7(MajorGameVersion.V1_4, "1.4.7"),
    V1_4_6(MajorGameVersion.V1_4, "1.4.6"),
    V1_4_5(MajorGameVersion.V1_4, "1.4.5"),
    V1_4_4(MajorGameVersion.V1_4, "1.4.4"),
    V1_4_2(MajorGameVersion.V1_4, "1.4.2"),
    V1_3_2(MajorGameVersion.V1_3, "1.3.2"),
    V1_3_1(MajorGameVersion.V1_3, "1.3.1"),
    V1_2_5(MajorGameVersion.V1_2, "1.2.5"),
    V1_2_4(MajorGameVersion.V1_2, "1.2.4"),
    V1_2_3(MajorGameVersion.V1_2, "1.2.3"),
    V1_2_2(MajorGameVersion.V1_2, "1.2.2"),
    V1_2_1(MajorGameVersion.V1_2, "1.2.1"),
    V1_1(MajorGameVersion.V1_1, "1.1"),
    V1_0_0(MajorGameVersion.V1_0, "1.0.0");

    companion object {
        private val VERSION_MAP: Map<String, GameVersion> = values().associateBy { it.displayName }

        fun parse(name: String): GameVersion =
            parseGracefully(name) ?: throw IllegalArgumentException("Unknown game version: $name")

        fun parseGracefully(name: String): GameVersion? = VERSION_MAP[name]
    }

    fun isCompatibleWith(other: GameVersion): Boolean = majorVersion == other.majorVersion

    override fun toString(): String = displayName
}
