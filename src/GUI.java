import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUI {
    private JFrame frame;
    private JLabel label;
    private JTextArea textArea;
    private JButton fileButton;
    private JPanel rootPanel;
    private JButton sendButton;
    private JFileChooser fileChooser;
    private static File file;

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GUI gui = new GUI();
                gui.frame.setVisible(true);
            }
        });
    }

    public GUI() {
        initialize();
        RNLP.main(null);
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
                fileChooser.setFileFilter(filter);
                int returnVal = fileChooser.showOpenDialog(rootPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    try {
                        Output(RNLP.analyzeText(getText()));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> str = new ArrayList<String>(Arrays.asList(textArea.getText().split(",")));
                try {
                    Output(RNLP.analyzeText(str));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    public void Output(String text) {
        JFrame outputFrame = new JFrame("Tagged Text");
        JTextArea messagesArea = new JTextArea();
        messagesArea.setEditable(false);
        outputFrame.add(messagesArea);
        outputFrame.setSize(400,300);
        outputFrame.setVisible(true);
        messagesArea.setFont(new Font("Raster Fonts", Font.BOLD,11));
        messagesArea.setLineWrap(true);
        messagesArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(messagesArea);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outputFrame.getContentPane().add(scroll);
        outputFrame.add(scroll);
        messagesArea.append(text);
    }

    private static List<String> getText() throws IOException {
        List<String> res = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) res.add(line);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (br != null) br.close();
        }
        return res;
    }

    private void initialize() {
        frame = new JFrame();
        frame.add(rootPanel);
        frame.setBounds(100, 100, 400, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
    }
}
