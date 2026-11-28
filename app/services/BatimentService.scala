package services

import javax.inject._
import repositories.BatimentRepository
import models.Batiment
import models.Enums

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BatimentService @Inject() (
    batimentRepository: BatimentRepository
)(implicit ec: ExecutionContext) {

  // 1. Créer un bâtiment
  def creer(batiment: Batiment): Future[Either[String, Long]] = {

    val nom = batiment.nom.trim

    if (nom.isEmpty) {
      Future.successful(Left("Le nom du bâtiment est obligatoire."))
    } else if (nom.length > 100) {
      Future.successful(Left("Le nom ne doit pas dépasser 100 caractères."))
    } else {
      batimentRepository.existsByNom(nom).flatMap {

        case true =>
          Future.successful(Left("Un bâtiment portant ce nom existe déjà."))

        case false =>
          val nouveauBatiment = batiment.copy(
            idBatiment = None,
            nom = nom,
            statut = Enums.StatutBatiment.Actif
          )

          batimentRepository
            .create(nouveauBatiment)
            .map(id => Right(id))
      }
    }
  }

  // 2. Rechercher un bâtiment par son identifiant
  def rechercherParId(id: Long): Future[Option[Batiment]] = {

    if (id <= 0) {
      Future.successful(None)
    } else {
      batimentRepository.findById(id)
    }
  }

  // 3. Lister tous les bâtiments
  def listerTous(): Future[Seq[Batiment]] = {
    batimentRepository.findAll()
  }

  // 4. Lister les bâtiments actifs
  def listerActifs(): Future[Seq[Batiment]] = {
    batimentRepository.findAllActifs()
  }

  // 5. Modifier un bâtiment
  def modifier(batiment: Batiment): Future[Either[String, Boolean]] = {

    val nom = batiment.nom.trim

    if (batiment.idBatiment.isEmpty || batiment.idBatiment.get <= 0) {
      Future.successful(Left("Identifiant du bâtiment invalide."))
    } else if (nom.isEmpty) {
      Future.successful(Left("Le nom du bâtiment est obligatoire."))
    } else if (nom.length > 100) {
      Future.successful(Left("Le nom ne doit pas dépasser 100 caractères."))
    } else {
      val id = batiment.idBatiment.get

      batimentRepository.findById(id).flatMap {

        case None =>
          Future.successful(Left("Bâtiment introuvable."))

        case Some(existant) =>

          val verificationNom =
            if (existant.nom.equalsIgnoreCase(nom)) {
              Future.successful(false)
            } else {
              batimentRepository.existsByNom(nom)
            }

          verificationNom.flatMap {

            case true =>
              Future.successful(
                Left("Ce nom est déjà utilisé par un autre bâtiment.")
              )

            case false =>
              val batimentModifie = batiment.copy(nom = nom)

              batimentRepository
                .update(batimentModifie)
                .map(resultat => Right(resultat))
          }
      }
    }
  }

  // 6. Supprimer un bâtiment
  def supprimer(id: Long): Future[Either[String, Boolean]] = {

    if (id <= 0) {
      Future.successful(Left("Identifiant invalide."))
    } else {
      batimentRepository.findById(id).flatMap {

        case None =>
          Future.successful(Left("Bâtiment introuvable."))

        case Some(_) =>
          batimentRepository
            .delete(id)
            .map(resultat => Right(resultat))
      }
    }
  }

  // 7. Rechercher un bâtiment
  def rechercher(motCle: String): Future[Seq[Batiment]] = {

    if (motCle.trim.isEmpty) {
      Future.successful(Seq.empty)
    } else {
      batimentRepository.search(motCle.trim)
    }
  }

  // 8. Changer le statut d'un bâtiment
  def changerStatut(
      id: Long,
      nouveauStatut: String
  ): Future[Either[String, Boolean]] = {

    val statutsAutorises = Seq(
      Enums.StatutBatiment.Actif,
      Enums.StatutBatiment.Inactif
    )

    if (id <= 0) {
      Future.successful(Left("Identifiant invalide."))
    } else if (!statutsAutorises.contains(nouveauStatut)) {
      Future.successful(Left("Statut invalide."))
    } else {
      batimentRepository.findById(id).flatMap {

        case None =>
          Future.successful(Left("Bâtiment introuvable."))

        case Some(_) =>
          batimentRepository
            .updateStatut(id, nouveauStatut)
            .map(resultat => Right(resultat))
      }
    }
  }
}
