package re.savio.mobile.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cold_measures",
    indices = [
        Index(value = ["interventionId", "equipmentId"], unique = true),
    ],
)
data class ColdMeasureEntity(
    @PrimaryKey val id: String,
    val interventionId: String,
    val equipmentId: String,

    val frigo: String = "",
    val charg: String = "",
    val tonnage: String = "",

    val minter1: String = "",
    val minter2: String = "",
    val minter3: String = "",
    val minter4: String = "",
    val minter5: String = "",
    val minter6: String = "",
    val minter7: String = "",
    val minter8: String = "",
    val minteraut: String = "",

    val obsern1: String = "",
    val obsern2: String = "",

    val detm1: String = "",
    val dett1: String = "",
    val detd1: String = "",

    val autofuite: String = "",
    val qtefri: String = "",
    val qtefri2: String = "",
    val qtefri3: String = "",
    val freqs1: String = "",
    val freqs2: String = "",
    val freqs3: String = "",
    val freqa1: String = "",
    val freqa2: String = "",
    val freqa3: String = "",
    val pasfuite: String = "",
    val fuiteloc1: String = "",
    val fuiterep1: String = "",
    val fuiteloc2: String = "",
    val fuiterep2: String = "",
    val fuiteloc3: String = "",
    val fuiterep3: String = "",

    val fluidrein: String = "",
    val fluidecv: String = "",
    val fluidecr: String = "",
    val fluidecrg: String = "",
    val fluidrecup: String = "",
    val fluidert: String = "",
    val fluideru: String = "",
    val fluiderc: String = "",
    val supplyContainerId: String = "",
    val recoveryContainerId: String = "",

    val un1078a: String = "",
    val un1078b: String = "",
    val nomdec: String = "",
    val adres1dec: String = "",
    val adres2dec: String = "",
    val villedec: String = "",
    val nomtrans: String = "",
    val adres1trans: String = "",
    val adres2trans: String = "",
    val villetrans: String = "",

    val fluidobs: String = "",
    val fluidobs2: String = "",

    val bordeqte: String = "",
    val bordetrans: String = "",
    val instatrait: String = "",
    val coderd: String = "",
    val qterecep: String = "",
    val frigo2: String = "",
    val bsff: String = "",
    val un3161a: String = "",
    val un3161b: String = "",

    @ColumnInfo(name = "updated_at")
    val updatedAt: String = "",
    @ColumnInfo(name = "is_dirty")
    val isDirty: Boolean = true,
)
