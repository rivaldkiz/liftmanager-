error id: file:///C:/Users/franc/Desktop/elevatormanager/app/views/components/dashbord/batimentSelect.scala.html:
file:///C:/Users/franc/Desktop/elevatormanager/app/views/components/dashbord/batimentSelect.scala.html
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1113
uri: file:///C:/Users/franc/Desktop/elevatormanager/app/views/components/dashbord/batimentSelect.scala.html
text:
```scala
@(batiments: Seq[models.Batiment],
  route: Call,
  selectedId: Option[Long] = None,
  selectId: String = "building-select")(implicit request: RequestHeader)

@selected = @{ selectedId.orElse(batiments.headOption.flatMap(_.idBatiment)) }
@adresse  = @{ batiments.find(_.idBatiment == selected).flatMap(_.adresse).getOrElse("") }

<section class="bg-surface-container-lowest rounded-xl p-6 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
  <div class="flex flex-col sm:flex-row sm:items-center gap-4 sm:gap-6 w-full">
    <label class="font-label-md text-label-md text-on-surface whitespace-nowrap font-medium" for="@selectId">Bâtiment</label>

    <div class="relative flex-1 max-w-xl">
      <select id="@selectId"
              data-route="@route.url"
              data-csrf="@helper.CSRF.getToken.value"
              class="w-full h-12 bg-surface-container-lowest text-on-surface font-body-md text-body-md rounded-lg px-3 appearance-none focus:outline-none shadow-sm cursor-pointer border border-outline-variant/30 pr-10 font-bold">
        @for(b <- batiments; i@@d <- b.idBatiment) {
          <option value="@id" data-adresse="@b.adresse.getOrElse("")" @if(selected.contains(id)) { selected }>
            @b.nom@b.adresse.map(a => s" — $a").getOrElse("")
          </option>
        }
      </select>
      <span class="material-symbols-outlined pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[20px]">expand_more</span>
    </div>
  </div>

  <div class="hidden lg:flex items-center text-on-surface-variant font-body-sm text-body-sm whitespace-nowrap">
    <span class="material-symbols-outlined text-[18px] mr-1 text-outline">location_on</span>
    <span id="@{selectId}-adresse">@adresse</span>
  </div>
</section>

<script>
  (function () {
    const select = document.getElementById("@selectId");
    const adresse = document.getElementById("@{selectId}-adresse");
    if (!select) return;

    select.addEventListener("change", async function () {
      const opt = select.options[select.selectedIndex];
      adresse.textContent = opt.dataset.adresse || "";
      select.disabled = true;

      try {
        const res = await fetch(select.dataset.route, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "Csrf-Token": select.dataset.csrf
          },
          body: JSON.stringify({ idBatiment: Number(select.value) })
        });

        if (!res.ok) throw new Error("HTTP " + res.status);
        window.location.reload(); // recharge étages et ascenseurs du nouveau bâtiment
      } catch (e) {
        console.error(e);
        alert("Impossible de changer de bâtiment.");
        select.disabled = false;
      }
    });
  })();
</script>
```


#### Short summary: 

empty definition using pc, found symbol in pc: 