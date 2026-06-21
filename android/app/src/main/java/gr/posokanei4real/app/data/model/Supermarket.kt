package gr.posokanei4real.app.data.model

data class Supermarket(
    val id: String,
    val name: String,
    val url: String,
    val logoRes: Int = 0,
) {
    companion object {
        val ALL = listOf(
            Supermarket("ab", "AB Βασιλόπουλος", "https://www.ab.gr"),
            Supermarket("sklavenitis", "Σκλαβενίτης", "https://www.sklavenitis.gr"),
            Supermarket("lidl", "Lidl", "https://www.lidl.gr"),
            Supermarket("masoutis", "Μασούτης", "https://www.masoutis.gr"),
            Supermarket("mymarket", "My Market", "https://www.mymarket.gr"),
            Supermarket("kritikos", "Κρητικός", "https://www.kritikos.gr"),
            Supermarket("bazaar", "Bazaar", "https://www.bazaar.com.gr"),
            Supermarket("chalkiadakis", "Χαλκιαδάκης", "https://www.chalkiadakis.gr"),
        )

        val MAP: Map<String, Supermarket> = ALL.associateBy { it.id }
    }
}
