package br.com.igorbag.githubsearch.data

import br.com.igorbag.githubsearch.domain.Repository
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface for the GitHub API.
 */
interface GitHubService {

    /**
     * Retrieves all repositories for a given user.
     *
     * @param user The username of the GitHub user.
     * @return A [Call] object containing a list of [Repository] objects.
     */
    @GET("users/{user}/repos")
    fun getAllRepositoriesByUser(@Path("user") user: String): Call<List<Repository>>

}
