package com.ahh.testblogapi

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.jsoup.Jsoup
import java.text.SimpleDateFormat

class AdapterPost(
    private val context: Context,
    private val postArrayList: ArrayList<ModelPost>
) :RecyclerView.Adapter<AdapterPost.HolderPost>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPost {

        //inflate layout row_post.xml
        val view = LayoutInflater.from(context).inflate(R.layout.row_post, parent, false)
        return HolderPost(view)

    }

    override fun getItemCount(): Int {

        return postArrayList.size


    }

    override fun onBindViewHolder(holder: HolderPost, position: Int) {
        //get data,format data,handle click
        val model = postArrayList[position]//get data at specific position/index of list
        //get data
        val authorName = model.authorName
        val content = model.content // html format နဲ့ ပြန်တာကို jsoup သုံးပီး text ပြောင်း
        val id = model.id
        val selfLink = model.selfLink
        val title = model.title
        val published = model.published // publish date need to format
        val updated = model.updated // date updated
        val url = model.url

        // html ကနေ text ပြောင်း
        val document = Jsoup.parse(content)
        try {
            // get image there may be multiple or no in post, try to get first
            val elements = document.select("img")
            val image = elements[0].attr("src")

            //set image
            Picasso.get().load(image).placeholder(R.drawable.ic_image_black).into(holder.imageIv)

        }
        catch (e: Exception){
            // no image in a post
            holder.imageIv.setImageResource(R.drawable.ic_image_black)

        }

        //formate date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val dateformat2 = SimpleDateFormat("dd/MM/yyyy K:mm a")
        var formattedDate = ""
        try {
            val date = dateFormat.parse(published)
            formattedDate = dateformat2.format(date)
        }
        catch (e: Exception){
            formattedDate = published
            e.printStackTrace()
        }

        holder.titleTv.text = title
        holder.description.text = document.text()
        /*holder.author_name.text = "By $authorName"//eg.By aung hein htet*/
        holder.format_date.text = "$formattedDate"

        //handle click, start activity with post id
        holder.itemView.setOnClickListener {
            val intent =  Intent(context, PostDetailsActivity::class.java)
            intent.putExtra("postId", id)
            context.startActivity(intent)
        }


    }

    inner class HolderPost(itemview: View) : RecyclerView.ViewHolder(itemview){

        //init UI Views
        //var moreBtn: ImageButton = itemview.findViewById(R.id.moreBtn)
        var titleTv: TextView = itemview.findViewById(R.id.titileTv)
       /* var author_name: TextView = itemview.findViewById(R.id.author_name)*/
        var imageIv: ImageView = itemview.findViewById(R.id.imageIv)
        var description: TextView = itemview.findViewById(R.id.descriptionTv)
        var format_date: TextView = itemview.findViewById(R.id.formatted_date)



    }
}