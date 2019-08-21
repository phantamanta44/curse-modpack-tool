package xyz.phanta.cmt.util

enum class License(val fullName: String, val key: String, val perms: Permission) {
    ALL_RIGHTS_RESERVED("All Rights Reserved", "All Rights Rese...", Permission.UNKNOWN),
    PUBLIC_DOMAIN("Public Domain", "Public Domain", Permission.IMPLICIT),
    GPL_V3("GPLv3", "GNU General Pub...", Permission.IMPLICIT),
    LGPL_V3("LGPLv3", "GNU Lesser Gene...", Permission.IMPLICIT),
    APACHE("Apache", "Apache License ...", Permission.IMPLICIT),
    CDDL("CDDL", "Common Developm...", Permission.IMPLICIT),
    MIT("MIT", "MIT License", Permission.IMPLICIT),
    MOZILLA("MPL", "Mozilla Public ...", Permission.IMPLICIT),
    CREATIVE_COMMONS("CC", "Creative Common...", Permission.IMPLICIT),
    UNKNOWN("Unknown", "", Permission.UNKNOWN);

    companion object {
        val mapping: Map<String, License> = values().associateBy { it.key.toLowerCase() }

        fun parse(name: String): License = mapping[name.toLowerCase()] ?: UNKNOWN
    }
}
