error id: file:///C:/Users/franc/Desktop/elevatormanager/app/controllers/AppelController.scala:RequestHeader#
file:///C:/Users/franc/Desktop/elevatormanager/app/controllers/AppelController.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -javax/inject/RequestHeader#
	 -play/api/mvc/RequestHeader#
	 -RequestHeader#
	 -scala/Predef.RequestHeader#
offset: 590
uri: file:///C:/Users/franc/Desktop/elevatormanager/app/controllers/AppelController.scala
text:
```scala
package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

import services.AppelService
import models.{Appel, Enums}

@Singleton
class AppelController @Inject() (
    cc: ControllerComponents,
    appelService: AppelService
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  // =====================================================
  // RECUPERER L'IDENTIFIANT DE L'UTILISATEUR CONNECTE
  // =====================================================

  private def utilisateurConnecte(request: @@RequestHeader): Option[Long] =
    request.session.get("userId").flatMap(_.toLongOption)

  // =====================================================
  // 1. AFFICHER LA LISTE DES APPELS
  // =====================================================

//   def index: Action[AnyContent] = Action.async { implicit request =>
//     utilisateurConnecte(request) match {

//       case None =>
//         Future.successful(
//           Redirect("/connexion")
//             .flashing("error" -> "Veuillez vous connecter")
//         )

//       case Some(_) =>
//         appelService.listerTous().map { appels =>
//           Ok(views.html.appels.index(appels))
//         }
//     }
//   }

  // =====================================================
  // 2. AFFICHER LE FORMULAIRE DE CREATION
  // =====================================================

  // =====================================================
  // 3. ENREGISTRER UN NOUVEL APPEL
  // =====================================================

  def save: Action[AnyContent] = Action.async { implicit request =>
    val donnees =
      request.body.asFormUrlEncoded.getOrElse(Map.empty)

    def valeur(champ: String): String =
      donnees
        .get(champ)
        .flatMap(_.headOption)
        .getOrElse("")
        .trim

    val idUser = utilisateurConnecte(request)

    val depart =
      scala.util.Try(valeur("depart").toLong).toOption

    val destination =
      scala.util.Try(valeur("destination").toLong).toOption

    val avecBagages =
      donnees.contains("avecBagages")

    val idBat = valeur("idBatiment")

    val urlResident =
      if (idBat.isEmpty) "/resident"
      else s"/resident?idBatiment=$idBat"

    (idUser, depart, destination) match {

      case (None, _, _) =>
        Future.successful(
          Redirect("/login")
            .flashing("error" -> "Veuillez vous connecter")
        )

      case (Some(user), Some(dep), Some(dest)) =>

        val appel = Appel(
          idAppel = None,
          idUser = user,
          depart = dep,
          destination = dest,
          idSimulation = None,
          direction = "",
          avecBagages = avecBagages
        )

        appelService.creer(appel).map {

          case Right(_) =>
            Redirect(urlResident)
              .flashing("success" -> "Votre appel a été enregistré")

          case Left(erreur) =>
            Redirect(urlResident)
              .flashing("error" -> erreur)
        }

      case _ =>
        Future.successful(
          Redirect(urlResident)
            .flashing("error" -> "Veuillez renseigner correctement les étages")
        )
    }
  }
  // =====================================================
  // 4. AFFICHER LES DETAILS D'UN APPEL
  // =====================================================

//   def details(id: Long): Action[AnyContent] = Action.async { implicit request =>
//     utilisateurConnecte(request) match {

//       case None =>
//         Future.successful(
//           Redirect("/connexion")
//             .flashing("error" -> "Veuillez vous connecter")
//         )

//       case Some(user) =>

//         appelService.rechercherParId(id).map {

//           case None =>
//             NotFound("Appel introuvable")

//           case Some(appel) =>

//             // Un résident ne consulte que ses propres appels.
//             if (appel.idUser != user) {
//               Forbidden("Vous ne pouvez pas consulter cet appel")
//             } else {
//               Ok(views.html.appels.details(appel))
//             }
//         }
//     }
//   }

  // =====================================================
  // 5. AFFICHER L'HISTORIQUE D'UN UTILISATEUR
  // =====================================================

//   def historique(idUser: Long): Action[AnyContent] = Action.async {
//     implicit request =>
//       utilisateurConnecte(request) match {

//         case None =>
//           Future.successful(
//             Redirect("/connexion")
//               .flashing("error" -> "Veuillez vous connecter")
//           )

//         case Some(user) =>

//           // Empêche un utilisateur de consulter
//           // l'historique d'un autre utilisateur.
//           if (user != idUser) {

//             Future.successful(
//               Forbidden("Accès non autorisé")
//             )

//           } else {

//             appelService.listerParUtilisateur(user).map { appels =>
//               Ok(views.html.appels.historique(appels))
//             }
//           }
//       }
//   }

  // =====================================================
  // 6. ANNULER UN APPEL
  // =====================================================

//   def annuler(id: Long): Action[AnyContent] = Action.async { implicit request =>
//     utilisateurConnecte(request) match {

//       case None =>
//         Future.successful(
//           Redirect("/connexion")
//             .flashing("error" -> "Veuillez vous connecter")
//         )

//       case Some(user) =>

//         appelService.rechercherParId(id).flatMap {

//           case None =>
//             Future.successful(
//               NotFound("Appel introuvable")
//             )

//           case Some(appel) =>

//             // Vérifier que l'appel appartient au résident.
//             if (appel.idUser != user) {

//               Future.successful(
//                 Forbidden("Vous ne pouvez pas annuler cet appel")
//               )

//             } else {

//               appelService
//                 .changerStatut(
//                   id,
//                   Enums.StatutAppel.Annule
//                 )
//                 .map {

//                   case Right(_) =>
//                     Redirect(routes.AppelController.historique(user))
//                       .flashing(
//                         "success" -> "Votre appel a été annulé"
//                       )

//                   case Left(erreur) =>
//                     Redirect(routes.AppelController.details(id))
//                       .flashing("error" -> erreur)
//                 }
//             }
//         }
//     }
//   }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 