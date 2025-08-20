import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;

public class BhagavadGitaApp {
    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}

// --- DatabaseManager Class ---
class DatabaseManager {
   
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Bhagavadgitha?useUnicode=true&characterEncoding=UTF-8";
    private static final String DB_USER = "root"; 
    private static final String DB_PASSWORD = "root"; 

    private DatabaseManager() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static boolean registerUser(String username, String password) {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { 
                JOptionPane.showMessageDialog(null, "Username '" + username + "' already exists. Please choose a different one.", "Registration Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Database error during registration: " + e.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
    }

    public static boolean authenticateUser(String username, String password) {
        String query = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword.equals(password);
            }
            return false;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error during login: " + e.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static HashMap<String, String> fetchVerseAndStory(String emotionType) {
        HashMap<String, String> data = new HashMap<>();
        String query = "SELECT v.chapter_no, v.verse_no, v.verse_hindi, v.verse_english, v.verse_kannada, v.meaning_english, s.story " +
                       "FROM verses v " +
                       "JOIN emotions e ON v.emotion_id = e.emotion_id " +
                       "LEFT JOIN moralstory s ON v.verse_id = s.verse_id " +
                       "WHERE e.emotion_type = ? " +
                       "ORDER BY RAND() LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, emotionType);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                data.put("chapter_verse", rs.getInt("chapter_no") + ":" + rs.getInt("verse_no"));
                data.put("verse_hindi", rs.getString("verse_hindi"));
                data.put("verse_english", rs.getString("verse_english"));
                data.put("verse_kannada", rs.getString("verse_kannada"));
                data.put("meaning_english", rs.getString("meaning_english"));
                data.put("moral_story", rs.getString("story"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error fetching verses/stories: " + e.getMessage(), "Data Fetch Error", JOptionPane.ERROR_MESSAGE);
        }
        return data;
    }
}

// --- LOGIN UI ---
class LoginUI extends JFrame {
    private final JTextField userField;
    private final JPasswordField passField;

    public LoginUI() {
        setTitle("Login - Bhagavad Gita Insights");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(255, 250, 240));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeLabel = new JLabel("Welcome Back!");
        welcomeLabel.setFont(new Font("Mangal", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(139, 69, 19));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Please login to continue");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subLabel.setForeground(new Color(105, 105, 105));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(welcomeLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        headerPanel.add(subLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(userLabel, gbc);

        userField = new JTextField(20);
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        userField.setPreferredSize(new Dimension(200, 30));
        userField.setBorder(BorderFactory.createLineBorder(new Color(170, 170, 170), 1));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        formPanel.add(userField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(passLabel, gbc);

        passField = new JPasswordField(20);
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passField.setPreferredSize(new Dimension(200, 30));
        passField.setBorder(BorderFactory.createLineBorder(new Color(170, 170, 170), 1));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        formPanel.add(passField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonsAndLinksPanel = new JPanel();
        buttonsAndLinksPanel.setLayout(new BoxLayout(buttonsAndLinksPanel, BoxLayout.Y_AXIS));
        buttonsAndLinksPanel.setOpaque(false);
        buttonsAndLinksPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setBackground(new Color(34, 139, 34));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(140, 35));
        loginBtn.setPreferredSize(new Dimension(140, 35));

        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        linksPanel.setOpaque(false);

        JButton registerLink = new JButton("<html>Don't have an account? <u style='color:blue;'>Register here</u></html>");
        registerLink.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.setForeground(new Color(0, 0, 128));

        JButton forgotPasswordLink = new JButton("<html><u style='color:blue;'>Forgot Password?</u></html>");
        forgotPasswordLink.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        forgotPasswordLink.setBorderPainted(false);
        forgotPasswordLink.setContentAreaFilled(false);
        forgotPasswordLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordLink.setForeground(new Color(0, 0, 128));
        forgotPasswordLink.addActionListener(e -> JOptionPane.showMessageDialog(this, "This feature is not implemented yet.", "Coming Soon", JOptionPane.INFORMATION_MESSAGE));

        linksPanel.add(registerLink);
        linksPanel.add(forgotPasswordLink);

        buttonsAndLinksPanel.add(loginBtn);
        buttonsAndLinksPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonsAndLinksPanel.add(linksPanel);

        mainPanel.add(buttonsAndLinksPanel, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (DatabaseManager.authenticateUser(username, password)) {
                dispose();
                new EmotionQuestionnaireUI(username).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerLink.addActionListener(e -> {
            dispose();
            new RegistrationUI().setVisible(true);
        });

        add(mainPanel);
    }
}

// --- REGISTRATION UI ---
class RegistrationUI extends JFrame {
    private final JTextField userField;
    private final JPasswordField passField;
    private final JPasswordField confirmPassField;

    public RegistrationUI() {
        setTitle("Register - Bhagavad Gita Insights");
        setSize(450, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(255, 250, 240));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeLabel = new JLabel("Create Your Account");
        welcomeLabel.setFont(new Font("Mangal", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(70, 130, 180));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Join us to explore Bhagavad Gita insights");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subLabel.setForeground(new Color(105, 105, 105));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(welcomeLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        headerPanel.add(subLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(userLabel, gbc);

        userField = new JTextField(20);
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        userField.setPreferredSize(new Dimension(200, 30));
        userField.setBorder(BorderFactory.createLineBorder(new Color(170, 170, 170), 1));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        formPanel.add(userField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(passLabel, gbc);

        passField = new JPasswordField(20);
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passField.setPreferredSize(new Dimension(200, 30));
        passField.setBorder(BorderFactory.createLineBorder(new Color(170, 170, 170), 1));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        formPanel.add(passField, gbc);

        JLabel confirmPassLabel = new JLabel("Confirm Password:");
        confirmPassLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        formPanel.add(confirmPassLabel, gbc);

        confirmPassField = new JPasswordField(20);
        confirmPassField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        confirmPassField.setPreferredSize(new Dimension(200, 30));
        confirmPassField.setBorder(BorderFactory.createLineBorder(new Color(170, 170, 170), 1));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        formPanel.add(confirmPassField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonsAndLinksPanel = new JPanel();
        buttonsAndLinksPanel.setLayout(new BoxLayout(buttonsAndLinksPanel, BoxLayout.Y_AXIS));
        buttonsAndLinksPanel.setOpaque(false);
        buttonsAndLinksPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        registerBtn.setBackground(new Color(70, 130, 180));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.setMaximumSize(new Dimension(140, 35));
        registerBtn.setPreferredSize(new Dimension(140, 35));

        JButton loginLink = new JButton("<html>Already have an account? <u style='color:blue;'>Login here</u></html>");
        loginLink.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginLink.setBorderPainted(false);
        loginLink.setContentAreaFilled(false);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.setForeground(new Color(0, 0, 128));
        loginLink.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonsAndLinksPanel.add(registerBtn);
        buttonsAndLinksPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonsAndLinksPanel.add(loginLink);

        mainPanel.add(buttonsAndLinksPanel, BorderLayout.SOUTH);

        registerBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String confirmPassword = new String(confirmPassField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match. Please re-enter.", "Input Error", JOptionPane.ERROR_MESSAGE);
                passField.setText("");
                confirmPassField.setText("");
                return;
            }

            if (DatabaseManager.registerUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new LoginUI().setVisible(true);
            }
        });

        loginLink.addActionListener(e -> {
            dispose();
            new LoginUI().setVisible(true);
        });

        add(mainPanel);
    }
}

// --- EMOTION QUESTIONNAIRE UI (Improved) ---
class EmotionQuestionnaireUI extends JFrame {
    private final String userName;
    private final String[][] questions = {
        {"How do you react when someone criticizes you?",
            "I feel very angry and defensive", "ANGER",
            "I try to understand their point", "CONFUSION",
            "I feel sad and withdrawn", "SAD",
            "I laugh it off happily", "HAPPY",
            "I get scared about what they might do next", "FEAR"},
        {"How do you feel when you are alone?",
            "Content and happy", "HAPPY",
            "Confused about what to do", "CONFUSION",
            "Lonely and sad", "SAD",
            "Angry at being left alone", "ANGER",
            "Anxious and fearful", "FEAR"},
        {"When faced with a tough situation, you:",
            "Get angry and want to fight back", "ANGER",
            "Try to stay calm and happy", "HAPPY",
            "Feel scared and unsure", "FEAR",
            "Feel sad and hopeless", "SAD",
            "Feel confused about the next step", "CONFUSION"},
        {"How often do you feel anxious about future events?",
            "Very often and fearful", "FEAR",
            "Rarely, I feel happy", "HAPPY",
            "Sometimes, I feel confused", "CONFUSION",
            "Sometimes, I get angry", "ANGER",
            "Sometimes, I get sad", "SAD"},
        {"What do you do when you fail at something?",
            "Feel sad and disappointed", "SAD",
            "Feel confused and question yourself", "CONFUSION",
            "Get angry at the situation", "ANGER",
            "Try to stay happy and optimistic", "HAPPY",
            "Feel fearful about failing again", "FEAR"}
    };
    private final ButtonGroup[] groups = new ButtonGroup[questions.length];

    public EmotionQuestionnaireUI(String userName) {
        this.userName = userName;
        setTitle("Identify Your Emotion - Bhagavad Gita");
        setSize(850, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout()); 
        mainContentPanel.setBackground(new Color(248, 240, 229)); 
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeLabel = new JLabel("Namaste, " + userName + "!");
        welcomeLabel.setFont(new Font("Mangal", Font.BOLD, 30));
        welcomeLabel.setForeground(new Color(102, 51, 0)); 
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructionLabel = new JLabel("Please select the option that best reflects your current feeling for each question:");
        instructionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 17));
        instructionLabel.setForeground(new Color(80, 80, 80));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(welcomeLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        headerPanel.add(instructionLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        mainContentPanel.add(headerPanel, BorderLayout.NORTH);

        // Questions Panel - will hold all individual question panels
        JPanel questionsContainerPanel = new JPanel();
        questionsContainerPanel.setLayout(new BoxLayout(questionsContainerPanel, BoxLayout.Y_AXIS));
        questionsContainerPanel.setOpaque(false); // Transparent
        questionsContainerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); // Inner padding

        for (int i = 0; i < questions.length; i++) {
            // Individual question panel
            JPanel questionPanel = new JPanel();
            questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
            questionPanel.setBackground(new Color(255, 255, 245)); // Slightly off-white for questions
            questionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 180, 150), 1), // Subtle border
                BorderFactory.createEmptyBorder(15, 20, 15, 20) // Internal padding
            ));
            questionPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Align panels to the left

            JLabel questionText = new JLabel("<html><body style='width: 600px; font-family: Mangal; font-size: 16pt; color: #4A4A4A;'>" + (i + 1) + ". " + questions[i][0] + "</body></html>");
            questionText.setFont(new Font("Mangal", Font.BOLD, 18));
            questionText.setForeground(new Color(70, 70, 70)); // Darker grey for question text
            questionText.setAlignmentX(Component.LEFT_ALIGNMENT);
            questionPanel.add(questionText);
            questionPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            ButtonGroup group = new ButtonGroup();
            groups[i] = group;

            for (int j = 1; j < questions[i].length; j += 2) {
                JRadioButton rb = new JRadioButton("<html><body style='font-family: Segoe UI; font-size: 14pt; color: #555555;'>" + questions[i][j] + "</body></html>");
                rb.setActionCommand(questions[i][j + 1]);
                rb.setBackground(new Color(255, 255, 245)); // Match panel background
                rb.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Slightly smaller font for options
                rb.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor for radio buttons
                group.add(rb);
                questionPanel.add(rb);
            }

            questionsContainerPanel.add(questionPanel);
            questionsContainerPanel.add(Box.createRigidArea(new Dimension(0, 25))); // More space between questions
        }

        JScrollPane scrollPane = new JScrollPane(questionsContainerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default scroll pane border
        scrollPane.setOpaque(false); // Transparent
        scrollPane.getViewport().setOpaque(false); // Transparent viewport

        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Submit Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton submitBtn = new JButton("Get Gita's Guidance");
        submitBtn.setFont(new Font("Mangal", Font.BOLD, 20));
        submitBtn.setBackground(new Color(204, 85, 0)); // Original accent color
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false); // Remove focus border
        submitBtn.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30)); // Padding
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor

        // Add a subtle shadow effect (optional, requires a custom Border or third-party library)
        // For simplicity, we'll use a raised border.
        submitBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(160, 60, 0), 1), // Darker border
            BorderFactory.createEmptyBorder(12, 30, 12, 30)
        ));

        buttonPanel.add(submitBtn);
        mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);


        submitBtn.addActionListener(e -> {
            HashMap<String, Integer> emotionCounts = new HashMap<>();
            String[] emotions = {"HAPPY", "SAD", "ANGER", "FEAR", "CONFUSION"};
            for (String em : emotions) emotionCounts.put(em, 0);

            boolean allAnswered = true;
            for (ButtonGroup group : groups) {
                if (group.getSelection() == null) {
                    allAnswered = false;
                    break;
                }
                String emotion = group.getSelection().getActionCommand();
                emotionCounts.put(emotion, emotionCounts.get(emotion) + 1);
            }

            if (!allAnswered) {
                JOptionPane.showMessageDialog(this, "Please answer all questions before proceeding.", "Incomplete", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String dominantEmotion = "HAPPY";
            int max = -1;
            for (String em : emotions) {
                if (emotionCounts.get(em) > max) {
                    dominantEmotion = em;
                    max = emotionCounts.get(em);
                }
            }

            dispose();
            new BhagavadGitaVersesUI(userName, dominantEmotion).setVisible(true);
        });

        setContentPane(mainContentPanel);
    }
}

// --- BHAGAVAD GITA VERSES UI (Improved Moral Story Section) ---
class BhagavadGitaVersesUI extends JFrame {
    private final String userName;
    private final String emotion;

    public BhagavadGitaVersesUI(String userName, String emotion) {
        this.userName = userName;
        this.emotion = emotion;

        setTitle("Bhagavad Gita Insights for " + userName + " [" + emotion + "]");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(new Color(255, 253, 240)); // Consistent background

        JLabel header = new JLabel("Insights for Your Emotion: " + emotion);
        header.setFont(new Font("Mangal", Font.BOLD, 24));
        header.setForeground(new Color(139, 69, 19)); // Darker brown
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(header);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        HashMap<String, String> verseAndStory = DatabaseManager.fetchVerseAndStory(emotion);

        if (verseAndStory.isEmpty()) {
            JLabel noData = new JLabel("No specific guidance found for this emotion. Perhaps reflect on its nature.");
            noData.setFont(new Font("Segoe UI", Font.ITALIC, 18));
            noData.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(noData);
        } else {
            JLabel verseTitle = new JLabel("Bhagavad Gita Verse:");
            verseTitle.setFont(new Font("Mangal", Font.BOLD, 20));
            verseTitle.setForeground(new Color(204, 85, 0));
            verseTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(verseTitle);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            // Define a font that supports Hindi and Kannada scripts
            Font unicodeFont = new Font("Nirmala UI", Font.PLAIN, 18); // Try "Mangal", "Arial Unicode MS", or "Segoe UI Historic" if Nirmala UI isn't available

            JTextArea verseArea = new JTextArea(
                "Chapter and Verse: " + verseAndStory.get("chapter_verse") + "\n\n" +
                "Hindi:\n" + (verseAndStory.get("verse_hindi") != null ? verseAndStory.get("verse_hindi") : "N/A") + "\n\n" +
                "English:\n" + (verseAndStory.get("verse_english") != null ? verseAndStory.get("verse_english") : "N/A") + "\n\n" +
                "Kannada:\n" + (verseAndStory.get("verse_kannada") != null ? verseAndStory.get("verse_kannada") : "N/A") + "\n\n" +
                "Meaning:\n" + (verseAndStory.get("meaning_english") != null ? verseAndStory.get("meaning_english") : "N/A")
            );
            verseArea.setLineWrap(true);
            verseArea.setWrapStyleWord(true);
            verseArea.setEditable(false);
            verseArea.setFont(unicodeFont); // Apply the unicode font here
            verseArea.setBackground(new Color(255, 250, 230)); // Slightly off-white for content
            verseArea.setBorder(BorderFactory.createLineBorder(new Color(255, 165, 0), 2, true));
            // Wrap JTextArea in a JScrollPane to make it scrollable if content overflows
            JScrollPane verseScrollPane = new JScrollPane(verseArea);
            verseScrollPane.setPreferredSize(new Dimension(800, 300)); // Give it a preferred size
            mainPanel.add(verseScrollPane);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

            // --- IMPROVED MORAL STORY UI START ---
            JPanel storyContainerPanel = new JPanel();
            storyContainerPanel.setLayout(new BoxLayout(storyContainerPanel, BoxLayout.Y_AXIS));
            storyContainerPanel.setOpaque(false); // Make it transparent to allow mainPanel's background to show

            JLabel storyTitle = new JLabel("Moral Story:");
            storyTitle.setFont(new Font("Mangal", Font.BOLD, 22)); // Slightly larger and bolder
            storyTitle.setForeground(new Color(139, 69, 19)); // Matching main header color
            storyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            storyContainerPanel.add(storyTitle);
            storyContainerPanel.add(Box.createRigidArea(new Dimension(0, 15))); // More space below title

            JTextArea storyArea = new JTextArea(
                (verseAndStory.get("moral_story") != null && !verseAndStory.get("moral_story").isEmpty()) ?
                verseAndStory.get("moral_story") : "No specific moral story available for this verse yet. Reflect on the verse's wisdom."
            );
            storyArea.setLineWrap(true);
            storyArea.setWrapStyleWord(true);
            storyArea.setEditable(false);
            storyArea.setFont(new Font("Segoe UI", Font.PLAIN, 20)); // Consistent font size for story
            storyArea.setBackground(new Color(250, 245, 225)); // A distinct, slightly darker background for the story
            storyArea.setForeground(new Color(50, 50, 50)); // Darker text for readability
            storyArea.setCaretPosition(0); // Scroll to top initially
            
            // Add padding to the JTextArea using a CompoundBorder
            storyArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 140, 90), 2), // Softer, thicker border
                BorderFactory.createEmptyBorder(15, 15, 15, 15) // Generous internal padding
            ));
            
            // Wrap JTextArea in a JScrollPane
            JScrollPane storyScrollPane = new JScrollPane(storyArea);
            storyScrollPane.setPreferredSize(new Dimension(800, 200)); // Increased height for more content
            storyScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default scroll pane border for cleaner look
            storyScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smoother scrolling

            storyContainerPanel.add(storyScrollPane);
            mainPanel.add(storyContainerPanel); // Add the entire story container panel
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            // --- IMPROVED MORAL STORY UI END ---
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(22);
        setContentPane(scrollPane);
    }
}
