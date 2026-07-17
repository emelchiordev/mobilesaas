package re.savio.mobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "measures",
    primaryKeys = ["interventionId", "equipmentOrder"],
)
data class MeasureEntity(
    val interventionId: String,
    val equipmentOrder: Int,

    val co: Double? = null,
    val coamb: Double? = null,
    val co2: Double? = null,
    val o2: Double? = null,
    val tair: Int? = null,
    val temfu: Double? = null,
    val rend: Double? = null,
    val nox: Double? = null,
    val eta: Double? = null,

    val thpa: Double? = null,
    val no: Double? = null,
    val no2: Double? = null,
    val o2ven: Double? = null,
    val condilu: Double? = null,
    val tgaz: Int? = null,
    val ta: Double? = null,

    val pregas: Double? = null,
    val prega: Double? = null,
    val pregn: Double? = null,
    val pregm: Double? = null,
    val puisgaz: Double? = null,
    val debga: Double? = null,

    val temec: Double? = null,
    val temef: Double? = null,
    val delta: Double? = null,
    val debio: Double? = null,

    val debfuel: Double? = null,
    val prefp: Double? = null,
    val puisfuel: Double? = null,
    val pulve: Double? = null,

    val spot: Int? = null,
    val testdsc: String? = null,
    val remplacond: String? = null,
    val templagigleur: String? = null,
    val remplapoly: String? = null,
    val etaventil: String? = null,
    val ctranode: String? = null,
    val ctrextvmc: String? = null,

    val suie1: Int? = null,
    val suie2: Int? = null,
    val suie3: Int? = null,
    val residhuil: Int? = null,
    val opaci: Double? = null,
    val ionis: Double? = null,
    val pgevg: Double? = null,
    val pgepg: Double? = null,
    val depre: Double? = null,
    val depr2: Double? = null,
    val gican: Double? = null,
    val gicle: Double? = null,
    val pabs: Double? = null,
    val perte: Double? = null,
    val ppm: Int? = null,

    val obser: String? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String = "",

    @ColumnInfo(name = "is_dirty")
    val isDirty: Boolean = true,
)
