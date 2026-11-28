package services

import javax.inject._
import repositories.{AscenseurRepository, BatimentRepository, EtageRepository}
import models.Ascenseur
import models.Enums

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AscenseurService @Inject() (
    ascenseurRepository: AscenseurRepository,
    batimentRepository: BatimentRepository,
    etageRepository: EtageRepository
)(implicit ec: ExecutionContext) {

  // --------------------------------------------------
  // 1. CREATION
  // --------------------------------------------------

  def creer(ascenseur: Ascenseur): Future[Either[String, Long]] = {

    if (ascenseur.idBatiment <= 0)
      return Future.successful(Left("Bâtiment invalide"))

    if (ascenseur.nom.trim.isEmpty)
      return Future.successful(Left("Le nom de l'ascenseur est obligatoire"))

    if (ascenseur.capacitePersonnes <= 0)
      return Future.successful(Left("La capacité doit être supérieure à zéro"))

    if (ascenseur.chargeMax <= 0)
      return Future.successful(
        Left("La charge maximale doit être supérieure à zéro")
      )

    if (ascenseur.position < 0)
      return Future.successful(Left("La position de l'ascenseur est invalide"))

    if (!Enums.DirectionAscenseur.values.contains(ascenseur.direction))
      return Future.successful(Left("Direction invalide"))

    if (!Enums.EtatAscenseur.values.contains(ascenseur.etat))
      return Future.successful(Left("État de l'ascenseur invalide"))

    // Un ascenseur virtuel doit obligatoirement appartenir à une simulation.
    if (ascenseur.isVirtual != ascenseur.idSimulation.isDefined)
      return Future.successful(
        Left("Configuration de simulation invalide")
      )

    for {
      batimentExiste <- batimentRepository.existsById(ascenseur.idBatiment)

      resultat <- {
        if (!batimentExiste) {
          Future.successful(Left("Le bâtiment sélectionné n'existe pas"))
        } else {
          etageRepository
            .findByNumero(
              ascenseur.idBatiment,
              ascenseur.position
            )
            .flatMap {
              case None =>
                Future.successful(
                  Left("L'étage initial n'existe pas dans ce bâtiment")
                )

              case Some(_) =>
                ascenseurRepository
                  .create(ascenseur)
                  .map(id => Right(id))
            }
        }
      }
    } yield resultat
  }

  // --------------------------------------------------
  // 2. CONSULTATION
  // --------------------------------------------------

  def rechercherParId(id: Long): Future[Option[Ascenseur]] = {
    ascenseurRepository.findById(id)
  }

  def listerTous(): Future[Seq[Ascenseur]] = {
    ascenseurRepository.findAll()
  }

  def listerParBatiment(idBatiment: Long): Future[Seq[Ascenseur]] = {
    ascenseurRepository.findByBatiment(idBatiment)
  }

  def listerReels(): Future[Seq[Ascenseur]] = {
    ascenseurRepository.findAllReels()
  }

  def listerParSimulation(idSimulation: Long): Future[Seq[Ascenseur]] = {
    ascenseurRepository.findBySimulation(idSimulation)
  }

  // --------------------------------------------------
  // 3. MODIFICATION DES CARACTERISTIQUES
  // --------------------------------------------------

  def modifier(
      id: Long,
      nom: String,
      capacitePersonnes: Int,
      chargeMax: BigDecimal
  ): Future[Either[String, Unit]] = {

    if (nom.trim.isEmpty)
      return Future.successful(Left("Le nom est obligatoire"))

    if (capacitePersonnes <= 0)
      return Future.successful(Left("La capacité doit être supérieure à zéro"))

    if (chargeMax <= 0)
      return Future.successful(
        Left("La charge maximale doit être supérieure à zéro")
      )

    ascenseurRepository.findById(id).flatMap {

      case None =>
        Future.successful(Left("Ascenseur introuvable"))

      case Some(existant) =>

        val ascenseurModifie = existant.copy(
          nom = nom.trim,
          capacitePersonnes = capacitePersonnes,
          chargeMax = chargeMax
        )

        ascenseurRepository.update(ascenseurModifie).map {
          case true =>
            Right(())

          case false =>
            Left("La modification de l'ascenseur a échoué")
        }
    }
  }

  // --------------------------------------------------
  // 4. CHANGEMENT DE POSITION ET DE DIRECTION
  // --------------------------------------------------

  def changerPosition(
      id: Long,
      nouvellePosition: Int,
      nouvelleDirection: String
  ): Future[Either[String, Unit]] = {

    if (nouvellePosition < 0)
      return Future.successful(Left("Position invalide"))

    if (!Enums.DirectionAscenseur.values.contains(nouvelleDirection))
      return Future.successful(Left("Direction invalide"))

    ascenseurRepository.findById(id).flatMap {

      case None =>
        Future.successful(Left("Ascenseur introuvable"))

      case Some(ascenseur) =>

        etageRepository
          .findByNumero(
            ascenseur.idBatiment,
            nouvellePosition
          )
          .flatMap {

            case None =>
              Future.successful(
                Left("L'étage demandé n'existe pas dans ce bâtiment")
              )

            case Some(_) =>
              ascenseurRepository
                .updatePosition(id, nouvellePosition, nouvelleDirection)
                .map {
                  case true =>
                    Right(())

                  case false =>
                    Left("Impossible de modifier la position")
                }
          }
    }
  }

  // --------------------------------------------------
  // 5. CHANGEMENT D'ETAT
  // --------------------------------------------------

  def changerEtat(
      id: Long,
      nouvelEtat: String
  ): Future[Either[String, Unit]] = {

    if (!Enums.EtatAscenseur.values.contains(nouvelEtat))
      return Future.successful(Left("État invalide"))

    ascenseurRepository.findById(id).flatMap {

      case None =>
        Future.successful(Left("Ascenseur introuvable"))

      case Some(_) =>
        ascenseurRepository.updateEtat(id, nouvelEtat).map {
          case true =>
            Right(())

          case false =>
            Left("Impossible de modifier l'état de l'ascenseur")
        }
    }
  }

  // --------------------------------------------------
  // 6. SUPPRESSION
  // --------------------------------------------------

  def supprimer(id: Long): Future[Either[String, Unit]] = {

    ascenseurRepository.findById(id).flatMap {

      case None =>
        Future.successful(Left("Ascenseur introuvable"))

      case Some(ascenseur) =>

        if (ascenseur.isVirtual) {
          Future.successful(
            Left("Un ascenseur virtuel doit être supprimé par la simulation")
          )
        } else if (
          ascenseur.etat == Enums.EtatAscenseur.Montee ||
          ascenseur.etat == Enums.EtatAscenseur.Descente ||
          ascenseur.etat == Enums.EtatAscenseur.Ouverture ||
          ascenseur.etat == Enums.EtatAscenseur.Fermeture
        ) {
          Future.successful(
            Left("Impossible de supprimer un ascenseur en fonctionnement")
          )
        } else {
          ascenseurRepository.delete(id).map {
            case true =>
              Right(())

            case false =>
              Left("La suppression de l'ascenseur a échoué")
          }
        }
    }
  }

  // --------------------------------------------------
  // 7. STATISTIQUES
  // --------------------------------------------------

  def compterParBatiment(idBatiment: Long): Future[Int] = {
    ascenseurRepository.countByBatiment(idBatiment)
  }

  def compterParSimulation(idSimulation: Long): Future[Int] = {
    ascenseurRepository.countBySimulation(idSimulation)
  }

}
