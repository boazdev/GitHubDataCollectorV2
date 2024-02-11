package com.GitHubDataCollectorV2.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.GitHubDataCollectorV2.model.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

@Service
public class JsonToCsvService {

    @Autowired
    UserService userService;

    public List<User> jsonStringToUsersAndSave(String jsonData) throws IOException, JSONException, InterruptedException {
        JSONArray jsonArray = new JSONArray(jsonData);
        List<User> users = new ArrayList<>();
        JSONObject obj;
        User user;

        for (int i = 0; i < jsonArray.length(); i++) {
            obj = jsonArray.getJSONObject(i);
            user = new User().JSONObjectToUser(obj);
            //userService.save(user);
            users.add(user);
        }

        return users;
    }

    public String writeJsonToCsv(String jsonData) throws IOException, InterruptedException {
        //Get users data
        List<User> users = jsonStringToUsersAndSave(jsonData);

        // Define headers for CSV
        String[] headers = { "Name", "Username", "Url", "Followers", "Following", "Forks", "Commits", "Stars", "Code Lines", "Tests", "Keywords",
                "Public Repos", "Forked Repositories", "Empty Repositories",
                "Java Repositories", "EJS Repositories", "C# Repositories", "JavaScript Repositories", "Jupyter Notebook Repositories",
                "C++ Repositories", "CSS Repositories", "Python Repositories", "Node.js Repositories", "Angular Repositories",
                "React Repositories","HTML Repositories", "Kotlin Repositories","C Repositories","TypeScript Repositories",
                "Dart Repositories", "Objective-C Repositories", "Swift Repositories", "Go Repositories", "Rust Repositories",
                "Ruby Repositories", "Scala Repositories", "PHP Repositories", "R Repositories", "SCSS Repositories",
                "Assembly Repositories", "Pawn Repositories",
        };
        String[] columnsMapping = {"name", "username", "url", "followers","following", "forks", "commits", "stars", "codeLines", "tests", "keywords",
                "publicRepos", "forkedRepos", "emptyRepos",
                "javaRepositories","ejsRepositories", "cSharpRepositories", "javaScriptRepositories", "jupyterRepositories", "cppRepositories",
                "cssRepositories","pythonRepositories","nodeJsRepositories", "angularRepositories","reactRepositories","htmlRepositories","kotlinRepositories",
                "cRepositories","typeScriptRepositories","dartRepositories", "objectiveCRepositories","swiftRepositories", "goRepositories", "rustRepositories",
                "rubyRepositories", "scalaRepositories", "phpRepositories", "rRepositories", "scssRepositories", "assemblyRepositories", "pawnRepositories",
        };

        //Writing to CSV file
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        try (ICsvBeanWriter csvWriter = new CsvBeanWriter(pw, CsvPreference.STANDARD_PREFERENCE)) {
            csvWriter.writeHeader(headers);

            for (User user : users) {
                csvWriter.write(user, columnsMapping);
            }
        }

        System.out.println("Csv file written successfully.");
        String res = baos.toString(StandardCharsets.UTF_8)
                .replaceAll("\r\n          ","")
                .replaceAll("\"\r\n","\"\n");
        return res;
    }
}

