package re.savio.mobile.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "attestation_ve",
    primaryKeys = ["interventionId", "equipmentOrder"],
)
data class AttestationVeEntity(
    val interventionId: String,
    val equipmentOrder: Int,
    val id: String,
    val type: String,

    val appareilMesure: String = "",
    val appareilMesureTension: String = "",
    val appareilMesureGenerateur: String = "",
    val defautsCorriges: String = "",
    val recommandationUsage: String = "",
    val recommandationAmeliorations: String = "",
    val recommandationRemplacement: String = "",
    val commentaire: String = "",
    val nomPersonnePresente: String = "",
    val remarquesHydraulique: String = "",
    val remarquesRegulation: String = "",
    val remarquesGenerateur: String = "",

    val co: String = "",
    val coConduitPpm: Double? = null,
    val tempFumees: String = "",
    val tempAmbiante: String = "",
    val co2Fumees: String = "",
    val o2Fumees: String = "",
    val rendementEvalue: String = "",
    val noxEmissions: String = "",
    val classeEnergetique: String = "",

    val indiceNoircissement: String = "",
    val pressionGicleur: String = "",

    val emissionsPoussieres: String = "",
    val emissionsCov: String = "",

    val tExterieurChauf: String = "",
    val tExterieurRefroid: String = "",
    val tInterieurChauf: String = "",
    val tInterieurRefroid: String = "",
    val tensionStatique: String = "",
    val tensionDynamique: String = "",
    val fluideRef: String = "",
    val chargeTotale: String = "",
    val pressionBp: String = "",
    val pressionHp: String = "",

    val bruleurMarque: String? = null,
    val bruleurModele: String? = null,
    val bruleurSerialNumber: String? = null,
    val bruleurCommissioningDate: String? = null,
    val bruleurPuissanceKw: Double? = null,
    val bruleurEquipmentOrder: Int? = null,
    val linkedEquipmentOrders: String = "",

    val isDirty: Boolean = true,
    val updatedAt: String = "",
)
