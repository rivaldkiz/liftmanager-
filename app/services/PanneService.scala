package services

import javax.inject._

import repositories.{PanneRepository, UtilisateurRepository}
import models.{Panne, Enums}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PanneService @Inject() (
    panneRepository: PanneRepository,
    utilisateurRepository: UtilisateurRepository,
    ascenseurService: AscenseurService
)(implicit ec: ExecutionContext) {

  // =====================================================
  // 1. SIGNALER UNE NOUVELLE PANNE
  // =====================================================

  def signaler(panne: Panne): Future[Either[String, Long]] = {

    // Vérifier les identifiants
    if (panne.idAscenseur <= 0 || panne.idUser <= 0) {
      Future.successful(
        Left("Identifiant utilisateur ou ascenseur invalide")
      )
    }

    // Vérifier la catégorie
    else if (!Enums.CategoriePanne.values.contains(panne.categorie)) {
      Future.successful(
        Left("Catégorie de panne invalide")
      )
    }

    else {

      // Vérifier l'existence de l'utilisateur
      utilisateurRepository.existsById(panne.idUser).flatMap {

        case false =>
          Future.successful(
            Left("Impossible de signaler la panne : utilisateur introuvable")
          )

        case true =>

          // Vérifier l'existence de l'ascenseur
          ascenseurService.rechercherParId(panne.idAscenseur).flatMap {

            case None =>
              Future.successful(
                Left("Ascenseur introuvable")
              )

            case Some(ascenseur) =>

              // Un ascenseur en panne ou en maintenance
              // ne peut pas recevoir un nouveau signalement.
              if (
                ascenseur.etat == Enums.EtatAscenseur.Panne ||
                ascenseur.etat == Enums.EtatAscenseur.Maintenance
              ) {

                Future.successful(
                  Left("Cet ascenseur est déjà immobilisé")
                )

              } else {

                val nouvellePanne = panne.copy(
                  idPanne = None,
                  statut = Enums.StatutPanne.Signalee,
                  datePanne = java.time.LocalDateTime.now()
                )

                // Enregistrer la panne
                panneRepository.create(nouvellePanne).flatMap { id =>
                  // Immobiliser automatiquement l'ascenseur
                  ascenseurService
                    .changerEtat(
                      panne.idAscenseur,
                      Enums.EtatAscenseur.Panne
                    )
                    .flatMap {

                      case Right(_) =>
                        Future.successful(Right(id))

                      case Left(erreur) =>

                        // Compensation : supprimer la panne
                        // si l'immobilisation de l'ascenseur échoue.
                        panneRepository.delete(id).map { _ =>
                          Left(
                            s"La panne n'a pas pu être prise en compte : $erreur"
                          )
                        }
                    }
                }
              }
          }
      }
    }
  }

  // =====================================================
  // 2. RECHERCHER UNE PANNE PAR SON IDENTIFIANT
  // =====================================================

  def rechercherParId(
      idPanne: Long
  ): Future[Option[Panne]] = {

    if (idPanne <= 0) {
      Future.successful(None)
    } else {
      panneRepository.findById(idPanne)
    }
  }

  // =====================================================
  // 3. LISTER TOUTES LES PANNES
  // =====================================================

  def listerToutes(): Future[Seq[Panne]] = {

    panneRepository.findAll()
  }

  // =====================================================
  // 4. LISTER LES PANNES D'UN ASCENSEUR
  // =====================================================

  def listerParAscenseur(
      idAscenseur: Long
  ): Future[Seq[Panne]] = {

    if (idAscenseur <= 0) {
      Future.successful(Seq.empty)
    } else {
      panneRepository.findByAscenseur(idAscenseur)
    }
  }

  // =====================================================
  // 5. LISTER LES PANNES SIGNALEES PAR UN UTILISATEUR
  // =====================================================

  def listerParUtilisateur(
      idUser: Long
  ): Future[Seq[Panne]] = {

    if (idUser <= 0) {
      Future.successful(Seq.empty)
    } else {
      panneRepository.findByUtilisateur(idUser)
    }
  }

  // =====================================================
  // 6. LISTER LES PANNES PAR STATUT
  // =====================================================

  def listerParStatut(
      statut: String
  ): Future[Seq[Panne]] = {

    if (!Enums.StatutPanne.values.contains(statut)) {
      Future.successful(Seq.empty)
    } else {
      panneRepository.findByStatut(statut)
    }
  }

  // =====================================================
  // 7. LISTER LES PANNES ACTIVES D'UN ASCENSEUR
  // =====================================================

  def listerPannesActives(
      idAscenseur: Long
  ): Future[Seq[Panne]] = {

    if (idAscenseur <= 0) {
      Future.successful(Seq.empty)
    } else {
      panneRepository.findActivesByAscenseur(idAscenseur)
    }
  }

  // =====================================================
  // 8. CHANGER LE STATUT D'UNE PANNE
  // =====================================================

  def changerStatut(
      idPanne: Long,
      nouveauStatut: String
  ): Future[Either[String, Unit]] = {

    if (idPanne <= 0) {
      Future.successful(
        Left("Identifiant de panne invalide")
      )
    }

    else if (!Enums.StatutPanne.values.contains(nouveauStatut)) {
      Future.successful(
        Left("Statut de panne invalide")
      )
    }

    else {

      panneRepository.findById(idPanne).flatMap {

        case None =>
          Future.successful(
            Left("Panne introuvable")
          )

        case Some(panne) =>

          val transitionsAutorisees = Map(
            Enums.StatutPanne.Signalee -> Seq(
              Enums.StatutPanne.EnCours,
              Enums.StatutPanne.Annulee
            ),

            Enums.StatutPanne.EnCours -> Seq(
              Enums.StatutPanne.Resolue,
              Enums.StatutPanne.Annulee
            )
          )

          val transitions =
            transitionsAutorisees.getOrElse(panne.statut, Seq.empty)

          if (!transitions.contains(nouveauStatut)) {

            Future.successful(
              Left(
                s"Transition impossible : ${panne.statut} vers $nouveauStatut"
              )
            )

          } else {

            // Modifier le statut de la panne
            panneRepository
              .updateStatut(
                idPanne,
                nouveauStatut
              )
              .flatMap {

                case 0 =>
                  Future.successful(
                    Left("La modification de la panne a échoué")
                  )

                case _ =>

                  // Une panne résolue ou annulée n'est plus active.
                  if (
                    nouveauStatut == Enums.StatutPanne.Resolue ||
                    nouveauStatut == Enums.StatutPanne.Annulee
                  ) {

                    verifierRemiseEnService(panne.idAscenseur).map {

                      case Left(erreur) => Left(erreur)
                      case Right(_)     => Right(())
                    }

                  } else {
                    Future.successful(Right(()))
                  }
              }
          }
      }
    }
  }

  // =====================================================
  // 9. VERIFIER SI L'ASCENSEUR PEUT ETRE REMIS EN SERVICE
  // =====================================================

  private def verifierRemiseEnService(
      idAscenseur: Long
  ): Future[Either[String, Unit]] = {

    panneRepository.findActivesByAscenseur(idAscenseur).flatMap {

      case pannes if pannes.nonEmpty =>
        Future.successful(Right(()))

      case _ =>

        ascenseurService.rechercherParId(idAscenseur).flatMap {

          case None =>
            Future.successful(
              Left("Ascenseur introuvable")
            )

          case Some(ascenseur) =>

            // Ne pas modifier un ascenseur placé
            // manuellement en maintenance.
            if (ascenseur.etat == Enums.EtatAscenseur.Panne) {

              ascenseurService.changerEtat(
                idAscenseur,
                Enums.EtatAscenseur.Arret
              )

            } else {
              Future.successful(Right(()))
            }
        }
    }
  }

  // =====================================================
  // 10. SUPPRIMER UNE PANNE
  // =====================================================

  def supprimer(
      idPanne: Long
  ): Future[Either[String, Unit]] = {

    if (idPanne <= 0) {
      Future.successful(
        Left("Identifiant de panne invalide")
      )
    } else {

      panneRepository.findById(idPanne).flatMap {

        case None =>
          Future.successful(
            Left("Panne introuvable")
          )

        case Some(panne) =>

          if (
            panne.statut == Enums.StatutPanne.EnCours ||
            panne.statut == Enums.StatutPanne.Resolue
          ) {

            Future.successful(
              Left(
                "Impossible de supprimer une panne prise en charge ou résolue"
              )
            )

          } else {

            panneRepository.delete(idPanne).map {

              case 0 =>
                Left("La suppression de la panne a échoué")

              case _ =>
                Right(())
            }
          }
      }
    }
  }
}
