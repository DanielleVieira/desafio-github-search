package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

/**
 * Main screen of the application, responsible for displaying the user's repositories.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var userName: EditText
    private lateinit var btnConfirm: Button
    private lateinit var repositoriesList: RecyclerView
    private lateinit var repositoriesProgressBar: ProgressBar
    private lateinit var repositoriesListErrorMessage: TextView
    private lateinit var githubApi: GitHubService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        setupListeners()
        showUserName()
        setupRetrofit()
        getAllReposByUserName()
    }

    /**
     * Initializes the view components.
     */
    fun setupView() {
        userName = findViewById(R.id.et_user_name)
        btnConfirm = findViewById(R.id.btn_confirm)
        repositoriesList = findViewById(R.id.rv_repositories_list)
        repositoriesProgressBar = findViewById(R.id.iv_repositoriesProgressBar)
        repositoriesListErrorMessage = findViewById(R.id.tv_repositories_list_error_message)
    }

    /**
     * Sets up the listeners for the view components.
     */
    private fun setupListeners() {
        btnConfirm.setOnClickListener {
            handleButtonClick()
        }

        userName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                handleEditTextValueChange()
            }
        })
    }

    /**
     * Handles the change in the EditText value.
     */
    private fun handleEditTextValueChange() {
        if (btnConfirm.text != getString(R.string.confirm)) {
            editBtnConfirm()
        }
    }

    /**
     * Handles the click on the confirm button.
     */
    private fun handleButtonClick() {
        if (!userName.text.isNullOrBlank()) {
            editBtnConfirm(isEnable = false, labelRes = R.string.btn_saving)
            saveUserLocal()
            editBtnConfirm(isEnable = false, labelRes = R.string.btn_saved)
            getAllReposByUserName()
        }
    }

    /**
     * Edits the confirm button's state and label.
     *
     * @param isEnable Whether the button should be enabled.
     * @param labelRes The string resource for the button's label.
     */
    private fun editBtnConfirm(isEnable: Boolean = true, labelRes: Int = R.string.confirm) {
        btnConfirm.isEnabled = isEnable
        btnConfirm.text = getString(labelRes)
    }


    /**
     * Saves the user's name locally using SharedPreferences.
     */
    private fun saveUserLocal() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val user = userName.text.toString()
        sharedPref.edit {
            putString("user", user)
        }
        Log.i("MainActivity", getString(R.string.user_saved_successfully, user))
    }

    /**
     * Retrieves the saved user name from SharedPreferences.
     *
     * @return The saved user name, or an empty string if not found.
     */
    private fun getSavedUserName(): String {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        return sharedPref.getString("user", "") ?: ""
    }

    /**
     * Displays the saved user name in the EditText.
     */
    private fun showUserName() {
        val user = getSavedUserName()
        if (user.isNotBlank()) {
            userName.setText(user)
            editBtnConfirm(isEnable = false, labelRes = R.string.btn_saved)
        }
    }

    /**
     * Sets up the Retrofit instance for API calls.
     */
    fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        githubApi = retrofit.create(GitHubService::class.java)
    }

    /**
     * Fetches all repositories for the saved user name.
     */
    fun getAllReposByUserName() {
        repositoriesProgressBar.isVisible = true
        repositoriesList.isVisible = false
        repositoriesListErrorMessage.isVisible = false
        try {
            val user = getSavedUserName()
            githubApi.getAllRepositoriesByUser(user).enqueue(object : Callback<List<Repository>> {
                override fun onResponse(
                    call: Call<List<Repository>?>,
                    response: Response<List<Repository>?>
                ) {
                    when (response.code()) {
                        HttpURLConnection.HTTP_OK -> handleResponseOK(
                            response.body(),
                            R.string.repositories_list_error_message_response_ok
                        )
                        HttpURLConnection.HTTP_NOT_FOUND -> handleResponseErrorMessage(
                            R.string.respositories_list_error_message_response_not_found
                        )
                    }
                }

                override fun onFailure(
                    call: Call<List<Repository>?>,
                    t: Throwable
                ) {
                    handleResponseErrorMessage(R.string.repositories_list_error_message)
                    Log.e("MainActivity", "${t.message}")
                }
            })

        } catch (e: Exception) {
            handleResponseErrorMessage(R.string.repositories_list_error_message)
            Log.e("MainActivity", "${e.message}")
        }
    }

    /**
     * Handles the successful API response.
     *
     * @param responseBody The list of repositories from the response.
     * @param errorMessage The error message to display if the body is null or empty.
     */
    private fun handleResponseOK(responseBody: List<Repository>?, errorMessage: Int) {
        if (responseBody.isNullOrEmpty()) {
            handleResponseErrorMessage(errorMessage = errorMessage)
        } else {
            repositoriesList.isVisible = true
            setupAdapter(responseBody)
        }
        repositoriesProgressBar.isVisible = false
    }

    /**
     * Handles the API response error.
     *
     * @param errorMessage The error message to display.
     */
    private fun handleResponseErrorMessage(errorMessage: Int) {
        repositoriesListErrorMessage.isVisible = true
        repositoriesListErrorMessage.text =
            getString(errorMessage)
        repositoriesProgressBar.isVisible = false
    }

    /**
     * Sets up the adapter for the RecyclerView.
     *
     * @param list The list of repositories to display.
     */
    fun setupAdapter(list: List<Repository>) {
        val adapter = RepositoryAdapter(list)
        adapter.btnShareLister = { shareRepositoryLink(it) }
        adapter.carItemLister = { openBrowser(it) }
        repositoriesList.adapter = adapter
    }

    /**
     * Shares the repository link using an Intent.
     *
     * @param urlRepository The URL of the repository to share.
     */
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    /**
     * Opens the repository link in a browser.
     *
     * @param urlRepository The URL of the repository to open.
     */
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                urlRepository.toUri()
            )
        )

    }

}
