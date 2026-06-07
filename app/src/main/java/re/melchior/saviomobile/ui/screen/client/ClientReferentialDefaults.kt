package re.melchior.saviomobile.ui.screen.client

import re.melchior.saviomobile.data.local.entity.CivilityOptionEntity
import re.melchior.saviomobile.data.local.entity.UnitTypeEntity

object ClientReferentialDefaults {
    val civilities: List<CivilityOptionEntity> =
        listOf(
            CivilityOptionEntity("M", "M."),
            CivilityOptionEntity("Mme", "Mme"),
            CivilityOptionEntity("M. and Mme", "M. et Mme"),
            CivilityOptionEntity("SARL", "SARL"),
            CivilityOptionEntity("EURL", "EURL"),
            CivilityOptionEntity("SCI", "SCI"),
            CivilityOptionEntity("SAS", "SAS"),
            CivilityOptionEntity("SASU", "SASU"),
            CivilityOptionEntity("Autre", "Autre"),
        )

    val unitTypes: List<UnitTypeEntity> =
        listOf(
            UnitTypeEntity("house", "Maison individuelle", "individual"),
            UnitTypeEntity("townhouse", "Maison de ville", "individual"),
            UnitTypeEntity("villa", "Villa", "individual"),
            UnitTypeEntity("bungalow", "Bungalow", "individual"),
            UnitTypeEntity("chalet", "Chalet", "individual"),
            UnitTypeEntity("farmhouse", "Ferme / Corps de ferme", "individual"),
            UnitTypeEntity("manor", "Manoir / Château", "individual"),
            UnitTypeEntity("apartment", "Appartement", "individual"),
            UnitTypeEntity("studio", "Studio", "individual"),
            UnitTypeEntity("duplex", "Duplex", "individual"),
            UnitTypeEntity("triplex", "Triplex", "individual"),
            UnitTypeEntity("loft", "Loft", "individual"),
            UnitTypeEntity("penthouse", "Penthouse", "individual"),
            UnitTypeEntity("room", "Chambre", "individual"),
            UnitTypeEntity("gite", "Gîte", "individual"),
            UnitTypeEntity("mobile_home", "Mobil-home", "individual"),
            UnitTypeEntity("building", "Immeuble", "collective"),
            UnitTypeEntity("residence", "Résidence", "collective"),
            UnitTypeEntity("copro", "Copropriété", "collective"),
            UnitTypeEntity("hlm", "HLM / Logement social", "collective"),
            UnitTypeEntity("ehpad", "EHPAD / Résidence seniors", "collective"),
            UnitTypeEntity("student_res", "Résidence étudiante", "collective"),
            UnitTypeEntity("tourism_res", "Résidence de tourisme", "collective"),
            UnitTypeEntity("office", "Bureau", "individual"),
            UnitTypeEntity("commercial", "Local commercial", "individual"),
            UnitTypeEntity("warehouse", "Entrepôt / Hangar", "individual"),
            UnitTypeEntity("workshop", "Atelier", "individual"),
            UnitTypeEntity("restaurant", "Restaurant / Hôtel", "individual"),
            UnitTypeEntity("school", "École / Établissement scolaire", "collective"),
            UnitTypeEntity("sport", "Salle de sport / Complexe sportif", "collective"),
            UnitTypeEntity("other", "Autre", "individual"),
        )

    private val floorUnitTypeCodes =
        setOf("apartment", "studio", "duplex", "triplex", "loft", "penthouse", "room")

    fun showsFloorField(category: String, unitTypeCode: String): Boolean =
        category == "individual" && unitTypeCode in floorUnitTypeCodes
}

data class CivilityOptionUi(
    val code: String,
    val label: String,
)

data class UnitTypeOptionUi(
    val code: String,
    val label: String,
    val category: String,
)

fun CivilityOptionEntity.toUi(): CivilityOptionUi = CivilityOptionUi(code = code, label = label)

fun UnitTypeEntity.toUi(): UnitTypeOptionUi =
    UnitTypeOptionUi(code = code, label = label, category = category)

fun UnitTypeOptionUi.showsFloorField(): Boolean =
    ClientReferentialDefaults.showsFloorField(category, code)
