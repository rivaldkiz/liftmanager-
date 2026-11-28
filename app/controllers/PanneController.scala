package controllers

import javax.inject._

import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

import services.PanneService
import models.{Panne, Enums}

@Singleton
class PanneController @Inject() (
    cc: ControllerComponents,
    panneService: PanneService
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  // =====================================================
  // RECUPERER L'UTILISATEUR CONNECTE
  // =====================================================

  private def utilisateurConnecte(
      request: RequestHeader
  ): Option[Long] = {

    request.session
      .get("idUser")
      .flatMap(_.toLongOption)
  }

//   // =====================================================
//   // 1. AFFICHER TOUTES LES PANNES
//   // =====================================================

//   def index: Action[AnyContent] = Action.async { implicit request =>
//     utilisateurConnecte(request) match {

//       case None =>
//         Future.successful(
//           Redirect("/connexion")
//             .flashing("error" -> "Veuillez vous connecter")
//         )

//       case Some(_) =>

//         panneService.listerToutes().map { pannes =>
//           Ok(views.html.pannes.index(pannes))
//         }
//     }
//   }

//   // =====================================================
//   // 2. AFFICHER LE FORMULAIRE DE SIGNALEMENT
//   // =====================================================

//   def create: Action[AnyContent] = Action { implicit request =>
//     utilisateurConnecte(request) match {

//       case None =>
//         Redirect("/connexion")
//           .flashing("error" -> "Veuillez vous connecter")

//       case Some(_) =>
//         Ok(views.html.pannes.create())
//     }
//   }

//   // =====================================================
//   // 3. ENREGISTRER UNE NOUVELLE PANNE
//   // =====================================================

  def save: Action[AnyContent] = Action.async { request =>
    val donnees =
      request.body.asFormUrlEncoded.getOrElse(Map.empty)

    def valeur(champ: String): String =
      donnees
        .get(champ)
        .flatMap(_.headOption)
        .getOrElse("")
        .trim

    val idUser =
      utilisateurConnecte(request)

    val idAscenseur =
      scala.util.Try(valeur("idAscenseur").toLong).toOption

    val description =
      Option(valeur("description")).filter(_.nonEmpty)

    val categorie =
      valeur("categorie")

    (idUser, idAscenseur) match {

      case (None, _) =>
        Future.successful(
          Redirect("/connexion")
            .flashing("error" -> "Veuillez vous connecter")
        )

      case (Some(user), Some(ascenseur)) =>

        val panne = Panne(
          idPanne = None,
          idAscenseur = ascenseur,
          idUser = user,
          description = description,
          categorie = categorie
        )

        panneService.signaler(panne).map {

          case Right(id) =>
            Redirect(routes.UtilisateurController.dashbord())
              .flashing(
                "success" -> "Votre signalement a été enregistré"
              )

          case Left(erreur) =>
            Redirect(routes.UtilisateurController.dashbord())
              .flashing("error" -> erreur)
        }

      case _ =>
        Future.successful(
          Redirect(routes.UtilisateurController.dashbord())
            .flashing(
              "error" -> "Veuillez sélectionner un ascenseur valide"
            )
        )
    }
  }

//   // =====================================================
//   // 4. AFFICHER LES DETAILS D'UNE PANNE
//   // =====================================================

//   def details(id: Long): Action[AnyContent] = Action.async { implicit request =>
//     utilisateurConnecte(request) match {

//       case None =>
//         Future.successful(
//           Redirect("/connexion")
//             .flashing("error" -> "Veuillez vous connecter")
//         )

//       case Some(user) =>

//         panneService.rechercherParId(id).map {

//           case None =>
//             NotFound("Panne introuvable")

//           case Some(panne) =>

//             // Le déclarant peut consulter son signalement.
//             // Les droits des techniciens et administrateurs
//             // seront ajoutés avec le contrôle des rôles.
//             if (panne.idUser != user) {
//               Forbidden("Vous ne pouvez pas consulter cette panne")
//             } else {
//               Ok(views.html.pannes.details(panne))
//             }
//         }
//     }
//   }

//   // =====================================================
//   // 5. AFFICHER LES PANNES D'UN UTILISATEUR
//   // =====================================================

//   def historique(idUser: Long): Action[AnyContent] = Action.async {
//     implicit request =>
//       utilisateurConnecte(request) match {

//         case None =>
//           Future.successful(
//             Redirect("/connexion")
//               .flashing("error" -> "Veuillez vous connecter")
//           )

//         case Some(user) =>

//           if (user != idUser) {

//             Future.successful(
//               Forbidden("Accès non autorisé")
//             )

//           } else {

//             panneService.listerParUtilisateur(user).map { pannes =>
//               Ok(views.html.pannes.historique(pannes))
//             }
//           }
//       }
//   }

//   // =====================================================
//   // 6. CHANGER LE STATUT D'UNE PANNE
//   // =====================================================

//   def changerStatut(id: Long): Action[AnyContent] = Action.async {
//     implicit request =>
//       utilisateurConnecte(request) match {

//         case None =>
//           Future.successful(
//             Redirect("/connexion")
//               .flashing("error" -> "Veuillez vous connecter")
//           )

//         case Some(_) =>

//           val donnees =
//             request.body.asFormUrlEncoded.getOrElse(Map.empty)

//           val nouveauStatut =
//             donnees
//               .get("statut")
//               .flatMap(_.headOption)
//               .getOrElse("")
//               .trim

//           panneService
//             .changerStatut(
//               id,
//               nouveauStatut
//             )
//             .map {

//               case Right(_) =>
//                 Redirect(routes.PanneController.details(id))
//                   .flashing(
//                     "success" -> "Statut de la panne modifié"
//                   )

//               case Left(erreur) =>
//                 Redirect(routes.PanneController.details(id))
//                   .flashing("error" -> erreur)
//             }
//       }
//   }

//   // =====================================================
//   // 7. SUPPRIMER UN SIGNALEMENT
//   // =====================================================

//   def supprimer(id: Long): Action[AnyContent] = Action.async {
//     implicit request =>
//       utilisateurConnecte(request) match {

//         case None =>
//           Future.successful(
//             Redirect("/connexion")
//               .flashing("error" -> "Veuillez vous connecter")
//           )

//         case Some(user) =>

//           panneService.rechercherParId(id).flatMap {

//             case None =>
//               Future.successful(
//                 NotFound("Panne introuvable")
//               )

//             case Some(panne) =>

//               if (panne.idUser != user) {

//                 Future.successful(
//                   Forbidden("Vous ne pouvez pas supprimer cette panne")
//                 )

//               } else {

//                 panneService.supprimer(id).map {

//                   case Right(_) =>
//                     Redirect(
//                       routes.PanneController.historique(user)
//                     ).flashing(
//                       "success" -> "Signalement supprimé"
//                     )

//                   case Left(erreur) =>
//                     Redirect(
//                       routes.PanneController.details(id)
//                     ).flashing("error" -> erreur)
//                 }
//               }
//           }
//       }
//   }
}
