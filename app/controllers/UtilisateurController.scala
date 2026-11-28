package controllers

import javax.inject._
import play.api.mvc._
import services.AuthService

import repositories.{
  BatimentRepository,
  EtageRepository,
  AscenseurRepository,
  PanneRepository
}

import services.{AppelService, PanneService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UtilisateurController @Inject() (
    cc: ControllerComponents,
    authService: AuthService,
    batimentRepository: BatimentRepository,
    etageRepository: EtageRepository,
    ascenseurRepository: AscenseurRepository,
    panneservice: PanneService,
    appelservice: AppelService
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  // Tes autres fonctions

  def dashbord: Action[AnyContent] = Action.async { implicit request =>
    request.session.get("userId") match {

      case None =>
        Future.successful(Redirect("/login"))

      case Some(id) =>

        id.toLongOption match {

          case None =>
            Future.successful(
              Redirect("/login").withNewSession
            )

          case Some(idUser) =>

            authService.findById(idUser).flatMap {

              case Some(utilisateur) =>

                authService.findRoleById(utilisateur.idRole).map {

                  case Some("ADMIN") =>
                    Redirect("/admin")

                  case Some("SUPERVISEUR") =>
                    Redirect("/superviseur")

                  case Some("TECHNICIEN") =>
                    Redirect("/technicien")

                  case Some("RESIDENT") =>
                    Redirect("/resident")

                  case _ =>
                    Redirect("/login").withNewSession
                      .flashing(
                        "error" -> "Rôle utilisateur non reconnu"
                      )
                }

              case None =>
                Future.successful(
                  Redirect("/login").withNewSession
                    .flashing(
                      "error" -> "Utilisateur introuvable"
                    )
                )
            }
        }
    }
  }

  def resident: Action[AnyContent] = Action.async { implicit request =>
    request.session.get("userId") match {

      case None =>
        Future.successful(Redirect("/login"))

      case Some(id) =>
        id.toLongOption match {

          case None =>
            Future.successful(Redirect("/login").withNewSession)

          case Some(idUser) =>
            authService.findById(idUser).flatMap {

              case Some(utilisateur) =>
                authService.findRoleById(utilisateur.idRole).flatMap {

                  case Some("RESIDENT") =>

                    // Bâtiment demandé dans l'URL : /resident?idBatiment=22
                    val idDemande: Option[Long] =
                      request
                        .getQueryString("idBatiment")
                        .flatMap(_.toLongOption)

                    val futurs = for {
                      batiments <- batimentRepository.findAll()
                      etages <- etageRepository.findAll()
                      ascenseurs <- ascenseurRepository.findAll()
                      pannes <- panneservice.listerParUtilisateur(idUser)
                      appels <- appelservice.listerParUtilisateur(idUser)
                    } yield {

                      // Bâtiment de l'URL s'il existe, sinon le premier
                      val idBatiment: Option[Long] =
                        idDemande
                          .filter(id =>
                            batiments.exists(_.idBatiment.contains(id))
                          )
                          .orElse(batiments.headOption.flatMap(_.idBatiment))

                      (
                        batiments,
                        idBatiment,
                        etages.filter(e => idBatiment.contains(e.idBatiment)),
                        ascenseurs
                          .filter(a => idBatiment.contains(a.idBatiment)),
                        pannes,
                        appels
                      )
                    }

                    futurs.map {
                      case (
                            batiments,
                            idBatiment,
                            etages,
                            ascenseurs,
                            pannes,
                            appels
                          ) =>
                        Ok(
                          views.html.dashbord.resident(
                            user = utilisateur,
                            batiments = batiments,
                            idBatiment = idBatiment,
                            etages = etages,
                            ascenseurs = ascenseurs,
                            pannes = pannes,
                            appels = appels
                          )
                        )
                    }

                  case _ =>
                    Future.successful(
                      Redirect("/dashboard")
                        .flashing("error" -> "Accès réservé aux résidents")
                    )
                }

              case None =>
                Future.successful(
                  Redirect("/login").withNewSession
                    .flashing("error" -> "Utilisateur introuvable")
                )
            }
        }
    }
  }

}
