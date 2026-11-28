error id: file:///C:/Users/franc/Desktop/elevatormanager/app/models/Ascenseur.scala:scala/Predef.String#
file:///C:/Users/franc/Desktop/elevatormanager/app/models/Ascenseur.scala
empty definition using pc, found symbol in pc: scala/Predef.String#
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -String#
	 -scala/Predef.String#
offset: 264
uri: file:///C:/Users/franc/Desktop/elevatormanager/app/models/Ascenseur.scala
text:
```scala
package models
import models.Enums

case class Ascenseur(
    idAscenseur: Option[Long],
    idBatiment: Long,
    idSimulation: Option[Long],
    nom: String,
    capacitePersonnes: Int,
    chargeMax: BigDecimal,
    position: Int = 0,
    direction: @@String = Enums.DirectionAscenseur.Arret,
    etat: String = Enums.EtatAscenseur.Arret,
    isVirtual: Boolean = false
)

```


#### Short summary: 

empty definition using pc, found symbol in pc: scala/Predef.String#