error id: file:///C:/Users/franc/Desktop/elevatormanager/app/repositories/BatimentRepository.scala:
file:///C:/Users/franc/Desktop/elevatormanager/app/repositories/BatimentRepository.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -javax/inject/statement.
	 -statement.
	 -scala/Predef.statement.
offset: 8736
uri: file:///C:/Users/franc/Desktop/elevatormanager/app/repositories/BatimentRepository.scala
text:
```scala
package repositories

import javax.inject._
import java.sql.Connection
import javax.sql.DataSource
import models.Batiment

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BatimentRepository @Inject() (
    dataSource: DataSource
)(implicit ec: ExecutionContext) {

  // Création d'une connexion
  private def withConnection[A](operation: Connection => A): A = {
    val connection = dataSource.getConnection

    try {
      operation(connection)
    } finally {
      connection.close()
    }
  }

  // 1. Créer un bâtiment
  def create(batiment: Batiment): Future[Long] = Future {
    withConnection { connection =>
      val sql =
        """
INSERT INTO BATIMENT
(ID_BATIMENT, NOM, ADRESSE, DESCRIPTION, STATUT)
VALUES (SEQ_BATIMENT.NEXTVAL, ?, ?, ?, ?)
""".stripMargin

      val statement = connection.prepareStatement(
        sql,
        Array("ID_BATIMENT")
      )

      try {
        statement.setString(1, batiment.nom)
        statement.setString(2, batiment.adresse.orNull)
        statement.setString(3, batiment.description.orNull)
        statement.setString(4, batiment.statut)

        statement.executeUpdate()

        val result = statement.getGeneratedKeys

        try {
          if (result.next()) result.getLong(1)
          else throw new Exception("Impossible de récupérer l'identifiant")
        } finally {
          result.close()
        }

      } finally {
        statement.close()
      }
    }
  }

  // 2. Rechercher un bâtiment par son identifiant
  def findById(id: Long): Future[Option[Batiment]] = Future {
    withConnection { connection =>
      val sql =
        """
SELECT ID_BATIMENT, NOM, ADRESSE, DESCRIPTION, STATUT
FROM BATIMENT
WHERE ID_BATIMENT = ?
""".stripMargin

      val statement = connection.prepareStatement(sql)

      try {
        statement.setLong(1, id)

        val result = statement.executeQuery()

        try {
          if (result.next()) {
            Some(
              Batiment(
                idBatiment = Some(result.getLong("ID_BATIMENT")),
                nom = result.getString("NOM"),
                adresse = Option(result.getString("ADRESSE")),
                description = Option(result.getString("DESCRIPTION")),
                statut = result.getString("STATUT")
              )
            )
          } else None
        } finally {
          result.close()
        }

      } finally {
        statement.close()
      }
    }
  }

  // 3. Récupérer tous les bâtiments
  def findAll(): Future[Seq[Batiment]] = Future {
    withConnection { connection =>
      val sql =
        """
SELECT ID_BATIMENT, NOM, ADRESSE, DESCRIPTION, STATUT
FROM BATIMENT
ORDER BY NOM
""".stripMargin

      val statement = connection.prepareStatement(sql)

      try {
        val result = statement.executeQuery()

        try {
          val buffer = scala.collection.mutable.ListBuffer[Batiment]()

          while (result.next()) {
            buffer += Batiment(
              idBatiment = Some(result.getLong("ID_BATIMENT")),
              nom = result.getString("NOM"),
              adresse = Option(result.getString("ADRESSE")),
              description = Option(result.getString("DESCRIPTION")),
              statut = result.getString("STATUT")
            )
          }

          buffer.toList
        } finally {
          result.close()
        }

      } finally {
        statement.close()
      }
    }
  }

  // 4. Récupérer les bâtiments actifs
  def findAllActifs(): Future[Seq[Batiment]] = Future {
    withConnection { connection =>
      val sql =
        """
SELECT ID_BATIMENT, NOM, ADRESSE, DESCRIPTION, STATUT
FROM BATIMENT
WHERE STATUT = ?
ORDER BY NOM
""".stripMargin

      val statement = connection.prepareStatement(sql)

      try {
        statement.setString(1, models.Enums.StatutBatiment.Actif)

        val result = statement.executeQuery()

        try {
          val buffer = scala.collection.mutable.ListBuffer[Batiment]()

          while (result.next()) {
            buffer += Batiment(
              Some(result.getLong("ID_BATIMENT")),
              result.getString("NOM"),
              Option(result.getString("ADRESSE")),
              Option(result.getString("DESCRIPTION")),
              result.getString("STATUT")
            )
          }

          buffer.toList
        } finally {
          result.close()
        }

      } finally {
        statement.close()
      }
    }
  }

  // 5. Modifier un bâtiment
  def update(batiment: Batiment): Future[Boolean] = Future {
    withConnection { connection =>
      val sql =
        """
UPDATE BATIMENT
SET NOM = ?, ADRESSE = ?, DESCRIPTION = ?, STATUT = ?
WHERE ID_BATIMENT = ?
""".stripMargin

      val statement = connection.prepareStatement(sql)

      try {
        statement.setString(1, batiment.nom)
        statement.setString(2, batiment.adresse.orNull)
        statement.setString(3, batiment.description.orNull)
        statement.setString(4, batiment.statut)
        statement.setLong(5, batiment.idBatiment.getOrElse(0L))

        statement.executeUpdate() > 0

      } finally {
        statement.close()
      }
    }
  }

  // 6. Supprimer un bâtiment
  def delete(id: Long): Future[Boolean] = Future {
    withConnection { connection =>
      val sql = "DELETE FROM BATIMENT WHERE ID_BATIMENT = ?"

      val statement = connection.prepareStatement(sql)

      try {
        statement.setLong(1, id)
        statement.executeUpdate() > 0
      } finally {
        statement.close()
      }
    }
  }

  // 7. Vérifier l'existence d'un bâtiment
  def existsById(id: Long): Future[Boolean] = Future {
    withConnection { connection =>
      val sql =
        "SELECT COUNT(*) FROM BATIMENT WHERE ID_BATIMENT = ?"

      val statement = connection.prepareStatement(sql)

      try {
        statement.setLong(1, id)

        val result = statement.executeQuery()

        try {
          result.next()
          result.getInt(1) > 0
        } finally {
          result.close()
        }
      } finally {
        statement.close()
      }
    }
  }

  // 8. Vérifier si un nom existe
  def existsByNom(nom: String): Future[Boolean] = Future {
    withConnection { connection =>
      val sql =
        "SELECT COUNT(*) FROM BATIMENT WHERE LOWER(NOM) = LOWER(?)"

      val statement = connection.prepareStatement(sql)

      try {
        statement.setString(1, nom.trim)

        val result = statement.executeQuery()

        try {
          result.next()
          result.getInt(1) > 0
        } finally {
          result.close()
        }
      } finally {
        statement.close()
      }
    }
  }

  // 9. Rechercher par nom ou adresse
  def search(motCle: String): Future[Seq[Batiment]] = Future {
    withConnection { connection =>
      val sql =
        """
SELECT ID_BATIMENT, NOM, ADRESSE, DESCRIPTION, STATUT
FROM BATIMENT
WHERE LOWER(NOM) LIKE LOWER(?)
OR LOWER(ADRESSE) LIKE LOWER(?)
ORDER BY NOM
""".stripMargin

      val statement = connection.prepareStatement(sql)

      try {
        val recherche = s"%${motCle.trim}%"

        statement.setString(1, recherche)
        statement.setString(2, recherche)

        val result = statement.executeQuery()

        try {
          val buffer = scala.collection.mutable.ListBuffer[Batiment]()

          while (result.next()) {
            buffer += Batiment(
              Some(result.getLong("ID_BATIMENT")),
              result.getString("NOM"),
              Option(result.getString("ADRESSE")),
              Option(result.getString("DESCRIPTION")),
              result.getString("STATUT")
            )
          }

          buffer.toList
        } finally {
          result.close()
        }

      } finally {
        statement.close()
      }
    }
  }

  // 10. Modifier uniquement le statut
  def updateStatut(id: Long, statut: String): Future[Boolean] = Future {
    withConnection { connection =>
      val sql =
        "UPDATE BATIMENT SET STATUT = ? WHERE ID_BATIMENT = ?"

      val statement = connection.prepareStatement(sql)

      try {
        statement.setString(1, statut)
        statemen@@t.setLong(2, id)

        statement.executeUpdate() > 0
      } finally {
        statement.close()
      }
    }
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 