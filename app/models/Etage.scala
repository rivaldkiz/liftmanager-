package models

import models.Enums

import play.api.libs.json._

case class Etage(
    idEtage: Option[Long],
    idBatiment: Long,
    numero: Int,
    nom: Option[String]
)

object Etage {
  implicit val format: OFormat[Etage] = Json.format[Etage]
}
