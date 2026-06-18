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

    object Main : Screen("main")

    /** @deprecated Utiliser [Main] — conservé pour deep links internes. */
    object Tournee : Screen("tournee")

    object CreateClient : Screen("create_client")

    object CreateIntervention :
        Screen(
            "create_intervention?unitId={unitId}&customerId={customerId}" +
                "&displayName={displayName}&addressLine={addressLine}&defaultDate={defaultDate}",
        ) {
        fun createRoute(
            unitId: String = "",
            customerId: String = "",
            displayName: String = "",
            addressLine: String = "",
            defaultDate: String = "",
        ): String =
            "create_intervention?" +
                "unitId=${Uri.encode(unitId)}&" +
                "customerId=${Uri.encode(customerId)}&" +
                "displayName=${Uri.encode(displayName)}&" +
                "addressLine=${Uri.encode(addressLine)}&" +
                "defaultDate=${Uri.encode(defaultDate)}"
    }

    object CreateOfflineIntervention :
        Screen("create_offline_intervention?defaultDate={defaultDate}") {
        fun createRoute(defaultDate: String = ""): String =
            "create_offline_intervention?defaultDate=${Uri.encode(defaultDate)}"
    }

    object PendingOfflineInterventions : Screen("pending_offline_interventions")

    object InterventionDetail : Screen("intervention/{interventionId}") {
        fun createRoute(interventionId: String) = "intervention/$interventionId"
    }

    object Invoice : Screen("invoice/{interventionId}") {
        fun createRoute(interventionId: String) = "invoice/$interventionId"
    }

    object DevisSignature : Screen("invoice/{interventionId}/devis-signature") {
        fun createRoute(interventionId: String) = "invoice/$interventionId/devis-signature"
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

    object ClientDetail :
        Screen(
            "client/{customerId}?unitId={unitId}&displayName={displayName}&addressLine={addressLine}&contextInterventionId={contextInterventionId}",
        ) {
        fun createRoute(
            customerId: String,
            unitId: String = "",
            displayName: String = "",
            addressLine: String = "",
            contextInterventionId: String = "",
        ): String =
            "client/${Uri.encode(customerId)}?" +
                "unitId=${Uri.encode(unitId)}&" +
                "displayName=${Uri.encode(displayName)}&" +
                "addressLine=${Uri.encode(addressLine)}&" +
                "contextInterventionId=${Uri.encode(contextInterventionId)}"

        fun createRouteFromIntervention(
            customerId: String,
            interventionId: String,
            unitId: String = "",
            displayName: String = "",
            addressLine: String = "",
        ): String =
            createRoute(
                customerId = customerId,
                unitId = unitId,
                displayName = displayName,
                addressLine = addressLine,
                contextInterventionId = interventionId,
            )
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

    object PlateScan : Screen("plate_scan/{interventionId}/{unitId}") {
        fun createRoute(interventionId: String, unitId: String) =
            "plate_scan/$interventionId/$unitId"
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
