package com.fh.netpf.crawler.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fh.netpf.crawler.conf.SystemConfigure;
import com.fh.netpf.crawler.task.Task;
import lombok.extern.log4j.Log4j2;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class HttpUtils {

    public static int postAddJob(List<String> list) {

        String url = SystemConfigure.getInstance().getProperty("postAddJobUrl");

        Map<String, String> param = buildParam(list);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();

        FormBody body = new FormBody.Builder()
                .add("job", param.get("job"))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .post(body)
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            int statusCode = response.code();
            log.info("响应结果:{}", response.body().string());
            return statusCode;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("{}", e);
        }
        return -1;
    }

    public static List<String> doPost(Task task) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        FormBody body = new FormBody.Builder()
                .add("reviewCode", task.getReviewCode())
                .add("page", "1")
                .add("pagesize", task.getPageSize())
                .add("tendencyCondition", "0")
                .build();

        Request request = new Request.Builder()
                .url("http://warning.51wyq.cn/warningCenter/getMoreWarningDetail.shtml")
                .post(body)
                .build();

        Response response;
        List<String> list = new ArrayList<>();
        try {
            response = client.newCall(request).execute();
            String result = response.body().string();
            log.info("Result:{}", result);
            list = jsonProcessor(result);
            list.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static Map<String, String> buildParam(List<String> param) {

        JSONObject jsonObject = new JSONObject();
        String projectId = SystemConfigure.getInstance().getProperty("projectId");

        jsonObject.put("projectid", projectId);
        jsonObject.put("projectname", "新浪微博搜索");
        jsonObject.put("srcurl", "http://weibo.com/");
        jsonObject.put("sitetype", 6);
        jsonObject.put("domaintype", 0);
        jsonObject.put("countryid", 1156);
        jsonObject.put("areaid", 110000);
        jsonObject.put("proxytype", 0);
        jsonObject.put("proxyip", "");
        jsonObject.put("proxyport", "");
        jsonObject.put("postdata", "");
        jsonObject.put("posturl", "");
        jsonObject.put("trycount", 3);
        jsonObject.put("timeout", 60);
        jsonObject.put("iscrawlercomment", 0);
        jsonObject.put("domainid", 10000000);

        JSONArray jsonArray = new JSONArray();
        param.forEach(url -> {
            JSONObject object = new JSONObject();
            object.put("url", url);
            jsonArray.add(object);
        });

        jsonObject.put("urls", jsonArray);

        Map<String, String> map = new HashMap<>();
        map.put("job", jsonObject.toJSONString());

        return map;
    }

    private static List<String> jsonProcessor(String json) {
        List<String> list = new ArrayList<>();
        JSONObject jsonObject = JSON.parseObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("icontentCommonNetList");
        jsonArray.forEach(o -> {
            JSONObject jsonObject1 = JSON.parseObject(o.toString());
            String webPageUrl = jsonObject1.getString("webpageUrl");
            if (webPageUrl.contains("http://weibo.com/")) {
                Matcher matcher = Pattern.compile("http://weibo.com/\\d+/[0-9A-Za-z]+").matcher(webPageUrl);
                if (matcher.find()) {
                    list.add(webPageUrl);
                }
            }
        });
        return list;
    }

//    public static void main(String[] args) {
//
//        List<String> list = new ArrayList<>();
//        list.add("http://weibo.com/2187179207/FsjQ8aNf2");
//        list.add("http://weibo.com/3170757643/FsjP1CDx5");
//        list.add("http://weibo.com/6396604349/FsjPboyRX");
//        String json = buildParam(list).get("job");
//        System.out.println(json);
//
//    }

}
