package re.melchior.saviomobile.data.remote.dto

import com.google.gson.annotations.SerializedName
import re.melchior.saviomobile.data.local.objectbox.TenantArticleBox

data class ArticlesForMobileResponseDto(
    @SerializedName("articles") val articles: List<TenantArticleDto>,
    @SerializedName("total") val total: Int,
    @SerializedName("syncedAt") val syncedAt: String,
)

data class TenantArticleDto(
    @SerializedName("id") val id: String,
    @SerializedName("reference") val reference: String,
    @SerializedName("referenceInterne") val referenceInterne: String?,
    @SerializedName("codeBarre") val codeBarre: String?,
    @SerializedName("designation") val designation: String,
    @SerializedName("designationPersonnalisee") val designationPersonnalisee: String?,
    @SerializedName("marque") val marque: String?,
    @SerializedName("famille") val famille: String?,
    @SerializedName("prixHt") val prixHt: Double,
    @SerializedName("remise") val remise: Double?,
    @SerializedName("prixNet") val prixNet: Double,
    @SerializedName("unite") val unite: String,
    @SerializedName("actif") val actif: Boolean,
    @SerializedName("updatedAt") val updatedAt: String,
)

fun TenantArticleDto.toBoxEntity(existing: TenantArticleBox? = null): TenantArticleBox {
    val target = existing ?: TenantArticleBox()
    target.id = id
    target.reference = reference
    target.referenceInterne = referenceInterne
    target.codeBarre = codeBarre
    target.designation = designation
    target.designationPersonnalisee = designationPersonnalisee
    target.marque = marque
    target.famille = famille
    target.prixHt = prixHt
    target.remise = remise
    target.prixNet = prixNet
    target.unite = unite
    target.actif = actif
    target.updatedAt = updatedAt
    return target
}

fun TenantArticleBox.toPrestationDto(): PrestationDto {
    val label = designationPersonnalisee?.takeIf { it.isNotBlank() } ?: designation
    return PrestationDto(
        type = "article",
        reference = reference,
        label = label,
        vatRate = 0.0,
        unitPriceHt = prixNet,
        referenceInterne = referenceInterne,
        marque = marque,
        famille = famille,
    )
}
