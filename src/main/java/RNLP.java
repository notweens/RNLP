import edu.stanford.nlp.international.russian.process.RussianLemmatizationAnnotator;
import edu.stanford.nlp.international.russian.process.RussianMorphoAnnotator;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.DependencyParseAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.util.List;
import java.util.Properties;

public class RNLP {
    private static StanfordCoreNLP pipeline;

    public static void main(String[] args) {
        String basePath = new File("").getAbsolutePath();
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit");
        pipeline = new StanfordCoreNLP(props);
        Properties parseProps = new Properties();
        parseProps.setProperty("model", basePath + "\\src\\main\\nndep.rus.modelMFWiki100HS400_80.txt.gz");
        parseProps.setProperty("tagger.model", basePath + "\\src\\main\\russian-ud-pos.tagger");
        pipeline.addAnnotator(new DependencyParseAnnotator(parseProps));
        pipeline.addAnnotator(new RussianMorphoAnnotator());
        pipeline.addAnnotator(new RussianLemmatizationAnnotator());
        pipeline.addAnnotator(new POSTaggerAnnotator(new MaxentTagger(basePath + "\\src\\main\\russian-ud-pos.tagger")));
    }

    public void analyzeText(List<String> text) {
        for (String line : text) {
            if (!line.isEmpty()) {
                Annotation annotation = pipeline.process(line);
                List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
                for (CoreMap sentence : sentences) {
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        String word = token.get(CoreAnnotations.TextAnnotation.class);
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        String lem = token.get(CoreAnnotations.LemmaAnnotation.class);
                        String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                        System.out.println(String.format("Print: word: [%s] pos: [%s] lem: [%s] ne: [%s]", word, pos, lem, ne));
                    }
                }
            }
        }
    }
}
