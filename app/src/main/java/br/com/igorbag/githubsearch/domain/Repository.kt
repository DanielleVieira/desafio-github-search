package br.com.igorbag.githubsearch.domain

import com.google.gson.annotations.SerializedName

/**
 * Represents a GitHub repository.
 *
 * @property name The name of the repository.
 * @property htmlUrl The URL of the repository.
 */
data class Repository(
    val name: String,
    @SerializedName("html_url")
    val htmlUrl: String
)
