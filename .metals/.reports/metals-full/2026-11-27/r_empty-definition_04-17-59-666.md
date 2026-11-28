error id: file:///C:/Users/franc/Desktop/elevatormanager/app/views/components/dashbord/appelActif.scala.html:
file:///C:/Users/franc/Desktop/elevatormanager/app/views/components/dashbord/appelActif.scala.html
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 97
uri: file:///C:/Users/franc/Desktop/elevatormanager/app/views/components/dashbord/appelActif.scala.html
text:
```scala
@(appel: Option[models.Appel],
  departNom: String,
  destinationNom: String,
  ascenseurNom: @@Option[String])(implicit request: RequestHeader)

@import helper._

<section class="bg-surface-container-lowest rounded-xl p-6 shadow-sm" id="active-call-card">
  <h2 class="font-headline-sm text-headline-sm text-on-surface mb-3">Mon appel</h2>

  @appel match {
    case None => {
      <p class="font-body-md text-body-md text-on-surface-variant">Aucun appel en cours.</p>
    }

    case Some(a) => {
      <div class="font-headline-sm text-headline-sm text-on-surface">
        @departNom → @destinationNom
      </div>

      <div class="mt-2">
        <span class="bg-secondary-fixed text-on-secondary-fixed-variant font-label-sm text-label-sm px-3 py-1 rounded-full inline-block">
          @ascenseurNom.map(n => s"Affecté à l'ascenseur $n").getOrElse("En attente d'affectation")
        </span>
      </div>

      <p class="font-body-md text-body-md text-on-surface-variant mt-2">
        Statut : @a.statut
      </p>

      <div class="mt-4">
        @if(a.statut == "AFFECTE") {
          <form method="POST" action="routes.AuthController.to_login()">
            @CSRF.formField
            <button type="submit"
              class="h-12 w-full bg-tertiary-container hover:bg-tertiary text-on-tertiary font-label-lg text-label-lg rounded-lg shadow-sm transition-colors">
              Embarquer
            </button>
          </form>
        }

        @if(a.statut == "EN_ATTENTE" || a.statut == "AFFECTE") {
          <form method="POST" action="routes.AuthController.to_login()">
            @CSRF.formField
            <button type="submit"
              class="font-body-sm text-body-sm text-outline hover:text-on-surface-variant underline block text-center mt-2 w-full bg-transparent">
              Annuler
            </button>
          </form>
        }
      </div>
    }
  }
</section>
```


#### Short summary: 

empty definition using pc, found symbol in pc: 