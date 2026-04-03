# 🤖 AI Quiz Generator

A **Java console application** that uses the **Claude API (Anthropic)** to generate multiple-choice quizzes on any topic — instantly.

---

## 🚀 Features

- Enter **any topic** (e.g., Java OOP, Indian History, DSA, Physics)
- Choose **1–10 questions** per session
- Claude generates fresh, relevant MCQs every time
- Instant feedback after each answer
- Final score & performance summary

---

## 🛠 Tech Stack

| Layer        | Technology                    |
|--------------|-------------------------------|
| Language     | Java 11+                      |
| AI Model     | Claude (claude-sonnet-4-20250514) |
| HTTP Client  | java.net.http.HttpClient      |
| Build Tool   | javac (no external dependencies) |

---

## ⚙️ Setup & Run

### 1. Prerequisites
- Java 11 or higher installed
- An [Anthropic API key](https://console.anthropic.com/)

### 2. Add your API Key
Open `QuizGenerator.java` and replace:
```java
private static final String API_KEY = "YOUR_ANTHROPIC_API_KEY";
```

### 3. Compile
```bash
javac -d out src/main/java/com/quizgen/QuizGenerator.java
```

### 4. Run
```bash
java -cp out com.quizgen.QuizGenerator
```

---

## 📸 Sample Output

```
╔══════════════════════════════════════╗
║      🤖 AI Quiz Generator v1.0       ║
║    Powered by Claude (Anthropic)     ║
╚══════════════════════════════════════╝

Enter a topic: Java OOP
How many questions? (1-10): 3

⏳ Generating your quiz on "Java OOP"...

Question 1 of 3
❓ Which keyword is used to inherit a class in Java?
   A) implements
   B) extends
   C) inherits
   D) super

Your answer (A/B/C/D): B
✅ Correct!
...
🏁 QUIZ COMPLETE!
Your Score: 3 / 3
Percentage: 100.0%
🏆 Perfect score! Outstanding!
```

---

## 📂 Project Structure

```
AIQuizGenerator/
├── src/
│   └── main/java/com/quizgen/
│       └── QuizGenerator.java     # Main application
├── README.md
└── docs/
    └── showcase.html              # CV showcase page
```

---

## 💡 How It Works

1. User inputs a **topic** and **question count**
2. A structured prompt is sent to **Claude API** via HTTP POST
3. Claude returns MCQs in a strict format
4. The app **parses** each question, options, and answer
5. User plays the quiz interactively in the terminal

---

## 👨‍💻 Author

**Kishore Harshvardhan**  
B.Tech CSE — ITER, SOA University  
GitHub: [your-github-handle]
