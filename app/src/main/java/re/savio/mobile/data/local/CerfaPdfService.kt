package re.savio.mobile.data.local

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import re.savio.mobile.data.local.entity.ColdMeasureEntity
import re.savio.mobile.data.local.entity.EquipmentEntity
import re.savio.mobile.data.local.entity.InterventionEntity

@Singleton
class CerfaPdfService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TEMPLATE = "cerfa15497_04.pdf"
    }

    suspend fun generate(
        measure: ColdMeasureEntity,
        intervention: InterventionEntity,
        equipment: EquipmentEntity,
        technicienNom: String = "",
        attestationCapacite: String = "",
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            PDFBoxResourceLoader.init(context)

            val outFile = File(
                context.filesDir,
                "cerfa_${intervention.id}_${equipment.id}.pdf",
            )
            context.assets.open(TEMPLATE).use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val document = PDDocument.load(outFile)
            try {
                val page = document.getPage(0)
                val font = PDType1Font.TIMES_BOLD
                val fontSize = 8f

                fun String?.clean(): String = (this ?: "")
                    .replace("\t", " ")
                    .replace(Regex("[\\x00-\\x1F]"), "")

                val acroForm = document.documentCatalog.acroForm

                val stream = PDPageContentStream(
                    document,
                    page,
                    PDPageContentStream.AppendMode.APPEND,
                    true,
                )
                try {
                    stream.setNonStrokingColor(0f, 0f, 0f)

                    fun drawText(text: String, x: Float, y: Float) {
                        if (text.isBlank()) return
                        stream.beginText()
                        stream.setFont(font, fontSize)
                        stream.newLineAtOffset(x, y)
                        stream.showText(text.clean())
                        stream.endText()
                    }

                    fun setText(field: String, value: String) {
                        try {
                            acroForm?.getField(field)?.setValue(value)
                        } catch (_: Exception) {
                        }
                    }

                    fun check(field: String) {
                        try {
                            acroForm?.getField(field)?.setValue("Yes")
                        } catch (_: Exception) {
                        }
                    }

                    setText(
                        "Detenteur",
                        "${intervention.customerFirstName ?: ""} " +
                            "${intervention.customerLastName ?: ""}\n" +
                            "${intervention.unitStreet}\n" +
                            "${intervention.unitPostalCode} " +
                            "${intervention.unitCity}",
                    )

                    if (attestationCapacite.isNotBlank()) {
                        setText("Attestation_no", attestationCapacite.clean())
                    }

                    val detenteurNom = listOfNotNull(
                        intervention.customerFirstName?.trim()?.takeIf { it.isNotEmpty() },
                        intervention.customerLastName?.trim()?.takeIf { it.isNotEmpty() },
                    ).joinToString(" ")
                    setText("Sign_Detenteur_Qualite", "Client")
                    if (detenteurNom.isNotBlank()) {
                        setText("Sign_Detenteur_Nom", detenteurNom)
                    }

                    val ficheNo = listOfNotNull(
                        intervention.number?.trim()?.takeIf { it.isNotEmpty() },
                        equipment.order?.toString()?.takeIf { it.isNotBlank() },
                    ).joinToString(" / ")
                    if (ficheNo.isNotBlank()) {
                        setText("Fiche_no", ficheNo)
                    }
                    drawText(
                        "${equipment.brand.clean()} ${equipment.model.clean()}",
                        125f,
                        655f,
                    )
                    drawText(measure.frigo.clean(), 500f, 673f)
                    drawText(measure.charg.clean(), 500f, 658f)
                    drawText(measure.tonnage.clean(), 483f, 643f)

                    if (measure.minter6.isNotBlank()) check("Case_Assemblage")
                    if (measure.minter1.isNotBlank()) check("Case_MiseService")
                    if (measure.minter4.isNotBlank()) check("Case_Modif")
                    if (measure.minter2.isNotBlank()) check("Case_Maintenance")
                    if (measure.minter3.isNotBlank()) check("Case_CtrlPerio")
                    if (measure.minter8.isNotBlank()) check("Case_CtrlNonPerio")
                    if (measure.minter5.isNotBlank()) check("Case_Demantel")
                    if (measure.minter7.isNotBlank()) check("Case_Autre")

                    val aut = measure.minteraut
                    val minterautText = aut.take(34).let { t ->
                        if (aut.length > 34) "$t..." else t
                    }
                    drawText(minterautText, 433f, 589f)

                    drawText(
                        "${measure.detm1.clean()} ${measure.dett1.clean()}",
                        220f,
                        560f,
                    )
                    val dateParts = (measure.detd1).split("/")
                    drawText(dateParts.getOrElse(0) { "--" }, 438f, 560f)
                    drawText(dateParts.getOrElse(1) { "--" }, 470f, 560f)
                    drawText(dateParts.getOrElse(2) { "----" }, 503f, 560f)

                    try {
                        val field = acroForm?.getField("Bouton_Oui")
                        when (measure.autofuite) {
                            "O" -> field?.setValue("1")
                            "N" -> field?.setValue("2")
                            else -> field?.setValue("Off")
                        }
                    } catch (_: Exception) {
                    }

                    when (measure.qtefri) {
                        "1" -> check("Case_HCFC_2")
                        "2" -> check("Case_HCFC_30")
                        "3" -> check("Case_HCFC_300")
                    }
                    when (measure.qtefri2) {
                        "1" -> check("Case_HFC_5")
                        "2" -> check("Case_HFC_50")
                        "3" -> check("Case_HFC_500")
                    }
                    when (measure.qtefri3) {
                        "1" -> check("Case_HFO_1")
                        "2" -> check("Case_HFO_10")
                        "3" -> check("Case_HFO_100")
                    }

                    if (measure.freqs1 == "O") check("Case_Sans_12m")
                    if (measure.freqs2 == "O") check("Case_Sans_6m")
                    if (measure.freqs3 == "O") check("Case_Sans_3m")

                    if (measure.freqa1 == "O") check("Case_Avec_24m")
                    if (measure.freqa2 == "O") check("Case_Avec_12m")
                    if (measure.freqa3 == "O") check("Case_Avec_6m")

                    when (measure.pasfuite) {
                        "O" -> check("Case_Fuite_Oui")
                        "N" -> check("Case_Fuite_Non")
                    }

                    if (measure.fuiteloc1.isNotBlank()) {
                        setText("Fuite_Loca_1", measure.fuiteloc1)
                    }
                    if (measure.fuiteloc2.isNotBlank()) {
                        setText("Fuite_Loca_2", measure.fuiteloc2)
                    }
                    if (measure.fuiteloc3.isNotBlank()) {
                        setText("Fuite_Loca_3", measure.fuiteloc3)
                    }

                    when (measure.fuiterep1) {
                        "F" -> check("Case_Rep_Fuite1_realisee")
                        "A" -> check("Case_Rep_Fuite1_AFaire")
                    }
                    when (measure.fuiterep2) {
                        "F" -> check("Case_Rep_Fuite2_realisee")
                        "A" -> check("Case_Rep_Fuite2_AFaire")
                    }
                    when (measure.fuiterep3) {
                        "F" -> check("Case_Rep_Fuite3_realisee")
                        "A" -> check("Case_Rep_Fuite3_AFaire")
                    }

                    if (measure.fluidrein.isNotBlank()) {
                        setText("11_Quantite", measure.fluidrein)
                    }
                    if (measure.fluidecv.isNotBlank()) {
                        setText("11_QA", measure.fluidecv)
                    }
                    if (measure.fluidecr.isNotBlank()) {
                        setText("11_QB", measure.fluidecr)
                    }
                    if (measure.fluidecrg.isNotBlank()) {
                        setText("11_QC", measure.fluidecrg)
                    }
                    if (measure.fluidert.isNotBlank()) {
                        setText("11_QD", measure.fluidert)
                    }
                    if (measure.fluideru.isNotBlank()) {
                        setText("11_QE", measure.fluideru)
                    }
                    if (measure.fluidrecup.isNotBlank()) {
                        setText("11_QDE", measure.fluidrecup)
                    }
                    if (measure.bsff.isNotBlank()) {
                        setText("11_BSFF", measure.bsff)
                    }
                    if (measure.frigo2.isNotBlank()) {
                        setText("11_Denom", measure.frigo2)
                    }
                    if (measure.fluiderc.isNotBlank()) {
                        setText("11_Contenant_ID", measure.fluiderc)
                    }

                    when (measure.un1078a) {
                        "O" -> check("Case_12_UN1078")
                        "N" -> check("Case_12_Autre140601")
                    }
                    when (measure.un3161a) {
                        "O" -> check("Case_12_UN3161")
                        "N" -> check("Case_12_Autre160504")
                    }
                    drawText(measure.un1078b.clean(), 490f, 269f)
                    drawText(measure.un3161b.clean(), 484f, 240f)

                    setText(
                        "13_Instal",
                        "${measure.nomdec.clean()} " +
                            "${measure.adres1dec.clean()} " +
                            "${measure.adres2dec.clean()} " +
                            "${measure.villedec.clean()}",
                    )

                    setText(
                        "14_Observations",
                        "${measure.fluidobs.clean()} ${measure.fluidobs2.clean()}",
                    )

                    setText("Sign_Operateur_Qualite", "Technicien")
                    if (technicienNom.isNotBlank()) {
                        setText("Sign_Operateur_Nom", technicienNom)
                    }

                    val signDate = intervention.completedAt
                        ?.take(10)
                        ?.let { iso ->
                            runCatching {
                                val parts = iso.split("-")
                                if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else null
                            }.getOrNull()
                        }
                        ?: java.time.LocalDate.now()
                            .format(
                                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                            )
                    setText("Sign_Operateur_Date", signDate)
                    setText("Sign_Detenteur_Date", signDate)

                    resolveLocalSignatureFile(intervention.techSignaturePath)?.let { sigFile ->
                        try {
                            val image = PDImageXObject.createFromFile(sigFile.absolutePath, document)
                            // Bloc signature bas de page (Nom / Qualité)
                            stream.drawImage(image, 175f, 42f, 130f, 28f)
                        } catch (_: Exception) {
                        }
                    }
                    resolveLocalSignatureFile(intervention.signaturePath)?.let { sigFile ->
                        try {
                            val image = PDImageXObject.createFromFile(sigFile.absolutePath, document)
                            stream.drawImage(image, 410f, 42f, 130f, 28f)
                        } catch (_: Exception) {
                        }
                    }
                } finally {
                    stream.close()
                }

                document.save(outFile)
                Result.success(outFile)
            } finally {
                document.close()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun resolveLocalSignatureFile(path: String?): File? {
        if (path.isNullOrBlank()) return null
        val normalized = path.removePrefix("file://")
        if (!normalized.startsWith("/")) return null
        val file = File(normalized)
        return file.takeIf { it.exists() && it.isFile }
    }
}
