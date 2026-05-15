package re.melchior.saviomobile.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Splash : Screen("splash")

    object Welcome : Screen("welcome")

    object Register : Screen("register")

    object Onboarding : Screen("onboarding")

    object Login : Screen("login")

    object SelectSociete : Screen("select_societe/{registerFlow}") {
        fun createRoute(registerFlow: Boolean = false) = "select_societe/$registerFlow"
    }

    object Tournee : Screen("tournee")

    object CreateClient : Screen("create_client")

    object CreateOfflineIntervention : Screen("create_offline_intervention")

    object PendingOfflineInterventions : Screen("pending_offline_interventions")

    object InterventionDetail : Screen("intervention/{interventionId}") {
        fun createRoute(interventionId: String) = "intervention/$interventionId"
    }

    object Invoice : Screen("invoice/{interventionId}") {
        fun createRoute(interventionId: String) = "invoice/$interventionId"
    }

    object ClotureRapport : Screen("intervention/{interventionId}/cloture/rapport") {
        fun createRoute(interventionId: String) = "intervention/$interventionId/cloture/rapport"
    }

    object ClotureSignature :
        Screen("intervention/{interventionId}/cloture/signature/{preselectedActualTypeKeys}") {
        fun createRoute(interventionId: String, preselectedActualTypeKeys: String = "_") =
            "intervention/$interventionId/cloture/signature/${
                Uri.encode(preselectedActualTypeKeys, "UTF-8")
            }"
    }

    object InterventionActive : Screen("intervention/{interventionId}/active") {
        fun createRoute(interventionId: String) = "intervention/$interventionId/active"
    }

    object ClientDetail : Screen("client/{customerId}") {
        fun createRoute(customerId: String) = "client/$customerId"
    }

    object EquipementDetail : Screen("equipement/{interventionId}/{equipmentId}") {
        fun createRoute(interventionId: String, equipmentId: String) =
            "equipement/$interventionId/$equipmentId"
    }

    object CerfaFroid : Screen("cerfa_froid/{interventionId}/{equipmentId}") {
        fun createRoute(interventionId: String, equipmentId: String) =
            "cerfa_froid/$interventionId/$equipmentId"
    }

    object CerfaPdf : Screen("cerfa_pdf/{interventionId}/{equipmentId}") {
        fun createRoute(interventionId: String, equipmentId: String) =
            "cerfa_pdf/$interventionId/$equipmentId"
    }

    object AttestationVe : Screen(
        "attestation-ve/{interventionId}/{equipmentOrder}/{type}",
    ) {
        fun createRoute(
            interventionId: String,
            equipmentOrder: Int,
            type: String,
        ) = "attestation-ve/$interventionId/$equipmentOrder/$type"
    }

    object Photos : Screen(
        "intervention/{interventionId}/photos/{unitId}/{customerId}",
    ) {
        fun createRoute(
            interventionId: String,
            unitId: String,
            customerId: String,
        ) = "intervention/$interventionId/photos/$unitId/$customerId"
    }

    object Camera : Screen(
        "intervention/{interventionId}/camera/{unitId}/{customerId}",
    ) {
        fun createRoute(
            interventionId: String,
            unitId: String,
            customerId: String,
        ) = "intervention/$interventionId/camera/$unitId/$customerId"
    }

    object CatalogSearch : Screen(
        "catalog_search/{interventionId}/{unitId}?parentEquipmentId={parentEquipmentId}&existingEquipmentId={existingEquipmentId}",
    ) {
        fun createRoute(
            interventionId: String,
            unitId: String,
            parentEquipmentId: String? = null,
            existingEquipmentId: String? = null,
        ): String {
            val base = "catalog_search/$interventionId/$unitId"
            val parts = mutableListOf<String>()
            if (!parentEquipmentId.isNullOrBlank()) {
                parts += "parentEquipmentId=$parentEquipmentId"
            }
            if (!existingEquipmentId.isNullOrBlank()) {
                parts += "existingEquipmentId=$existingEquipmentId"
            }
            return if (parts.isEmpty()) base else "$base?${parts.joinToString("&")}"
        }
    }

    object EquipmentForm : Screen(
        "equipment_form/{interventionId}/{unitId}?catalogEquipmentId={catalogEquipmentId}&existingEquipmentId={existingEquipmentId}&parentEquipmentId={parentEquipmentId}",
    ) {
        fun createRoute(
            interventionId: String,
            unitId: String,
            catalogEquipmentId: String? = null,
            existingEquipmentId: String? = null,
            parentEquipmentId: String? = null,
        ) = "equipment_form/$interventionId/$unitId?catalogEquipmentId=${catalogEquipmentId ?: ""}&existingEquipmentId=${existingEquipmentId ?: ""}&parentEquipmentId=${parentEquipmentId ?: ""}"
    }

    object Measure : Screen("measure/{interventionId}/{equipmentOrder}") {
        fun createRoute(
            interventionId: String,
            equipmentOrder: Int,
        ) = "measure/$interventionId/$equipmentOrder"
    }

    object PacMeasure : Screen("pac_measures/{interventionId}/{equipmentOrder}") {
        fun createRoute(
            interventionId: String,
            equipmentOrder: Int,
        ) = "pac_measures/$interventionId/$equipmentOrder"
    }

    /** Deep link + navigation interne : jeton d’activation compte (URL-encoded). */
    object Activation : Screen("activation/{token}") {
        fun createRoute(token: String) =
            "activation/${Uri.encode(token, "UTF-8")}"
    }
}
