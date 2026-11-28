package repositories

import javax.inject._
import database.DatabaseConfig
import models.Ascenseur

import slick.jdbc.GetResult

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AscenseurRepository @Inject() (
    databaseConfig: DatabaseConfig
)(implicit ec: ExecutionContext) {

  private val db = databaseConfig.db
  private val profile = databaseConfig.profile

  import profile.api._

  // =====================================================
  // MAPPING SQL VERS ASCENSEUR
  // =====================================================

  implicit val ascenseurGetResult: GetResult[Ascenseur] =
    GetResult { r =>
      val idAscenseur = r.nextLong()
      val idBatiment = r.nextLong()
      val idSimulation = r.nextLongOption()

      val nom = r.nextString()
      val capacitePersonnes = r.nextInt()
      val chargeMax = r.nextBigDecimal()
      val position = r.nextInt()
      val direction = r.nextString()
      val etat = r.nextString()
      val isVirtual = r.nextInt() == 1

      Ascenseur(
        idAscenseur = Some(idAscenseur),
        idBatiment = idBatiment,
        idSimulation = idSimulation,
        nom = nom,
        capacitePersonnes = capacitePersonnes,
        chargeMax = chargeMax,
        position = position,
        direction = direction,
        etat = etat,
        isVirtual = isVirtual
      )
    }

  // =====================================================
  // 1. CREER UN ASCENSEUR
  // =====================================================

  def create(ascenseur: Ascenseur): Future[Long] = {

    val action = for {

      id <- sql"""
        SELECT SEQ_ASCENSEUR.NEXTVAL
        FROM DUAL
      """.as[Long].head

      _ <- {

        ascenseur.idSimulation match {

          case Some(idSimulation) =>

            sqlu"""
              INSERT INTO ASCENSEUR (
                ID_ASCENSEUR,
                ID_BATIMENT,
                ID_SIMULATION,
                NOM,
                CAPACITE_PERSONNES,
                CHARGE_MAX,
                POSITION,
                DIRECTION,
                ETAT,
                IS_VIRTUAL
              )
              VALUES (
                $id,
                ${ascenseur.idBatiment},
                $idSimulation,
                ${ascenseur.nom},
                ${ascenseur.capacitePersonnes},
                ${ascenseur.chargeMax},
                ${ascenseur.position},
                ${ascenseur.direction},
                ${ascenseur.etat},
                ${if (ascenseur.isVirtual) 1 else 0}
              )
            """

          case None =>

            sqlu"""
              INSERT INTO ASCENSEUR (
                ID_ASCENSEUR,
                ID_BATIMENT,
                ID_SIMULATION,
                NOM,
                CAPACITE_PERSONNES,
                CHARGE_MAX,
                POSITION,
                DIRECTION,
                ETAT,
                IS_VIRTUAL
              )
              VALUES (
                $id,
                ${ascenseur.idBatiment},
                NULL,
                ${ascenseur.nom},
                ${ascenseur.capacitePersonnes},
                ${ascenseur.chargeMax},
                ${ascenseur.position},
                ${ascenseur.direction},
                ${ascenseur.etat},
                ${if (ascenseur.isVirtual) 1 else 0}
              )
            """
        }
      }

    } yield id

    db.run(action.transactionally)
  }

  // =====================================================
  // 2. RECHERCHER UN ASCENSEUR PAR IDENTIFIANT
  // =====================================================

  def findById(id: Long): Future[Option[Ascenseur]] = {

    db.run(
      sql"""
        SELECT
          ID_ASCENSEUR,
          ID_BATIMENT,
          ID_SIMULATION,
          NOM,
          CAPACITE_PERSONNES,
          CHARGE_MAX,
          POSITION,
          DIRECTION,
          ETAT,
          IS_VIRTUAL
        FROM ASCENSEUR
        WHERE ID_ASCENSEUR = $id
      """.as[Ascenseur].headOption
    )
  }

  // =====================================================
  // 3. RECUPERER TOUS LES ASCENSEURS
  // =====================================================

  def findAll(): Future[Seq[Ascenseur]] = {

    db.run(
      sql"""
        SELECT
          ID_ASCENSEUR,
          ID_BATIMENT,
          ID_SIMULATION,
          NOM,
          CAPACITE_PERSONNES,
          CHARGE_MAX,
          POSITION,
          DIRECTION,
          ETAT,
          IS_VIRTUAL
        FROM ASCENSEUR
        ORDER BY ID_BATIMENT, NOM
      """.as[Ascenseur]
    )
  }

  // =====================================================
  // 4. RECUPERER LES ASCENSEURS D'UN BATIMENT
  // =====================================================

  def findByBatiment(
      idBatiment: Long
  ): Future[Seq[Ascenseur]] = {

    db.run(
      sql"""
        SELECT
          ID_ASCENSEUR,
          ID_BATIMENT,
          ID_SIMULATION,
          NOM,
          CAPACITE_PERSONNES,
          CHARGE_MAX,
          POSITION,
          DIRECTION,
          ETAT,
          IS_VIRTUAL
        FROM ASCENSEUR
        WHERE ID_BATIMENT = $idBatiment
        ORDER BY NOM
      """.as[Ascenseur]
    )
  }

  // =====================================================
  // 5. RECUPERER UNIQUEMENT LES ASCENSEURS REELS
  // =====================================================

  def findAllReels(): Future[Seq[Ascenseur]] = {

    db.run(
      sql"""
        SELECT
          ID_ASCENSEUR,
          ID_BATIMENT,
          ID_SIMULATION,
          NOM,
          CAPACITE_PERSONNES,
          CHARGE_MAX,
          POSITION,
          DIRECTION,
          ETAT,
          IS_VIRTUAL
        FROM ASCENSEUR
        WHERE IS_VIRTUAL = 0
        ORDER BY NOM
      """.as[Ascenseur]
    )
  }

  // =====================================================
  // 6. RECUPERER LES ASCENSEURS D'UNE SIMULATION
  // =====================================================

  def findBySimulation(
      idSimulation: Long
  ): Future[Seq[Ascenseur]] = {

    db.run(
      sql"""
        SELECT
          ID_ASCENSEUR,
          ID_BATIMENT,
          ID_SIMULATION,
          NOM,
          CAPACITE_PERSONNES,
          CHARGE_MAX,
          POSITION,
          DIRECTION,
          ETAT,
          IS_VIRTUAL
        FROM ASCENSEUR
        WHERE ID_SIMULATION = $idSimulation
        AND IS_VIRTUAL = 1
        ORDER BY NOM
      """.as[Ascenseur]
    )
  }

  // =====================================================
  // 7. MODIFIER LES CARACTERISTIQUES D'UN ASCENSEUR
  // =====================================================

  def update(ascenseur: Ascenseur): Future[Boolean] = {

    val id = ascenseur.idAscenseur.getOrElse(0L)

    db.run(
      sqlu"""
        UPDATE ASCENSEUR
        SET
          ID_BATIMENT = ${ascenseur.idBatiment},
          NOM = ${ascenseur.nom},
          CAPACITE_PERSONNES = ${ascenseur.capacitePersonnes},
          CHARGE_MAX = ${ascenseur.chargeMax}
        WHERE ID_ASCENSEUR = $id
      """
    ).map(_ > 0)
  }

  // =====================================================
  // 8. MODIFIER LA POSITION ET LA DIRECTION
  // =====================================================

  def updatePosition(
      id: Long,
      position: Int,
      direction: String
  ): Future[Boolean] = {

    db.run(
      sqlu"""
        UPDATE ASCENSEUR
        SET
          POSITION = $position,
          DIRECTION = $direction
        WHERE ID_ASCENSEUR = $id
      """
    ).map(_ > 0)
  }

  // =====================================================
  // 9. MODIFIER L'ETAT D'UN ASCENSEUR
  // =====================================================

  def updateEtat(
      id: Long,
      etat: String
  ): Future[Boolean] = {

    db.run(
      sqlu"""
        UPDATE ASCENSEUR
        SET ETAT = $etat
        WHERE ID_ASCENSEUR = $id
      """
    ).map(_ > 0)
  }

  // =====================================================
  // 10. SUPPRIMER UN ASCENSEUR
  // =====================================================

  def delete(id: Long): Future[Boolean] = {

    db.run(
      sqlu"""
        DELETE FROM ASCENSEUR
        WHERE ID_ASCENSEUR = $id
      """
    ).map(_ > 0)
  }

  // =====================================================
  // 11. VERIFIER L'EXISTENCE D'UN ASCENSEUR
  // =====================================================

  def existsById(id: Long): Future[Boolean] = {

    db.run(
      sql"""
        SELECT COUNT(*)
        FROM ASCENSEUR
        WHERE ID_ASCENSEUR = $id
      """.as[Long]
    ).map(_.headOption.getOrElse(0L) > 0)
  }

  // =====================================================
  // 12. COMPTER LES ASCENSEURS D'UN BATIMENT
  // =====================================================

  def countByBatiment(
      idBatiment: Long
  ): Future[Int] = {

    db.run(
      sql"""
        SELECT COUNT(*)
        FROM ASCENSEUR
        WHERE ID_BATIMENT = $idBatiment
      """.as[Long].head
    ).map(_.toInt)
  }

  // =====================================================
  // 13. COMPTER LES ASCENSEURS D'UNE SIMULATION
  // =====================================================

  def countBySimulation(
      idSimulation: Long
  ): Future[Int] = {

    db.run(
      sql"""
        SELECT COUNT(*)
        FROM ASCENSEUR
        WHERE ID_SIMULATION = $idSimulation
        AND IS_VIRTUAL = 1
      """.as[Long].head
    ).map(_.toInt)
  }
}
