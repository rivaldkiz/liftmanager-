package services

import javax.inject._

import repositories.EtageRepository
import repositories.BatimentRepository

import models.Etage

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EtageService @Inject() (
    etageRepository: EtageRepository,
    batimentRepository: BatimentRepository
)(implicit ec: ExecutionContext) {

  // 1. Créer un étage
  def creer(etage: Etage): Future[Either[String, Long]] = {

    if (etage.idBatiment <= 0) {
      Future.successful(Left("Identifiant du bâtiment invalide."))
    } else if (etage.numero < 0) {
      Future.successful(Left("Le numéro de l'étage ne peut pas être négatif."))
    } else {
      batimentRepository.findById(etage.idBatiment).flatMap {

        case None =>
          Future.successful(Left("Le bâtiment indiqué n'existe pas."))

        case Some(_) =>
          etageRepository
            .existsByNumero(
              etage.idBatiment,
              etage.numero
            )
            .flatMap {

              case true =>
                Future.successful(
                  Left("Ce numéro d'étage existe déjà dans ce bâtiment.")
                )

              case false =>
                etageRepository
                  .create(etage.copy(idEtage = None))
                  .map(id => Right(id))
            }
      }
    }
  }

  // 2. Rechercher un étage par identifiant
  def rechercherParId(id: Long): Future[Option[Etage]] = {

    if (id <= 0) Future.successful(None)
    else etageRepository.findById(id)
  }

  // 3. Lister tous les étages
  def listerTous(): Future[Seq[Etage]] = {
    etageRepository.findAll()
  }

  // 4. Lister les étages d'un bâtiment
  def listerParBatiment(
      idBatiment: Long
  ): Future[Either[String, Seq[Etage]]] = {

    if (idBatiment <= 0) {
      Future.successful(Left("Identifiant du bâtiment invalide."))
    } else {
      batimentRepository.findById(idBatiment).flatMap {

        case None =>
          Future.successful(Left("Bâtiment introuvable."))

        case Some(_) =>
          etageRepository
            .findByBatiment(idBatiment)
            .map(etages => Right(etages))
      }
    }
  }

  // 5. Modifier un étage
  def modifier(etage: Etage): Future[Either[String, Boolean]] = {

    val id = etage.idEtage.getOrElse(0L)

    if (id <= 0) {
      Future.successful(Left("Identifiant de l'étage invalide."))
    } else if (etage.idBatiment <= 0) {
      Future.successful(Left("Identifiant du bâtiment invalide."))
    } else if (etage.numero < 0) {
      Future.successful(Left("Le numéro de l'étage ne peut pas être négatif."))
    } else {
      etageRepository.findById(id).flatMap {

        case None =>
          Future.successful(Left("Étage introuvable."))

        case Some(existant) =>
          batimentRepository.findById(etage.idBatiment).flatMap {

            case None =>
              Future.successful(Left("Le bâtiment indiqué n'existe pas."))

            case Some(_) =>
              etageRepository
                .findByNumero(
                  etage.idBatiment,
                  etage.numero
                )
                .flatMap {

                  case Some(autre) if autre.idEtage != etage.idEtage =>
                    Future.successful(
                      Left("Ce numéro est déjà utilisé dans ce bâtiment.")
                    )

                  case _ =>
                    etageRepository
                      .update(etage)
                      .map(resultat => Right(resultat))
                }
          }
      }
    }
  }

  // 6. Supprimer un étage
  def supprimer(id: Long): Future[Either[String, Boolean]] = {

    if (id <= 0) {
      Future.successful(Left("Identifiant invalide."))
    } else {
      etageRepository.findById(id).flatMap {

        case None =>
          Future.successful(Left("Étage introuvable."))

        case Some(_) =>
          etageRepository
            .delete(id)
            .map(resultat => Right(resultat))
      }
    }
  }

  // 7. Rechercher par numéro dans un bâtiment
  def rechercherParNumero(
      idBatiment: Long,
      numero: Int
  ): Future[Option[Etage]] = {

    if (idBatiment <= 0 || numero < 0) {
      Future.successful(None)
    } else {
      etageRepository.findByNumero(idBatiment, numero)
    }
  }

  // 8. Compter les étages d'un bâtiment
  def compterParBatiment(
      idBatiment: Long
  ): Future[Int] = {

    if (idBatiment <= 0) Future.successful(0)
    else etageRepository.countByBatiment(idBatiment)
  }
}
