import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.international.russian.process.RussianLemmatizationAnnotator;
import edu.stanford.nlp.international.russian.process.RussianMorphoAnnotator;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.*;
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
import java.util.UUID;
import java.util.regex.Pattern;

public class RNLP {
    private static StanfordCoreNLP pipeline;
    private static String basePath = new File("").getAbsolutePath();

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

    public String tag(Person person, String word) {
        word = "<" + person.uuid + ">" + word + "</" + person.uuid + ">";
        return word;
    }

    public void analyzeText(List<String> text) throws IOException {
        Env env = TokenSequencePattern.getNewEnv();
        env.setDefaultStringMatchFlags(NodePattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        env.setDefaultStringPatternFlags(Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        CoreMapExpressionExtractor extractor = CoreMapExpressionExtractor.createExtractorFromFile(env, basePath + "\\src\\main\\example.rules");
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
        for (String line : text) {
            if (!line.isEmpty()) {
                Annotation annotation = pipeline.process(line);
                List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
                for (CoreMap sentence : sentences) {
                    List<MatchedExpression> matchedExpressions = extractor.extractExpressions(sentence);
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        String lemma = token.lemma();
                        lemma = lemma.substring(0, 1).toUpperCase() + lemma.substring(1);
                        //System.out.print("[" + lemma + "]");
                        String word = token.word();
                        String ner = token.ner() == null ? "0" : token.ner();
                        String pos = token.tag();
                        for (int i = 0; i < persons.length; i++) {
                            //System.out.print("'" + token.lemma() + "'");
                            if (lemma.contains(persons[i].firstName) || lemma.contains(persons[i].lastName) || lemma.contains(persons[i].middleName)) word = tag(persons[i], word);
                            else if (persons[i].address.contains(lemma)/* && ner == "ADDRESS"*/) word = tag(persons[i], word);
                            else if (ner.contains("PHNUM") && persons[i].number.contains(word)) word = tag(persons[i], word);
                            else if (ner.contains("DATE") || pos.contains("NUM") && persons[i].date.contains(word)) word = tag(persons[i], word);
                            else if (pos.contains("DET")) word = tag(persons[i], word);
                        }
                        /*String word = token.word();
                        String pos = token.tag();
                        String lem = token.lemma();
                        String ne = token.ner();*/
                        System.out.print(token.after().contains(".") || token.after().contains("!") || token.after().contains("?") ? word : word + " ");
                        //System.out.println(String.format("Print: word: [%s] pos: [%s] lem: [%s] ne: [%s] (%s)", word, pos, lem, ne == null ? "0" : ne, token.get(CoreAnnotations.CoNLLUFeats.class)));
                    }
                    for (MatchedExpression me : matchedExpressions) {
                        System.out.println("matched expression: " + me.getText());
                    }
                }
            }
        }
        System.out.println();
    }
}
