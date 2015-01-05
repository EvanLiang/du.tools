package du.tools.main.stash;

import du.tools.main.ConfigAccessor;
import du.tools.main.Constants;
import du.tools.main.commons.utils.XmlHelper;
import du.tools.main.windows.WinStash;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Stash {

    private Logger logger = LoggerFactory.getLogger(Stash.class);
    private HttpClient hc = HttpClients.createDefault();
    private boolean login = false;
    private String stashDir = Constants.APP_TMP_DIR + "/Stash";

    private WinStash ui;

    public Stash(WinStash ui) {
        this.ui = ui;
        File dir = new File(stashDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException();
            }
        }
    }

    private void login() throws IOException {
        if (login) {
            logger.info("Already login");
            return;
        }
        logger.info("To login to Stash...");
        HttpPost post = new HttpPost("http://isstash.mandg.co.uk:8080/j_stash_security_check");
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("j_username", ConfigAccessor.getInstance().getStashUser()));
        nvps.add(new BasicNameValuePair("j_password", ConfigAccessor.getInstance().getStashPassword()));
        post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        HttpResponse hr = hc.execute(post);
        if (hr.getStatusLine().getStatusCode() == 302) {
            login = true;
            logger.info("Login successfully...");
            ui.showMsg("Login successfully with user:" + ConfigAccessor.getInstance().getStashUser());
        }

        logger.debug(hr.getStatusLine().toString());
        Header[] headers = hr.getAllHeaders();
        for (Header header : headers) {
            logger.debug(header.toString());
        }
        String responseString = "Login failed.";
        if (hr.getEntity() != null) {
            responseString = EntityUtils.toString(hr.getEntity());
            logger.debug(responseString);
        }

        if (!login) {
            ui.showMsg(responseString);
            throw new IllegalArgumentException(responseString);
        }
    }

    private void getRemoteProjects() throws IOException {
        login();
        logger.info("Get Remote Projects");
        ui.showMsg("Get Remote Projects");
        HttpGet get = new HttpGet("http://isstash.mandg.co.uk:8080/projects?start=0&limit=50&avatarSize=96");
        HttpResponse hr = hc.execute(get);
        logger.debug(hr.getStatusLine().toString());
        if (hr.getEntity() != null) {
            String responseString = EntityUtils.toString(hr.getEntity());
            logger.debug(responseString);
            ui.showMsg(responseString);
            IOUtils.write(responseString, new FileOutputStream(stashDir + "/projects.html"));
        }
    }

    private void getRemoteRepositories(String key) throws IOException {
        login();
        logger.info("Get Remote Repositories: {}", key);
        ui.showMsg("Get Remote Repositories: " + key);
        HttpGet get = new HttpGet("http://isstash.mandg.co.uk:8080/projects/" + key + "?start=0&limit=50&avatarSize=96");
        HttpResponse hr = hc.execute(get);
        logger.debug(hr.getStatusLine().toString());
        if (hr.getEntity() != null) {
            String responseString = EntityUtils.toString(hr.getEntity());
            logger.debug(responseString);
            ui.showMsg(responseString);
            IOUtils.write(responseString, new FileOutputStream(stashDir + "/" + key + ".html"));
        }
    }

    public String getProjectsXml() throws ParserConfigurationException, TransformerException, IOException {
        List<StashProject> projects = getProjects();
        org.w3c.dom.Document doc = XmlHelper.buildEmptyDoc();
        org.w3c.dom.Element root = doc.createElement("Projects");
        root.setAttribute("name", "MandG projects");
        doc.appendChild(root);
        for (StashProject project : projects) {
            org.w3c.dom.Element elProject = doc.createElement("Project");
            root.appendChild(elProject);

            elProject.setAttribute("name", project.getName());
            elProject.setAttribute("key", project.getKey());
            elProject.setAttribute("description", project.getDescription());

            if (project.getRepositories() != null) {
                for (String repository : project.getRepositories()) {
                    org.w3c.dom.Element elRepository = doc.createElement("Repository");
                    elProject.appendChild(elRepository);

                    elRepository.setAttribute("name", repository);
                }
            }
        }
        return new XmlHelper(doc).getContent();
    }

    public List<StashProject> getProjects() throws IOException {
        File file = new File(stashDir + "/projects.html");
        if (!file.exists()) {
            getRemoteProjects();
        }
        logger.info("Get Projects");
        ui.showMsg("Get Projects");
        String responseString = IOUtils.toString(new FileInputStream(file));
        Document doc = Jsoup.parse(responseString);
        Element ptable = doc.getElementById("projects-table");
        Elements ptrs = ptable.getElementsByTag("tbody").first().getElementsByTag("tr");

        List<StashProject> projects = new ArrayList<>();

        for (Element ptr : ptrs) {
            String name = ptr.getElementsByClass("project-name").first().getElementsByTag("a").first().text();
            String key = ptr.getElementsByClass("project-key").first().text();
            Elements pdescs = ptr.getElementsByClass("project-description");
            String desc = "";
            if (pdescs.first() != null) {
                desc = pdescs.first().getElementsByTag("span").first().text();
            }
            StashProject project = new StashProject(name, key, desc);
            project.setRepositories(getRepositories(key));
            projects.add(project);
        }

        return projects;
    }

    private List<String> getRepositories(String key) throws IOException {
        File file = new File(stashDir + "/" + key + ".html");
        if (!file.exists()) {
            getRemoteRepositories(key);
        }
        logger.info("Get Repositories: {}", key);
        ui.showMsg("Get Repositories: " + key);
        String responseString = IOUtils.toString(new FileInputStream(file));
        Document doc = Jsoup.parse(responseString);
        Elements rnames = doc.getElementsByClass("repository-name");

        List<String> names = new ArrayList<>();
        for (Element rname : rnames) {
            names.add(rname.getElementsByTag("a").first().text());
        }
        return names;
    }
}
