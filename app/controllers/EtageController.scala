// package controllers

// import javax.inject._

// import play.api.mvc._

// import services.EtageService
// import services.BatimentService

// import models.Etage

// import scala.concurrent.{ExecutionContext, Future}

// @Singleton
// class EtageController @Inject() (
//     cc: ControllerComponents,
//     etageService: EtageService,
//     batimentService: BatimentService
// )(implicit ec: ExecutionContext)
//     extends AbstractController(cc) {

//   // 1. Afficher tous les étages
//   def index: Action[AnyContent] = Action.async {

//     etageService.listerTous().map { etages =>
//       Ok(views.html.etages.index(etages))
//     }
//   }

//   // 2. Afficher les détails d'un étage
//   def details(id: Long): Action[AnyContent] = Action.async {

//     etageService.rechercherParId(id).map {

//       case Some(etage) =>
//         Ok(views.html.etages.details(etage))

//       case None =>
//         NotFound("Étage introuvable.")
//     }
//   }

//   // 3. Afficher les étages d'un bâtiment
//   def parBatiment(idBatiment: Long): Action[AnyContent] = Action.async {

//     etageService.listerParBatiment(idBatiment).map {

//       case Right(etages) =>
//         Ok(views.html.etages.index(etages))

//       case Left(erreur) =>
//         NotFound(erreur)
//     }
//   }

//   // 4. Afficher le formulaire de création
//   def create(idBatiment: Long): Action[AnyContent] = Action.async {

//     batimentService.rechercherParId(idBatiment).map {

//       case Some(batiment) =>
//         Ok(views.html.etages.create(batiment))

//       case None =>
//         NotFound("Bâtiment introuvable.")
//     }
//   }

//   // 5. Enregistrer un étage
//   def save: Action[AnyContent] = Action.async { request =>
//     val data = request.body.asFormUrlEncoded.getOrElse(Map.empty)

//     def getValue(key: String): Option[String] =
//       data.get(key).flatMap(_.headOption)

//     val idBatiment = getValue("idBatiment").flatMap(_.toLongOption)
//     val numero = getValue("numero").flatMap(_.toIntOption)
//     val nom = getValue("nom").filter(_.trim.nonEmpty)

//     (idBatiment, numero) match {

//       case (Some(batimentId), Some(num)) =>

//         val etage = Etage(
//           idEtage = None,
//           idBatiment = batimentId,
//           numero = num,
//           nom = nom
//         )

//         etageService.creer(etage).map {

//           case Right(_) =>
//             Redirect(routes.EtageController.parBatiment(batimentId))
//               .flashing("success" -> "Étage créé avec succès.")

//           case Left(erreur) =>
//             Redirect(routes.EtageController.create(batimentId))
//               .flashing("error" -> erreur)
//         }

//       case _ =>
//         Future.successful(
//           BadRequest("Les informations de l'étage sont invalides.")
//         )
//     }
//   }

//   // 6. Afficher le formulaire de modification
//   def edit(id: Long): Action[AnyContent] = Action.async {

//     etageService.rechercherParId(id).map {

//       case Some(etage) =>
//         Ok(views.html.etages.edit(etage))

//       case None =>
//         NotFound("Étage introuvable.")
//     }
//   }

//   // 7. Modifier un étage
//   def update(id: Long): Action[AnyContent] = Action.async { request =>
//     val data = request.body.asFormUrlEncoded.getOrElse(Map.empty)

//     def getValue(key: String): Option[String] =
//       data.get(key).flatMap(_.headOption)

//     val idBatiment = getValue("idBatiment").flatMap(_.toLongOption)
//     val numero = getValue("numero").flatMap(_.toIntOption)
//     val nom = getValue("nom").filter(_.trim.nonEmpty)

//     (idBatiment, numero) match {

//       case (Some(batimentId), Some(num)) =>

//         val etage = Etage(
//           idEtage = Some(id),
//           idBatiment = batimentId,
//           numero = num,
//           nom = nom
//         )

//         etageService.modifier(etage).map {

//           case Right(true) =>
//             Redirect(routes.EtageController.parBatiment(batimentId))
//               .flashing("success" -> "Étage modifié avec succès.")

//           case Right(false) =>
//             NotFound("Étage introuvable.")

//           case Left(erreur) =>
//             Redirect(routes.EtageController.edit(id))
//               .flashing("error" -> erreur)
//         }

//       case _ =>
//         Future.successful(
//           BadRequest("Les informations de l'étage sont invalides.")
//         )
//     }
//   }

//   // 8. Supprimer un étage
//   def delete(id: Long): Action[AnyContent] = Action.async {

//     etageService.supprimer(id).map {

//       case Right(true) =>
//         Redirect(routes.EtageController.index())
//           .flashing("success" -> "Étage supprimé avec succès.")

//       case Right(false) =>
//         NotFound("Étage introuvable.")

//       case Left(erreur) =>
//         BadRequest(erreur)
//     }
//   }
// }
