package com.aswin._v1.Service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class ScraperService {

    public String scrapeProblemDescription(String contestId, String problemIndex) {
        // Example URL: https://codeforces.com/problemset/problem/71/A
        String url = "https://codeforces.com/problemset/problem/" + contestId + "/" + problemIndex;

        try {
            System.out.println("Connecting to Codeforces: " + url);
            
            // 1. Fetch the whole webpage
            Document doc = Jsoup.connect(url)
    // Pretend to be Google Chrome on Windows 11
    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    .header("Accept-Language", "en-US,en;q=0.9")
    .timeout(10000) // Give it 10 seconds to load just in case
    .get();
            // 2. Find the exact HTML div that holds the problem statement
            Element statementDiv = doc.selectFirst(".problem-statement > div:nth-child(2)");

            if (statementDiv != null) {
                // Return the text cleanly!
                return statementDiv.text();
            } else {
                return "Could not find the problem description on the page.";
            }

        } catch (Exception e) {
            return "Error scraping Codeforces: " + e.getMessage();
        }
    }
}