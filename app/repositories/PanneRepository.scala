package repositories

import javax.inject._
import database.DatabaseConfig
import models.Panne

import java.sql.Timestamp
import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.GetResult

@Singleton
class PanneRepository @Inject() (
    databaseConfig: DatabaseConfig
)(implicit ec: ExecutionContext) {

  private val db = databaseConfig.db
  private val profile = databaseConfig.profile

  import profile.api._

  // =====================================================
  // CONVERTIR UNE LIGNE SQL EN OBJET PANNE
  // =====================================================

  private def mapPanne(
      row: (
          Long,
          Long,
          Long,
          Option[String],
          String,
          String,
          Timestamp
      )
  ): Panne = {

    Panne(
      idPanne = Some(row._1),
      idAscenseur = row._2,
      idUser = row._3,
      description = row._4,
      categorie = row._5,
      statut = row._6,
      datePanne = row._7.toLocalDateTime
    )
  }

  // =====================================================
  // 1. CREER UNE PANNE
  // =====================================================

  def create(panne: Panne): Future[Long] = {

    db.run(
      sql"""
        INSERT INTO PANNE (
          ID_PANNE,
          ID_ASCENSEUR,
          ID_USER,
          DESCRIPTION,
          CATEGORIE,
          STATUT,
          DATE_PANNE
        )
        VALUES (
          SEQ_PANNE.NEXTVAL,
          ${panne.idAscenseur},
          ${panne.idUser},
          ${panne.description},
          ${panne.categorie},
          ${panne.statut},
          ${Timestamp.valueOf(panne.datePanne)}
        )
      """.asUpdate
    ).flatMap { _ =>
      db.run(
        sql"SELECT SEQ_PANNE.CURRVAL FROM DUAL".as[Long]
      ).map(_.head)
    }
  }

  // =====================================================
  // 2. RECHERCHER UNE PANNE PAR SON ID
  // =====================================================

  def findById(idPanne: Long): Future[Option[Panne]] = {

    db.run(
      sql"""
        SELECT
          ID_PANNE,
          ID_ASCENSEUR,
          ID_USER,
          DESCRIPTION,
          CATEGORIE,
          STATUT,
          DATE_PANNE
        FROM PANNE
        WHERE ID_PANNE = $idPanne
      """.as[
        (
            Long,
            Long,
            Long,
            Option[String],
            String,
            String,
            Timestamp
        )
      ]
    ).map(_.headOption.map(mapPanne))
  }

  // =====================================================
  // 3. LISTER TOUTES LES PANNES
  // =====================================================

  def findAll(): Future[Seq[Panne]] = {

    db.run(
      sql"""
        SELECT
          ID_PANNE,
          ID_ASCENSEUR,
          ID_USER,
          DESCRIPTION,
          CATEGORIE,
          STATUT,
          DATE_PANNE
        FROM PANNE
        ORDER BY DATE_PANNE DESC
      """.as[
        (
            Long,
            Long,
            Long,
            Option[String],
            String,
            String,
            Timestamp
        )
      ]
    ).map(_.map(mapPanne))
  }

  // =====================================================
  // 4. RECHERCHER LES PANNES D'UN ASCENSEUR
  // =====================================================

  def findByAscenseur(idAscenseur: Long): Future[Seq[Panne]] = {

    db.run(
      sql"""
        SELECT
          ID_PANNE,
          ID_ASCENSEUR,
          ID_USER,
          DESCRIPTION,
          CATEGORIE,
          STATUT,
          DATE_PANNE
        FROM PANNE
        WHERE ID_ASCENSEUR = $idAscenseur
        ORDER BY DATE_PANNE DESC
      """.as[
        (
            Long,
            Long,
            Long,
            Option[String],
            String,
            String,
            Timestamp
        )
      ]
    ).map(_.map(mapPanne))
  }

  // =====================================================
  // 5. RECHERCHER LES PANNES D'UN UTILISATEUR
  // =====================================================

  def findByUtilisateur(idUser: Long): Future[Seq[Panne]] = {

    db.run(
      sql"""
        SELECT
          ID_PANNE,
          ID_ASCENSEUR,
          ID_USER,
          DESCRIPTION,
          CATEGORIE,
          STATUT,
          DATE_PANNE
        FROM PANNE
        WHERE ID_USER = $idUser
        ORDER BY DATE_PANNE DESC
      """.as[
        (
            Long,
            Long,
            Long,
            Option[String],
            String,
            String,
            Timestamp
        )
      ]
    ).map(_.map(mapPanne))
  }

  // =====================================================
  // 6. RECHERCHER LES PANNES PAR STATUT
  // =====================================================

  def findByStatut(statut: String): Future[Seq[Panne]] = {

    db.run(
      sql"""
        SELECT
          ID_PANNE,
          ID_ASCENSEUR,
          ID_USER,
          DESCRIPTION,
          CATEGORIE,
          STATUT,
          DATE_PANNE
        FROM PANNE
        WHERE STATUT = $statut
        ORDER BY DATE_PANNE DESC
      """.as[
        (
            Long,
            Long,
            Long,
            Option[String],
            String,
            String,
            Timestamp
        )
      ]
    ).map(_.map(mapPanne))
  }

  // =====================================================
  // 7. RECHERCHER LES PANNES ACTIVES D'UN ASCENSEUR
  // =====================================================

  def findActivesByAscenseur(
      idAscenseur: Long
  ): Future[Seq[Panne]] = {

    db.run(
      sql"""
        SELECT
          ID_PANNE,
          ID_ASCENSEUR,
          ID_USER,
          DESCRIPTION,
          CATEGORIE,
          STATUT,
          DATE_PANNE
        FROM PANNE
        WHERE ID_ASCENSEUR = $idAscenseur
          AND STATUT IN (
            'SIGNALEE',
            'EN_COURS'
          )
        ORDER BY DATE_PANNE DESC
      """.as[
        (
            Long,
            Long,
            Long,
            Option[String],
            String,
            String,
            Timestamp
        )
      ]
    ).map(_.map(mapPanne))
  }

  // =====================================================
  // 8. MODIFIER LE STATUT D'UNE PANNE
  // =====================================================

  def updateStatut(
      idPanne: Long,
      statut: String
  ): Future[Int] = {

    db.run(
      sql"""
        UPDATE PANNE
        SET STATUT = $statut
        WHERE ID_PANNE = $idPanne
      """.asUpdate
    )
  }

  // =====================================================
  // 9. SUPPRIMER UNE PANNE
  // =====================================================

  def delete(idPanne: Long): Future[Int] = {

    db.run(
      sql"""
        DELETE FROM PANNE
        WHERE ID_PANNE = $idPanne
      """.asUpdate
    )
  }

  // =====================================================
  // 10. VERIFIER L'EXISTENCE D'UNE PANNE
  // =====================================================

  def existsById(idPanne: Long): Future[Boolean] = {

    db.run(
      sql"""
        SELECT COUNT(*)
        FROM PANNE
        WHERE ID_PANNE = $idPanne
      """.as[Long]
    ).map(_.headOption.getOrElse(0L) > 0)
  }
}
