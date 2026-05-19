package re.melchior.saviomobile.data.local.objectbox

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique

@Entity
data class TenantArticleBox(
    @Id var boxId: Long = 0,
    @Unique var id: String = "",
    @Index var reference: String = "",
    var referenceInterne: String? = null,
    var codeBarre: String? = null,
    var designation: String = "",
    var designationPersonnalisee: String? = null,
    @Index var marque: String? = null,
    var famille: String? = null,
    var prixHt: Double = 0.0,
    var remise: Double? = null,
    var prixNet: Double = 0.0,
    var unite: String = "U",
    var actif: Boolean = true,
    var updatedAt: String = "",
)
