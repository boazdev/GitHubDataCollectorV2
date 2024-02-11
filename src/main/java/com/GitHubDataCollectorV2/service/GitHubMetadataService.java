package com.GitHubDataCollectorV2.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.GitHubDataCollectorV2.constants.ConstantsGit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.GitHubDataCollectorV2.service.GitHubHtmlRepoService.isEmptyPatternHtmlPage;
;

@Service
public class GitHubMetadataService {





    public static final Pattern REPOSITORIES_PATTERN = Pattern.compile("codeRepository[^\\s]+[^\\S][^\\n][^\\S]+([^<]+)");
    public static final Pattern NUM_OF_REPOSITORIES_PATTERN = Pattern.compile("Repositories[^<]+<span title=\\\"([^\\\"]+)");
    public static final Pattern NAME_PATTERN = Pattern.compile("itemprop=\\\"name[^\\s]+[^\\S][^\\n][^\\S]+([^\\n]+)");
    public static final Pattern FOLLOWERS_PATTERN = Pattern.compile("class=\\\"text-bold color-fg-default\\\">([^<]+)[^\\d]+follower");
    public static final Pattern FOLLOWING_PATTERN = Pattern.compile("fg-default\\\">([^<]+)[^]d]+\\bfollowing\\b");
    public static final Pattern FORKS_PATTERN1 = Pattern.compile("text-bold[^>]+>([^<]+)[^f]+forks");
    public static final Pattern FORKS_PATTERN2 = Pattern.compile("</svg>Fork[^C]+[^\\d]+([^<]+)");
    public static final Pattern STARS_PATTERN1 = Pattern.compile("text-bold[^>]+>([^<]+)[^f]+stars");
    public static final Pattern STARS_PATTERN2 = Pattern.compile("repo-stars-counter-star[^=]+=\\\"([^ ]+)");
    //public static final Pattern COMMITS_PATTERN = Pattern.compile("<span class=\\\"d-none d-sm-inline\\\">[^\\S]+<strong>([^<]+)");
    public static final Pattern COMMITS_PATTERN = Pattern.compile("<span class=\"Text-sc-17v1xeu-0 hfRvxg\">([^\\s]+) Commits</span>");
    public static final Pattern PROGRAMMING_LANGUAGE_PATTERN = Pattern.compile("programmingLanguage\\\">([^<]+)");
    public static final Pattern FORKED_FROM_PATTERN = Pattern.compile("forked[ ]from([^h]+)");
    public static final Pattern EMPTY_PATTERN = Pattern.compile("This repository is empty([^.]+)");
    public static final Pattern USER_NOT_FOUND_PATTERN = Pattern.compile("This is not the ([^ ]+)");

    private static OkHttpClient client = new OkHttpClient.Builder() //TODO: Clean up this file from unused code and comments
            .callTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(10))
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    //Main method


    public JSONObject getUserMetadataOnly(String username) throws IOException, InterruptedException {

        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode objectNode = factory.objectNode();

        System.out.println("Getting user " + username + " metadata only and repositoriesUrl");
        objectNode.setAll(metadata(username)); //name,username,url,public_repos,followers,following
        //objectNode.put("processed_public_repos",0);
        objectNode.put("processed_public_repos","0");
        if(objectNode.get("USER_NOT_FOUND").asBoolean())
            return null;
        objectNode.put("num_public_repos",objectNode.get("public_repos").asText());
        objectNode.remove("public_repos");
        objectNode.remove("USER_NOT_FOUND");
        String jsonString = objectNode.toString();
        System.out.println(jsonString);


        JSONObject jsonObj = new JSONObject(objectNode.toString());
        return jsonObj;
    }

    //Data gathering
    public static ObjectNode metadata(String username) throws IOException, InterruptedException {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode objectNode = factory.objectNode();
        String html = getRepositoriesPageHtml(username,1);

        if(getRegexGroup(USER_NOT_FOUND_PATTERN,html,"",username).equals("USER_NOT_FOUND")){
            objectNode.put("USER_NOT_FOUND",true);
            return objectNode;
        }
        else
            objectNode.put("USER_NOT_FOUND",false);


        String name = getRegexGroup(NAME_PATTERN,html, "Name", username); //name,username,url,public_repos,followers,following
        if (name.equals("</span>")) name = "N/A";
        objectNode.put("name",name);
        objectNode.put("username",username);
        objectNode.put("url","https://github.com/" + username);
        objectNode.put("public_repos", Integer.parseInt(getRegexGroup(NUM_OF_REPOSITORIES_PATTERN,html, "Number of Repositories", username)));
        objectNode.put("followers", Integer.parseInt(getRegexGroup(FOLLOWERS_PATTERN,html, "Followers", username)));
        objectNode.put("following", Integer.parseInt(getRegexGroup(FOLLOWING_PATTERN,html , "Following", username)));

        return objectNode;
    }




    public static ObjectNode oneRepoMetaData(String repoUrl)
    {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode objectNode = factory.objectNode();
        boolean isEmpty;
        String html;
        int forks = 0, stars = 0;
        try {
            html = getPageHtml(repoUrl);
            isEmpty = isEmptyPatternHtmlPage(html);
        }
        catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(isEmpty)
            objectNode.put("emptyRepo",true);
        else {
            forks += Integer.parseInt(getRegexGroup(FORKS_PATTERN1,html,"URL","username"));
            stars += Integer.parseInt(getRegexGroup(STARS_PATTERN1, html,"URL","username"));
            objectNode.put("emptyRepo",false);
            objectNode.put("forked_repo",false);
        }
        objectNode.put("forks", forks);
        objectNode.put("stars", stars);
        return objectNode;
    }
    public static ObjectNode repositoriesMetadata(List<String> allRepositories, String username) {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode objectNode = factory.objectNode();
        List<String> validRepositories = new ArrayList<>();
        int forks = 0, commits = 0, stars = 0, emptyRepositories = 0, forkedRepositories = 0;

        for(String URL : allRepositories){
            boolean isEmpty;
            boolean isForked;
            String html;
            String commitsHtml;
            try {
                html = getPageHtml(URL);
                isEmpty = isEmptyPatternHtmlPage(html);
                isForked = !getRegexGroup(FORKED_FROM_PATTERN, html, "Forked", username).equals("0");
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (isEmpty)
                emptyRepositories++;
            else if (isForked)
                forkedRepositories++;
            else{
                validRepositories.add(URL);
                forks += Integer.parseInt(getRegexGroup(FORKS_PATTERN1,html,URL,username));
                commits += 0;//getNumCommits(URL);//getNumCommitsInHtml(html);//Integer.parseInt(getRegexGroup(COMMITS_PATTERN, html,URL,username));
                stars += Integer.parseInt(getRegexGroup(STARS_PATTERN1, html,URL,username));
            }
        }


        objectNode.put("forks", forks);
        objectNode.put("commits", commits);
        objectNode.put("stars", stars);
        objectNode.put("forked_repos", forkedRepositories);
        objectNode.put("empty_repos", emptyRepositories);
        objectNode.put("repositoriesUrls",validRepositories.toString());

        return objectNode;
    }

    //Help methods
    public static String getRegexGroup(Pattern pattern, String html, String source, String username) {
        Matcher matcher = pattern.matcher(html);
        matcher.find();

        try {
            String ret = matcher.group(1);
            if (pattern.equals(USER_NOT_FOUND_PATTERN)) {
                return "USER_NOT_FOUND";
            }
            if (ret.endsWith("k"))
                ret = convertToNumber(ret);
            if (ret.contains(",")) {
                ret = ret.replace(",", "");
            }
            return ret;
        } catch (Exception e) {
            if (pattern.equals(USER_NOT_FOUND_PATTERN)) {
                return "USER_FOUND";
            }
            if (pattern.equals(FORKS_PATTERN1)) {
                return getRegexGroup(FORKS_PATTERN2, html, source, username);
            }
            if (pattern.equals(STARS_PATTERN1)) {
                return getRegexGroup(STARS_PATTERN2, html, source, username);
            }
            if (pattern.equals(FORKED_FROM_PATTERN) || pattern.equals(EMPTY_PATTERN))
                return "0";
        }
        return "0";
    }
    public static String convertToNumber(String str) {
        double num = Double.parseDouble(str.substring(0, str.length() - 1));
        return String.valueOf((int) (num * 1000));
    }
    public static String getRepositoriesPageHtml(String username, int pageNumber) throws IOException, InterruptedException { //TODO: all http calls should be done in a loop until they succeed
        Request request = new Request.Builder()   //TODO: or some threshold is reached
                .url("https://github.com/" + username + "?page=" + pageNumber +"&tab=repositories")
                .method("GET", null)
                .build();

        Response response = makeRequestWithRetries(request, ConstantsGit.MAX_HTTP_RETRY);//client.newCall(request).execute();
        return response.body().string();
    }
    public static String getPageHtml(String url) throws IOException, InterruptedException {
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .build();

        Response response = makeRequestWithRetries(request, ConstantsGit.MAX_HTTP_RETRY);//client.newCall(request).execute();
        return response.body().string();
    }


    public static Response makeRequestWithRetries(Request request, int numTries) throws IOException, InterruptedException {
        IOException lastException = null;
        for (int i = 0; i < numTries; i++) {
            try {
                Response response = client.newCall(request).execute();
                if(response.isSuccessful()) {
                    return response;
                }
            } catch (IOException e) {
                System.out.println(String.format("exception in okHttp: %s", e.toString()));
                lastException = e;
            }
            Thread.sleep(1000);
        }
     throw new IOException("Could not make request to git hub after max tries");
    }



}