package com.GitHubDataCollector.controller;

import com.GitHubDataCollector.service.GitHubMetadataService;
import com.GitHubDataCollector.service.GitHubRepoService;
import com.GitHubDataCollector.service.GitHubService;
import com.GitHubDataCollector.service.JsonToCsvService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("")
public class MainController {

    @Autowired
    GitHubService gitHubService;

    @Autowired
    JsonToCsvService jsonToCsvService;

    Map<String, String> tokenToResult = new HashMap<>();

    @Autowired
    GitHubMetadataService gitHubMetadataService;

    @Autowired
    GitHubRepoService gitHubRepoService;

    @PostMapping("/collectGitHubData")
    public ResponseEntity collectGitHubData(@RequestBody Map<String, List<String>> requestData) throws IOException, InterruptedException {
        String token = UUID.randomUUID().toString();
        tokenToResult.put(token, "");
        new Thread(() -> {
            try {
                List<String> usernames = requestData.get("usernames");
                List<String> keywords = requestData.get("keywords");

                fixUsernames(usernames);
                printUsernamesAndKeywords(usernames,keywords);

                String jsonString = gitHubService.getMultiplyUsersInfo(usernames, keywords);
                String csvString = jsonToCsvService.writeJsonToCsv(jsonString);
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=githubdata.csv");
                headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
                headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
                tokenToResult.put(token, csvString);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        return ResponseEntity.ok(token);
    }

    @GetMapping("/collectGitHubDataResult")
    public ResponseEntity collectGitHubData(@RequestParam String token) throws IOException, InterruptedException {
        String csvString = tokenToResult.get(token);
        if (csvString == null || csvString.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=githubdata.csv")
                .header("Access-Control-Expose-Headers", "*")
                .contentLength(csvString.length())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvString);
    }

   /* @RequestMapping(value = "/getRepositoryMetadata", method = RequestMethod.GET)
    public String getRepositoryMetadata(@RequestParam String repositoryUrl, @RequestParam String keyword) throws IOException, InterruptedException {
        ObjectNode objNode = GitHubMetadataService.oneRepoMetaData(repositoryUrl);
        List <String> requestKeyWords = new ArrayList<>();
        requestKeyWords.add(keyword);
        JSONObject objNode2 = gitHubRepoService.getRepositoryData(repositoryUrl, requestKeyWords);
        String catStr = objNode.toString() + objNode2.toString();
        return catStr;
    }

    @RequestMapping(value = "/getUserMetaData", method = RequestMethod.GET)
    public String getUserMetadata(@RequestParam String username, @RequestParam String keyword) throws IOException, InterruptedException {

        return "foobar";
    }*/

    public void fixUsernames(List<String> usernames){
        for (int i = 0; i < usernames.size(); i++) {
            String user = usernames.get(i);
            if (user.contains("https")) {
                int lastSlashIndex = user.lastIndexOf("/");
                if (lastSlashIndex >= 0 && lastSlashIndex < user.length() - 1) {
                    String lastPathComponent = user.substring(lastSlashIndex + 1);
                    usernames.set(i, lastPathComponent);
                }
            }
        }
    }

    public void printUsernamesAndKeywords(List<String> usernames, List<String> keywords) {
        System.out.println();

        // Print usernames
        int numUsernames = usernames.size();
        System.out.println("Usernames (" + numUsernames + "):");
        System.out.print("    ");
        for (int i = 0; i < usernames.size(); i++) {
            System.out.print(usernames.get(i));
            if (i != usernames.size() - 1) { // not the last element
                System.out.print(", ");
                if ((i + 1) % 4 == 0) { // print new line after every 4 elements
                    System.out.println();
                    System.out.print("    "); // indent to align with "Usernames: "
                }
            }
        }

        System.out.println();

        // Print keywords
        int numKeywords = keywords.size();
        System.out.println("Keywords (" + numKeywords + "): ");
        System.out.print("    ");
        for (int i = 0; i < keywords.size(); i++) {
            System.out.print(keywords.get(i));
            if (i != keywords.size() - 1) { // not the last element
                System.out.print(", ");
                if ((i + 1) % 4 == 0) { // print new line after every 4 elements
                    System.out.println();
                    System.out.print("    "); // indent to align with "Usernames: "
                }
            }
        }
        System.out.println();
    }
}