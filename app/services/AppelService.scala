package services

import javax.inject._

import repositories.{AppelRepository, EtageRepository, UtilisateurRepository}
import models.{Appel, Enums}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AppelService @Inject() (
    appelRepository: AppelRepository,
    etageRepository: EtageRepository,
    utilisateurRepository: UtilisateurRepository
)(implicit ec: ExecutionContext) {

  // --------------------------------------------------
  // 1. Déterminer automatiquement la direction
  // --------------------------------------------------

  private def determinerDirection(
      depart: Long,
      destination: Long
  ): String = {

    if (destination > depart)
      Enums.DirectionAppel.Montee
    else
      Enums.DirectionAppel.Descente
  }

  // --------------------------------------------------
  // 2. Créer un appel
  // --------------------------------------------------

  def creer(appel: Appel): Future[Either[String, Long]] = {

    // Vérifications élémentaires
    if (appel.idUser <= 0)
      return Future.successful(Left("Identifiant utilisateur invalide"))

    if (appel.depart <= 0 || appel.destination <= 0)
      return Future.successful(Left("Identifiant d'étage invalide"))

    if (appel.depart == appel.destination)
      return Future.successful(
        Left("Le départ et la destination doivent être différents")
      )

    if (appel.idSimulation.exists(_ <= 0))
      return Future.successful(Left("Identifiant de simulation invalide"))

    // Vérifier d'abord l'existence de l'utilisateur
    utilisateurRepository.existsById(appel.idUser).flatMap {

      case false =>
        Future.successful(
          Left("Impossible de créer l'appel : utilisateur introuvable")
        )

      case true =>

        // Vérifier l'existence des deux étages
        for {

          etageDepart <-
            etageRepository.findById(appel.depart)

          etageDestination <-
            etageRepository.findById(appel.destination)

          resultat <- {

            if (etageDepart.isEmpty)
              Future.successful(
                Left("L'étage de départ est introuvable")
              )

            else if (etageDestination.isEmpty)
              Future.successful(
                Left("L'étage de destination est introuvable")
              )

            else if (
              etageDepart.get.idBatiment !=
                etageDestination.get.idBatiment
            )
              Future.successful(
                Left("Les étages doivent appartenir au même bâtiment")
              )

            else {

              // Préparation de l'appel
              val nouvelAppel = appel.copy(
                idAppel = None,
                direction = determinerDirection(
                  appel.depart,
                  appel.destination
                ),
                statut = Enums.StatutAppel.EnAttente,
                dateAppel = java.time.LocalDateTime.now(),
                scoreAttribution = None
              )

              // Enregistrement dans la base
              appelRepository.create(nouvelAppel).map { id =>
                Right(id)
              }
            }
          }

        } yield resultat
    }
  }

  // --------------------------------------------------
  // 3. Rechercher un appel par son identifiant
  // --------------------------------------------------

  def rechercherParId(
      idAppel: Long
  ): Future[Option[Appel]] = {

    if (idAppel <= 0)
      Future.successful(None)
    else
      appelRepository.findById(idAppel)
  }

  // --------------------------------------------------
  // 4. Lister tous les appels
  // --------------------------------------------------

  def listerTous(): Future[Seq[Appel]] = {
    appelRepository.findAll()
  }

  // --------------------------------------------------
  // 5. Lister les appels d'un utilisateur
  // --------------------------------------------------

  def listerParUtilisateur(
      idUser: Long
  ): Future[Seq[Appel]] = {

    if (idUser <= 0)
      Future.successful(Seq.empty)
    else
      appelRepository.findByUser(idUser)
  }

  // --------------------------------------------------
  // 6. Lister les appels réels en attente
  // --------------------------------------------------

  def listerEnAttenteReels(): Future[Seq[Appel]] = {
    appelRepository.findEnAttenteReels()
  }

  // --------------------------------------------------
  // 7. Lister les appels d'une simulation
  // --------------------------------------------------

  def listerParSimulation(
      idSimulation: Long
  ): Future[Seq[Appel]] = {

    if (idSimulation <= 0)
      Future.successful(Seq.empty)
    else
      appelRepository.findBySimulation(idSimulation)
  }

  // --------------------------------------------------
  // 8. Lister les appels en attente d'une simulation
  // --------------------------------------------------

  def listerEnAttenteParSimulation(
      idSimulation: Long
  ): Future[Seq[Appel]] = {

    if (idSimulation <= 0)
      Future.successful(Seq.empty)
    else
      appelRepository.findEnAttenteParSimulation(idSimulation)
  }

  // --------------------------------------------------
  // 9. Modifier le statut d'un appel
  // --------------------------------------------------

  def changerStatut(
      idAppel: Long,
      nouveauStatut: String
  ): Future[Either[String, Unit]] = {

    if (idAppel <= 0)
      return Future.successful(Left("Identifiant d'appel invalide"))

    if (!Enums.StatutAppel.values.contains(nouveauStatut))
      return Future.successful(Left("Statut d'appel invalide"))

    appelRepository.findById(idAppel).flatMap {

      case None =>
        Future.successful(Left("Appel introuvable"))

      case Some(appel) =>

        // Définir les transitions autorisées
        val transitionsAutorisees: Map[String, Seq[String]] = Map(
          Enums.StatutAppel.EnAttente -> Seq(
            Enums.StatutAppel.Affecte,
            Enums.StatutAppel.Annule,
            Enums.StatutAppel.Refuse
          ),

          Enums.StatutAppel.Affecte -> Seq(
            Enums.StatutAppel.EnCours,
            Enums.StatutAppel.Annule,
            Enums.StatutAppel.Refuse
          ),

          Enums.StatutAppel.EnCours -> Seq(
            Enums.StatutAppel.Termine,
            Enums.StatutAppel.Annule
          )
        )

        val autorise =
          transitionsAutorisees
            .getOrElse(appel.statut, Seq.empty)
            .contains(nouveauStatut)

        if (!autorise)
          Future.successful(
            Left(
              s"Transition impossible : ${appel.statut} vers $nouveauStatut"
            )
          )

        else
          appelRepository.updateStatut(idAppel, nouveauStatut).map {

            case 1 =>
              Right(())

            case _ =>
              Left("La modification du statut a échoué")
          }
    }
  }

  // --------------------------------------------------
  // 10. Enregistrer le score d'attribution
  // --------------------------------------------------

  def enregistrerScore(
      idAppel: Long,
      score: BigDecimal
  ): Future[Either[String, Unit]] = {

    if (idAppel <= 0)
      return Future.successful(Left("Identifiant d'appel invalide"))

    if (score < 0)
      return Future.successful(Left("Le score ne peut pas être négatif"))

    appelRepository.findById(idAppel).flatMap {

      case None =>
        Future.successful(Left("Appel introuvable"))

      case Some(appel) if appel.statut != Enums.StatutAppel.EnAttente =>

        Future.successful(
          Left("Seuls les appels en attente peuvent recevoir un score")
        )

      case Some(_) =>

        appelRepository.updateScoreAttribution(idAppel, score).map {

          case 1 =>
            Right(())

          case _ =>
            Left("Impossible d'enregistrer le score")
        }
    }
  }

  // --------------------------------------------------
  // 11. Supprimer un appel
  // --------------------------------------------------

  def supprimer(
      idAppel: Long
  ): Future[Either[String, Unit]] = {

    if (idAppel <= 0)
      return Future.successful(Left("Identifiant d'appel invalide"))

    appelRepository.findById(idAppel).flatMap {

      case None =>
        Future.successful(Left("Appel introuvable"))

      case Some(appel)
          if appel.statut == Enums.StatutAppel.EnCours ||
            appel.statut == Enums.StatutAppel.Termine =>

        Future.successful(
          Left("Impossible de supprimer un appel commencé ou terminé")
        )

      case Some(_) =>

        appelRepository.delete(idAppel).map {

          case 1 =>
            Right(())

          case _ =>
            Left("La suppression de l'appel a échoué")
        }
    }
  }
}
