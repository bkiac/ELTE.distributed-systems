# Osztott rendszerek beadandó feladat

A beadandó programban olyan játékot készítünk el, amelyben két ügynökség titkosügynökeit reprezentáljuk. A program legyen jó minőségű: a kód legyen rendezett, és a lehető leginkább helyes módon használja a tanult eszközöket.

A program működésének minden főbb lépése megtételekor írjon ki információt a sztenderd kimenetre arról, mi történik éppen.

Az ```agent.AgentMain``` főprogram parancssori paraméterként megkapja a titkosügynökök darabszámát (```n``` és ```m```), és elindít mindegyiküknek egy-egy szálat. A főprogram megkapja továbbá a programban szükséges várakozás hosszának alsó és felső korlátját ezredmásodpercben mérve (```t1``` és ```t2```).

Mindegyik titkosügynök adatai egy fájlban vannak leírva. A fájl neve ```agentO-U.txt```, ahol ```O``` értéke ```1``` vagy ```2``` attól függően, hogy melyik ügynökségbe tartozik az ügynök, és ```U``` az ügynök sorszáma. Például ```agent2-5.txt``` a második ügynökség ötös sorszámú ügynökéhez tartozik. A fájl két sort tartalmaz.

1. Az első sorban az ügynök álnevei szerepelnek szóközökkel elválasztva. Feltehető, hogy a játékban minden álnév különböző.
2. A második sor egy titkos üzenet egy szavát tartalmazza.

Az ügynökök induláskor véletlenszerűen választanak egy portot a ```20000..20100``` intervallumban. Ha a port foglalt, akkor újat választanak egész addig, amíg nem sikerült saját portot találniuk. Ezután mindegyik ügynök két tevékenységbe kezd bele egymástól függetlenül.

1. Egyrészt elkezd várni egy kapcsolatra legfeljebb t2 ideig (a hossz beállításához lásd setSoTimeout).
    * Ha az idő lejárt, vagy egy kapcsolat lezajlott, akkor bezárja a portot, és egy másik portot keres véletlenszerűen.
2. Másrészt véletlenszerűen választott (```t1..t2 ``` közötti ezredmásodperc) idő elteltével megpróbál egy másik szerverhez csatlakozni.
    * A szerver portját szintén a ```20000..20100``` intervallumból választja, persze a saját portja kivételével.
    * Akár sikerült kapcsolatot teremteni, akár nem, ```t1..t2``` idő elteltével újra próbálkozik.

Amikor kapcsolatba kerül egymással a két fél, a következők történnek.

1. A szerver elküldi az álnevei közül az egyiket véletlenszerűen.
2. Erre a kliens elküldi azt, hogy szerinte a szerver melyik ügynökséghez tartozik.
    * Ha a kliens már találkozott ezzel az álnévvel, akkor tudja a helyes választ erre a kérdésre, és azt küldi el, különben tippel.
3. Ha a kliens tévedett, akkor a szerver bontja a kapcsolatot.
4. Különben a szerver elküldi az ```OK``` szöveget.
5. A kliens, ha azonos ügynökséghez tartozik, elküldi az ```OK``` szöveget, majd mindketten elküldenek egy-egy titkos szöveget a másiknak, amit ismernek, és ezután bontják a kapcsolatot. A kapott titkot mindketten felveszik az ismert titkaik közé.
    * Ha a titkot már ismerte, aki megkapja, az nem baj. Az is előfordulhat, hogy ismerte, és már el is árulta valakinek.
    * Ez a küldés nem számít a titok elárulásának.
6. A kliens, ha a másik ügynökséghez tartozik, elküldi a ```???``` szöveget, majd egy számot, ami szerinte a másik ügynök sorszáma lehet. (Ha már találkozott vele, akkor olyan tippet nem ad, ami biztosan téves.)
    1. A szerver azonnal bontja a kapcsolatot, ha téves a sorszám.
    2. Ha helyes a sorszám, elküldi az általa ismert titkok egyikét. Ha többet is ismer, véletlenszerűen választja ki, melyiket árulja el, de csak olyat, amelyiket eddig még nem árult el.
        * Az elárult titok megmarad az ügynöknél, de meglesz a másik ügynöknél is.
    3. Ezután, ha már minden általa ismert titkot elárult, a szerveroldali ügynök le van tartóztatva, és nem végez több tevékenységet (bezárja a portját; nem fogad és nem kezdeményez több kapcsolatot).
    4. Ha van még legalább egy olyan titok, amelyet nem árult el, folytatja a tevékenységét.

A játéknak az alábbi esetekben van vége.

* Az egyik ügynökség összes tagját letartóztatták. A másik ügynökség győzött.
* Az egyik ügynök megszerezte a másik csapat összes titkát. Az ő ügynöksége győzött.

Amikor vége a játéknak, a program a lehető leggyorsabban lépjen ki, és a program végén íródjon ki a győztes ügynökség.

A programot "rendes eszközökkel" kell terminálásra bírni, pl. a Thread.stop nem megengedett.
