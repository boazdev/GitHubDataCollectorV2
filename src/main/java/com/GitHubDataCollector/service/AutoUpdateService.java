package com.GitHubDataCollector.service;

import com.GitHubDataCollector.model.User;
import com.GitHubDataCollector.util.Dates;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class AutoUpdateService {
    private static final int DAY_TIME_IN_MILLISECONDS = 86400000; // 1 Day
    public static final Pattern SEARCH_USER = Pattern.compile("url=\\\"/users/([^/]+)");
    public static final Pattern RATE_LIMIT = Pattern.compile("before you ([^;]+)");
    private static final Pattern MAX_LIMIT = Pattern.compile("You have exceeded a secondary rate ([^t]+)");
    private static final String SOFTWARE_FIELD = "selRjCNORbnPgaY0r";
    public static final int NUMBER_OF_THREADS = 20;
    private static final int DAYS_TO_UPDATE = 3;
    private static final int MONTHS_AGO_TO_CHECK = 3;

    @Autowired
    GitHubService gitHubService;

    @Autowired
    UserService userService;

    @Autowired
    JsonToCsvService jsonToCsvService;

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(60))
            .readTimeout(Duration.ofSeconds(60))
            .connectTimeout(Duration.ofSeconds(60))
            .build();

    public void startAutoUpdate() throws IOException, InterruptedException {
        Iterable<User> users;
        int daysDifference;
        List<String> existingNames = new ArrayList<>();

        while(true) {
            System.out.println("Start updating DB");
            users = userService.all();
            for (User user : users) {
                daysDifference = Dates.getDaysDifference(user.getLastUpdated(), Dates.nowUTC());
                if (daysDifference > DAYS_TO_UPDATE) {
                    System.out.println("Updating user: " + user.getUsername());
                    String userString = gitHubService.getUserInfo(user.getUsername(), null);
                    User updatedUser = user.JSONObjectToUser(new JSONObject(userString));
                    userService.save(updatedUser);
                }
                existingNames.add(user.getName());
            }
            searchGitHubUsers(existingNames);
            System.out.println("Finished update DB");
            Thread.sleep(DAY_TIME_IN_MILLISECONDS);
        }
    }

    public void saveNewUsers(List<String> usernames) throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        for(String username : usernames){
            executorService.execute(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(gitHubService.getUserInfo(username, null));
                    User user = new User().JSONObjectToUser(jsonObject);
                    userService.save(user);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.HOURS);
    }

    public void searchGitHubUsers(List<String> existingNames) throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS/2);

        //Fetching names
        List<String> names = getUsersFromGoozali(existingNames);
        //add names source 2...
        //add names source 3...
        System.out.println("Start getting usernames from GitHub");


        //Searching on GitHub
        Request request;
        Response response;
        String jsonString;
        JSONObject jsonObject, jsonUser;
        int results, numOfPages;
        boolean flag = true;
        String template = "";
        Random random = new Random();

        for(String name : names){ //Building the search url with the names
            template += " fullname:\"" + name + "\"";
        }

        for(int i=1 ; flag ; i++) {
            request = new Request.Builder()
                    .url("https://github.com/search?ref=advsearch&q=" + template + "&type=users&p=" + i)
                    .method("GET", null)
                    .addHeader("authority", "github.com")
                    .addHeader("accept", "application/json")
                    .addHeader("accept-language", "en-US,en;q=0.9,he;q=0.8")
                    .addHeader("cookie", "_octo=GH1.1.635886773.1666110862; _device_id=28918aa01edf99a154d0ea5d3dd6fa63; user_session=CtFg7O6mfA8MpLPkeE-sZTg0R6jwaKKaEb-pOo63hqu3-C8p; __Host-user_session_same_site=CtFg7O6mfA8MpLPkeE-sZTg0R6jwaKKaEb-pOo63hqu3-C8p; logged_in=yes; dotcom_user=NoamGivaty; color_mode=%7B%22color_mode%22%3A%22auto%22%2C%22light_theme%22%3A%7B%22name%22%3A%22light%22%2C%22color_mode%22%3A%22light%22%7D%2C%22dark_theme%22%3A%7B%22name%22%3A%22dark%22%2C%22color_mode%22%3A%22dark%22%7D%7D; preferred_color_mode=light; tz=Asia%2FJerusalem; has_recent_activity=1; _gh_sess=C3J4poaISxeCGyQTgg6eH%2B3yUophW4pNbhSeXiz1k1BM5drizK1Rk7iL9R6zck7njTzXbPKfytgMVgnojtB57d5kCbLJccOWdGUuLXRLevmCdgMEsZvyhJzOcxm7nwW3iXb04p5XJN7YGYC9%2BfwubGuvJkBPfgHC%2B0UA8VThm%2Fpd8NyfNf%2Bh29WYzvSBbqdbmghMuOOogae0jBre8Ra20P%2FIKDXb3kmfMlesMy3z7N9l7A61GlqNh%2FqhTdPNa0guwgQ17zwUNyDkGHDBAqatVzQJsFUT3d30Gn%2FftDZpsQeNpQZNMlkXFb9UCaI6VK33KLP2WERE%2B1m2gVlZDQ%3D%3D--fv0kAxxGK0zsBm3L--Uxfgj51Gg9ZARfbOICvlww%3D%3D; _gh_sess=wHKiO6qTETme7BkMMha2KAyD7CWVXYtUTKdn6XgzHFPz%2BZ28NRGukcJV4Y4vNuKnmPiRz5D1%2Fmmv1wAI%2BOki81h%2BQzRCGK5fvZc9tyX%2BXCWhrDLiv2%2F4rj6gPecSvtyCm3pHYU%2BnR%2F%2BcN%2BFAoWOnwgQwM3r23MWApOtNcOF%2F5VM68hz4ypbMPygJ0b%2Bxxv2s5XJrqcnNg248eM6rUUIeRe%2FClMJ1LaCvIBRurziPsmFOYrqGmz9kPGKQB6LX3efubXeNUt%2FDKZJnoOha7VdMfdeEBXnab6V8grOm218Fu7P6lbfmCDITqBL%2B%2F5loBvk9MBC1FSfzNRFcmDGg6g%3D%3D--Zt0D2GGkBZxEvDFw--Q3z1iixC4%2BYCo%2FVhsjqIdg%3D%3D; _octo=GH1.1.1338388705.1682315579; has_recent_activity=1; logged_in=no")
                    //.addHeader("referer", "https://github.com/search?ref=advsearch&q=" + template + "&type=users&p=1")
                    .addHeader("sec-ch-ua", "\"Chromium\";v=\"112\", \"Google Chrome\";v=\"112\", \"Not:A-Brand\";v=\"99\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("sec-ch-ua-platform", "\"Windows\"")
                    .addHeader("sec-fetch-dest", "empty")
                    .addHeader("sec-fetch-mode", "cors")
                    .addHeader("sec-fetch-site", "same-origin")
                    .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36")
                    .addHeader("x-github-target", "dotcom")
                    .addHeader("x-requested-with", "XMLHttpRequest")
                    .build();
            response = client.newCall(request).execute();
            if(response.code()==200) {
                List<String> usernames = new ArrayList<>();
                jsonString = response.body().string();
                jsonObject = new JSONObject(jsonString); //Page with results
                results = jsonObject.getJSONObject("payload").getJSONArray("results").length(); //Number of users results

                for (int j = 0; j < results; j++) {
                    jsonUser = jsonObject.getJSONObject("payload").getJSONArray("results").getJSONObject(j);
                    if (jsonUser.isNull("location") || jsonUser.getString("location").equals("Israel")) // null / Israel
                        usernames.add(jsonUser.getString("login"));
                }

                //Saving users
                executorService.execute(() -> {
                    try {
                        saveNewUsers(usernames);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

                if (jsonObject.getJSONObject("payload").getInt("page_count") == i) //checking number of page results
                    flag = false;
                Thread.sleep(3000);
            }
            else if(response.code()==429){
                System.err.println("GitHub throw 429 (Rate limit exceeded)");
                Thread.sleep(20000 + random.nextInt(20000));
            }
            else{
                System.err.println("GitHub throw " + response.code());
                Thread.sleep(20000 + random.nextInt(20000));
            }
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.HOURS);
    }

    public List<String> getUsersFromGoozali(List<String> existingNames) throws IOException {
        System.out.println("Goozali.com | Getting new names");

        Request request = new Request.Builder()
                .url("https://airtable.com/v0.3/view/viwagEIbkfz2iMsLU/readSharedViewData?stringifiedObjectParams=%7B%22shouldUseNestedResponseFormat%22%3Atrue%7D&requestId=reqqDdsRs5avkhsev&accessPolicy=%7B%22allowedActions%22%3A%5B%7B%22modelClassName%22%3A%22view%22%2C%22modelIdSelector%22%3A%22viwagEIbkfz2iMsLU%22%2C%22action%22%3A%22readSharedViewData%22%7D%2C%7B%22modelClassName%22%3A%22view%22%2C%22modelIdSelector%22%3A%22viwagEIbkfz2iMsLU%22%2C%22action%22%3A%22getMetadataForPrinting%22%7D%2C%7B%22modelClassName%22%3A%22view%22%2C%22modelIdSelector%22%3A%22viwagEIbkfz2iMsLU%22%2C%22action%22%3A%22readSignedAttachmentUrls%22%7D%2C%7B%22modelClassName%22%3A%22row%22%2C%22modelIdSelector%22%3A%22rows%20*%5BdisplayedInView%3DviwagEIbkfz2iMsLU%5D%22%2C%22action%22%3A%22createDocumentPreviewSession%22%7D%5D%2C%22shareId%22%3A%22shr97tl6luEk4Ca9R%22%2C%22applicationId%22%3A%22app5sYJyDgcRbJWYU%22%2C%22generationNumber%22%3A0%2C%22expires%22%3A%222023-05-25T00%3A00%3A00.000Z%22%2C%22signature%22%3A%22361eb85e46a72cbc038b21d07578e13c0ec967f154f8ddddc9271fa574c4d8c7%22%7D")
                .method("GET", null)
                .addHeader("authority", "airtable.com")
                .addHeader("accept", "*/*")
                .addHeader("accept-language", "en-US,en;q=0.9,he;q=0.8")
                .addHeader("sec-ch-ua", "\"Chromium\";v=\"112\", \"Google Chrome\";v=\"112\", \"Not:A-Brand\";v=\"99\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"Windows\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36")
                .addHeader("x-airtable-accept-msgpack", "true")
                .addHeader("x-airtable-application-id", "app5sYJyDgcRbJWYU")
                .addHeader("x-airtable-inter-service-client", "webClient")
                .addHeader("x-airtable-page-load-id", "pglml02bmH6mNBo0C")
                .addHeader("x-early-prefetch", "true")
                .addHeader("x-requested-with", "XMLHttpRequest")
                .addHeader("x-time-zone", "Asia/Jerusalem")
                .addHeader("x-user-locale", "en")
                .build();
        Response response = client.newCall(request).execute();
        String html = response.body().string();
        JSONArray jsonArray = new JSONObject(html).getJSONObject("data").getJSONObject("table").getJSONArray("rows");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime createdTime, monthsAgo = LocalDateTime.now().minusMonths(MONTHS_AGO_TO_CHECK); ;

        List<String> users = new ArrayList<>();
        List<String> oldUsers = new ArrayList<>();
        boolean field;
        String name;
        int size = jsonArray.length();
//        int count = 0;

        for(int i = 0 ; i < size ; i++){
            JSONObject userData = jsonArray.getJSONObject(i).getJSONObject("cellValuesByColumnId"); // User json data
            field = userData.getString("fldtLKVZMOHgt7mOT").equals(SOFTWARE_FIELD); // Field of interest
            createdTime = LocalDateTime.parse((userData.getString("fldlTVRtzF5Rf5e2S")), formatter); //Date created

            if(createdTime.isAfter(monthsAgo) && field){ //Checking if: up to 3 months old && software engineering field
                name = userData.get("fldhfFhFeRX8qLvUp").toString();
//                count++;
                if(!existingNames.contains(name)) //Checking if username already exist
                    users.add(name);
//                else
//                    oldUsers.add(name);
                //System.out.println("Adding user: " + name);
            }
        }
        System.out.println("Goozali.com | Got " + users.size() + " new names");
        return users;
    }
}
