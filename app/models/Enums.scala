package models

// =============================================================================
// 1. VALEURS PRÉDÉTERMINÉES (équivalent des ENUM ; les CHECK sont dans le SQL)
// =============================================================================
object Enums {
  object StatutUtilisateur {
    val Actif = "ACTIF"; val Bloque = "BLOQUE"; val Desactive = "DESACTIVE"
    val values = Seq(Actif, Bloque, Desactive)
  }
  object NomRole {
    val Admin = "ADMIN"; val Resident = "RESIDENT";
    val Technicien = "TECHNICIEN"; val Superviseur = "SUPERVISEUR"
    val values = Seq(Admin, Resident, Technicien, Superviseur)
  }
  object StatutBatiment {
    val Actif = "ACTIF"; val Inactif = "INACTIF"
    val values = Seq(Actif, Inactif)
  }
  object DirectionAscenseur {
    val Monte = "MONTEE"; val Descend = "DESCENTE"; val Arret = "ARRET"
    val values = Seq(Monte, Descend, Arret)
  }
  object EtatAscenseur {
    val Arret = "ARRET"; val Montee = "MONTEE"; val Descente = "DESCENTE";
    val Ouverture = "OUVERTURE"
    val Ouvert = "OUVERT"; val Fermeture = "FERMETURE"; val Panne = "PANNE";
    val Maintenance = "MAINTENANCE"
    val values = Seq(
      Arret,
      Montee,
      Descente,
      Ouverture,
      Ouvert,
      Fermeture,
      Panne,
      Maintenance
    )
  }
  object DirectionAppel {
    val Montee = "MONTEE"; val Descente = "DESCENTE"
    val values = Seq(Montee, Descente)
  }
  object StatutAppel {
    val EnAttente = "EN_ATTENTE"; val Affecte = "AFFECTE";
    val EnCours = "EN_COURS"
    val Termine = "TERMINE"; val Annule = "ANNULE"; val Refuse = "REFUSE"
    val values = Seq(EnAttente, Affecte, EnCours, Termine, Annule, Refuse)
  }
  object CategoriePanne {
    val Electrique = "ELECTRIQUE"; val Mecanique = "MECANIQUE";
    val Electronique = "ELECTRONIQUE"; val Autre = "AUTRE"
    val values = Seq(Electrique, Mecanique, Electronique, Autre)
  }
  object StatutPanne {
    val Signalee = "SIGNALEE"; val EnCours = "EN_COURS";
    val Resolue = "RESOLUE"; val Annulee = "ANNULEE"
    val values = Seq(Signalee, EnCours, Resolue, Annulee)
  }
  object TypeIntervention {
    val Preventive = "PREVENTIVE"; val Corrective = "CORRECTIVE"
    val values = Seq(Preventive, Corrective)
  }
  object StatutIntervention {
    val Planifiee = "PLANIFIEE"; val EnCours = "EN_COURS";
    val Terminee = "TERMINEE"; val Annulee = "ANNULEE"
    val values = Seq(Planifiee, EnCours, Terminee, Annulee)
  }
  object TypeNotification {
    val Appel = "APPEL"; val Ascenseur = "ASCENSEUR"; val Panne = "PANNE";
    val Maintenance = "MAINTENANCE"; val Systeme = "SYSTEME"
    val values = Seq(Appel, Ascenseur, Panne, Maintenance, Systeme)
  }
  object StatutSimulation {
    val Active = "ACTIVE"; val Arretee = "ARRETEE"; val Terminee = "TERMINEE"
    val values = Seq(Active, Arretee, Terminee)
  }
  object TypeAction {
    val Connexion = "CONNEXION"; val Deconnexion = "DECONNEXION";
    val AppelCree = "APPEL_CREE"
    val AppelAffecte = "APPEL_AFFECTE"; val Embarquement = "EMBARQUEMENT";
    val Debarquement = "DEBARQUEMENT"
    val AscenseurDeplace = "ASCENSEUR_DEPLACE";
    val PanneSignalee = "PANNE_SIGNALEE"; val PanneResolue = "PANNE_RESOLUE"
    val MaintenanceCreee = "MAINTENANCE_CREEE";
    val MaintenanceTerminee = "MAINTENANCE_TERMINEE"
    val SimulationDemarree = "SIMULATION_DEMARREE";
    val SimulationArretee = "SIMULATION_ARRETEE"
    val UtilisateurBloque = "UTILISATEUR_BLOQUE";
    val UtilisateurModifie = "UTILISATEUR_MODIFIE"
    val values = Seq(
      Connexion,
      Deconnexion,
      AppelCree,
      AppelAffecte,
      Embarquement,
      Debarquement,
      AscenseurDeplace,
      PanneSignalee,
      PanneResolue,
      MaintenanceCreee,
      MaintenanceTerminee,
      SimulationDemarree,
      SimulationArretee,
      UtilisateurBloque,
      UtilisateurModifie
    )
  }
}
