package controllers

import javax.inject._
import play.api.mvc._
import services.{BatimentService, EtageService, AscenseurService}
import models.Batiment
import play.api.libs.json.Json
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BatimentController @Inject() (
    cc: ControllerComponents,
    batimentservice: BatimentService,
    etageservice: EtageService,
    ascenseurservice: AscenseurService
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {}
// Afficher tous les bâtiments
//   def index: Action[AnyContent] = Action.async {
//     batimentService.listerTous().map { batiments =>
//       Ok(views.html.batiments.index(batiments))
//     }
//   }

//   // Afficher les détails d'un bâtiment
//   def details(id: Long): Action[AnyContent] = Action.async {
//     batimentService.rechercherParId(id).map {
//       case Some(batiment) =>
//         Ok(views.html.batiments.details(batiment))

//       case None =>
//         NotFound("Bâtiment introuvable.")
//     }
//   }

//   // Afficher le formulaire de création
//   def create: Action[AnyContent] = Action {
//     Ok(views.html.batiments.create())
//   }

//   // Enregistrer un bâtiment
//   def save: Action[AnyContent] = Action.async { request =>
//     val data = request.body.asFormUrlEncoded.getOrElse(Map.empty)

//     val nom = data.get("nom").flatMap(_.headOption).getOrElse("")
//     val adresse = data.get("adresse").flatMap(_.headOption)
//     val description = data.get("description").flatMap(_.headOption)

//     val batiment = Batiment(
//       idBatiment = None,
//       nom = nom,
//       adresse = adresse.filter(_.trim.nonEmpty),
//       description = description.filter(_.trim.nonEmpty)
//     )

//     batimentService.creer(batiment).map {

//       case Right(_) =>
//         Redirect(routes.BatimentController.index())
//           .flashing("success" -> "Bâtiment créé avec succès.")

//       case Left(erreur) =>
//         Redirect(routes.BatimentController.create())
//           .flashing("error" -> erreur)
//     }
//   }

// Afficher le formulaire de modification
//   def edit(id: Long): Action[AnyContent] = Action.async {
//     batimentService.rechercherParId(id).map {

//       case Some(batiment) =>
//         Ok(views.html.batiments.edit(batiment))

//       case None =>
//         NotFound("Bâtiment introuvable.")
//     }
//   }

// Désactiver un bâtiment
//   def delete(id: Long): Action[AnyContent] = Action.async {

//     batimentService.changerStatut(id, "INACTIF").map {

//       case Right(true) =>
//         Redirect(routes.BatimentController.index())
//           .flashing("success" -> "Bâtiment désactivé.")

//       case Right(false) =>
//         NotFound("Bâtiment introuvable.")

//       case Left(erreur) =>
//         BadRequest(erreur)
//     }
//   }
