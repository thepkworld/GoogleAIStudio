package com.example.data.catalog

import com.example.data.model.CastMember
import com.example.data.model.CatalogTitle
import com.example.data.model.MediaType

object CatalogRepository {

    private val curatedTitles: List<CatalogTitle> = listOf(
        CatalogTitle(
            id = "dune_part_two",
            title = "Dune: Part Two",
            tagline = "Long live the fighters.",
            mediaType = MediaType.MOVIE,
            releaseYear = 2024,
            genres = listOf("Sci-Fi", "Drama", "Adventure"),
            directorOrCreator = "Denis Villeneuve",
            studioOrNetwork = "Legendary Pictures",
            country = "United States / Canada",
            language = "English",
            ageRating = "PG-13",
            runtimeOrSeasons = "2h 46m",
            totalEpisodes = 0,
            criticScore = 8.8,
            synopsis = "Paul Atreides unites with Chani and the Fremen while on a warpath of revenge against the conspirators who destroyed his family. Facing a choice between the love of his life and the fate of the known universe, he endeavors to prevent a terrible future only he can foresee.",
            posterKey = "scifi",
            cast = listOf(
                CastMember("Timothée Chalamet", "Paul Atreides"),
                CastMember("Zendaya", "Chani"),
                CastMember("Rebecca Ferguson", "Lady Jessica"),
                CastMember("Javier Bardem", "Stilgar"),
                CastMember("Austin Butler", "Feyd-Rautha")
            ),
            awardsHighlight = "Academy Award Winner · Best Sound & Visual Effects",
            isFeatured = true
        ),
        CatalogTitle(
            id = "severance",
            title = "Severance",
            tagline = "There's more to work than life.",
            mediaType = MediaType.SERIES,
            releaseYear = 2022,
            genres = listOf("Sci-Fi", "Mystery", "Thriller"),
            directorOrCreator = "Dan Erickson",
            studioOrNetwork = "Apple TV+",
            country = "United States",
            language = "English",
            ageRating = "TV-MA",
            runtimeOrSeasons = "2 Seasons · 19 Episodes",
            totalEpisodes = 19,
            criticScore = 8.9,
            synopsis = "Mark leads a team of office workers whose memories have been surgically divided between their work and personal lives. When a mysterious colleague appears outside of work, it begins a journey to discover the truth about their jobs at Lumon Industries.",
            posterKey = "noir",
            cast = listOf(
                CastMember("Adam Scott", "Mark Scout"),
                CastMember("Britt Lower", "Helly Riggs"),
                CastMember("Zach Cherry", "Dylan George"),
                CastMember("John Turturro", "Irving Bailiff"),
                CastMember("Patricia Arquette", "Harmony Cobel")
            ),
            awardsHighlight = "Peabody Award Winner · 14 Emmy Nominations",
            isFeatured = true
        ),
        CatalogTitle(
            id = "past_lives",
            title = "Past Lives",
            tagline = "What if this is a past life as well, and we are already something else to each other in our next life?",
            mediaType = MediaType.MOVIE,
            releaseYear = 2023,
            genres = listOf("Drama", "Romance"),
            directorOrCreator = "Celine Song",
            studioOrNetwork = "A24",
            country = "United States / South Korea",
            language = "Korean / English",
            ageRating = "PG-13",
            runtimeOrSeasons = "1h 46m",
            totalEpisodes = 0,
            criticScore = 8.4,
            synopsis = "Nora and Hae Sung, two deeply connected childhood friends, are wrested apart after Nora's family emigrates from South Korea. Two decades later, they are reunited in New York for one fateful week as they confront destiny, love, and the choices that make a life.",
            posterKey = "drama",
            cast = listOf(
                CastMember("Greta Lee", "Nora Moon"),
                CastMember("Teo Yoo", "Hae Sung"),
                CastMember("John Magaro", "Arthur Zaturansky")
            ),
            awardsHighlight = "Academy Award Nominee · Best Picture"
        ),
        CatalogTitle(
            id = "shogun_2024",
            title = "Shōgun",
            tagline = "An epic saga of war, passion, and power.",
            mediaType = MediaType.SERIES,
            releaseYear = 2024,
            genres = listOf("Drama", "History", "Action"),
            directorOrCreator = "Rachel Kondo & Justin Marks",
            studioOrNetwork = "FX / Hulu",
            country = "United States / Japan",
            language = "Japanese / English",
            ageRating = "TV-MA",
            runtimeOrSeasons = "1 Season · 10 Episodes",
            totalEpisodes = 10,
            criticScore = 9.0,
            synopsis = "In 1600 Japan, Lord Yoshii Toranaga fights for his life as his enemies on the Council of Regents unite against him, when a mysterious European ship is found marooned in a nearby fishing village.",
            posterKey = "drama",
            cast = listOf(
                CastMember("Hiroyuki Sanada", "Lord Yoshii Toranaga"),
                CastMember("Cosmo Jarvis", "John Blackthorne"),
                CastMember("Anna Sawai", "Toda Mariko"),
                CastMember("Tadanobu Asano", "Kashigi Yabushige")
            ),
            awardsHighlight = "Record-Breaking 18 Emmy Awards",
            isFeatured = true
        ),
        CatalogTitle(
            id = "blade_runner_2049",
            title = "Blade Runner 2049",
            tagline = "More human than human was our motto.",
            mediaType = MediaType.MOVIE,
            releaseYear = 2017,
            genres = listOf("Sci-Fi", "Noir & Thriller", "Mystery"),
            directorOrCreator = "Denis Villeneuve",
            studioOrNetwork = "Alcon Entertainment",
            country = "United States",
            language = "English",
            ageRating = "R",
            runtimeOrSeasons = "2h 44m",
            totalEpisodes = 0,
            criticScore = 8.6,
            synopsis = "Young Blade Runner K's discovery of a long-buried secret leads him to track down former Blade Runner Rick Deckard, who has been missing for thirty years.",
            posterKey = "noir",
            cast = listOf(
                CastMember("Ryan Gosling", "Officer K"),
                CastMember("Harrison Ford", "Rick Deckard"),
                CastMember("Ana de Armas", "Joi"),
                CastMember("Sylvia Hoeks", "Luv")
            ),
            awardsHighlight = "Academy Award Winner · Best Cinematography"
        ),
        CatalogTitle(
            id = "the_bear",
            title = "The Bear",
            tagline = "Every second counts.",
            mediaType = MediaType.SERIES,
            releaseYear = 2022,
            genres = listOf("Drama", "Comedy"),
            directorOrCreator = "Christopher Storer",
            studioOrNetwork = "FX",
            country = "United States",
            language = "English",
            ageRating = "TV-MA",
            runtimeOrSeasons = "3 Seasons · 28 Episodes",
            totalEpisodes = 28,
            criticScore = 8.6,
            synopsis = "A young chef from the fine dining world returns to Chicago to run his family's sandwich shop after a heartbreaking death in his family.",
            posterKey = "cinema",
            cast = listOf(
                CastMember("Jeremy Allen White", "Carmen Berzatto"),
                CastMember("Ayo Edebiri", "Sydney Adamu"),
                CastMember("Ebon Moss-Bachrach", "Richard Jerimovich"),
                CastMember("Abby Elliott", "Natalie Berzatto")
            ),
            awardsHighlight = "Emmy Winner · Outstanding Comedy Series"
        ),
        CatalogTitle(
            id = "anatomy_of_a_fall",
            title = "Anatomy of a Fall",
            tagline = "Did she do it?",
            mediaType = MediaType.MOVIE,
            releaseYear = 2023,
            genres = listOf("Crime", "Drama", "Mystery"),
            directorOrCreator = "Justine Triet",
            studioOrNetwork = "Les Films Pelléas / Neon",
            country = "France",
            language = "French / English",
            ageRating = "R",
            runtimeOrSeasons = "2h 31m",
            totalEpisodes = 0,
            criticScore = 8.5,
            synopsis = "A woman is suspected of her husband's murder, and their blind son faces a moral dilemma as the main witness during an intense courtroom dissection of their marriage.",
            posterKey = "noir",
            cast = listOf(
                CastMember("Sandra Hüller", "Sandra Voyter"),
                CastMember("Swann Arlaud", "Vincent Renzi"),
                CastMember("Milo Machado-Graner", "Daniel Maleski")
            ),
            awardsHighlight = "Palme d'Or Winner · Academy Award Best Original Screenplay"
        ),
        CatalogTitle(
            id = "andor",
            title = "Andor",
            tagline = "Rebellion begins with a single spark.",
            mediaType = MediaType.SERIES,
            releaseYear = 2022,
            genres = listOf("Sci-Fi", "Action", "Noir & Thriller"),
            directorOrCreator = "Tony Gilroy",
            studioOrNetwork = "Lucasfilm / Disney+",
            country = "United Kingdom / United States",
            language = "English",
            ageRating = "TV-14",
            runtimeOrSeasons = "1 Season · 12 Episodes",
            totalEpisodes = 12,
            criticScore = 8.7,
            synopsis = "In an era filled with danger, deception and intrigue, Cassian Andor embarks on a path that is destined to turn him into a rebel hero against an expanding authoritarian empire.",
            posterKey = "scifi",
            cast = listOf(
                CastMember("Diego Luna", "Cassian Andor"),
                CastMember("Stellan Skarsgård", "Luthen Rael"),
                CastMember("Genevieve O'Reilly", "Mon Mothma"),
                CastMember("Andy Serkis", "Kino Loy")
            ),
            awardsHighlight = "Peabody Award Winner"
        ),
        CatalogTitle(
            id = "portrait_of_a_lady_on_fire",
            title = "Portrait of a Lady on Fire",
            tagline = "Don't regret. Remember.",
            mediaType = MediaType.MOVIE,
            releaseYear = 2019,
            genres = listOf("Drama", "Romance"),
            directorOrCreator = "Céline Sciamma",
            studioOrNetwork = "Lilies Films",
            country = "France",
            language = "French",
            ageRating = "R",
            runtimeOrSeasons = "2h 02m",
            totalEpisodes = 0,
            criticScore = 8.7,
            synopsis = "On an isolated island in Brittany at the end of the eighteenth century, a female painter is obliged to paint a wedding portrait of a young woman without her knowing.",
            posterKey = "drama",
            cast = listOf(
                CastMember("Noémie Merlant", "Marianne"),
                CastMember("Adèle Haenel", "Héloïse"),
                CastMember("Luàna Bajrami", "Sophie")
            ),
            awardsHighlight = "Cannes Best Screenplay Winner"
        ),
        CatalogTitle(
            id = "true_detective_s1",
            title = "True Detective",
            tagline = "Touch darkness and darkness touches you back.",
            mediaType = MediaType.SERIES,
            releaseYear = 2014,
            genres = listOf("Crime", "Mystery", "Noir & Thriller"),
            directorOrCreator = "Nic Pizzolatto",
            studioOrNetwork = "HBO",
            country = "United States",
            language = "English",
            ageRating = "TV-MA",
            runtimeOrSeasons = "4 Seasons · 30 Episodes",
            totalEpisodes = 30,
            criticScore = 8.9,
            synopsis = "An anthology series in which police investigations unearth the personal and professional secrets of those involved, both within and outside the law.",
            posterKey = "noir",
            cast = listOf(
                CastMember("Matthew McConaughey", "Rustin Cohle"),
                CastMember("Woody Harrelson", "Martin Hart"),
                CastMember("Michelle Monaghan", "Maggie Hart")
            ),
            awardsHighlight = "Critics' Choice & Golden Globe Nominee"
        ),
        CatalogTitle(
            id = "spider_man_across_spiderverse",
            title = "Spider-Man: Across the Spider-Verse",
            tagline = "It's how you wear the mask that matters.",
            mediaType = MediaType.MOVIE,
            releaseYear = 2023,
            genres = listOf("Animation", "Action", "Sci-Fi"),
            directorOrCreator = "Joaquim Dos Santos, Kemp Powers & Justin K. Thompson",
            studioOrNetwork = "Sony Pictures Animation",
            country = "United States",
            language = "English",
            ageRating = "PG",
            runtimeOrSeasons = "2h 20m",
            totalEpisodes = 0,
            criticScore = 8.8,
            synopsis = "Miles Morales catapults across the Multiverse, where he encounters a team of Spider-People charged with protecting its very existence when a new threat forces him to redefine what it means to be a hero.",
            posterKey = "scifi",
            cast = listOf(
                CastMember("Shameik Moore", "Miles Morales"),
                CastMember("Hailee Steinfeld", "Gwen Stacy"),
                CastMember("Oscar Isaac", "Miguel O'Hara"),
                CastMember("Daniel Kaluuya", "Hobie Brown")
            ),
            awardsHighlight = "Annie Award Winner · Best Feature"
        ),
        CatalogTitle(
            id = "succession",
            title = "Succession",
            tagline = "Make your own reality.",
            mediaType = MediaType.SERIES,
            releaseYear = 2018,
            genres = listOf("Drama", "Comedy"),
            directorOrCreator = "Jesse Armstrong",
            studioOrNetwork = "HBO",
            country = "United States",
            language = "English",
            ageRating = "TV-MA",
            runtimeOrSeasons = "4 Seasons · 39 Episodes",
            totalEpisodes = 39,
            criticScore = 9.1,
            synopsis = "The Roy family is known for controlling the biggest media and entertainment company in the world. However, their world changes when their aging patriarch steps down from the company.",
            posterKey = "cinema",
            cast = listOf(
                CastMember("Brian Cox", "Logan Roy"),
                CastMember("Jeremy Strong", "Kendall Roy"),
                CastMember("Sarah Snook", "Shiv Roy"),
                CastMember("Kieran Culkin", "Roman Roy")
            ),
            awardsHighlight = "3x Emmy Winner · Outstanding Drama Series"
        ),
        CatalogTitle(
            id = "arrival",
            title = "Arrival",
            tagline = "Why are they here?",
            mediaType = MediaType.MOVIE,
            releaseYear = 2016,
            genres = listOf("Sci-Fi", "Drama", "Mystery"),
            directorOrCreator = "Denis Villeneuve",
            studioOrNetwork = "Paramount Pictures",
            country = "United States",
            language = "English / Mandarin",
            ageRating = "PG-13",
            runtimeOrSeasons = "1h 56m",
            totalEpisodes = 0,
            criticScore = 8.6,
            synopsis = "A linguist works with the military to communicate with alien lifeforms after twelve mysterious spacecraft appear around the world, uncovering a secret that reshapes her perception of time.",
            posterKey = "scifi",
            cast = listOf(
                CastMember("Amy Adams", "Louise Banks"),
                CastMember("Jeremy Renner", "Ian Donnelly"),
                CastMember("Forest Whitaker", "Colonel Weber")
            ),
            awardsHighlight = "Academy Award Winner · Best Sound Editing"
        ),
        CatalogTitle(
            id = "arcane",
            title = "Arcane",
            tagline = "Every legend has a beginning.",
            mediaType = MediaType.SERIES,
            releaseYear = 2021,
            genres = listOf("Animation", "Fantasy", "Action"),
            directorOrCreator = "Christian Linke & Alex Yee",
            studioOrNetwork = "Fortiche / Netflix",
            country = "France / United States",
            language = "English",
            ageRating = "TV-14",
            runtimeOrSeasons = "2 Seasons · 18 Episodes",
            totalEpisodes = 18,
            criticScore = 9.0,
            synopsis = "Set in the utopian region of Piltover and the oppressed underground of Zaun, the story follows the origins of two iconic champions and the power that will tear them apart.",
            posterKey = "noir",
            cast = listOf(
                CastMember("Hailee Steinfeld", "Vi"),
                CastMember("Ella Purnell", "Jinx"),
                CastMember("Kevin Alejandro", "Jayce Talis"),
                CastMember("Katie Leung", "Caitlyn Kiramman")
            ),
            awardsHighlight = "Emmy Winner · Outstanding Animated Program"
        ),
        CatalogTitle(
            id = "parasite",
            title = "Parasite",
            tagline = "Act like you own the place.",
            mediaType = MediaType.MOVIE,
            releaseYear = 2019,
            genres = listOf("Noir & Thriller", "Drama", "Crime"),
            directorOrCreator = "Bong Joon-ho",
            studioOrNetwork = "Barunson E&A / CJ Entertainment",
            country = "South Korea",
            language = "Korean",
            ageRating = "R",
            runtimeOrSeasons = "2h 12m",
            totalEpisodes = 0,
            criticScore = 9.2,
            synopsis = "Greed and class discrimination threaten the newly formed symbiotic relationship between the wealthy Park family and the destitute Kim clan.",
            posterKey = "drama",
            cast = listOf(
                CastMember("Song Kang-ho", "Kim Ki-taek"),
                CastMember("Lee Sun-kyun", "Park Dong-ik"),
                CastMember("Cho Yeo-jeong", "Choi Yeon-gyo"),
                CastMember("Park So-dam", "Kim Ki-jung")
            ),
            awardsHighlight = "4 Academy Awards including Best Picture"
        ),
        CatalogTitle(
            id = "dark_series",
            title = "Dark",
            tagline = "The question is not where, but when.",
            mediaType = MediaType.SERIES,
            releaseYear = 2017,
            genres = listOf("Sci-Fi", "Mystery", "Drama"),
            directorOrCreator = "Baran bo Odar & Jantje Friese",
            studioOrNetwork = "Netflix",
            country = "Germany",
            language = "German",
            ageRating = "TV-MA",
            runtimeOrSeasons = "3 Seasons · 26 Episodes",
            totalEpisodes = 26,
            criticScore = 8.9,
            synopsis = "A family saga with a supernatural twist, set in a German town where the disappearance of two young children exposes the double lives and fractured relationships among four families across generations.",
            posterKey = "scifi",
            cast = listOf(
                CastMember("Louis Hofmann", "Jonas Kahnwald"),
                CastMember("Lisa Vicari", "Martha Nielsen"),
                CastMember("Andreas Pietschmann", "The Stranger")
            ),
            awardsHighlight = "Grimme-Preis Winner"
        )
    )

    val allGenres: List<String> = listOf(
        "All",
        "Sci-Fi",
        "Drama",
        "Noir & Thriller",
        "Mystery",
        "Crime",
        "Action",
        "Animation",
        "Fantasy",
        "Romance",
        "Comedy"
    )

    fun allTitles(): List<CatalogTitle> = curatedTitles

    fun featuredTitle(): CatalogTitle = curatedTitles.first()

    fun findById(id: String): CatalogTitle? = curatedTitles.firstOrNull { it.id == id }
}
