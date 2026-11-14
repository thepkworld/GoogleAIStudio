package com.example

import com.example.data.catalog.CatalogRepository
import com.example.data.model.MediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogRepositoryTest {

    @Test
    fun catalog_hasUniqueIds_andBothMoviesAndSeries() {
        val titles = CatalogRepository.allTitles()
        assertTrue(titles.size >= 16)
        assertEquals(titles.size, titles.map { it.id }.distinct().size)
        assertTrue(titles.any { it.mediaType == MediaType.MOVIE })
        assertTrue(titles.any { it.mediaType == MediaType.SERIES })
        assertTrue(titles.all { it.synopsis.isNotBlank() && it.cast.isNotEmpty() && it.genres.isNotEmpty() })
    }
}
