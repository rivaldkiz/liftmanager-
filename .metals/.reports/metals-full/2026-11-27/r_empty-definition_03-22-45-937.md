error id: file:///C:/Users/franc/Desktop/elevatormanager/app/repositories/AppelRepository.scala:
file:///C:/Users/franc/Desktop/elevatormanager/app/repositories/AppelRepository.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -javax/inject/rs/getInt.
	 -javax/inject/rs/getInt#
	 -javax/inject/rs/getInt().
	 -rs/getInt.
	 -rs/getInt#
	 -rs/getInt().
	 -scala/Predef.rs.getInt.
	 -scala/Predef.rs.getInt#
	 -scala/Predef.rs.getInt().
offset: 965
uri: file:///C:/Users/franc/Desktop/elevatormanager/app/repositories/AppelRepository.scala
text:
```scala
package repositories

import javax.inject._
import play.api.db.Database

import models.{Appel, Enums}

import java.sql.{Connection, PreparedStatement, ResultSet, Timestamp}
import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Using

@Singleton
class AppelRepository @Inject() (
    db: Database
)(implicit ec: ExecutionContext) {

  // Conversion ResultSet -> Appel
  private def mapAppel(rs: ResultSet): Appel = {

    val idSimulation = rs.getLong("ID_SIMULATION")
    val score = rs.getBigDecimal("SCORE_ATTRIBUTION")

    Appel(
      idAppel = Some(rs.getLong("ID_APPEL")),
      idUser = rs.getLong("ID_USER"),
      depart = rs.getLong("DEPART"),
      destination = rs.getLong("DESTINATION"),
      idSimulation =
        if (rs.wasNull()) None else Some(idSimulation),
      direction = rs.getString("DIRECTION"),
      statut = rs.getString("STATUT"),
      avecBagages = rs.g@@etInt("AVEC_BAGAGES") == 1,
      dateAppel = rs.getTimestamp("DATE_APPEL").toLocalDateTime,
      scoreAttribution = Option(score).map(BigDecimal(_))
    )
  }

  private val colonnes =
    """
      ID_APPEL, ID_USER, DEPART, DESTINATION,
      ID_SIMULATION, DIRECTION, STATUT,
      AVEC_BAGAGES, DATE_APPEL, SCORE_ATTRIBUTION
    """

  // Création d'un appel
  def create(appel: Appel): Future[Long] = Future {

    db.withConnection { connection =>
      // Récupération du prochain identifiant
      val sqlSequence =
        "SELECT SEQ_APPEL.NEXTVAL FROM DUAL"

      val id = Using.resource(
        connection.prepareStatement(sqlSequence)
      ) { statement =>
        Using.resource(statement.executeQuery()) { rs =>
          rs.next()
          rs.getLong(1)
        }
      }

      val sql =
        """
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
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """

      Using.resource(connection.prepareStatement(sql)) { statement =>
        statement.setLong(1, id)
        statement.setLong(2, appel.idUser)
        statement.setLong(3, appel.depart)
        statement.setLong(4, appel.destination)

        appel.idSimulation match {
          case Some(idSim) => statement.setLong(5, idSim)
          case None        => statement.setNull(5, java.sql.Types.NUMERIC)
        }

        statement.setString(6, appel.direction)
        statement.setString(7, appel.statut)
        statement.setInt(8, if (appel.avecBagages) 1 else 0)

        statement.setTimestamp(
          9,
          Timestamp.valueOf(appel.dateAppel)
        )

        appel.scoreAttribution match {
          case Some(score) =>
            statement.setBigDecimal(10, score.bigDecimal)
          case None =>
            statement.setNull(10, java.sql.Types.NUMERIC)
        }

        statement.executeUpdate()
      }

      id
    }
  }

  // Recherche par identifiant
  def findById(id: Long): Future[Option[Appel]] = Future {

    db.withConnection { connection =>
      val sql =
        s"SELECT $colonnes FROM APPEL WHERE ID_APPEL = ?"

      Using.resource(connection.prepareStatement(sql)) { statement =>
        statement.setLong(1, id)

        Using.resource(statement.executeQuery()) { rs =>
          if (rs.next()) Some(mapAppel(rs))
          else None
        }
      }
    }
  }

  // Tous les appels
  def findAll(): Future[Seq[Appel]] = Future {

    db.withConnection { connection =>
      val sql =
        s"SELECT $colonnes FROM APPEL ORDER BY DATE_APPEL DESC"

      Using.resource(connection.prepareStatement(sql)) { statement =>
        Using.resource(statement.executeQuery()) { rs =>
          val appels = scala.collection.mutable.ListBuffer[Appel]()

          while (rs.next()) {
            appels += mapAppel(rs)
          }

          appels.toSeq
        }
      }
    }
  }

  // Appels d'un utilisateur
  def findByUser(idUser: Long): Future[Seq[Appel]] = Future {

    db.withConnection { connection =>
      val sql =
        s"""
          SELECT $colonnes
          FROM APPEL
          WHERE ID_USER = ?
          ORDER BY DATE_APPEL DESC
        """

      Using.resource(connection.prepareStatement(sql)) { statement =>
        statement.setLong(1, idUser)

        Using.resource(statement.executeQuery()) { rs =>
          val appels = scala.collection.mutable.ListBuffer[Appel]()

          while (rs.next()) {
            appels += mapAppel(rs)
          }

          appels.toSeq
        }
      }
    }
  }

  // Appels en attente réels
  def findEnAttenteReels(): Future[Seq[Appel]] = Future {

    db.withConnection { connection =>
      val sql =
        s"""
          SELECT $colonnes
          FROM APPEL
          WHERE STATUT = ?
          AND ID_SIMULATION IS NULL
          ORDER BY DATE_APPEL ASC
        """

      Using.resource(connection.prepareStatement(sql)) { statement =>
        statement.setString(1, Enums.StatutAppel.EnAttente)

        Using.resource(statement.executeQuery()) { rs =>
          val appels = scala.collection.mutable.ListBuffer[Appel]()

          while (rs.next()) {
            appels += mapAppel(rs)
          }

          appels.toSeq
        }
      }
    }
  }

  // Appels d'une simulation
  def findBySimulation(idSimulation: Long): Future[Seq[Appel]] = Future {

    db.withConnection { connection =>
      val sql =
        s"""
          SELECT $colonnes
          FROM APPEL
          WHERE ID_SIMULATION = ?
          ORDER BY DATE_APPEL DESC
        """

      Using.resource(connection.prepareStatement(sql)) { statement =>
        statement.setLong(1, idSimulation)

        Using.resource(statement.executeQuery()) { rs =>
          val appels = scala.collection.mutable.ListBuffer[Appel]()

          while (rs.next()) {
            appels += mapAppel(rs)
          }

          appels.toSeq
        }
      }
    }
  }

  // Appels en attente d'une simulation
  def findEnAttenteParSimulation(
      idSimulation: Long
  ): Future[Seq[Appel]] = Future {

    db.withConnection { connection =>
      val sql =
        s"""
          SELECT $colonnes
          FROM APPEL
          WHERE ID_SIMULATION = ?
          AND STATUT = ?
          ORDER BY DATE_APPEL ASC
        """

      Using.resource(connection.prepareStatement(sql)) { statement =>
        statement.setLong(1, idSimulation)
        statement.setString(2, Enums.StatutAppel.EnAttente)

        Using.resource(statement.executeQuery()) { rs =>
          val appels = scala.collection.mutable.ListBuffer[Appel]()

          while (rs.next()) {
            appels += mapAppel(rs)
          }

          appels.toSeq
        }
      }
    }
  }

  // Modification du statut
  def updateStatut(
      idAppel: Long,
      nouveauStatut: String
  ): Future[Int] = Future {

    db.withConnection { connection =>
      val sql =
        "UPDATE APPEL SET STATUT = ? WHERE ID_APPEL = ?"

      Using.resource(connection.prepareStatement(sql)) { statement =>
        statement.setString(1, nouveauStatut)
        statement.setLong(2, idAppel)

        statement.executeUpdate()
      }
    }
  }

  // Modification du score d'attribution
  def updateScoreAttribution(
      idAppel: Long,
      score: BigDecimal
  ): Future[Int] = Future {

    db.withConnection { connection =>
      val sql =
        """
          UPDATE APPEL
          SET SCORE_ATTRIBUTION = ?
          WHERE ID_APPEL = ?
        """

      Using.resource(connection.prepareStatement(sql)) { statement =>
        statement.setBigDecimal(1, score.bigDecimal)
        statement.setLong(2, idAppel)

        statement.executeUpdate()
      }
    }
  }

  // Suppression d'un appel
  def delete(idAppel: Long): Future[Int] = Future {

    db.withConnection { connection =>
      val sql =
        "DELETE FROM APPEL WHERE ID_APPEL = ?"

      Using.resource(connection.prepareStatement(sql)) { statement =>
        statement.setLong(1, idAppel)
        statement.executeUpdate()
      }
    }
  }

  // Vérifier l'existence d'un appel
  def existsById(idAppel: Long): Future[Boolean] = Future {

    db.withConnection { connection =>
      val sql =
        "SELECT COUNT(*) FROM APPEL WHERE ID_APPEL = ?"

      Using.resource(connection.prepareStatement(sql)) { statement =>
        statement.setLong(1, idAppel)

        Using.resource(statement.executeQuery()) { rs =>
          rs.next()
          rs.getInt(1) > 0
        }
      }
    }
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 