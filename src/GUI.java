import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
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
        final RNLP core = new RNLP();
        core.main(null);
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
                        core.analyzeText(getText());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    //System.out.println(getText());
                }
            }
        });
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> str = new ArrayList<String>(Arrays.asList(textArea.getText().split(",")));
                core.analyzeText(str);
                System.out.println(str);
            }
        });
    }

    private static List<String> getText() throws IOException {
        List<String> res = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                res.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                br.close();
            }
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
