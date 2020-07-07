import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
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
        SwingUtilities.invokeLater(() -> {
            GUI gui = null;
            try {
                gui = new GUI();
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert gui != null;
            gui.frame.setVisible(true);
        });
    }

    public GUI() throws IOException {
        initialize();
        RNLP.main(null);
        FileInputStream XMLFile = new FileInputStream(RNLP.basePath + "\\src\\main\\person.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(XMLFile);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Person [] persons = new Person[sheet.getPhysicalNumberOfRows() - 1];
        DataFormatter formatter = new DataFormatter();
        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            XSSFRow row = sheet.getRow(i);
            String[] cells = new String[row.getPhysicalNumberOfCells()];
            for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) cells[j] = formatter.formatCellValue(row.getCell(j));
            persons[i - 1] = new Person(cells[0], cells[1], cells[2], cells[3], cells[4], cells[5], cells[6]);
        }
        fileButton.addActionListener(e -> {
            fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
            fileChooser.setFileFilter(filter);
            int returnVal = fileChooser.showOpenDialog(rootPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                try {
                    Output(RNLP.analyzeText(getText(), persons));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        sendButton.addActionListener(e -> {
            List<String> str = new ArrayList<>(Arrays.asList(textArea.getText().split(",")));
            Output(RNLP.analyzeText(str, persons));
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
        List<String> res = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) res.add(line);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
