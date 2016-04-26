/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author ledai
 */
public class NGramTokenBaseAnalyzer extends Analyzer {
     /*public static final CharArraySet VIETNAMESE_STOP_WORDS_SET;
     static {
        final List<String> stopWords = Arrays.asList(
                "bị", "bởi", "cả", "các", "cái", "cần", "càng", "chỉ", "chiếc", "cho", "chứ", "chưa", "chuyện",
                "có", "có thể", "cứ", "của", "cùng", "cũng", "đã", "đang", "đây", "để", "đến nỗi", "đều", "điều",
                "do", "đó", "được", "dưới", "gì", "khi", "không", "là", "lại", "lên", "lúc", "mà", "mỗi", "một cách",
                "này", "nên", "nếu", "ngay", "nhiều", "như", "nhưng", "những", "nơi", "nữa", "phải", "qua", "ra",
                "rằng", "rằng", "rất", "rất", "rồi", "sau", "sẽ", "so", "sự", "tại", "theo", "thì", "trên", "trước",
                "từ", "từng", "và", "vẫn", "vào", "vậy", "vì", "việc", "với", "vừa"
        );
        final CharArraySet stopSet = new CharArraySet(stopWords, false);
        VIETNAMESE_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
    }*/
    private static int min = 2;
    private static int max = 2;
    private boolean unigramOutput = true;
    public NGramTokenBaseAnalyzer(int maxShingleSize){
        super();
        this.max = maxShingleSize;
    }
    public NGramTokenBaseAnalyzer(int min,int max, boolean unigram){
        super();
        this.min = min;
        this.max = max;
        this.unigramOutput = unigram;
    }
    public NGramTokenBaseAnalyzer(){
        super();
    }
    public NGramTokenBaseAnalyzer(int min, int max){
        this(min,max,true);
    }
    public static ShingleFilter filter(TokenStream tok, boolean unigram){
        ShingleFilter sf = new ShingleFilter(tok,NGramTokenBaseAnalyzer.min,NGramTokenBaseAnalyzer.max);
        sf.setOutputUnigrams(unigram);
        return sf;
    }
    @Override
    protected Analyzer.TokenStreamComponents createComponents(String fieldName,
            Reader reader) {
            
        Tokenizer src = new StandardTokenizer(reader);
        
        TokenStream tok = new LowerCaseFilter(src);
        tok = filter(tok,this.unigramOutput);
        return new TokenStreamComponents(src,tok);
    }
} 

    
