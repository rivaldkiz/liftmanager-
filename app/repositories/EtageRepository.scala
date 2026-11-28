package repositories

import javax.inject._
import database.DatabaseConfig
import models.Etage
import slick.jdbc.GetResult

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EtageRepository @Inject() (
    databaseConfig: DatabaseConfig
)(implicit ec: ExecutionContext) {

  // Configuration de la base de données
  private val db = databaseConfig.db
  private val profile = databaseConfig.profile

  import profile.api._

  // Mapping des résultats SQL vers Etage
  implicit val etageGetResult: GetResult[Etage] =
    GetResult(r =>
      Etage(
        idEtage = Some(r.nextLong()),
        idBatiment = r.nextLong(),
        numero = r.nextInt(),
        nom = r.nextStringOption()
      )
    )

  // 1. Créer un étage
  def create(etage: Etage): Future[Long] = {

    val action = for {

      // Récupération du prochain identifiant
      id <- sql"""
        SELECT SEQ_ETAGE.NEXTVAL
        FROM DUAL
      """.as[Long].head

      // Insertion de l'étage
      _ <- sqlu"""
        INSERT INTO ETAGE (
          ID_ETAGE,
          ID_BATIMENT,
          NUMERO,
          NOM
        )
        VALUES (
          $id,
          ${etage.idBatiment},
          ${etage.numero},
          ${etage.nom}
        )
      """

    } yield id

    db.run(action.transactionally)
  }

  // 2. Rechercher un étage par identifiant
  def findById(id: Long): Future[Option[Etage]] = {

    db.run(
      sql"""
        SELECT
          ID_ETAGE,
          ID_BATIMENT,
          NUMERO,
          NOM
        FROM ETAGE
        WHERE ID_ETAGE = $id
      """.as[Etage].headOption
    )
  }

  // 3. Récupérer tous les étages
  def findAll(): Future[Seq[Etage]] = {

    db.run(
      sql"""
        SELECT
          ID_ETAGE,
          ID_BATIMENT,
          NUMERO,
          NOM
        FROM ETAGE
        ORDER BY ID_BATIMENT, NUMERO
      """.as[Etage]
    )
  }

  // 4. Récupérer les étages d'un bâtiment
  def findByBatiment(idBatiment: Long): Future[Seq[Etage]] = {

    db.run(
      sql"""
        SELECT
          ID_ETAGE,
          ID_BATIMENT,
          NUMERO,
          NOM
        FROM ETAGE
        WHERE ID_BATIMENT = $idBatiment
        ORDER BY NUMERO
      """.as[Etage]
    )
  }

  // 5. Modifier un étage
  def update(etage: Etage): Future[Boolean] = {

    val id = etage.idEtage.getOrElse(0L)

    db.run(
      sqlu"""
        UPDATE ETAGE
        SET
          ID_BATIMENT = ${etage.idBatiment},
          NUMERO = ${etage.numero},
          NOM = ${etage.nom}
        WHERE ID_ETAGE = $id
      """
    ).map(_ > 0)
  }

  // 6. Supprimer un étage
  def delete(id: Long): Future[Boolean] = {

    db.run(
      sqlu"""
        DELETE FROM ETAGE
        WHERE ID_ETAGE = $id
      """
    ).map(_ > 0)
  }

  // 7. Vérifier l'existence d'un étage
  def existsById(id: Long): Future[Boolean] = {

    db.run(
      sql"""
        SELECT COUNT(*)
        FROM ETAGE
        WHERE ID_ETAGE = $id
      """.as[Long]
    ).map(_.headOption.getOrElse(0L) > 0)
  }

  // 8. Vérifier si un numéro existe dans un bâtiment
  def existsByNumero(
      idBatiment: Long,
      numero: Int
  ): Future[Boolean] = {

    db.run(
      sql"""
        SELECT COUNT(*)
        FROM ETAGE
        WHERE ID_BATIMENT = $idBatiment
        AND NUMERO = $numero
      """.as[Long]
    ).map(_.headOption.getOrElse(0L) > 0)
  }

  // 9. Compter les étages d'un bâtiment
  def countByBatiment(idBatiment: Long): Future[Int] = {

    db.run(
      sql"""
        SELECT COUNT(*)
        FROM ETAGE
        WHERE ID_BATIMENT = $idBatiment
      """.as[Long]
    ).map(_.headOption.getOrElse(0L).toInt)
  }

  // 10. Rechercher un étage par son numéro dans un bâtiment
  def findByNumero(
      idBatiment: Long,
      numero: Int
  ): Future[Option[Etage]] = {

    db.run(
      sql"""
        SELECT
          ID_ETAGE,
          ID_BATIMENT,
          NUMERO,
          NOM
        FROM ETAGE
        WHERE ID_BATIMENT = $idBatiment
        AND NUMERO = $numero
      """.as[Etage].headOption
    )
  }
}
