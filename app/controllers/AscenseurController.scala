// package controllers

// import javax.inject._
// import play.api.mvc._
// import services.AscenseurService
// import models.Ascenseur

// import scala.concurrent.{ExecutionContext, Future}
// import scala.util.Try

// @Singleton
// class AscenseurController @Inject() (
//     cc: ControllerComponents,
//     ascenseurService: AscenseurService
// )(implicit ec: ExecutionContext)
//     extends AbstractController(cc) {

//   // Liste de tous les ascenseurs
//   def index: Action[AnyContent] = Action.async {
//     ascenseurService.listerTous.map { ascenseurs =>
//       Ok(views.html.ascenseurs.index(ascenseurs))
//     }
//   }

//   // Détails d'un ascenseur
//   def details(id: Long): Action[AnyContent] = Action.async {
//     ascenseurService.rechercherParId(id).map {
//       case Some(ascenseur) =>
//         Ok(views.html.ascenseurs.details(ascenseur))

//       case None =>
//         NotFound("Ascenseur introuvable")
//     }
//   }

//   // Liste des ascenseurs d'un bâtiment
//   def parBatiment(idBatiment: Long): Action[AnyContent] = Action.async {
//     ascenseurService.listerParBatiment(idBatiment).map { ascenseurs =>
//       Ok(views.html.ascenseurs.index(ascenseurs))
//     }
//   }

//   // Formulaire de création
//   def create: Action[AnyContent] = Action {
//     Ok(views.html.ascenseurs.create())
//   }

//   // Enregistrement d'un ascenseur physique
//   def save: Action[AnyContent] = Action.async { request =>
//     val donnees = request.body.asFormUrlEncoded.getOrElse(Map.empty)

//     def valeur(champ: String): String =
//       donnees.get(champ).flatMap(_.headOption).getOrElse("").trim

//     val nom = valeur("nom")
//     val idBatiment = Try(valeur("idBatiment").toLong).toOption
//     val capacite = Try(valeur("capacitePersonnes").toInt).toOption
//     val chargeMax = Try(BigDecimal(valeur("chargeMax"))).toOption
//     val position = Try(valeur("position").toInt).toOption

//     (idBatiment, capacite, chargeMax, position) match {

//       case (Some(idB), Some(cap), Some(charge), Some(pos)) =>

//         val ascenseur = Ascenseur(
//           idAscenseur = None,
//           idBatiment = idB,
//           idSimulation = None,
//           nom = nom,
//           capacitePersonnes = cap,
//           chargeMax = charge,
//           position = pos,
//           isVirtual = false
//         )

//         ascenseurService.creer(ascenseur).map {
//           case Right(id) =>
//             Redirect(routes.AscenseurController.details(id))
//               .flashing("success" -> "Ascenseur créé avec succès")

//           case Left(erreur) =>
//             BadRequest(erreur)
//         }

//       case _ =>
//         Future.successful(
//           BadRequest("Veuillez vérifier les informations saisies")
//         )
//     }
//   }

//   // Formulaire de modification
//   def edit(id: Long): Action[AnyContent] = Action.async {
//     ascenseurService.rechercherParId(id).map {
//       case Some(ascenseur) =>
//         Ok(views.html.ascenseurs.edit(ascenseur))

//       case None =>
//         NotFound("Ascenseur introuvable")
//     }
//   }

//   // Modification des caractéristiques
//   def update(id: Long): Action[AnyContent] = Action.async { request =>
//     val donnees = request.body.asFormUrlEncoded.getOrElse(Map.empty)

//     def valeur(champ: String): String =
//       donnees.get(champ).flatMap(_.headOption).getOrElse("").trim

//     val nom = valeur("nom")
//     val capacite = Try(valeur("capacitePersonnes").toInt).toOption
//     val chargeMax = Try(BigDecimal(valeur("chargeMax"))).toOption

//     (capacite, chargeMax) match {

//       case (Some(cap), Some(charge)) =>

//         val ascenseur = Ascenseur(
//           idAscenseur = Some(id),
//           idBatiment = 0L,
//           idSimulation = None,
//           nom = nom,
//           capacitePersonnes = cap,
//           chargeMax = charge,
//           isVirtual = false
//         )

//         ascenseurService.modifier(ascenseur).map {
//           case Right(_) =>
//             Redirect(routes.AscenseurController.details(id))
//               .flashing("success" -> "Ascenseur modifié avec succès")

//           case Left(erreur) =>
//             BadRequest(erreur)
//         }

//       case _ =>
//         Future.successful(
//           BadRequest("Veuillez vérifier les informations saisies")
//         )
//     }
//   }

//   // Suppression d'un ascenseur
//   def delete(id: Long): Action[AnyContent] = Action.async {
//     ascenseurService.supprimer(id).map {
//       case Right(_) =>
//         Redirect(routes.AscenseurController.index())
//           .flashing("success" -> "Ascenseur supprimé avec succès")

//       case Left(erreur) =>
//         BadRequest(erreur)
//     }
//   }
// }
