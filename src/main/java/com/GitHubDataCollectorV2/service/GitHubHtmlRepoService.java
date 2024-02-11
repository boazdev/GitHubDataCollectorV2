package com.GitHubDataCollectorV2.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class GitHubHtmlRepoService {

    private static String numCommitsSelector = "#repo-content-pjax-container > div > div > div.Layout.Layout--flowRow-until-md.react-repos-overview-margin.Layout--sidebarPosition-end.Layout--sidebarPosition-flowRow-end > div.Layout-main > react-partial > div > div > div.Box-sc-g0xbh4-0.fSWWem > div > div > div.Box-sc-g0xbh4-0.hFCATI > div.Box-sc-g0xbh4-0.jAVTmU > div > div:nth-child(3) > div.Box-sc-g0xbh4-0.yfPnm > div > div > table > tbody > tr.Box-sc-g0xbh4-0.dXXclB > td > div > div.Box-sc-g0xbh4-0.jGfYmh > a > span > span:nth-child(2) > sp";
    public static Boolean isEmptyPatternHtmlPage(String htmlString) {
        Document doc = Jsoup.parse(htmlString);
        Element emptyElement = doc.selectFirst("#repo-content-pjax-container > div > div > h3");

        if(emptyElement!=null && emptyElement.text().equals("This repository is empty."))
            return true;
        else
            return false;
    }

    public static Integer getNumCommitsInHtml(String html){

        System.out.println("get num commits in html");
        Document doc = Jsoup.parse(html);
        Element scriptElement = doc.selectFirst("script[data-target='react-app.embeddedData']");
        if (scriptElement != null) {
            String text = scriptElement.data();
            System.out.println("script text: " + text);
            try {
                return getNumberOfCommits(text);
            } catch (Exception ex) {
                System.out.println("Cannot parse commits, exception: " + ex.toString());
            }
        } else {
            System.out.println("Commits script element not found");
        }
        return 0;
    }

    public static Integer getNumberOfCommits(String json) throws Exception { //https://github.com/miguelgrinberg/Flask-SocketIO/commits
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode commitGroups = root.get("payload").get("commitGroups");

        // Traverse each commitGroup and count commits
        int commitCount = 0;
        for (JsonNode commitGroup : commitGroups) {
            commitCount += commitGroup.get("commits").size();
        }

        return commitCount;
    }


}
