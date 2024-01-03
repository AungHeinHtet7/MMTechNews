package com.ahh.testblogapi

import android.app.ProgressDialog
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.appcompat.widget.SearchView



class MainActivity : AppCompatActivity() {

    private var url = "" //complete url for retrieving posts
    private var nextToken = "" //next page token to load more post

    private lateinit var postArrayList: ArrayList<ModelPost>
    private lateinit var adapterPost: AdapterPost
    private var isSearch = false
    private lateinit var actionBar: ActionBar

    private lateinit var progressDialog: ProgressDialog

    private val TAG = "MAIN_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //Setup progress dialog
        progressDialog = ProgressDialog(this)

        //init & clear list before adding data into it
        //set adapter to recyclerview
        postArrayList = ArrayList()
        postArrayList.clear()


        loadPosts()
        //Search
        searchEt.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                nextToken =""
                url = ""

                postArrayList = ArrayList()
                postArrayList.clear()
                val query = searchEt.text.toString().trim()
                searchPosts(query)
                closeKeyBoard()
                searchEt.clearFocus()
                !searchEt.isCursorVisible
                return@OnKeyListener true

            }
            false
        })



        //handle click , laod more posts
        /*loadMoreBtn.setOnClickListener {
            loadPosts()
        }*/
    }
    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun loadPosts() {
        isSearch = false
        progressDialog.show()

        url = when(nextToken){
            "" -> {
                Log.d(TAG, "loadPosts: NextPageToken is empty, more posts")
                ("https://www.googleapis.com/blogger/v3/blogs/${Constants.BLOG_ID}/posts?maxResults=${Constants.MAX_RESULTS}&key=${Constants.API_Key}")

            }
            "end" -> {
                Log.d(
                    TAG,
                    "loadPosts: Next page token is end, no more posts or i.e.loaded all posts"
                )
                Toast.makeText(this, "No more posts...", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                return
            }
            else ->{
                Log.d(TAG, "loadPosts: NextPage Token: $nextToken")
                ("https://www.googleapis.com/blogger/v3/blogs/${Constants.BLOG_ID}/posts?maxResults=${Constants.MAX_RESULTS}&pageToken=$nextToken&key=${Constants.API_Key}")
            }
        }

        Log.d(TAG, "loadPosts: URL: $url")

        //request data, Method is GET
        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            //we got response, so dismiss dialog first
            progressDialog.dismiss()
            Log.d(TAG, "loadPosts: $response")

            try {
                //we have response as JSON object
                val jsonObject = JSONObject(response)
                try {
                    nextToken = jsonObject.getString("nextPageToken")
                    Log.d(TAG, "loadPosts: NextPageToken: $nextToken")

                } catch (e: Exception) {
                    Toast.makeText(this, "Reached end of the page...", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "loadPosts: Reached end of the page...")
                    nextToken = "end"

                }

                //get json array dat from json object
                val jsonArray = jsonObject.getJSONArray("items")

                //continue getting data unitl completed all
                for (i in 0 until jsonArray.length()) {
                    try {
                        val jsonObject01 = jsonArray.getJSONObject(i)

                        val id = jsonObject01.getString("id");
                        val title = jsonObject01.getString("title");
                        val content = jsonObject01.getString("content");
                        val published = jsonObject01.getString("published");
                        val updated = jsonObject01.getString("updated");
                        val url = jsonObject01.getString("url");
                        val selfLink = jsonObject01.getString("selfLink");
                        val authorName =
                            jsonObject01.getJSONObject("author").getString("displayName");
                        //val image = jsonObject01.getJSONObject("author").getString("image");
                        //set data
                        val modelPost = ModelPost(
                            "$authorName",
                            "$content",
                            "$id",
                            "$published",
                            "$selfLink",
                            "$title",
                            "$updated",
                            "$url"
                        )

                        //add data to list
                        postArrayList.add(modelPost)


                    } catch (e: Exception) {
                        Log.d(TAG, "loadPosts: 1 ${e.message}")
                        Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()


                    }
                }

                //setup adapter
                adapterPost = AdapterPost(this@MainActivity, postArrayList)
                //set adapter to recyclerview
                postsRv.adapter = adapterPost
                progressDialog.dismiss()

            } catch (e: Exception) {
                Log.d(TAG, "loadPosts: 2 ${e.message}")
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()

            }
        }, { error ->
            Log.d(TAG, "loadPosts: ${error.message}")
            Toast.makeText(this, "${error.message}", Toast.LENGTH_SHORT).show()
        })

        //add request in queue
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(stringRequest)

    }
    private fun searchPosts(query: String){
        isSearch =true
        progressDialog.show()

        url = when(nextToken){
            "" -> {
                Log.d(TAG, "searchPosts: NextPageToken is empty, more posts")
                ("https://www.googleapis.com/blogger/v3/blogs/${Constants.BLOG_ID}/posts/search?q=$query&key=${Constants.API_Key}")

            }
            "end" -> {
                Log.d(
                    TAG,
                    "searchPosts: Next page token is end, no more posts or i.e.loaded all posts"
                )
                Toast.makeText(this, "No more posts...", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                return
            }
            else ->{
                Log.d(TAG, "searchPosts: NextPage Token: $nextToken")
                ("https://www.googleapis.com/blogger/v3/blogs/${Constants.BLOG_ID}/posts/search?q=$query&pageToken=$nextToken&key=${Constants.API_Key}")
            }
        }

        Log.d(TAG, "searchPosts: URL: $url")

        //request data, Method is GET
        val stringRequest = StringRequest(Request.Method.GET, url, { response ->
            //we got response, so dismiss dialog first
            progressDialog.dismiss()
            Log.d(TAG, "searchPosts: $response")

            try {
                //we have response as JSON object
                val jsonObject = JSONObject(response)
                try {
                    nextToken = jsonObject.getString("nextPageToken")
                    Log.d(TAG, "searchPosts: NextPageToken: $nextToken")

                } catch (e: Exception) {
                    Toast.makeText(this, "Reached end of the page...", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "searchPosts: Reached end of the page...")
                    nextToken = "end"

                }

                //get json array dat from json object
                val jsonArray = jsonObject.getJSONArray("items")

                //continue getting data unitl completed all
                for (i in 0 until jsonArray.length()) {
                    try {
                        val jsonObject01 = jsonArray.getJSONObject(i)

                        val id = jsonObject01.getString("id");
                        val title = jsonObject01.getString("title");
                        val content = jsonObject01.getString("content");
                        val published = jsonObject01.getString("published");
                        val updated = jsonObject01.getString("updated");
                        val url = jsonObject01.getString("url");
                        val selfLink = jsonObject01.getString("selfLink");
                        val authorName =
                            jsonObject01.getJSONObject("author").getString("displayName");
                        //val image = jsonObject01.getJSONObject("author").getString("image");
                        //set data
                        val modelPost = ModelPost(
                            "$authorName",
                            "$content",
                            "$id",
                            "$published",
                            "$selfLink",
                            "$title",
                            "$updated",
                            "$url"
                        )

                        //add data to list
                        postArrayList.add(modelPost)


                    } catch (e: Exception) {
                        Log.d(TAG, "loadPosts: 1 ${e.message}")
                        Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()


                    }
                }

                //setup adapter
                adapterPost = AdapterPost(this@MainActivity, postArrayList)
                //set adapter to recyclerview
                postsRv.adapter = adapterPost
                progressDialog.dismiss()

            } catch (e: Exception) {
                Log.d(TAG, "loadPosts: 2 ${e.message}")
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()

            }
        }, { error ->
            Log.d(TAG, "loadPosts: ${error.message}")
            Toast.makeText(this, "${error.message}", Toast.LENGTH_SHORT).show()
        })

        //add request in queue
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(stringRequest)

    }


}
