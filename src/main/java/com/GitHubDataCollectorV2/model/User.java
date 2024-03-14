package com.GitHubDataCollectorV2.model;

import com.GitHubDataCollectorV2.util.Dates;
import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "gitHubUsers")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Date createdAt = Dates.nowUTC();

    @NotNull
    @Column(nullable = false)
    private Date lastUpdated = Dates.nowUTC();

    private String name;
    private String username;
    private String url;
    private int publicRepos;
    private Integer publicReposPercentile;
    private int forkedRepos;
    private int emptyRepos;
    private int followers;
    private int following;
    private int scssRepositories;
    private int assemblyRepositories;
    private int pawnRepositories;
    private int javaRepositories;
    private int ejsRepositories;
    private int cSharpRepositories;
    private int javaScriptRepositories;
    private int jupyterRepositories;
    private int cppRepositories;
    private int cssRepositories;
    private int pythonRepositories;
    private int nodeJsRepositories;
    private int angularRepositories;
    private int reactRepositories;
    private int objectiveCRepositories;
    private int dartRepositories;
    private int typeScriptRepositories;
    private int CRepositories;
    private int kotlinRepositories;
    private int htmlRepositories;
    private int swiftRepositories;
    private int goRepositories;
    private int rustRepositories;
    private int rubyRepositories;
    private int scalaRepositories;
    private int phpRepositories;
    private int rRepositories;
    private int forks;
    private int commits;
    private Integer commitsPercentile;
    private int stars;
    private int codeLines;
    private Integer codeLinesPercentile;
    private int tests;
    private String keywords;

    public void updateUser(User user) {
        user.setLastUpdated(Dates.nowUTC());
        user.setName(name);
        user.setUsername(username);
        user.setUrl(url);
        user.setPublicRepos(publicRepos);
        user.setForkedRepos(forkedRepos);
        user.setEmptyRepos(emptyRepos);
        user.setFollowers(followers);
        user.setFollowing(following);
        user.setScssRepositories(scssRepositories);
        user.setAssemblyRepositories(assemblyRepositories);
        user.setPawnRepositories(pawnRepositories);
        user.setJavaRepositories(javaRepositories);
        user.setEjsRepositories(ejsRepositories);
        user.setcSharpRepositories(cSharpRepositories);
        user.setJavaScriptRepositories(javaScriptRepositories);
        user.setJupyterRepositories(jupyterRepositories);
        user.setCppRepositories(cppRepositories);
        user.setCssRepositories(cssRepositories);
        user.setPythonRepositories(pythonRepositories);
        user.setNodeJsRepositories(nodeJsRepositories);
        user.setAngularRepositories(angularRepositories);
        user.setReactRepositories(reactRepositories);
        user.setObjectiveCRepositories(objectiveCRepositories);
        user.setDartRepositories(dartRepositories);
        user.setTypeScriptRepositories(typeScriptRepositories);
        user.setCRepositories(CRepositories);
        user.setKotlinRepositories(kotlinRepositories);
        user.setHtmlRepositories(htmlRepositories);
        user.setSwiftRepositories(swiftRepositories);
        user.setGoRepositories(goRepositories);
        user.setRustRepositories(rustRepositories);
        user.setRubyRepositories(rubyRepositories);
        user.setScalaRepositories(scalaRepositories);
        user.setPhpRepositories(phpRepositories);
        user.setrRepositories(rRepositories);
        user.setForks(forks);
        user.setCommits(commits);
        user.setStars(stars);
        user.setCodeLines(codeLines);
        user.setTests(tests);
        user.setKeywords(keywords);
        user.setCommitsPercentile(commitsPercentile);
        user.setPublicReposPercentile(publicReposPercentile);
        user.setCodeLinesPercentile(codeLinesPercentile);
    }

    public User JSONObjectToUser(JSONObject obj){
        User user = new User();
        user.setName(obj.getString("name"));
        user.setUsername(obj.getString("username"));
        user.setUrl(obj.getString("url"));
        user.setPublicRepos(obj.getInt("public_repos"));
        user.setForkedRepos(obj.getInt("forked_repos"));
        user.setEmptyRepos(obj.getInt("empty_repos"));
        user.setFollowers(obj.getInt("followers"));
        user.setFollowing(obj.getInt("following"));
        user.setJavaRepositories(obj.getInt("java_repositories"));
        user.setEjsRepositories(obj.getInt("ejs_repositories"));
        user.setScssRepositories(obj.getInt("scss_repositories"));
        user.setAssemblyRepositories(obj.getInt("assembly_repositories"));
        user.setPawnRepositories(obj.getInt("pawn_repositories"));
        user.setcSharpRepositories(obj.getInt("cSharp_repositories"));
        user.setJavaScriptRepositories(obj.getInt("javaScript_repositories"));
        user.setJupyterRepositories(obj.getInt("jupyter_repositories"));
        user.setCppRepositories(obj.getInt("cpp_repositories"));
        user.setCssRepositories(obj.getInt("css_repositories"));
        user.setPythonRepositories(obj.getInt("python_repositories"));
        user.setNodeJsRepositories(obj.getInt("node.js_repositories"));
        user.setAngularRepositories(obj.getInt("angular_repositories"));
        user.setReactRepositories(obj.getInt("react_repositories"));
        user.setObjectiveCRepositories(obj.getInt("objectiveC_repositories"));
        user.setDartRepositories(obj.getInt("dart_repositories"));
        user.setTypeScriptRepositories(obj.getInt("typeScript_repositories"));
        user.setCRepositories(obj.getInt("c_repositories"));
        user.setKotlinRepositories(obj.getInt("kotlin_repositories"));
        user.setHtmlRepositories(obj.getInt("html_repositories"));
        user.setSwiftRepositories(obj.getInt("swift_repositories"));
        user.setGoRepositories(obj.getInt("go_repositories"));
        user.setRustRepositories(obj.getInt("rust_repositories"));
        user.setRubyRepositories(obj.getInt("ruby_repositories"));
        user.setScalaRepositories(obj.getInt("scala_repositories"));
        user.setPhpRepositories(obj.getInt("php_repositories"));
        user.setrRepositories(obj.getInt("r_repositories"));
        user.setForks(obj.getInt("forks"));
        user.setCommits(obj.getInt("commits"));
        user.setStars(obj.getInt("stars"));
        user.setCodeLines(obj.getInt("code_lines"));
        user.setTests(obj.getInt("tests"));
        user.setKeywords(obj.getString("keywords"));
        user.setCommitsPercentile(obj.has("commits_percentile") ?  obj.getInt("commits_percentile") : 0);
        user.setPublicReposPercentile(obj.has("public_repos_percentile") ? obj.getInt("public_repos_percentile") : 0);
        user.setCodeLinesPercentile(obj.has("code_lines_percentile") ? obj.getInt("code_lines_percentile") : 0);

        return user;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getEmptyRepos() {
        return emptyRepos;
    }

    public void setEmptyRepos(int emptyRepos) {
        this.emptyRepos = emptyRepos;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPublicRepos(int publicRepos) {
        this.publicRepos = publicRepos;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public void setScssRepositories(int scssRepositories) {
        this.scssRepositories = scssRepositories;
    }

    public void setAssemblyRepositories(int assemblyRepositories) {
        this.assemblyRepositories = assemblyRepositories;
    }

    public void setPawnRepositories(int pawnRepositories) {
        this.pawnRepositories = pawnRepositories;
    }

    public void setJavaRepositories(int javaRepositories) {
        this.javaRepositories = javaRepositories;
    }

    public void setEjsRepositories(int ejsRepositories) {
        this.ejsRepositories = ejsRepositories;
    }

    public void setcSharpRepositories(int cSharpRepositories) {
        this.cSharpRepositories = cSharpRepositories;
    }

    public int getForkedRepos() {
        return forkedRepos;
    }

    public void setForkedRepos(int forkedRepos) {
        this.forkedRepos = forkedRepos;
    }

    public void setJavaScriptRepositories(int javaScriptRepositories) {
        this.javaScriptRepositories = javaScriptRepositories;
    }

    public void setJupyterRepositories(int jupyterRepositories) {
        this.jupyterRepositories = jupyterRepositories;
    }

    public void setCppRepositories(int cppRepositories) {
        this.cppRepositories = cppRepositories;
    }

    public void setCssRepositories(int cssRepositories) {
        this.cssRepositories = cssRepositories;
    }

    public void setPythonRepositories(int pythonRepositories) {
        this.pythonRepositories = pythonRepositories;
    }

    public void setNodeJsRepositories(int nodeJsRepositories) {
        this.nodeJsRepositories = nodeJsRepositories;
    }

    public void setAngularRepositories(int angularRepositories) {
        this.angularRepositories = angularRepositories;
    }

    public void setReactRepositories(int reactRepositories) {
        this.reactRepositories = reactRepositories;
    }

    public void setObjectiveCRepositories(int objectiveCRepositories) {
        this.objectiveCRepositories = objectiveCRepositories;
    }

    public void setDartRepositories(int dartRepositories) {
        this.dartRepositories = dartRepositories;
    }

    public void setTypeScriptRepositories(int typeScriptRepositories) {
        this.typeScriptRepositories = typeScriptRepositories;
    }

    public void setCRepositories(int CRepositories) {
        this.CRepositories = CRepositories;
    }

    public void setKotlinRepositories(int kotlinRepositories) {
        this.kotlinRepositories = kotlinRepositories;
    }

    public void setHtmlRepositories(int htmlRepositories) {
        this.htmlRepositories = htmlRepositories;
    }

    public void setSwiftRepositories(int swiftRepositories) {
        this.swiftRepositories = swiftRepositories;
    }

    public void setGoRepositories(int goRepositories) {
        this.goRepositories = goRepositories;
    }

    public void setRustRepositories(int rustRepositories) {
        this.rustRepositories = rustRepositories;
    }

    public void setRubyRepositories(int rubyRepositories) {
        this.rubyRepositories = rubyRepositories;
    }

    public void setScalaRepositories(int scalaRepositories) {
        this.scalaRepositories = scalaRepositories;
    }

    public void setPhpRepositories(int phpRepositories) {
        this.phpRepositories = phpRepositories;
    }

    public void setrRepositories(int rRepositories) {
        this.rRepositories = rRepositories;
    }

    public void setForks(int forks) {
        this.forks = forks;
    }

    public void setCommits(int commits) {
        this.commits = commits;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public void setCodeLines(int codeLines) {
        this.codeLines = codeLines;
    }

    public void setTests(int tests) {
        this.tests = tests;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public int getPublicReposPercentile() {
        return publicReposPercentile;
    }

    public void setPublicReposPercentile(Integer publicReposPercentile) {
        this.publicReposPercentile = publicReposPercentile;
    }

    public int getCommitsPercentile() {
        return commitsPercentile;
    }

    public void setCommitsPercentile(Integer commitsPercentile) {
        this.commitsPercentile = commitsPercentile;
    }

    public int getCodeLinesPercentile() {
        return codeLinesPercentile;
    }

    public void setCodeLinesPercentile(Integer codeLinesPercentile) {
        this.codeLinesPercentile = codeLinesPercentile;
    }


    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getUrl() {
        return url;
    }

    public int getPublicRepos() {
        return publicRepos;
    }

    public int getFollowers() {
        return followers;
    }

    public int getFollowing() {
        return following;
    }

    public int getScssRepositories() {
        return scssRepositories;
    }

    public int getAssemblyRepositories() {
        return assemblyRepositories;
    }

    public int getPawnRepositories() {
        return pawnRepositories;
    }

    public int getJavaRepositories() {
        return javaRepositories;
    }

    public int getEjsRepositories() {
        return ejsRepositories;
    }

    public int getcSharpRepositories() {
        return cSharpRepositories;
    }

    public int getJavaScriptRepositories() {
        return javaScriptRepositories;
    }

    public int getJupyterRepositories() {
        return jupyterRepositories;
    }

    public int getCppRepositories() {
        return cppRepositories;
    }

    public int getCssRepositories() {
        return cssRepositories;
    }

    public int getPythonRepositories() {
        return pythonRepositories;
    }

    public int getNodeJsRepositories() {
        return nodeJsRepositories;
    }

    public int getAngularRepositories() {
        return angularRepositories;
    }

    public int getReactRepositories() {
        return reactRepositories;
    }

    public int getObjectiveCRepositories() {
        return objectiveCRepositories;
    }

    public int getDartRepositories() {
        return dartRepositories;
    }

    public int getTypeScriptRepositories() {
        return typeScriptRepositories;
    }

    public int getCRepositories() {
        return CRepositories;
    }

    public int getKotlinRepositories() {
        return kotlinRepositories;
    }

    public int getHtmlRepositories() {
        return htmlRepositories;
    }

    public int getSwiftRepositories() {
        return swiftRepositories;
    }

    public int getGoRepositories() {
        return goRepositories;
    }

    public int getRustRepositories() {
        return rustRepositories;
    }

    public int getRubyRepositories() {
        return rubyRepositories;
    }

    public int getScalaRepositories() {
        return scalaRepositories;
    }

    public int getPhpRepositories() {
        return phpRepositories;
    }

    public int getrRepositories() {
        return rRepositories;
    }

    public int getForks() {
        return forks;
    }

    public int getCommits() {
        return commits;
    }

    public int getStars() {
        return stars;
    }

    public int getCodeLines() {
        return codeLines;
    }

    public int getTests() {
        return tests;
    }

    public String getKeywords() {
        return keywords;
    }
}
