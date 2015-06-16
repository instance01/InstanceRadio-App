package com.instancedev.instanceradio;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class IYoutubeDownloader {

	/*
     * Original Version can be found here:
	 * http://stackoverflow.com/questions/4700945/youtube-data-api-get-access-to-media-stream-and-play-java
	 *
	 * This is the shortened version of the following edit: https://github.com/instance01/YoutubeDownloaderScript/blob/master/IYoutubeDownloader.java
	 *
	 */

    public IYoutubeDownloader(iService s, String video) {
        try {
            download(s, video);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static int fails = 0;

    public static String download(iService s, String videoId) throws Throwable {
        String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13";
        System.out.println("Retrieving " + videoId);
        URI uri = URI.create("https://youtube.com/get_video_info?video_id=" + videoId + "&asv=3&el=detailpage&hl=en_US");//getUri("get_video_info", qparams);

        CookieStore cookieStore = new BasicCookieStore();
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(uri);
        httpget.setHeader("User-Agent", userAgent);

        System.out.println("Executing " + uri);
        HttpResponse response = httpclient.execute(httpget, localContext);
        HttpEntity entity = response.getEntity();
        if (entity != null && response.getStatusLine().getStatusCode() == 200) {
            InputStream instream = entity.getContent();
            String videoInfo = getStringFromInputStream("UTF-8", instream);
            if (videoInfo != null && videoInfo.length() > 0) {
                List<NameValuePair> infoMap = new ArrayList<>();
                URLEncodedUtils.parse(infoMap, new Scanner(videoInfo), "UTF-8");
                String downloadUrl = "";
                for (NameValuePair pair : infoMap) {
                    String key = pair.getName();
                    String val = pair.getValue();
                    // System.out.println(key + " == " + val);
                    if (key.equals("url_encoded_fmt_stream_map")) {
                        String t_ = URLDecoder.decode(URLDecoder.decode(val, "UTF-8"), "UTF-8");
                        System.out.println(t_);
                        String[] formats = t_.split("url=");
                        System.out.println("[AWESOME] Url: " + t_);
                        boolean found = false;

                        // boolean skippedFirst = false;
                        for (String fmt : formats) {
                            /*if(!skippedFirst){
                                skippedFirst = true;
                                System.out.println("SKIPPED FORMAT: " + fmt);
                                continue;
                            }*/
                            System.out.println("FORMAT: " + fmt);
                            if (fmt.contains("video") && fmt.contains("http")) {
                                int endindex = fmt.contains(";") ? fmt.indexOf(";") : fmt.length() - 1;
                                String u = fmt.substring(fmt.indexOf("http"), endindex);
                                //String u = fmt.substring(fmt.indexOf("url=") + 4);
                                System.out.println("[AWESOME] Url decoded: " + u);
                                found = true;
                                downloadUrl = u;//+ "&signature="; From what I've seen the signature parameter is always there, so this is not the issue
                                if (validVideo(downloadUrl)) {
                                    break;
                                } else {
                                    System.out.println("[ERROR] No valid video found.");
                                    System.out.println("Retrying - Fails: " + fails);
                                    fails++;
                                    if (fails > 4) { // 4 retries
                                        fails = 0;
                                        s.playNext();
                                        return "";
                                    }
                                    return download(s, videoId);
                                }
                            }
                        }
                        if (!found) {
                            System.out.println("Could not find video matching specified format (mp4).");
                        }
                    }
                }
                return downloadUrl;
            }
        } else {
            System.out.println("Could not contact youtube: " + response.getStatusLine());
        }
        return "";
    }

    public static boolean validVideo(String url) {
        try {
            URL obj = new URL(url);
            URLConnection conn = obj.openConnection();

            String type = conn.getHeaderField("Content-Type");
            System.out.println(type);
            if (type.contains("video")) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private static String getStringFromInputStream(String encoding, InputStream instream) throws UnsupportedEncodingException, IOException {
        Writer writer = new StringWriter();

        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(instream, encoding));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            instream.close();
        }
        String result = writer.toString();
        return result;
    }

}