import edu.stanford.nlp.international.russian.process.RussianLemmatizationAnnotator;
import edu.stanford.nlp.international.russian.process.RussianMorphoAnnotator;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class RNLP {
    private static StanfordCoreNLP pipeline;
    private static final String basePath = new File("").getAbsolutePath();
    private static String personMentioned = "";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos");
        pipeline = new StanfordCoreNLP(props);
        Properties parseProps = new Properties();
        parseProps.setProperty("model", basePath + "\\src\\main\\nndep.rus.modelMFWiki100HS400_80.txt.gz");
        parseProps.setProperty("tagger.model", basePath + "\\src\\main\\russian-ud-pos.tagger");
        pipeline.addAnnotator(new DependencyParseAnnotator(parseProps));
        pipeline.addAnnotator(new RussianMorphoAnnotator());
        pipeline.addAnnotator(new RussianLemmatizationAnnotator());
        pipeline.addAnnotator(new POSTaggerAnnotator(new MaxentTagger(basePath + "\\src\\main\\russian-ud-pos.tagger")));
        pipeline.addAnnotator(new TokensRegexAnnotator(basePath + "\\src\\main\\example.txt"));
    }

    public static String tag(Person person, String word) {
        personMentioned = person.uuid;
        word = "<" + person.uuid + ">" + word + "</" + person.uuid + ">";
        return word;
    }

    public static String analyzeText(List<String> text) throws IOException {
        FileInputStream file = new FileInputStream(basePath + "\\src\\main\\person.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Person [] persons = new Person[sheet.getPhysicalNumberOfRows() - 1];
        DataFormatter formatter = new DataFormatter();
        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            XSSFRow row = sheet.getRow(i);
            String[] cells = new String[row.getPhysicalNumberOfCells()];
            for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) cells[j] = formatter.formatCellValue(row.getCell(j));
            persons[i - 1] = new Person(cells[0], cells[1], cells[2], cells[3], cells[4], cells[5], cells[6]);
        }
        StringBuilder wholeWord = new StringBuilder();
        CoreLabel token, nextToken = null;
        Person ignoreNext = null;
        for (String line : text) {
            if (!line.isEmpty()) {
                Annotation annotation = pipeline.process(line);
                List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
                for (CoreMap sentence : sentences) {
                    for (int i = 0; i < sentence.get(CoreAnnotations.TokensAnnotation.class).size(); i++) {
                        token = sentence.get(CoreAnnotations.TokensAnnotation.class).get(i);
                        String word = token.word();
                        if (i + 1 != sentence.get(CoreAnnotations.TokensAnnotation.class).size()) nextToken = sentence.get(CoreAnnotations.TokensAnnotation.class).get(i + 1);
                        if (ignoreNext != null) {
                            word = word + "</" + ignoreNext.uuid + ">";
                            wholeWord.append(word);
                            if (nextToken != null) {
                                if (!nextToken.word().contains(".") || !nextToken.word().contains("!") || !nextToken.word().contains("?")) wholeWord.append(" ");
                            }
                            else wholeWord.append(" ");
                            ignoreNext = null;
                            continue;
                        }
                        String lemma = token.lemma();
                        lemma = lemma.substring(0, 1).toUpperCase() + lemma.substring(1);
                        String ner = token.ner() == null ? "0" : token.ner();
                        String pos = token.tag();
                        for (Person person : persons) {
                            if (lemma.contains(person.firstName) || lemma.contains(person.lastName) || lemma.contains(person.middleName)) word = tag(person, word);
                            else if (ner.contains("ADDRESS") && nextToken != null && nextToken.ner() != null && nextToken.ner().contains("ADDRESS") && (person.address.contains(nextToken.word()) || person.address.contains(word))) {
                                ignoreNext = person;
                                word = "<" + person.uuid + ">" + word;
                                personMentioned = person.uuid;
                            } else if (person.address.contains(lemma)) word = tag(person, word);
                            else if (ner.contains("PHNUM") && person.number.contains(word)) word = tag(person, word);
                            else if (ner.contains("DATE") || pos.contains("NUM") && person.date.contains(word)) word = tag(person, word);
                            else if (pos.contains("DET") && !lemma.contains("Свой") && personMentioned.equals(person.uuid)) word = tag(person, word);
                        }
                        wholeWord.append(word);
                        if (nextToken != null) {
                            if (!nextToken.word().contains(".") || !nextToken.word().contains("!") || !nextToken.word().contains("?")) wholeWord.append(" ");
                        }
                        else wholeWord.append(" ");
                    }
                }
            }
        }
        return wholeWord.toString();
    }
}
