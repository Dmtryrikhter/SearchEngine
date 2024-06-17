package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lemmatizer {

    private static final String[] PARTICLES_NAMES = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ"};
    public HashMap<String, Integer> getLemmas(String text) throws IOException {
        HashMap<String, Integer> result = new HashMap<>();

        String[] words = clearingHTML(text).replaceAll("[^a-zA-Zа-яА-Я ]", " ").toLowerCase().split(" ");
        LuceneMorphology luceneMorph;
        List<String> wordBaseForms ;

        for (String word : words) {
            List<String> info;
            if (word.equals(word.replaceAll("[^а-яА-Я ]", " "))
                    && !word.isEmpty()) {
                luceneMorph = new RussianLuceneMorphology();
                info = luceneMorph.getMorphInfo(word);
                if (!anyWordBaseBelongToParticle(info)) {
                    wordBaseForms = luceneMorph.getNormalForms(word);
                    wordBaseForms.forEach(s -> result.put(s, lemmaCounter(result, s) + 1));
                }
            }
            else if (word.equals(word.replaceAll("[^a-zA-Z ]", " "))
                    && !word.isEmpty()){
                luceneMorph = new EnglishLuceneMorphology();
                info = luceneMorph.getMorphInfo(word);
                if (!anyWordBaseBelongToParticle(info)) {
                    wordBaseForms = luceneMorph.getNormalForms(word);
                    wordBaseForms.forEach(s -> result.put(s, lemmaCounter(result, s) + 1));
                }
            }
        }
        return result;
    }
    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }
    private boolean hasParticleProperty(String wordBase) {
        for (String property : PARTICLES_NAMES) {
            if (wordBase.contains(property)) {
                return true;
            }
        }
        return false;
    }
    private String clearingHTML(String text) {
        return Jsoup.parse(text).select("body").text();
    }
    private int lemmaCounter(Map<String, Integer> result, String lemma) {
        int count = 0;
        for (Map.Entry<String, Integer> entry : result.entrySet()) {
            if (entry.getKey().equals(lemma)) {
                count = entry.getValue();
            }
        }
        return count;
    }
}
