package repositories

import javax.inject._
import database.DatabaseConfig
import models.Batiment
import slick.jdbc.GetResult

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BatimentRepository @Inject() (
    databaseConfig: DatabaseConfig
)(implicit ec: ExecutionContext) {

  // Configuration de la base de données
  private val db = databaseConfig.db
  private val profile = databaseConfig.profile

  import profile.api._

  // Mapping des résultats SQL vers Batiment
  implicit val batimentGetResult: GetResult[Batiment] =
    GetResult(r =>
      Batiment(
        idBatiment = Some(r.nextLong()),
        nom = r.nextString(),
        adresse = r.nextStringOption(),
        description = r.nextStringOption(),
        statut = r.nextString()
      )
    )

  // 1. Créer un bâtiment
  def create(batiment: Batiment): Future[Long] = {

    val action = for {
      // Récupération du prochain identifiant
      id <- sql"""
        SELECT SEQ_BATIMENT.NEXTVAL
        FROM DUAL
      """.as[Long].head

      // Insertion du bâtiment
      _ <- sqlu"""
        INSERT INTO BATIMENT (
          ID_BATIMENT,
          NOM,
          ADRESSE,
          DESCRIPTION,
          STATUT
        )
        VALUES (
          $id,
          ${batiment.nom},
          ${batiment.adresse},
          ${batiment.description},
          ${batiment.statut}
        )
      """
    } yield id

    db.run(action.transactionally)
  }

  // 2. Rechercher un bâtiment par son identifiant
  def findById(id: Long): Future[Option[Batiment]] = {

    db.run(
      sql"""
        SELECT
          ID_BATIMENT,
          NOM,
          ADRESSE,
          DESCRIPTION,
          STATUT
        FROM BATIMENT
        WHERE ID_BATIMENT = $id
      """.as[Batiment].headOption
    )
  }

  // 3. Récupérer tous les bâtiments
  def findAll(): Future[Seq[Batiment]] = {

    db.run(
      sql"""
        SELECT
          ID_BATIMENT,
          NOM,
          ADRESSE,
          DESCRIPTION,
          STATUT
        FROM BATIMENT
        ORDER BY NOM
      """.as[Batiment]
    )
  }

  // 4. Récupérer les bâtiments actifs
  def findAllActifs(): Future[Seq[Batiment]] = {

    val statutActif = models.Enums.StatutBatiment.Actif

    db.run(
      sql"""
        SELECT
          ID_BATIMENT,
          NOM,
          ADRESSE,
          DESCRIPTION,
          STATUT
        FROM BATIMENT
        WHERE STATUT = $statutActif
        ORDER BY NOM
      """.as[Batiment]
    )
  }

  // 5. Modifier un bâtiment
  def update(batiment: Batiment): Future[Boolean] = {

    val id = batiment.idBatiment.getOrElse(0L)

    db.run(
      sqlu"""
        UPDATE BATIMENT
        SET
          NOM = ${batiment.nom},
          ADRESSE = ${batiment.adresse},
          DESCRIPTION = ${batiment.description},
          STATUT = ${batiment.statut}
        WHERE ID_BATIMENT = $id
      """
    ).map(_ > 0)
  }

  // 6. Supprimer un bâtiment
  def delete(id: Long): Future[Boolean] = {

    db.run(
      sqlu"""
        DELETE FROM BATIMENT
        WHERE ID_BATIMENT = $id
      """
    ).map(_ > 0)
  }

  // 7. Vérifier l'existence d'un bâtiment
  def existsById(id: Long): Future[Boolean] = {

    db.run(
      sql"""
        SELECT COUNT(*)
        FROM BATIMENT
        WHERE ID_BATIMENT = $id
      """.as[Long]
    ).map(_.headOption.getOrElse(0L) > 0)
  }

  // 8. Vérifier si un nom existe
  def existsByNom(nom: String): Future[Boolean] = {

    val nomRecherche = nom.trim

    db.run(
      sql"""
        SELECT COUNT(*)
        FROM BATIMENT
        WHERE LOWER(NOM) = LOWER($nomRecherche)
      """.as[Long]
    ).map(_.headOption.getOrElse(0L) > 0)
  }

  // 9. Rechercher par nom ou adresse
  def search(motCle: String): Future[Seq[Batiment]] = {

    val recherche = s"%${motCle.trim}%"

    db.run(
      sql"""
        SELECT
          ID_BATIMENT,
          NOM,
          ADRESSE,
          DESCRIPTION,
          STATUT
        FROM BATIMENT
        WHERE LOWER(NOM) LIKE LOWER($recherche)
           OR LOWER(ADRESSE) LIKE LOWER($recherche)
        ORDER BY NOM
      """.as[Batiment]
    )
  }

  // 10. Modifier uniquement le statut
  def updateStatut(id: Long, statut: String): Future[Boolean] = {

    db.run(
      sqlu"""
        UPDATE BATIMENT
        SET STATUT = $statut
        WHERE ID_BATIMENT = $id
      """
    ).map(_ > 0)
  }
}
