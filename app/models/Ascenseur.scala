package models
import models.Enums
import play.api.libs.json._

case class Ascenseur(
    idAscenseur: Option[Long],
    idBatiment: Long,
    idSimulation: Option[Long],
    nom: String,
    capacitePersonnes: Int,
    chargeMax: BigDecimal,
    position: Int = 0,
    direction: String = Enums.DirectionAscenseur.Arret,
    etat: String = Enums.EtatAscenseur.Arret,
    isVirtual: Boolean = false
)

object Ascenseur {
  implicit val format: OFormat[Ascenseur] = Json.format[Ascenseur]
}
