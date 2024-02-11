package com.GitHubDataCollectorV2.service;

import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.json.JSONArray;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;

@Service
public class GitHubService {
    private static String[] EXTENSIONS = new String[]{".java", ".py", ".js", ".php", ".rb", ".cpp", ".h", ".c", ".cs", ".html", ".ipynb", ".css", ".ts", ".kt", ".dart", ".m", ".swift", ".go", ".rs", ".rb", ".scala", ".scss", ".asm", ".pwn", ".inc"};
    public static final Pattern REPOSITORIES_PATTERN = Pattern.compile("codeRepository[^\\s]+[^\\S][^\\n][^\\S]+([^<]+)");
    public static final Pattern NUM_OF_REPOSITORIES_PATTERN = Pattern.compile("Repositories[^<]+<span title=\\\"([^\\\"]+)");
    public static final Pattern NAME_PATTERN = Pattern.compile("itemprop=\\\"name[^\\s]+[^\\S][^\\n][^\\S]+([^\\n]+)");
    public static final Pattern FOLLOWERS_PATTERN = Pattern.compile("class=\\\"text-bold color-fg-default\\\">([^<]+)[^\\d]+follower");
    public static final Pattern FOLLOWING_PATTERN = Pattern.compile("fg-default\\\">([^<]+)[^]d]+\\bfollowing\\b");
    public static final Pattern FORKS_PATTERN1 = Pattern.compile("text-bold[^>]+>([^<]+)[^f]+forks");
    public static final Pattern FORKS_PATTERN2 = Pattern.compile("</svg>Fork[^C]+[^\\d]+([^<]+)");
    public static final Pattern COMMITS_PATTERN = Pattern.compile("<span class=\\\"d-none d-sm-inline\\\">[^\\S]+<strong>([^<]+)");
    public static final Pattern STARS_PATTERN1 = Pattern.compile("text-bold[^>]+>([^<]+)[^f]+stars");
    public static final Pattern STARS_PATTERN2 = Pattern.compile("repo-stars-counter-star[^=]+=\\\"([^ ]+)");
    public static final Pattern PROGRAMMING_LANGUAGE_PATTERN = Pattern.compile("programmingLanguage\\\">([^<]+)");
    public static final Pattern FORKED_FROM_PATTERN = Pattern.compile("forked[ ]from([^h]+)");
    public static final Pattern EMPTY_PATTERN = Pattern.compile("This repository is ([^.]+)");
    public static final Pattern USER_NOT_FOUND_PATTERN = Pattern.compile("Not ([^ ]+)");
    public static final int NUMBER_OF_THREADS = 20;

    private static OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(60))
            .readTimeout(Duration.ofSeconds(60))
            .connectTimeout(Duration.ofSeconds(60))
            .build();


    //----main-methods------------
    public String getMultiplyUsersInfo(List<String> users, List<String> keywords) throws IOException, InterruptedException {

        JSONArray jsonArray = new JSONArray();
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        AtomicInteger count = new AtomicInteger();
        for (String user : users) {
            executorService.execute(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(getUserInfo(user, keywords));
                    jsonArray.put(jsonObject);
                    count.getAndIncrement();
                    System.out.println(user + " | Succeed getting user info | " + count.get() + "/" + users.size() + " Users");
                } catch (IOException | InterruptedException e) {
                    System.err.println(user + " | Failed to get user info");
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1000000, TimeUnit.HOURS);

        System.out.println();
        System.out.println("Succeed to get " +count + " users info from " + users.size() + " users");

        return jsonArray.toString();
    }
    public String getUserInfo(String username, List<String> keywords) throws IOException, InterruptedException {

        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode objectNode = factory.objectNode();

        objectNode.setAll(rawData(username));
        if(objectNode.get("UserNotFoundError")!=null)
            return objectNode.toString();
        objectNode.setAll(programmingLanguageUsed(username,objectNode.get("public_repos").asText()));
        objectNode.setAll(repositoriesData(username, keywords, objectNode.get("public_repos").asText()));

        return  objectNode.toString();
    } //main method

    //----data-collectors------------
    public static ObjectNode rawData (String username) throws IOException, InterruptedException {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode objectNode = factory.objectNode();
        String html = getRepositoriesPageHtml(username,1);

        if(getRegexGroup(USER_NOT_FOUND_PATTERN,html,"",username).equals("Found")){
            objectNode.put("name","User not found");
            objectNode.put("UserNotFoundError",true);
            return objectNode;
        }


        String name = getRegexGroup(NAME_PATTERN,html, "Name", username);
        if (name.equals("</span>")) name = "N/A";
        objectNode.put("name",name);
        objectNode.put("username",username);
        objectNode.put("url","https://github.com/" + username);
        objectNode.put("public_repos",getRegexGroup(NUM_OF_REPOSITORIES_PATTERN,html, "Number of Repositories", username));
        objectNode.put("followers",getRegexGroup(FOLLOWERS_PATTERN,html, "Followers", username));
        objectNode.put("following",getRegexGroup(FOLLOWING_PATTERN,html , "Following", username));

        System.out.println(username + " | Succeed getting user raw data");

        return objectNode;
    } //"dry" data from the user
    public static ObjectNode repositoriesData(String username, List<String> keywords, String NumOfPublicRepos) throws IOException, InterruptedException {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode objectNode = factory.objectNode();
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        List<Thread> threads = new ArrayList<>();
        List<String> allRepositories = getAllRepositories(username,NumOfPublicRepos);
        List<String> validRepositories = new ArrayList<>();

        AtomicInteger forks = new AtomicInteger(0);
        AtomicInteger commits = new AtomicInteger(0);
        AtomicInteger stars = new AtomicInteger(0);
        AtomicInteger emptyRepositories = new AtomicInteger(0);
        AtomicInteger forkedRepositories = new AtomicInteger(0);

        for(String URL : allRepositories){
            Thread thread = new Thread(() -> {
                boolean isEmpty;
                boolean isForked;
                String html;
                try {
                    html = getPageHtml(URL);
                    isEmpty = !getRegexGroup(EMPTY_PATTERN, html, "Empty", username).equals("0");
                    isForked = !getRegexGroup(FORKED_FROM_PATTERN, html, "Forked", username).equals("0");
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }


                if (isEmpty)
                    emptyRepositories.incrementAndGet();
                else if (isForked)
                    forkedRepositories.incrementAndGet();
                else{
                    validRepositories.add(URL);
                    try {
                        forks.addAndGet(Integer.parseInt(getRegexGroup(FORKS_PATTERN1,html,URL,username)));
                        //commits.addAndGet(0);//.addAndGet(Integer.parseInt(getRegexGroup(COMMITS_PATTERN, html,URL,username)));
                        stars.addAndGet(Integer.parseInt(getRegexGroup(STARS_PATTERN1, html,URL,username)));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            threads.add(thread);
            executorService.execute(thread);
        }
        executorService.shutdown();
        executorService.awaitTermination(10000,TimeUnit.HOURS);

        objectNode.put("forks", forks.get());
        //objectNode.put("commits", commits.get());
        objectNode.put("stars", stars.get());
        objectNode.put("forked_repos", forkedRepositories.get());
        objectNode.put("empty_repos", emptyRepositories.get());

        System.out.println(
                username +
                " | Total repositories: " + allRepositories.size() +
                " | Valid: " + validRepositories.size() +
                " | Empty: " + emptyRepositories.get() +
                " | Forked: " + forkedRepositories.get());

        System.out.println(username + " | Succeed scraping repositories");

        String path = "data/" + username + "Repositories" + generateRandomString();
        cloneRepositories(validRepositories,username, path); //Cloning repositories

        objectNode.setAll(getFilesData(keywords, path));
        Integer numCommits = getNumCommitsFromGitFolders(validRepositories,path);
        System.out.println(String.format("number of commits for the user %s: %s",username,numCommits));
        deleteRepositoriesFolder(path); //Deleting cloned repositories
        objectNode.put("commits", numCommits);
        return objectNode;
    } //data that fetches from each repository

    private static Integer getNumCommitsFromGitFolders(List<String> validRepositories, String path) throws InterruptedException {
        AtomicInteger commitsCount = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        executorService.execute(new Thread(() -> {
            for (String repoUrl : validRepositories) {
                String repoName = getRepoNameFromGitCloneUrl(repoUrl);
                commitsCount.getAndAdd(getNumCommitsFromGitClone(path, repoName));
            }
        }));
        executorService.shutdown();
        executorService.awaitTermination(1000,TimeUnit.HOURS);

        return commitsCount.intValue();
    }

    private static String getRepoNameFromGitCloneUrl(String repoUrl) {
        return repoUrl.split("/")[4].split("\\.")[0];
    }

    public static ObjectNode programmingLanguageUsed(String username, String publicRepos) throws IOException {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode objectNode = factory.objectNode();

        float numOfRepositoriesPages = Float.parseFloat(publicRepos) / 30;
        int java = 0, python = 0, node = 0, angular = 0, react = 0,
                net = 0, ejs = 0, cSharp = 0, js = 0, jupyter = 0,
                cpp = 0, css = 0, kotlin = 0, c = 0, dart = 0, typeScript = 0,
                htmlRep = 0, objectiveC = 0, swift = 0, go = 0, rust = 0, ruby = 0,
                scala = 0, php = 0, r = 0, scss = 0, assembly = 0, pawn = 0;


        for(int i=1 ; numOfRepositoriesPages > 0 ; i++, numOfRepositoriesPages--){
            String html = getRepositoriesPageHtml(username,i);
            Matcher matcher = PROGRAMMING_LANGUAGE_PATTERN.matcher(html);

            while(matcher.find()){
                switch (matcher.group(1)){
                    case "SCSS":
                        scss++;
                        break;
                    case "Assembly":
                        assembly++;
                        break;
                    case "Pawn":
                        pawn++;
                        break;
                    case "Objective-C":
                        objectiveC++;
                        break;
                    case "Dart":
                        dart++;
                        break;
                    case "TypeScript":
                        typeScript++;
                        break;
                    case "C":
                        c++;
                        break;
                    case "Kotlin":
                        kotlin++;
                        break;
                    case "HTML":
                        htmlRep++;
                        break;
                    case "Java":
                        java++;
                        break;
                    case "EJS":
                        ejs++;
                        break;
                    case "C#":
                        cSharp++;
                        break;
                    case "JavaScript":
                        js++;
                        break;
                    case "Jupyter Notebook":
                        jupyter++;
                        break;
                    case "C++":
                        cpp++;
                        break;
                    case "CSS":
                        css++;
                        break;
                    case "Python":
                        python++;
                        break;
                    case "Node.js":
                        node++;
                        break;
                    case "Angular":
                        angular++;
                        break;
                    case "React":
                        react++;
                        break;
                    case ".NET":
                        net++;
                        break;
                    case "PHP":
                        php++;
                        break;
                    case "Ruby":
                        ruby++;
                        break;
                    case "Scala":
                        scala++;
                        break;
                    case "Swift":
                        swift++;
                        break;
                    case "Go":
                        go++;
                        break;
                    case "R":
                        r++;
                        break;
                    case "Rust":
                        rust++;
                        break;
                }
            }
        }
        {
            objectNode.put("scss_repositories", scss);
            objectNode.put("assembly_repositories", assembly);
            objectNode.put("pawn_repositories", pawn);
            objectNode.put("objectiveC_repositories", objectiveC);
            objectNode.put("kotlin_repositories", kotlin);
            objectNode.put("dart_repositories", dart);
            objectNode.put("c_repositories", c);
            objectNode.put("typeScript_repositories", typeScript);
            objectNode.put("html_repositories", htmlRep);
            objectNode.put("java_repositories", java);
            objectNode.put("ejs_repositories", ejs);
            objectNode.put("cSharp_repositories", cSharp);
            objectNode.put("javaScript_repositories", js);
            objectNode.put("jupyter_repositories", jupyter);
            objectNode.put("cpp_repositories", cpp);
            objectNode.put("css_repositories", css);
            objectNode.put("python_repositories", python);
            objectNode.put("node.js_repositories", node);
            objectNode.put("angular_repositories", angular);
            objectNode.put("react_repositories", react);
            objectNode.put(".net_repositories", net);
            objectNode.put("php_repositories", php);
            objectNode.put("ruby_repositories", ruby);
            objectNode.put("scala_repositories", scala);
            objectNode.put("swift_repositories", swift);
            objectNode.put("go_repositories", go);
            objectNode.put("r_repositories", r);
            objectNode.put("rust_repositories", rust);
        } //Switch case

        System.out.println(username + " | Succeed counting repositories languages");
        return objectNode;
    } //data on programming language

    //----clone-count-delete-(from files)------------
    public static void cloneRepositories(List<String> repositoryUrls, String username, String path) throws InterruptedException {
        File repositoriesFolder = new File(path);
        Boolean isFolderCreated = repositoriesFolder.mkdirs();
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        AtomicInteger succeed = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        System.out.println(username + " | Start clone repositories");
        //path = "data/" +username + "_" + repositoryName + "_" + generateRandomString();
        for (String repositoryUrl : repositoryUrls) {
            executorService.execute(new Thread(() -> {
                String[] command = {"git", "clone", repositoryUrl};
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.directory(repositoriesFolder);
                ////////######FIX
                boolean isCloned = false;
                for(int i = 0 ; i < 3 && !isCloned ; i++) {
                    try {
                        Map<String, String> environment = processBuilder.environment();
                        environment.put("GIT_LFS_SKIP_SMUDGE", "1");
                        Process process = processBuilder.start();
                        process.waitFor();
                        succeed.getAndIncrement();
                        isCloned = true;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(!isCloned){
                    System.err.println(username + " | Failed to clone repository | " + repositoryUrl);
                    failed.getAndIncrement();
                }
            }));
        }

        executorService.shutdown();
        executorService.awaitTermination(1000,TimeUnit.HOURS);
        System.out.println(username + " | Finish cloning repositories | Succeed: " + succeed + " | Failed: " + failed);
    }
    public static ObjectNode getFilesData(List<String> keys, String path) throws IOException {
        List<String> importantKeywords = importantKeywords();
        List<String> keywords = new ArrayList<>();
        keywords.addAll(importantKeywords);
        if(keys!=null)
            keywords.addAll(keys);

        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode objectNode = factory.objectNode();
        Map<String, Integer> linesOfCodePerRepository = new HashMap<>();
        Map<String, Integer> keywordCount = new HashMap<>();
        AtomicInteger totalLinesOfCode = new AtomicInteger();

        Path repositoriesFolder = Path.of(path);
        Files.walk(repositoriesFolder)
                .filter(Files::isRegularFile)
                .filter(x-> !(x.getFileName().toAbsolutePath().toString().contains(".git")))
                .filter(file -> {
                    String fileName = file.getFileName().toString();
                    for (String extension : EXTENSIONS) {
                        if (fileName.endsWith(extension)) {
                            return true;
                        }
                    }
                    return false;
                })
                .forEach(file -> {
                    try {
                        List<String> lines = Files.readAllLines(file);
                        int linesOfCode = lines.size();
                        totalLinesOfCode.addAndGet(linesOfCode);

                        Path repositoryPath = repositoriesFolder.relativize(file.getParent());
                        String repositoryName = repositoryPath.toString();
                        linesOfCodePerRepository.merge(repositoryName, linesOfCode, Integer::sum);

                        for (String keyword : keywords) {
                            int count = 0;
                            String regex = Pattern.quote(keyword.toLowerCase());
                            Pattern pattern = Pattern.compile(regex);
                            for (String line : lines) {
                                Matcher matcher = pattern.matcher(line.toLowerCase());
                                while (matcher.find()) {
                                    count++;
                                }
                            }
                            keywordCount.merge(keyword, count, Integer::sum);
                        }

                        //System.out.println("Read file: " + file);
                    } catch (IOException e) {
                        System.err.println("Failed to read file: " + file);
                    }
                });

        int count = 0;
        Map<String, Integer> words = new HashMap<>();

        for (String keyword : keywordCount.keySet()) {
            if(importantKeywords.contains(keyword))
                count += keywordCount.get(keyword);
            else
                words.put(keyword,keywordCount.get(keyword));
        }

        objectNode.put("code_lines",totalLinesOfCode.toString());
        objectNode.put("tests",count);
        if(words.toString().contains("{="))
            objectNode.put("keywords","-");
        else{
            String wordsString = words.toString();
            wordsString = wordsString.replace("{","");
            wordsString = wordsString.replace("}","");
            wordsString = wordsString.replace(","," ");
            objectNode.put("keywords",wordsString);
        }

        return objectNode;
    }
    public static List<String> importantKeywords(){
        List<String> keywords = new ArrayList<>();
        keywords.add("@Test"); //Java
        keywords.add("@def test_"); //Python
        keywords.add("[TestMethod]"); //C#
        keywords.add("[Test]"); //C#
        keywords.add("def test_"); //Ruby
        keywords.add("public function tes"); //PHP
        keywords.add("@func Test"); //Go
        keywords.add("@Test");

        return keywords;
    }
    public static void deleteRepositoriesFolder(String path){
        File repositoriesFolder = new File(path);
        System.out.println("deleting repositories folder");
        try {
            FileUtils.deleteDirectory(repositoriesFolder);
        } catch (IOException e) {
            System.err.println("Failed to delete directory: " + repositoriesFolder);
        }
    }
    //----different------------
    public static List<String> getAllRepositories(String username, String NumOfPublicRepos) throws IOException {
        float numOfRepositoriesPages = Float.parseFloat(NumOfPublicRepos) / 30;
        List<String> repositoryUrls = new ArrayList<>();

        for(int i=1 ; numOfRepositoriesPages > 0 ; i++, numOfRepositoriesPages--){
            String html = getRepositoriesPageHtml(username,i);
            Matcher matcher = REPOSITORIES_PATTERN.matcher(html);

            while(matcher.find()){
                repositoryUrls.add("https://github.com/" + username + "/" + matcher.group(1) + ".git");
            }
        }

        return repositoryUrls;
    }
    //----html/regex-requests------------
    public static String getRegexGroup(Pattern pattern, String html, String source, String username) throws InterruptedException {
        Random random = new Random();
        Matcher matcher = pattern.matcher(html);
        matcher.find();

        try {
            String ret = matcher.group(1);
            if (ret.endsWith("k"))
                ret = convertToNumber(ret);
            if (ret.contains(",")) {
                ret = ret.replace(",", "");
            }
            return ret;
        } catch (Exception e) {

            if (pattern.equals(FORKS_PATTERN1)) {
                return getRegexGroup(FORKS_PATTERN2, html, source, username);
            }
            if (pattern.equals(STARS_PATTERN1)) {
                return getRegexGroup(STARS_PATTERN2, html, source, username);
            }
            if (!pattern.equals(FORKED_FROM_PATTERN) && !pattern.equals(EMPTY_PATTERN) && !pattern.equals(USER_NOT_FOUND_PATTERN))
                System.err.println("Error | User: " + username + " | Source: " + source + " | Regex: " + pattern);
        }
        return "0";
    }
    public static String convertToNumber(String str) {
        double num = Double.parseDouble(str.substring(0, str.length() - 1));
        return String.valueOf((int) (num * 1000));
    }
    public static String getPageHtml(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
    public static String getRepositoriesPageHtml(String username, int pageNumber) throws IOException {
        Request request = new Request.Builder()
                .url("https://github.com/" + username + "?page=" + pageNumber +"&tab=repositories")
                .method("GET", null)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
    public static String generateRandomString() {
        final int LENGTH = 5;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(LENGTH);

        for (int i = 0; i < LENGTH; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    public static Integer getNumCommitsFromGitClone(String path, String repositoryName)  {
        File clonedRepoDir = new File(path,repositoryName);
        //System.out.println(String.format("count commits count in folder %s using git cmd",clonedRepoDir.getAbsolutePath()));
        String[] revListCommand = {"git", "rev-list", "--all", "--count"};
        ProcessBuilder revListProcessBuilder = new ProcessBuilder(revListCommand);
        revListProcessBuilder.directory(clonedRepoDir);
        try {
            Process revListProcess = revListProcessBuilder.start();
            int revListExitCode = revListProcess.waitFor();

            if (revListExitCode == 0) {
                try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(revListProcess.getInputStream()))) {
                    return Integer.parseInt(outputReader.readLine());
                }
            } else {
                System.err.println("Failed to run git rev-list command in order to get number of commits in repo");
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(revListProcess.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println(line);
                    }
                }
                System.out.println("error from git: " + revListProcess.getErrorStream().toString());
            }
        }
        catch (Exception ex){
            System.out.println("Exception: " + ex.toString());
            ex.printStackTrace();
        }


        return 0;
    }
}