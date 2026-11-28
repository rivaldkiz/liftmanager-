error id: file:///C:/Users/franc/Desktop/elevatormanager/app/controllers/BatimentController.scala:ExecutionContext#
file:///C:/Users/franc/Desktop/elevatormanager/app/controllers/BatimentController.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -javax/inject/ExecutionContext#
	 -play/api/mvc/ExecutionContext#
	 -scala/concurrent/ExecutionContext#
	 -ExecutionContext#
	 -scala/Predef.ExecutionContext#
offset: 320
uri: file:///C:/Users/franc/Desktop/elevatormanager/app/controllers/BatimentController.scala
text:
```scala
package controllers

import javax.inject._
import play.api.mvc._
import services.BatimentService
import models.Batiment

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BatimentController @Inject() (
    cc: ControllerComponents,
    batimentService: BatimentService
)(implicit ec: Exec@@utionContext)
    extends AbstractController(cc) {

        // Afficher le tableau de bord
  def index = Action.async { implicit request =>

    batimentService.listerTous().flatMap { batiments =>

      // Récupérer le bâtiment mémorisé
      val idBatiment = request.session
        .get("idBatiment")
        .flatMap(_.toLongOption)
        .orElse(batiments.headOption.flatMap(_.idBatiment))

      idBatiment match {

        case Some(id) =>

          for {
            batimentOpt <- batimentService.rechercherParId(id)
            etagesResult <- etageService.listerParBatiment(id)
            ascenseurs <- ascenseurService.listerParBatiment(id)

          } yield {

            (batimentOpt, etagesResult) match {

              case (Some(batiment), Right(etages)) =>

                Ok(views.html.dashboard.index(
                  batiments,
                  batiment,
                  etages,
                  ascenseurs
                ))

              case (None, _) =>
                NotFound("Bâtiment introuvable.")

              case (_, Left(erreur)) =>
                InternalServerError(erreur)
            }
          }

        case None =>

          Future.successful(
            Ok(views.html.dashboard.index(
              batiments,
              null,
              Seq.empty,
              Seq.empty
            ))
          )
      }

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
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 