package br.com.igorbag.githubsearch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository

/**
 * Adapter for the list of repositories.
 *
 * @property repositories The list of repositories to be displayed.
 */
class RepositoryAdapter(private val repositories: List<Repository>) :
    RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    /**
     * Click listener for the card item.
     */
    var carItemLister: (String) -> Unit = {}

    /**
     * Click listener for the share button.
     */
    var btnShareLister: (String) -> Unit = {}

    /**
     * Creates a new view holder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.repository_item, parent, false)
        return ViewHolder(view)
    }

    /**
     * Binds the data to the view holder.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.repositoryName.text = repositories[position].name
        holder.repositoryShareButton.setOnClickListener {
            btnShareLister(repositories[position].htmlUrl)
        }
        holder.repositoryCard.setOnClickListener {
            carItemLister(repositories[position].htmlUrl)
        }
    }

    /**
     * Returns the total number of items in the list.
     */
    override fun getItemCount(): Int = repositories.size

    /**
     * View holder for a repository item.
     *
     * @property itemView The view for the item.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val repositoryName: TextView
        val repositoryShareButton: ImageView
        val repositoryCard: CardView

        init {
            view.apply {
                repositoryName = findViewById(R.id.tv_repository_name)
                repositoryShareButton = findViewById(R.id.iv_share)
                repositoryCard = findViewById(R.id.cv_repository)
            }
        }
    }
}
