package re.melchior.saviomobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "pac_measures",
    primaryKeys = ["interventionId", "equipmentOrder"],
)
data class PacMeasureEntity(
    val interventionId: String,
    val equipmentOrder: Int,

    val pacVentilation: String = "",
    val pacNetail: String = "",
    val pacVerail: String = "",
    val pacFiltre: String = "",
    val pacFuite: String = "",
    val pacEvac: String = "",
    val pacPression1: String = "",
    val pacPression2: String = "",
    val pacGlycol1: String = "",
    val pacGlycol2: String = "",

    val pacTenStat: String = "",
    val pacTenDyna: String = "",
    val pacIntensite: String = "",
    val pacResserage1: String = "",
    val pacResserage2: String = "",

    val pacInterieure: String = "",
    val pacExterieure: String = "",
    val pacDepart: String = "",
    val pacRetour: String = "",
    val pacDeltaT: String = "",
    val pacHiver: String = "",
    val pacAppoint: String = "",
    val pacConfort: String = "",
    val pacNonChauf: String = "",
    val pacEcsConsigne: String = "",
    val pacEcs: String = "",
    val pacManometreBp: String = "",
    val pacManometreHp: String = "",

    val pacDegivrage: String = "",
    val pacInversion: String = "",
    val pacHFonct: String = "",
    val pacHComp1: String = "",
    val pacHVenti: String = "",
    val pacNbDemarr: String = "",
    val pacHAppoint1: String = "",
    val pacHAppoint2: String = "",

    val pacAlarme1: String = "",
    val pacAlarme2: String = "",
    val pacBlocage1: String = "",
    val pacBlocage2: String = "",
    val pacReleve: String = "",
    val pacRem1: String = "",

    @ColumnInfo(name = "updated_at")
    val updatedAt: String = "",

    @ColumnInfo(name = "is_dirty")
    val isDirty: Boolean = false,
)
