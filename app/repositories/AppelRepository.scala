package repositories

import javax.inject._
import database.DatabaseConfig
import models.{Appel, Enums}
import slick.jdbc.GetResult

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AppelRepository @Inject() (
    databaseConfig: DatabaseConfig
)(implicit ec: ExecutionContext) {

  // Configuration de la base de données
  private val db = databaseConfig.db
  private val profile = databaseConfig.profile

  import profile.api._

  // Mapping des résultats SQL vers Appel
  implicit val appelGetResult: GetResult[Appel] =
    GetResult(r => {

      val idAppel = r.nextLong()
      val idUser = r.nextLong()
      val depart = r.nextLong()
      val destination = r.nextLong()
      val idSimulation = r.nextLongOption()
      val direction = r.nextString()
      val statut = r.nextString()
      val avecBagages = r.nextInt() == 1
      val dateAppel = r.nextTimestamp().toLocalDateTime
      val scoreAttribution: Option[BigDecimal] = r.nextBigDecimalOption()

      Appel(
        idAppel = Some(idAppel),
        idUser = idUser,
        depart = depart,
        destination = destination,
        idSimulation = idSimulation,
        direction = direction,
        statut = statut,
        avecBagages = avecBagages,
        dateAppel = dateAppel,
        scoreAttribution = scoreAttribution
      )
    })

  // Colonnes communes aux requêtes
  private val colonnes =
    """
      ID_APPEL,
      ID_USER,
      DEPART,
      DESTINATION,
      ID_SIMULATION,
      DIRECTION,
      STATUT,
      AVEC_BAGAGES,
      DATE_APPEL,
      SCORE_ATTRIBUTION
    """

  // 1. Création d'un appel
  def create(appel: Appel): Future[Long] = {

    val action = for {
      // Récupération du prochain identifiant
      id <- sql"""
      SELECT SEQ_APPEL.NEXTVAL
      FROM DUAL
    """.as[Long].head

      // Insertion de l'appel
      _ <- sqlu"""
      INSERT INTO APPEL (
        ID_APPEL,
        ID_USER,
        DEPART,
        DESTINATION,
        ID_SIMULATION,
        DIRECTION,
        STATUT,
        AVEC_BAGAGES,
        DATE_APPEL,
        SCORE_ATTRIBUTION
      )
      VALUES (
        $id,
        ${appel.idUser},
        ${appel.depart},
        ${appel.destination},
        ${appel.idSimulation},
        ${appel.direction},
        ${appel.statut},
        ${if (appel.avecBagages) 1 else 0},
        ${java.sql.Timestamp.valueOf(appel.dateAppel)},
        ${appel.scoreAttribution}
      )
    """
    } yield id

    db.run(action.transactionally)
  }

  // 2. Recherche par identifiant
  def findById(id: Long): Future[Option[Appel]] = {

    db.run(
      sql"""
        SELECT #$colonnes
        FROM APPEL
        WHERE ID_APPEL = $id
      """.as[Appel].headOption
    )
  }

  // 3. Récupérer tous les appels
  def findAll(): Future[Seq[Appel]] = {

    db.run(
      sql"""
        SELECT #$colonnes
        FROM APPEL
        ORDER BY DATE_APPEL DESC
      """.as[Appel]
    )
  }

  // 4. Récupérer les appels d'un utilisateur
  def findByUser(idUser: Long): Future[Seq[Appel]] = {

    db.run(
      sql"""
        SELECT #$colonnes
        FROM APPEL
        WHERE ID_USER = $idUser
        ORDER BY DATE_APPEL DESC
      """.as[Appel]
    )
  }

  // 5. Récupérer les appels en attente réels
  def findEnAttenteReels(): Future[Seq[Appel]] = {

    val statut = Enums.StatutAppel.EnAttente

    db.run(
      sql"""
        SELECT #$colonnes
        FROM APPEL
        WHERE STATUT = $statut
        AND ID_SIMULATION IS NULL
        ORDER BY DATE_APPEL ASC
      """.as[Appel]
    )
  }

  // 6. Récupérer les appels d'une simulation
  def findBySimulation(
      idSimulation: Long
  ): Future[Seq[Appel]] = {

    db.run(
      sql"""
        SELECT #$colonnes
        FROM APPEL
        WHERE ID_SIMULATION = $idSimulation
        ORDER BY DATE_APPEL DESC
      """.as[Appel]
    )
  }

  // 7. Récupérer les appels en attente d'une simulation
  def findEnAttenteParSimulation(
      idSimulation: Long
  ): Future[Seq[Appel]] = {

    val statut = Enums.StatutAppel.EnAttente

    db.run(
      sql"""
        SELECT #$colonnes
        FROM APPEL
        WHERE ID_SIMULATION = $idSimulation
        AND STATUT = $statut
        ORDER BY DATE_APPEL ASC
      """.as[Appel]
    )
  }

  // 8. Modifier le statut d'un appel
  def updateStatut(
      idAppel: Long,
      nouveauStatut: String
  ): Future[Int] = {

    db.run(
      sqlu"""
        UPDATE APPEL
        SET STATUT = $nouveauStatut
        WHERE ID_APPEL = $idAppel
      """
    )
  }

  // 9. Modifier le score d'attribution
  def updateScoreAttribution(
      idAppel: Long,
      score: BigDecimal
  ): Future[Int] = {

    db.run(
      sqlu"""
    UPDATE APPEL
    SET SCORE_ATTRIBUTION = $score
    WHERE ID_APPEL = $idAppel
  """
    )
  }

  // 10. Supprimer un appel
  def delete(idAppel: Long): Future[Int] = {

    db.run(
      sqlu"""
        DELETE FROM APPEL
        WHERE ID_APPEL = $idAppel
      """
    )
  }

  // 11. Vérifier l'existence d'un appel
  def existsById(idAppel: Long): Future[Boolean] = {

    db.run(
      sql"""
        SELECT COUNT(*)
        FROM APPEL
        WHERE ID_APPEL = $idAppel
      """.as[Long]
    ).map(_.headOption.getOrElse(0L) > 0)
  }
}
