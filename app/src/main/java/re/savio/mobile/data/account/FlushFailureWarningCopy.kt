package re.savio.mobile.data.account

import re.savio.mobile.data.repository.UnsyncedLocalWorkSummary

/**
 * User-facing copy for flush-failure override (legacy sync-before-handover tone).
 */
object FlushFailureWarningCopy {

    fun build(
        unsynced: UnsyncedLocalWorkSummary,
        isLogout: Boolean,
    ): String {
        val action =
            if (isLogout) {
                "vous déconnecter"
            } else {
                "changer de compte"
            }
        val countLine =
            when {
                unsynced.interventionLikeCount > 0 -> {
                    val n = unsynced.interventionLikeCount
                    val label = if (n == 1) "intervention non synchronisée" else "interventions non synchronisées"
                    "$n $label"
                }
                unsynced.totalItems > 0 -> {
                    val n = unsynced.totalItems
                    val label = if (n == 1) "élément non synchronisé" else "éléments non synchronisés"
                    "$n $label"
                }
                else -> "des données non synchronisées"
            }
        return buildString {
            append("La synchronisation n'a pas abouti. ")
            append("Assurez-vous d'avoir synchronisé vos données avant de continuer. ")
            append("Il reste $countLine. ")
            append("Si vous continuez pour $action, les données non synchronisées seront perdues définitivement.")
        }
    }
}
