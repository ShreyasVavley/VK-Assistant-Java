import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Random;

/**
 * --- 1. The Core Bot Logic (Command Pattern) ---
 */

interface Command {
    String execute(String argument);
}

class TimeCommand implements Command {
    @Override
    public String execute(String argument) {
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        return "The current time is " + now.format(formatter) + ".";
    }
}

class DateCommand implements Command {
    @Override
    public String execute(String argument) {
        return "Today's date is " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")) + ".";
    }
}

class WeatherCommand implements Command {
    private static final String API_KEY = ""; // Note: Secure this key

    @Override
    public String execute(String argument) {
        if (API_KEY.equals("YOUR_API_KEY_HERE")) {
            return "Weather API key not set. Please get a free key from WeatherAPI.com and add it to the WeatherCommand class.";
        }
        if (argument == null || argument.trim().isEmpty()) {
            return "Please provide a city. Example: 'weather London' or 'weather 90210'";
        }
        try {
            String city = java.net.URLEncoder.encode(argument.trim(), "UTF-8");
            URL url = new URL("https://api.weatherapi.com/v1/current.json?key=" + API_KEY + "&q=" + city);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return parseWeatherJson(response.toString(), argument);
            } else if (responseCode == 400) {
                return "Sorry, I couldn't find weather for '" + argument + "'.";
            } else {
                return "Sorry, there was an error connecting to the weather service (Code: " + responseCode + ").";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, an error occurred while fetching the weather.";
        }
    }

    private String parseWeatherJson(String jsonResponse, String city) {
        try {
            String tempKey = "\"temp_c\":";
            int tempIndex = jsonResponse.indexOf(tempKey) + tempKey.length();
            int tempEndIndex = jsonResponse.indexOf(',', tempIndex);
            String temp = jsonResponse.substring(tempIndex, tempEndIndex).trim();

            String feelsLikeKey = "\"feelslike_c\":";
            int feelsLikeIndex = jsonResponse.indexOf(feelsLikeKey) + feelsLikeKey.length();
            int feelsLikeEndIndex = jsonResponse.indexOf(',', feelsLikeIndex);
            String feelsLike = jsonResponse.substring(feelsLikeIndex, feelsLikeEndIndex).trim();

            String conditionKey = "\"text\":\"";
            int conditionIndex = jsonResponse.indexOf(conditionKey) + conditionKey.length();
            int conditionEndIndex = jsonResponse.indexOf('\"', conditionIndex);
            String condition = jsonResponse.substring(conditionIndex, conditionEndIndex);

            String humidityKey = "\"humidity\":";
            int humidityIndex = jsonResponse.indexOf(humidityKey) + humidityKey.length();
            int humidityEndIndex = jsonResponse.indexOf(',', humidityIndex);
            String humidity = jsonResponse.substring(humidityIndex, humidityEndIndex).trim();

            return "🌤️ Weather in " + city + ":\n" +
                    "• Condition: " + condition + "\n" +
                    "• Temperature: " + temp + "°C (Feels like " + feelsLike + "°C)\n" +
                    "• Humidity: " + humidity + "%";
        } catch (Exception e) {
            return "Sorry, I received weather data but couldn't parse it.";
        }
    }
}

class GreetCommand implements Command {
    @Override
    public String execute(String argument) {
        String[] greetings = {
                "Hello there! How can I help you today? 👋",
                "Hi! Ready to assist you! 😊",
                "Hey! What can I do for you? 🌟",
                "Greetings! How may I be of service? 🤖"
        };
        return greetings[new Random().nextInt(greetings.length)];
    }
}

class JokeCommand implements Command {
    private String[] jokes = {
            "Why don't scientists trust atoms? Because they make up everything! 🤓",
            "Why did the scarecrow win an award? He was outstanding in his field! 🌾",
            "Why don't eggs tell jokes? They'd crack each other up! 🥚",
            "I'm reading a book on anti-gravity. It's impossible to put down! 📚",
            "Why did the math book look so sad? Because it had too many problems! 📖"
    };

    @Override
    public String execute(String argument) {
        return jokes[new Random().nextInt(jokes.length)];
    }
}

class QuoteCommand implements Command {
    private String[] quotes = {
            "The only way to do great work is to love what you do. - Steve Jobs 💡",
            "Innovation distinguishes between a leader and a follower. - Steve Jobs 🚀",
            "The future belongs to those who believe in the beauty of their dreams. - Eleanor Roosevelt ✨",
            "Success is not final, failure is not fatal: it is the courage to continue that counts. - Winston Churchill 🏆",
            "The only impossible journey is the one you never begin. - Tony Robbins 🌟"
    };

    @Override
    public String execute(String argument) {
        return "💫 Inspirational Quote:\n" + quotes[new Random().nextInt(quotes.length)];
    }
}

class ClearCommand implements Command {
    @Override
    public String execute(String argument) {
        return "CLEAR_CHAT"; // Special signal to clear the chat
    }
}

class AskCommand implements Command {
    private static final String API_KEY = ""; // Note: Secure this key
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-09-2025:generateContent?key=" + API_KEY;

    @Override
    public String execute(String argument) {
        if (API_KEY.equals("YOUR_GEMINI_API_KEY")) {
            return "Gemini API key not set. Please get a free key from Google AI Studio and add it to the AskCommand class.";
        }

        if (argument == null || argument.trim().isEmpty()) {
            return "Please ask a question. Example: 'ask What is the capital of France?'";
        }

        try {
            String escapedArgument = argument.replace("\"", "\\\"").replace("\n", "\\n");
            String jsonPayload = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapedArgument + "\"}]}]}";

            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            StringBuilder response = new StringBuilder();
            BufferedReader in;

            if (responseCode == 200) {
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (responseCode == 200) {
                return parseGeminiJson(response.toString());
            } else {
                return "Error from Gemini API (Code: " + responseCode + "): " + response.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "An internal error occurred while contacting the Gemini API.";
        }
    }

    private String parseGeminiJson(String jsonResponse) {
        try {
            String textKey = "\"text\": \"";
            int startIndex = jsonResponse.indexOf(textKey) + textKey.length();
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            String rawText = jsonResponse.substring(startIndex, endIndex);

            return "🤖 Gemini AI:\n" + rawText.replace("\\n", "\n").replace("\\\"", "\"");
        } catch (Exception e) {
            if (jsonResponse.contains("promptFeedback")) {
                return "My apologies, I can't answer that. The query was blocked for safety reasons.";
            }
            return "Sorry, I received a response from Gemini but couldn't parse it.";
        }
    }
}

class HelpCommand implements Command {
    private HashMap<String, Command> commands;

    public HelpCommand(HashMap<String, Command> commands) {
        this.commands = commands;
    }

    @Override
    public String execute(String argument) {
        StringBuilder sb = new StringBuilder("🎯 I can understand the following commands:\n\n");
        sb.append("🤖 Basic Commands:\n");
        sb.append("- hello/hi (Simple greeting)\n");
        sb.append("- time (Current time)\n");
        sb.append("- date (Current date)\n");
        sb.append("- help (Show this list)\n");
        sb.append("- clear (Clear chat history)\n\n");

        sb.append("🌤️ Weather:\n");
        sb.append("- weather [city] (Get current weather)\n\n");

        sb.append("🎉 Fun Commands:\n");
        sb.append("- joke (Tell a random joke)\n");
        sb.append("- quote (Get inspirational quote)\n\n");

        sb.append("🤖 AI Commands:\n");
        sb.append("- ask [question] (Ask Gemini AI a question)\n\n");

        sb.append("⚡ System:\n");
        sb.append("- exit/quit (Close the application)\n");

        return sb.toString();
    }
}

/**
 * --- 2. The Bot's "Brain" ---
 */
class CommandProcessor {
    private HashMap<String, Command> commands;

    public CommandProcessor() {
        commands = new HashMap<>();
        commands.put("time", new TimeCommand());
        commands.put("date", new DateCommand());
        commands.put("weather", new WeatherCommand());
        commands.put("hello", new GreetCommand());
        commands.put("hi", new GreetCommand());
        commands.put("joke", new JokeCommand());
        commands.put("quote", new QuoteCommand());
        commands.put("clear", new ClearCommand());
        commands.put("ask", new AskCommand());
        commands.put("help", new HelpCommand(commands));
    }

    public String processInput(String userInput) {
        String input = userInput.toLowerCase().trim();

        if (input.equals("exit") || input.equals("quit")) {
            return "EXIT_APP"; // Special signal to exit the application
        }

        String[] parts = input.split(" ", 2);
        String commandName = parts[0];
        String argument = (parts.length > 1) ? parts[1] : null;
        Command command = commands.get(commandName);

        if (command != null) {
            return command.execute(argument);
        } else {
            return "Sorry, I don't understand that. Type 'help' to see what I can do. 🤔";
        }
    }
}

/**
 * --- 3. The Enhanced GUI "Face" ---
 */
public class GuiAssistant1 extends JFrame {

    // --- Modern Color Palette ---
    private static final Color COLOR_BG_DARK = new Color(0x1A1A2E);
    private static final Color COLOR_BG_CARD = new Color(0x16213E);
    private static final Color COLOR_ACCENT = new Color(0x0F3460);
    private static final Color COLOR_PRIMARY = new Color(0xE94560);
    private static final Color COLOR_TEXT = new Color(0xF0F0F0);
    private static final Color COLOR_TEXT_SECONDARY = new Color(0xB0B0B0);
    private static final Color COLOR_BOT = new Color(0x4A90E2);
    private static final Color COLOR_USER = new Color(0x7ED321);
    private static final Color COLOR_LOADING = new Color(0xFFC107);
    private static final Color COLOR_SYSTEM = new Color(0x9B59B6);

    // --- Glass Morphism Effect ---
    private static final Color GLASS_BG = new Color(255, 255, 255, 25);
    private static final Color GLASS_BORDER = new Color(255, 255, 255, 50);

    // --- Constants ---
    private final String PLACEHOLDER = "Type a command... (try 'help' for options)";
    
    // --- Define CSS as a constant (for DRY principle) ---
    private static final String CHAT_STYLES = "<html><head><style>"
            + "@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');"
            + "body { font-family: 'Inter', sans-serif; background: transparent; color: #F0F0F0; padding: 20px; margin: 0; }"
            + ".message { margin-bottom: 16px; padding: 16px 20px; border-radius: 20px; max-width: 80%; word-wrap: break-word; line-height: 1.5; backdrop-filter: blur(10px); border: 1px solid rgba(255,255,255,0.1); }"
            + ".bot { background: linear-gradient(135deg, rgba(74, 144, 226, 0.9), rgba(53, 122, 189, 0.9)); color: white; margin-right: auto; border-bottom-left-radius: 5px; box-shadow: 0 8px 32px rgba(74, 144, 226, 0.3); }"
            + ".user { background: linear-gradient(135deg, rgba(126, 211, 33, 0.9), rgba(107, 191, 26, 0.9)); color: black; margin-left: auto; border-bottom-right-radius: 5px; box-shadow: 0 8px 32px rgba(126, 211, 33, 0.3); }"
            + ".system { background: linear-gradient(135deg, rgba(155, 89, 182, 0.9), rgba(142, 68, 173, 0.9)); color: white; margin: 15px auto; max-width: 90%; text-align: center; box-shadow: 0 8px 32px rgba(155, 89, 182, 0.3); }"
            + ".speaker { font-weight: 600; margin-bottom: 6px; font-size: 0.85em; opacity: 0.9; letter-spacing: 0.5px; }"
            + ".timestamp { font-size: 0.75em; opacity: 0.6; margin-top: 8px; text-align: right; }"
            + "</style></head><body>";


    private CommandProcessor botLogic;
    private JEditorPane chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton clearButton;
    private JButton themeButton;
    private StringBuilder htmlContent;
    private boolean darkMode = true;

    private java.awt.event.MouseAdapter sendButtonHoverListener;

    public GuiAssistant1() {
        botLogic = new CommandProcessor();

        // 1. Modern Window Setup
        setTitle("🌟 VK Assistant");
        setSize(550, 750);
        setMinimumSize(new Dimension(450, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG_DARK);
        setLocationRelativeTo(null);

        // 2. Create Header with Gradient
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // 3. Enhanced HTML/CSS Styling
        htmlContent = new StringBuilder(CHAT_STYLES);

        // 4. Chat Area with Modern Scrollbar
        chatArea = new JEditorPane();
        chatArea.setEditable(false);
        chatArea.setContentType("text/html");
        chatArea.setBackground(COLOR_BG_DARK);
        chatArea.setText(htmlContent.toString() + "</body></html>");

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(COLOR_BG_DARK);

        // Custom scrollbar
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setBackground(COLOR_BG_CARD);
        verticalScrollBar.setForeground(COLOR_PRIMARY);

        add(scrollPane, BorderLayout.CENTER);

        // 5. Enhanced Input Panel with Glass Effect
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.SOUTH);

        // 6. Welcome message with features
        appendSystemMessage("🚀 Welcome to VK Assistant!");
        appendMessage("Bot", "Hello! I'm VK Assistant powered by Gemini. Type 'help' to discover all my features! ✨", false);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0x0F3460));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Gradient background effect
        headerPanel.setLayout(new BorderLayout());

        // Title with icon
        JLabel titleLabel = new JLabel("🌟 VK Assistant", JLabel.LEFT);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // Status indicator
        JLabel statusLabel = new JLabel("🟢 Online", JLabel.RIGHT);
        statusLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(0x7ED321));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(12, 12));
        inputPanel.setBackground(COLOR_BG_CARD);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x2A2A4E)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Input Field with Modern Styling
        inputField = new JTextField();
        inputField.setFont(new Font("Inter", Font.PLAIN, 14));
        inputField.setBackground(GLASS_BG);
        inputField.setForeground(COLOR_TEXT);
        inputField.setCaretColor(COLOR_PRIMARY);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GLASS_BORDER, 2, true),
                new EmptyBorder(12, 15, 12, 15)
        ));
        inputField.setToolTipText("Enter commands here. Type 'help' for all available commands.");

        // Rounded corners
        inputField.setOpaque(false);

        // Enhanced placeholder logic
        inputField.setText(PLACEHOLDER);
        inputField.setForeground(COLOR_TEXT_SECONDARY);

        inputField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (inputField.getText().equals(PLACEHOLDER)) {
                    inputField.setText("");
                    inputField.setForeground(COLOR_TEXT);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (inputField.getText().isEmpty()) {
                    inputField.setText(PLACEHOLDER);
                    inputField.setForeground(COLOR_TEXT_SECONDARY);
                }
            }
        });
        
        // --- FIX: Add listener for "Enter" key ---
        inputField.addActionListener(e -> handleUserInput());


        // Button Panel with modern buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(COLOR_BG_CARD);
        buttonPanel.setOpaque(false);

        // Theme Toggle Button
        themeButton = new JButton("🌙");
        styleModernButton(themeButton, COLOR_ACCENT);
        themeButton.setToolTipText("Toggle theme");
        themeButton.addActionListener(e -> toggleTheme());

        // Clear Button
        clearButton = new JButton("🗑️");
        styleModernButton(clearButton, new Color(0xE74C3C));
        clearButton.setToolTipText("Clear chat history");
        clearButton.addActionListener(e -> clearChat());

        // Send Button
        sendButton = new JButton("🚀");
        styleModernButton(sendButton, COLOR_PRIMARY);
        sendButton.setFont(new Font("Inter", Font.BOLD, 16));
        sendButton.setMargin(new Insets(8, 16, 8, 16));
        sendButton.setMnemonic(java.awt.event.KeyEvent.VK_ENTER);
        sendButton.setToolTipText("Send message (Enter)");

        sendButtonHoverListener = new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (sendButton.isEnabled()) {
                    sendButton.setBackground(COLOR_PRIMARY.brighter());
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (sendButton.isEnabled()) {
                    sendButton.setBackground(COLOR_PRIMARY);
                }
            }
        };
        sendButton.addMouseListener(sendButtonHoverListener);
        
        // --- FIX: Add listener for button click ---
        sendButton.addActionListener(e -> handleUserInput());


        buttonPanel.add(themeButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(sendButton);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        return inputPanel;
    }

    private void styleModernButton(JButton button, Color bgColor) {
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Rounded corners
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        if (darkMode) {
            themeButton.setText("🌙");
            applyDarkTheme();
        } else {
            themeButton.setText("☀️");
            applyLightTheme();
        }
    }

    private void applyDarkTheme() {
        getContentPane().setBackground(COLOR_BG_DARK);
        chatArea.setBackground(COLOR_BG_DARK);
        // Update other components for dark theme
    }

    private void applyLightTheme() {
        getContentPane().setBackground(Color.WHITE);
        chatArea.setBackground(Color.WHITE);
        // Update other components for light theme
    }

    private void showCreditsPopup() {
        // Create a custom dialog for credits
        JDialog creditsDialog = new JDialog(this, "Credits", true);
        creditsDialog.setLayout(new BorderLayout());
        creditsDialog.setSize(400, 400);
        creditsDialog.setLocationRelativeTo(this);
        creditsDialog.getContentPane().setBackground(COLOR_BG_DARK);

        // Header
        JLabel headerLabel = new JLabel("🌟 VK Assistant - Credits", JLabel.CENTER);
        headerLabel.setFont(new Font("Inter", Font.BOLD, 18));
        headerLabel.setForeground(COLOR_PRIMARY);
        headerLabel.setBorder(new EmptyBorder(20, 20, 10, 20));

        // Credits content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COLOR_BG_CARD);
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel createdBy = new JLabel("Created by:", JLabel.CENTER);
        createdBy.setFont(new Font("Inter", Font.BOLD, 14));
        createdBy.setForeground(COLOR_TEXT);
        createdBy.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name1 = new JLabel("Shreyas S V", JLabel.CENTER);
        name1.setFont(new Font("Inter", Font.PLAIN, 16));
        name1.setForeground(COLOR_USER);
        name1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name2 = new JLabel("Shreelatha Kulkarni", JLabel.CENTER);
        name2.setFont(new Font("Inter", Font.PLAIN, 16));
        name2.setForeground(COLOR_USER);
        name2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel thankYou = new JLabel("Thank you for using VK Assistant! 💫", JLabel.CENTER);
        thankYou.setFont(new Font("Inter", Font.ITALIC, 12));
        thankYou.setForeground(COLOR_TEXT_SECONDARY);
        thankYou.setAlignmentX(Component.CENTER_ALIGNMENT);
        thankYou.setBorder(new EmptyBorder(15, 0, 0, 0));

        contentPanel.add(createdBy);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(name1);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        contentPanel.add(name2);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(thankYou);

        // Close button
        JButton closeButton = new JButton("Close");
        styleModernButton(closeButton, COLOR_PRIMARY);
        closeButton.addActionListener(e -> {
            creditsDialog.dispose();
            System.exit(0);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(COLOR_BG_DARK);
        buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        buttonPanel.add(closeButton);

        creditsDialog.add(headerLabel, BorderLayout.NORTH);
        creditsDialog.add(contentPanel, BorderLayout.CENTER);
        creditsDialog.add(buttonPanel, BorderLayout.SOUTH);

        creditsDialog.setVisible(true);
    }

    private class CommandWorker extends SwingWorker<String, Void> {
        private final String userInput;

        public CommandWorker(String userInput) {
            this.userInput = userInput;
        }

        @Override
        protected String doInBackground() throws Exception {
            return botLogic.processInput(userInput);
        }

        @Override
        protected void done() {
            inputField.setEnabled(true);
            sendButton.setEnabled(true);
            sendButton.setText("🚀");
            sendButton.setBackground(COLOR_PRIMARY);
            sendButton.addMouseListener(sendButtonHoverListener);

            try {
                String botResponse = get();

                if (botResponse.equals("CLEAR_CHAT")) {
                    clearChat();
                } else if (botResponse.equals("EXIT_APP")) {
                    // Show goodbye message in chat
                    appendMessage("Bot", "Goodbye! Thanks for using VK Assistant! 👋", false);

                    // Show credits popup after a delay
                    Timer timer = new Timer(1500, e -> showCreditsPopup());
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    appendMessage("Bot", botResponse, false);
                }

                inputField.requestFocusInWindow();

            } catch (Exception e) {
                appendMessage("Bot", "An internal error occurred while processing the command.", false);
            }
        }
    }

    private void handleUserInput() {
        String userInput = inputField.getText();

        if (userInput.isEmpty() || userInput.equals(PLACEHOLDER)) {
            return;
        }

        appendMessage("You", userInput, false);
        inputField.setText("");

        // Loading state with animation
        inputField.setEnabled(false);
        sendButton.setEnabled(false);
        sendButton.setText("⏳");
        sendButton.setBackground(COLOR_LOADING);
        sendButton.removeMouseListener(sendButtonHoverListener);

        new CommandWorker(userInput).execute();
    }

    private void appendMessage(String speaker, String message, boolean isTransient) {
        String cssClass = speaker.equals("Bot") ? "bot" : "user";
        String speakerTag = speaker.equals("Bot") ? "🤖 VK" : "👤 You";
        String timestamp = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        String formattedMessage = message.replace("\n", "<br>");

        htmlContent.append("<div class='message " + cssClass + "'>");
        htmlContent.append("<div class='speaker'>" + speakerTag + "</div>");
        htmlContent.append("<div>" + formattedMessage + "</div>");
        htmlContent.append("<div class='timestamp'>" + timestamp + "</div>");
        htmlContent.append("</div>");

        updateChatArea();
    }

    private void appendSystemMessage(String message) {
        String timestamp = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        String formattedMessage = message.replace("\n", "<br>");

        htmlContent.append("<div class'message system'>");
        htmlContent.append("<div class='speaker'>⚡ System</div>");
        htmlContent.append("<div>" + formattedMessage + "</div>");
        htmlContent.append("<div class='timestamp'>" + timestamp + "</div>");
        htmlContent.append("</div>");

        updateChatArea();
    }

    private void clearChat() {
        // Use the constant to reset the HTML content
        htmlContent = new StringBuilder(CHAT_STYLES);
        
        updateChatArea();
        appendSystemMessage("✨ Chat history cleared! Ready for a fresh start!");
    }

    private void updateChatArea() {
        chatArea.setText(htmlContent.toString() + "</body></html>");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Set modern UI improvements
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 999);
            UIManager.put("ProgressBar.arc", 999);
            UIManager.put("TextComponent.arc", 999);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GuiAssistant1().setVisible(true);
            }
        });
    }
}