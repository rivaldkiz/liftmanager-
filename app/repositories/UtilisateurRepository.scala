package repositories

import javax.inject._
import database.DatabaseConfig
import models.Utilisateur

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UtilisateurRepository @Inject() (
    databaseConfig: DatabaseConfig
)(implicit ec: ExecutionContext) {

  // Configuration de la base de données
  private val db = databaseConfig.db
  private val profile = databaseConfig.profile

  import profile.api._

  // =====================================================
  // 1. RECHERCHER UN UTILISATEUR PAR SON EMAIL
  // =====================================================

  def findByEmail(email: String): Future[Option[Utilisateur]] = {

    db.run(
      sql"""
        SELECT ID_USER,
               ID_ROLE,
               ID_SIMULATION,
               NOM,
               EMAIL,
               MOT_DE_PASSE,
               TELEPHONE,
               STATUT,
               DATE_CREATION
        FROM UTILISATEUR
        WHERE EMAIL = $email
      """.as[
        (
            Long,
            Long,
            Option[Long],
            String,
            String,
            String,
            Option[String],
            String,
            java.sql.Timestamp
        )
      ]
    ).map(_.headOption.map {
      case (
            idUser,
            idRole,
            idSimulation,
            nom,
            email,
            motDePasse,
            telephone,
            statut,
            dateCreation
          ) =>
        Utilisateur(
          idUser = Some(idUser),
          idRole = idRole,
          idSimulation = idSimulation,
          nom = nom,
          email = email,
          motDePasse = motDePasse,
          telephone = telephone,
          statut = statut,
          dateCreation = dateCreation.toLocalDateTime
        )
    })
  }

  // =====================================================
  // 2. RECHERCHER UN UTILISATEUR PAR SON ID
  // =====================================================

  def findById(id: Long): Future[Option[Utilisateur]] = {

    db.run(
      sql"""
        SELECT ID_USER,
               ID_ROLE,
               ID_SIMULATION,
               NOM,
               EMAIL,
               MOT_DE_PASSE,
               TELEPHONE,
               STATUT,
               DATE_CREATION
        FROM UTILISATEUR
        WHERE ID_USER = $id
      """.as[
        (
            Long,
            Long,
            Option[Long],
            String,
            String,
            String,
            Option[String],
            String,
            java.sql.Timestamp
        )
      ]
    ).map(_.headOption.map {
      case (
            idUser,
            idRole,
            idSimulation,
            nom,
            email,
            motDePasse,
            telephone,
            statut,
            dateCreation
          ) =>
        Utilisateur(
          idUser = Some(idUser),
          idRole = idRole,
          idSimulation = idSimulation,
          nom = nom,
          email = email,
          motDePasse = motDePasse,
          telephone = telephone,
          statut = statut,
          dateCreation = dateCreation.toLocalDateTime
        )
    })
  }

  // =====================================================
  // 3. VERIFIER SI UN EMAIL EXISTE
  // =====================================================

  def emailExiste(email: String): Future[Boolean] = {

    db.run(
      sql"""
        SELECT COUNT(*)
        FROM UTILISATEUR
        WHERE EMAIL = $email
      """.as[Long]
    ).map(_.headOption.getOrElse(0L) > 0)
  }

  // =====================================================
  // 4. RECHERCHER L'ID D'UN ROLE PAR SON CODE
  // =====================================================

  def findIdRoleByCode(code: String): Future[Option[Long]] = {

    db.run(
      sql"""
        SELECT ID_ROLE
        FROM ROLE
        WHERE NOM_ROLE = $code
      """.as[Long]
    ).map(_.headOption)
  }

  // =====================================================
  // 5. CREER UN UTILISATEUR
  // =====================================================

  def create(utilisateur: Utilisateur): Future[Int] = {

    db.run(
      sqlu"""
        INSERT INTO UTILISATEUR (
          ID_ROLE,
          ID_SIMULATION,
          NOM,
          EMAIL,
          MOT_DE_PASSE,
          TELEPHONE,
          STATUT,
          DATE_CREATION
        )
        VALUES (
          ${utilisateur.idRole},
          ${utilisateur.idSimulation},
          ${utilisateur.nom},
          ${utilisateur.email},
          ${utilisateur.motDePasse},
          ${utilisateur.telephone},
          ${utilisateur.statut},
          ${java.sql.Timestamp.valueOf(utilisateur.dateCreation)}
        )
      """
    )
  }

  // =====================================================
  // 6. CHERCHER UN ROLE PAR SON ID
  // =====================================================

  def findRoleById(idRole: Long): Future[Option[String]] = {
    val query = sql"""
    SELECT NOM_ROLE
    FROM ROLE
    WHERE ID_ROLE = $idRole
  """.as[String].headOption

    db.run(query)
  }

  // =====================================================
// 7. VERIFIER SI UN UTILISATEUR EXISTE PAR SON ID
// =====================================================

  def existsById(idUser: Long): Future[Boolean] = {

    db.run(
      sql"""
      SELECT COUNT(*)
      FROM UTILISATEUR
      WHERE ID_USER = $idUser
    """.as[Long]
    ).map(_.headOption.getOrElse(0L) > 0)
  }

}
