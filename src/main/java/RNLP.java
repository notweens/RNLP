import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.international.russian.process.RussianLemmatizationAnnotator;
import edu.stanford.nlp.international.russian.process.RussianMorphoAnnotator;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class RNLP {
    private static StanfordCoreNLP pipeline;

    public static void main(String[] args) {
        String basePath = new File("").getAbsolutePath();
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

    public void analyzeText(List<String> text) {
        Env env = TokenSequencePattern.getNewEnv();
        env.setDefaultStringMatchFlags(NodePattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        env.setDefaultStringPatternFlags(Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        CoreMapExpressionExtractor extractor = CoreMapExpressionExtractor.createExtractorFromFile(env, new File("").getAbsolutePath() + "\\src\\main\\example.rules");
        for (String line : text) {
            if (!line.isEmpty()) {
                Annotation annotation = pipeline.process(line);
                List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
                for (CoreMap sentence : sentences) {
                    List<MatchedExpression> matchedExpressions = extractor.extractExpressions(sentence);
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        String word = token.word();
                        String pos = token.tag();
                        String lem = token.lemma();
                        String ne = token.ner();
                        System.out.println(String.format("Print: word: [%s] pos: [%s] lem: [%s] ne: [%s] (%s)", word, pos, lem, ne == null ? "0" : ne, token.get(CoreAnnotations.CoNLLUFeats.class)));
                    }
                    for (MatchedExpression me : matchedExpressions) {
                        System.out.println("matched expression: " + me.getText());
                    }
                }
            }
        }
    }
}
