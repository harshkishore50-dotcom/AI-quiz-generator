package com.quizgen;

import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.io.*;

/**
 * AIQuizGenerator - A console-based Java app that uses the Claude API
 * to generate multiple-choice quizzes on any topic.
 *
 * Author: Kishore Harshvardhan
 * Tech: Java 11+, Claude API (claude-sonnet-4-20250514), HttpClient
 */
public class QuizGenerator {

    // ── Replace with your actual Anthropic API key ──────────────────────────
    private static final String API_KEY = "YOUR_ANTHROPIC_API_KEY";
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL   = "claude-sonnet-4-20250514";
    // ────────────────────────────────────────────────────────────────────────

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private int score = 0;
    private int totalQuestions = 0;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        QuizGenerator app = new QuizGenerator();

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║      🤖 AI Quiz Generator v1.0       ║");
        System.out.println("║    Powered by Claude (Anthropic)     ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();

        System.out.print("Enter a topic for your quiz (e.g. Java, World History, DSA): ");
        String topic = scanner.nextLine().trim();

        System.out.print("How many questions? (1-10): ");
        int numQuestions = 5;
        try {
            numQuestions = Math.min(10, Math.max(1, Integer.parseInt(scanner.nextLine().trim())));
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Defaulting to 5 questions.");
        }

        System.out.println("\n⏳ Generating your quiz on \"" + topic + "\"...\n");

        String rawQuiz = app.fetchQuizFromClaude(topic, numQuestions);
        List<Question> questions = app.parseQuestions(rawQuiz);

        if (questions.isEmpty()) {
            System.out.println("❌ Failed to parse quiz. Raw API response:\n" + rawQuiz);
            return;
        }

        app.runQuiz(questions, scanner);
        app.showFinalScore();
        scanner.close();
    }

    // ── API Call ─────────────────────────────────────────────────────────────

    private String fetchQuizFromClaude(String topic, int numQ) throws Exception {
        String prompt = buildPrompt(topic, numQ);

        String requestBody = "{"
            + "\"model\": \"" + MODEL + "\","
            + "\"max_tokens\": 1500,"
            + "\"messages\": [{\"role\": \"user\", \"content\": " + jsonString(prompt) + "}]"
            + "}";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Content-Type", "application/json")
            .header("x-api-key", API_KEY)
            .header("anthropic-version", "2023-06-01")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API Error " + response.statusCode() + ": " + response.body());
        }

        return extractTextFromResponse(response.body());
    }

    // ── Prompt Engineering ───────────────────────────────────────────────────

    private String buildPrompt(String topic, int numQ) {
        return "Generate exactly " + numQ + " multiple-choice questions about \"" + topic + "\".\n\n"
             + "Use this EXACT format for each question — no deviation:\n\n"
             + "Q1. <question text>\n"
             + "A) <option>\n"
             + "B) <option>\n"
             + "C) <option>\n"
             + "D) <option>\n"
             + "ANSWER: <A/B/C/D>\n\n"
             + "Repeat for Q2, Q3, etc. Only output the questions, nothing else.";
    }

    // ── JSON Helpers ─────────────────────────────────────────────────────────

    private String jsonString(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                       .replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    private String extractTextFromResponse(String json) {
        // Simple extraction: find the "text" field in the content array
        int idx = json.indexOf("\"text\":");
        if (idx == -1) return "";
        int start = json.indexOf("\"", idx + 7) + 1;
        int end = findClosingQuote(json, start);
        String raw = json.substring(start, end);
        // Unescape JSON string
        return raw.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private int findClosingQuote(String s, int start) {
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '"' && s.charAt(i - 1) != '\\') return i;
        }
        return s.length();
    }

    // ── Parser ───────────────────────────────────────────────────────────────

    private List<Question> parseQuestions(String raw) {
        List<Question> questions = new ArrayList<>();
        String[] lines = raw.split("\n");
        
        Question current = null;
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.matches("Q\\d+\\..*")) {
                if (current != null) questions.add(current);
                current = new Question(line.replaceFirst("Q\\d+\\.", "").trim());
            } else if (current != null) {
                if (line.startsWith("A)")) current.options[0] = line.substring(2).trim();
                else if (line.startsWith("B)")) current.options[1] = line.substring(2).trim();
                else if (line.startsWith("C)")) current.options[2] = line.substring(2).trim();
                else if (line.startsWith("D)")) current.options[3] = line.substring(2).trim();
                else if (line.startsWith("ANSWER:")) {
                    String ans = line.replace("ANSWER:", "").trim();
                    current.correctIndex = "ABCD".indexOf(ans.charAt(0));
                }
            }
        }
        if (current != null) questions.add(current);
        return questions;
    }

    // ── Quiz Runner ───────────────────────────────────────────────────────────

    private void runQuiz(List<Question> questions, Scanner scanner) {
        totalQuestions = questions.size();
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.println("Question " + (i + 1) + " of " + totalQuestions);
            System.out.println("❓ " + q.text);
            System.out.println("   A) " + q.options[0]);
            System.out.println("   B) " + q.options[1]);
            System.out.println("   C) " + q.options[2]);
            System.out.println("   D) " + q.options[3]);
            System.out.print("\nYour answer (A/B/C/D): ");

            String userInput = scanner.nextLine().trim().toUpperCase();
            int userIndex = "ABCD".indexOf(userInput.isEmpty() ? "X" : userInput.charAt(0) + "");

            if (userIndex == q.correctIndex) {
                score++;
                System.out.println("✅ Correct!\n");
            } else {
                String correctLetter = "ABCD".charAt(q.correctIndex) + "";
                System.out.println("❌ Wrong! Correct answer: " + correctLetter + ") " + q.options[q.correctIndex] + "\n");
            }
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        }
    }

    private void showFinalScore() {
        System.out.println("🏁 QUIZ COMPLETE!");
        System.out.println("Your Score: " + score + " / " + totalQuestions);
        double pct = (double) score / totalQuestions * 100;
        System.out.printf("Percentage: %.1f%%%n", pct);
        if (pct == 100) System.out.println("🏆 Perfect score! Outstanding!");
        else if (pct >= 80) System.out.println("🎉 Excellent work!");
        else if (pct >= 60) System.out.println("👍 Good job! Keep learning.");
        else System.out.println("📚 Keep studying — you'll get there!");
    }

    // ── Inner Model Class ────────────────────────────────────────────────────

    static class Question {
        String text;
        String[] options = new String[4];
        int correctIndex = 0;

        Question(String text) {
            this.text = text;
            Arrays.fill(options, "");
        }
    }
}
