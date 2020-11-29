package com.example.rasberrypiryapp

import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class JSONTask : AsyncTask<String, String, String>() {
    override fun doInBackground(vararg params: String): String {
        try {
            //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
            var jsonObject = JSONObject()
            jsonObject.accumulate("user_id", "androidTest")
            jsonObject.accumulate("name", "yun")

            lateinit var reader :BufferedReader
            lateinit var con: HttpURLConnection
            try {
                val url =  URL(params[0])
                //연결을 함
                con = url.openConnection() as HttpURLConnection
                con.requestMethod = "GET"//POST방식으로 보냄
                con.setRequestProperty("Cache-Control", "no-cache")//캐시 설정
                con.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )//application JSON 형식으로 전송

                con.setRequestProperty("Accept", "text/html")//서버에 response 데이터를 html로 받음
                con.doOutput = true//Outstream으로 post 데이터를 넘겨주겠다는 의미
                con.doInput = true//Inputstream으로 서버로부터 응답을 받겠다는 의미
                con.connect()

                //서버로 보내기위해서 스트림 만듬
                val outStream = con.outputStream
                //버퍼를 생성하고 넣음
                val writer = BufferedWriter(OutputStreamWriter (outStream))
                writer.write(jsonObject.toString())
                writer.flush()
                writer.close()//버퍼를 받아줌

                //서버로 부터 데이터를 받음
                val stream = con.inputStream

                reader = BufferedReader(InputStreamReader(stream))

                val buffer =StringBuffer()

                var line = reader.readLine()
                while (line != null) {
                    line = reader.readLine()
                    buffer.append(line)
                }

                return buffer.toString()//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e:IOException) {
                e.printStackTrace()
            } finally {
                con.disconnect()
                try {
                    reader.close()
                } catch (e:IOException) {
                    e.printStackTrace()
                }
            }
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return "-1"
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        Log.e("hey", "result : $result");
    }
}
