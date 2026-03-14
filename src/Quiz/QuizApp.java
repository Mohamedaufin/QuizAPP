package Quiz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class QuizApp extends JFrame implements ActionListener, WindowFocusListener {

    JLabel questionLabel, timerLabel;
    JRadioButton[] options;
    ButtonGroup optionsGroup;
    JButton nextBtn, submitBtn;
    JTextField nameField;

    // ALL 25 QUESTIONS (full pool)
    String[][] allQuestions = {
            {"Which language is platform independent?", "C", "C++", "Java", "Python", "Java"},
            {"Which keyword is used to inherit a class in Java?", "this", "super", "extends", "import", "extends"},
            {"What is JVM?", "Java Virtual Machine", "Java Verified Machine", "Just Virtual Mode", "None", "Java Virtual Machine"},
            {"Which is not OOP concept?", "Inheritance", "Encapsulation", "Polymorphism", "Compilation", "Compilation"},
            {"Who developed Java?", "Microsoft", "Sun Microsystems", "Google", "Apple", "Sun Microsystems"},
            {"Which is used to create object?", "new", "this", "super", "class", "new"},
            {"Which feature allows multiple forms?", "Abstraction", "Polymorphism", "Encapsulation", "Inheritance", "Polymorphism"},
            {"Which starts a thread?", "run()", "start()", "execute()", "main()", "start()"},
            {"Which keyword prevents inheritance?", "private", "final", "static", "const", "final"},
            {"Which package loads automatically?", "java.lang", "java.util", "java.io", "java.sql", "java.lang"},
            {"Which data type stores decimal?", "int", "float", "boolean", "char", "float"},
            {"Which operator compares values?", "=", "==", "<>", ":=", "=="},
            {"Which collection has no duplicates?", "List", "Set", "Queue", "Map", "Set"},
            {"Which loop checks condition first?", "do-while", "while", "for", "foreach", "while"},
            {"Bytecode is executed by?", "JRE", "JVM", "JDK", "Compiler", "JVM"},
            {"Which handles exceptions?", "if", "try-catch", "switch", "goto", "try-catch"},
            {"Objects are stored in?", "Stack", "Heap", "Register", "Cache", "Heap"},
            {"Which allocates memory?", "malloc", "alloc", "new", "create", "new"},
            {"Convert string to int:", "parseInt()", "toInt()", "valueOf()", "convert()", "parseInt()"},
            {"Entry point method:", "start()", "main()", "run()", "init()", "main()"},
            {"Wrapper for int:", "IntClass", "IntWrap", "Integer", "NumberInt", "Integer"},
            {"Comment symbol:", "//", "\\\\", "##", "!!", "//"},
            {"Logical AND:", "&&", "&", "||", "!", "&&"},
            {"To uppercase:", "toUpper()", "makeUpper()", "upper()", "toUpperCase()", "toUpperCase()"},
            {"Parent class of all:", "System", "Object", "Class", "Main", "Object"}
    };

    // Quiz questions chosen randomly (only 10)
    String[][] questions;

    int index = 0, correct = 0, timeLeft = 15, tabSwitchCount = 0;
    final int MAX_TAB_SWITCH = 3;
    boolean quizActive = false, internalDialogActive = false;
    javax.swing.Timer timer;
    File scoreFile = new File("scores.txt");
    String username = "";

    public QuizApp() {
        setTitle("EXAM QUIZ SYSTEM");
        setSize(750, 550);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        addWindowFocusListener(this);

        try { if (!scoreFile.exists()) scoreFile.createNewFile(); } catch (IOException ignored) {}

        showLogin();
    }

    void showLogin() {
        JPanel login = new JPanel(null);
        login.setBackground(new Color(235, 245, 255));

        JLabel l = new JLabel("ENTER YOUR NAME:");
        l.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l.setBounds(230, 150, 300, 40);
        login.add(l);

        nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        nameField.setBounds(230, 200, 300, 40);
        login.add(nameField);

        JButton startBtn = new JButton("START QUIZ");
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        startBtn.setBounds(260, 270, 240, 45);
        startBtn.setBackground(new Color(46, 139, 87));
        startBtn.setForeground(Color.WHITE);
        startBtn.addActionListener(e -> startQuiz());
        login.add(startBtn);

        add(login, BorderLayout.CENTER);
    }

    void startQuiz() {
        username = nameField.getText().trim();
        if (username.isEmpty()) {
            showInternalMsg("Please enter your name!");
            return;
        }

        // Randomly pick 10 questions
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<allQuestions.length;i++) list.add(i);
        Collections.shuffle(list);

        questions = new String[10][6];
        for(int i=0;i<10;i++) questions[i] = allQuestions[list.get(i)];

        quizActive = true;
        tabSwitchCount = 0;

        buildQuizUI();
        loadQuestion(index);
    }

    void buildQuizUI() {
        getContentPane().removeAll();

        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(20,10,20,10));

        timerLabel = new JLabel("Time: 15s", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timerLabel.setForeground(Color.RED);
        timerLabel.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));

        JPanel top = new JPanel(new BorderLayout());
        top.add(questionLabel, BorderLayout.CENTER);
        top.add(timerLabel, BorderLayout.EAST);

        JPanel optionsPanel = new JPanel(new GridLayout(4,1,8,8));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20,60,20,60));
        options = new JRadioButton[4];
        optionsGroup = new ButtonGroup();
        for(int i=0;i<4;i++){
            options[i] = new JRadioButton();
            options[i].setFont(new Font("Segoe UI", Font.PLAIN, 18));
            optionsGroup.add(options[i]);
            optionsPanel.add(options[i]);
        }

        JPanel bottom = new JPanel();
        nextBtn = new JButton("NEXT");
        submitBtn = new JButton("SUBMIT");

        nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));

        nextBtn.setBackground(new Color(70,130,180));
        nextBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(new Color(50,205,50));
        submitBtn.setForeground(Color.WHITE);

        nextBtn.addActionListener(this);
        submitBtn.addActionListener(this);

        bottom.add(nextBtn); bottom.add(submitBtn);

        add(top, BorderLayout.NORTH);
        add(optionsPanel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        revalidate(); repaint();
    }

    void loadQuestion(int i) {
        if(!quizActive) return;

        questionLabel.setText("Q"+(i+1)+": "+questions[i][0]);
        for(int j=0;j<4;j++) options[j].setText(questions[i][j+1]);
        optionsGroup.clearSelection();

        timeLeft = 15;
        timerLabel.setText("Time: " + timeLeft + "s");

        if(timer != null) timer.stop();
        timer = new javax.swing.Timer(1000, e -> {
            if(!quizActive){ timer.stop(); return; }
            timeLeft--;
            timerLabel.setText("Time: " + timeLeft + "s");
            if(timeLeft <= 0){
                timer.stop();
                showInternalMsg("Time’s up! Moving to next question.");
                nextQuestion();
            }
        });
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(!quizActive) return;
        if(timer!=null) timer.stop();

        for (JRadioButton opt : options)
            if (opt.isSelected() && opt.getText().equals(questions[index][5])) correct++;

        if(e.getSource() == nextBtn) nextQuestion();
        else showResult();
    }

    void nextQuestion() {
        index++;
        if(index < questions.length) loadQuestion(index);
        else showResult();
    }

    void showResult() {
        quizActive = false;
        saveScore();

        getContentPane().removeAll();
        JLabel r = new JLabel(username + ", your score: " + correct + "/10", SwingConstants.CENTER);
        r.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(r, BorderLayout.CENTER);

        JButton lb = new JButton("Leaderboard");
        lb.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lb.addActionListener(e -> showLeaderboard());

        JPanel p = new JPanel();
        p.add(lb);
        add(p, BorderLayout.SOUTH);

        revalidate(); repaint();
    }

    void saveScore() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(scoreFile,true))) {
            w.write(username + "," + correct + "\n");
        } catch (Exception ignored) {}
    }

    void showLeaderboard() {
        Map<String,Integer> scores = new HashMap<>();
        try (BufferedReader r = new BufferedReader(new FileReader(scoreFile))) {
            String l;
            while((l=r.readLine())!=null){
                String[] p = l.split(",");
                if(p.length==2) scores.put(p[0], Integer.parseInt(p[1]));
            }
        } catch (Exception ignored) {}

        List<Map.Entry<String,Integer>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort((a,b)->b.getValue()-a.getValue());

        StringBuilder sb = new StringBuilder("Leaderboard:\n\n");
        int rank=1;
        for (Map.Entry<String,Integer> e : sorted){
            sb.append(rank++).append(". ").append(e.getKey()).append(" - ").append(e.getValue()).append("\n");
            if(rank>5) break;
        }
        showInternalMsg(sb.toString());
    }

    // INTERNAL dialogs (do NOT count as tab-switch)
    void showInternalMsg(String msg){
        internalDialogActive = true;
        JOptionPane.showMessageDialog(this, msg);
        internalDialogActive = false;
    }

    // TAB SWITCH DETECTION
    @Override public void windowGainedFocus(WindowEvent e){}
    @Override public void windowLostFocus(WindowEvent e){
        if(!quizActive || internalDialogActive) return;
        handleTabSwitch();
    }

    void handleTabSwitch(){
        tabSwitchCount++;
        if(tabSwitchCount > MAX_TAB_SWITCH){
            quizActive=false;
            if(timer!=null) timer.stop();
            correct = 0;
            showInternalMsg("Test Ended due to Multiple External Window Switches!");
            showResult();
        } else {
            showInternalMsg("Warning! External switch detected (" + tabSwitchCount + ")");
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new QuizApp().setVisible(true));
    }
}
