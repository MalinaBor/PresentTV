package com.example.presenttv

import android.os.AsyncTask
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class PresentationActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentation)

        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()

        // Выполнение запроса к сервису LibreSignage
        FetchPresentationUrlTask().execute("http://192.168.10.69/app/?q=2")
    }

    private inner class FetchPresentationUrlTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String {
            val urlStr = params[0]
            val param = "q=" + URLEncoder.encode(params[1], "UTF-8")
            var result = ""

            try {
                val url = URL(urlStr)
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.connectTimeout = 5000
                urlConnection.readTimeout = 5000
                urlConnection.doOutput = true

                val outputStream: OutputStream = urlConnection.outputStream
                outputStream.write(param.toByteArray())
                outputStream.flush()
                outputStream.close()

                val inputStream = urlConnection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()

                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }

                bufferedReader.close()
                result = stringBuilder.toString()

            } catch (e: IOException) {
                e.printStackTrace()
            }

            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            try {
                val jsonObject = JSONObject(result)
                val presentationUrl = jsonObject.getString("presentation_url")

                // Загрузка презентации в WebView
                webView.loadUrl(presentationUrl)

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
}