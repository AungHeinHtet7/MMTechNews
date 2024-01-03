package com.ahh.testblogapi

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_post_details.*
import org.json.JSONObject
import org.jsoup.Jsoup
import java.text.SimpleDateFormat


class PostDetailsActivity : AppCompatActivity() {

    private var postId:String? = null
    private val TAG = "POST_DETAILS_TAG"

    //action bar
    private lateinit var actionBar:ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)

        //init actionbar
        actionBar = supportActionBar!!
        actionBar.title = "Details"
        //add back button
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        //get post id from intent
        postId = intent.getStringExtra("postId")
        Log.d(TAG,"onCreate: $postId")

        loadPostDetails()



    }

    private fun loadPostDetails(){
        val url = ("https://www.googleapis.com/blogger/v3/blogs/${Constants.BLOG_ID}/posts/$postId?key=${Constants.API_Key}")
        Log.d(TAG,"loadPostDetails: $url")

        //request api
        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            //successfully recieved response
            Log.d(TAG,"loadPostDetails: $response")

            //Response is in JSON Object form
            try {
                val jsonObject = JSONObject(response)

                //Get data
                val title = jsonObject.getString("title")
                val published = jsonObject.getString("published")
                val content = jsonObject.getString("content")
                val url = jsonObject.getString("url")
                val displayName = jsonObject.getJSONObject("author").getString("displayName")

                //convert time to format
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                val dateformat2 = SimpleDateFormat("dd/MM/yyyy K:mm a")
                var formattedDate = ""
                try {
                    val date = dateFormat.parse(published)
                    formattedDate = dateformat2.format(date)
                }
                catch (e:Exception){
                    formattedDate = published
                    e.printStackTrace()
                }

                val document = Jsoup.parse(content)
                val p_content: TextView = findViewById(R.id.post_content)
                try {
                    // get image there may be multiple or no in post, try to get first
                    val elements = document.select("img")
                    val image = elements[0].attr("src")

                    //set image
                    Picasso.get().load(image)

                }
                catch (e:Exception){
                    // no image in a post
                   /* postimg.setImageResource(R.drawable.ic_image_black)*/

                }

                //set data
                /*actionBar.subtitle = title*/
                titleTv.text = title
                displayHtml(content)
                post_date.text = "$formattedDate"
                /*formatted_date.text ="$formattedDate"*/

                //webview in html
                /*webView.loadDataWithBaseURL(null,content,"text/html",OutputKeys.ENCODING,null)*/


            }
            catch (e:Exception){
                Log.d(TAG,"loadPostDetails: ${e.message}")
                Toast.makeText(this,"${e.message}",Toast.LENGTH_SHORT).show()
            }

        }) {error ->
            //if response fail, show error message
            Log.d(TAG,"loadPostDetails: ${error.message}")
            Toast.makeText(this,"${error.message}",Toast.LENGTH_SHORT).show()
        }

        //add request to queue
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(stringRequest)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun displayHtml(html: String) {

        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(resources, post_content)

        // Using Html framework to parse html
        val styledText=HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_MODE_LEGACY,
            imageGetter,null)

        // to enable image/link clicking
        post_content.movementMethod = LinkMovementMethod.getInstance()

        // setting the text after formatting html and downloadig and setting images
        post_content.text = styledText
    }
}
