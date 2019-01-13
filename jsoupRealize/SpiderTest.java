package com.wgc.SpringBoot20181229.spider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import redis.clients.jedis.Jedis;

import java.io.IOException;

public class SpiderTest {

    private String url = "http://www.zhsme.gov.cn/policy/getPolicyList?pageNum=1&NameOrWords=&areaSreachValue=&areaSreachId=&scaleSreachValue=&scaleSreachId=&levelSreachValue=&levelSreachId=&isShuangChuang=";

    private String contentUrl = " http://www.zhsme.gov.cn/policy/getTextPolicyByTextPolicyId?textPolicyId=";
    private Jedis jedis;
    //int i = 0;

    public static void main(String[] args) {
        SpiderTest spiderTest = new SpiderTest();
        try {
            for (Integer i = 1; i <= spiderTest.page(spiderTest.doc(spiderTest.url)); i++) {
                try {
                    String page = "http://www.zhsme.gov.cn/policy/getPolicyList?pageNum=" + i + "&NameOrWords=&areaSreachValue=&areaSreachId=&scaleSreachValue=&scaleSreachId=&levelSreachValue=&levelSreachId=&isShuangChuang=";
                    //连接
                    System.out.println(page);
                    Document doc = spiderTest.doc(page);
                    Elements element = doc.select(".list-txt > h4 > a");
                    spiderTest.articleFor(element, spiderTest.contentUrl);
                } catch (Exception e) {
                    continue;
                }

            }
        } catch (IOException e) {
            System.out.println("连不上");
        }
       // System.out.println(i);
    }

    public Document doc(String url) throws IOException {
			  //这里可以设置很多方法
        //userAgent()设置 Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36
        //是那台电脑访问
        return Jsoup.connect(url).timeout(50000).get();
    }

    /*每篇政策详情的id*/
    public String id(Elements select) {
        StringBuffer buffer = new StringBuffer(String.valueOf(select));
        try {
            int start = buffer.indexOf("(");
            if (start == -1) {
                return null;
            }
            String substring = buffer.substring(start, buffer.indexOf(")"));
            String str = substring.substring(2, substring.length() - 1);
            return str;
        } catch (Exception e) {
            return "报错了，请您要去解决";
        }

    }

    /*政策内容*/
    public void article(String url) throws IOException {
        Document document = doc(url);
        /*标题*/
        Elements select1 = document.select("h1 > strong");
        System.out.println(select1.text());
        /*时间*/
        Elements selectTime = document.select("h2 > span");
        for (Element element1 : selectTime) {
            System.out.println(element1.text());
        }

        /*内容*/
        Elements selectContent = document.select(".view-content > p");
        for (Element element1 : selectContent) {
            System.out.println(element1.text());
        }
    }

    /*页数*/
    public Integer page(Document doc) {
        Elements select1 = doc.select("#pagination > script");
        StringBuffer s = new StringBuffer(String.valueOf(select1));
        String substring = s.substring(s.indexOf("totalPages"), s.indexOf("visiblePages"));
        Integer num = Integer.valueOf(substring.substring(substring.indexOf("1"), substring.indexOf(",")));
        return num;
    }

    /*政策详情*/
    public void articleFor(Elements element, String contentUrl) throws IOException {

        for (Element element1 : element) {
            Document policyDetails = doc("http://www.zhsme.gov.cn" + element1.attr("href"));
            //发布文号
            Elements policyBasis = policyDetails.select(".policy-txt > p");
            //主题/标题
            Elements select = policyDetails.select(".policy-txt > a");
            //发布日期-结束日期
            Elements dates = policyDetails.select(".policy-con > p span");
            String id = id(select);
            if (id == null) {
                System.out.println(" =============================================================== ");
                continue;
            } else {

                System.out.println(policyBasis.text());
                article(contentUrl + id);
                System.out.println(dates.text());
                System.out.println("  ");
            }

        }
    }
}
