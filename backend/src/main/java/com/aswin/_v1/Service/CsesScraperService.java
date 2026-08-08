package com.aswin._v1.Service;

import com.aswin._v1.entity.Problem;
import com.aswin._v1.repository.ProbRepo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CsesScraperService {

    @Autowired
    private ProbRepo probRepo;

    // 1. Scrapes a single problem
    public String scrapeAndSave(String taskId) {
        String url = "https://cses.fi/problemset/task/" + taskId;

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)") 
                    .get();

            Element titleElement = doc.selectFirst("h1");
            String title = titleElement != null ? titleElement.text() : "Unknown Title";

            Element contentElement = doc.selectFirst(".content");
            String description = "No description found.";
            
            if (contentElement != null) {
                contentElement.select("script, style").remove();
                description = contentElement.html();
            }

            Problem newProblem = new Problem();
            newProblem.setTitle(title);
            newProblem.setDescription(description);
            newProblem.setDifficulty("Medium"); 
            newProblem.setTimeLimit(1.0);       
            newProblem.setMemoryLimit(512);     

            probRepo.save(newProblem);

            return "✅ Successfully scraped and saved: " + title;

        } catch (Exception e) {
            return "❌ Error scraping CSES: " + e.getMessage();
        }
    }

    // 2. FIX 2: The missing method that scrapes the entire platform
    public void scrapeEntirePlatform() {
        String indexUrl = "https://cses.fi/problemset/";
        try {
            System.out.println("🤖 Fetching CSES main index page to map out all problems...");
            Document indexDoc = Jsoup.connect(indexUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .get();

            org.jsoup.select.Elements links = indexDoc.select("a[href^=/problemset/task/]");
            java.util.List<String> taskIds = new java.util.ArrayList<>();

            for (Element link : links) {
                String href = link.attr("href"); 
                String[] parts = href.split("/");
                String taskId = parts[parts.length - 1]; 
                
                if (!taskIds.contains(taskId)) {
                    taskIds.add(taskId);
                }
            }

            System.out.println("🎯 Discovered " + taskIds.size() + " unique CSES problems to scrape!");

            int successCount = 0;
            for (int i = 0; i < taskIds.size(); i++) {
                String taskId = taskIds.get(i);
                System.out.println(String.format("Processing [%d/%d] - Task ID: %s", (i + 1), taskIds.size(), taskId));
                
                String result = scrapeAndSave(taskId);
                if (result.startsWith("✅")) {
                    successCount++;
                }

                // Pause for 1 second so CSES doesn't block us
                Thread.sleep(1000);
            }

            System.out.println("🎉 Bulk operation complete! Successfully imported " + successCount + " problems.");

        } catch (Exception e) {
            System.err.println("❌ Critical failure during bulk scrape operation: " + e.getMessage());
        }
    }
}