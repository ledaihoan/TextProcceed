/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 *
 * @author ledai
 */
public class ProcessText {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        HashMap<String,Integer> unigram = new HashMap<>();
        HashMap<String,Integer> bigram = new HashMap<>();
        HashMap<String,Integer> trigram = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("D:/phrases"));
        String line;
        Analyzer biAnalyzer = new NGramTokenBaseAnalyzer(2,2,false);
        Analyzer triAnalyzer = new NGramTokenBaseAnalyzer(3,3,false);
        while ((line = br.readLine()) != null) {
            line = line.replaceAll("\\s+", " ").trim();
            //Loai bo 1 so ky hieu
            line = line.replaceAll("<3","");
            line = line.replaceAll(":3","");
            line = line.replaceAll(":v","");
            line = line.replaceAll(":d","");
            line = line.replaceAll(":D","");
            line = line.replaceAll("p/s:", "");
            line = line.replaceAll(":\\)","");
            //unigram process
            String[] arr = line.split("\\s");
            for (String item : arr) {
                item = item.replaceAll("\\s", "");
                if(item.length()>0) {
                    item = item.toLowerCase();
                    Integer freq = unigram.get(item);
                    if(freq!=null) {
                        unigram.put(item, freq + 1);
                    }
                    else unigram.put(item, 1);
                }
            }
            //bigram process
            if(line.length() > 0){
                TokenStream stream = biAnalyzer.tokenStream(null, new StringReader(line));
                CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
                stream.reset();
                while (stream.incrementToken()) {
                  String item = cattr.toString();
                  //item = item.replaceAll("$[\\s]","");
                  Integer count = bigram.get(item);
                  int fcount = 0;
                  if(count==null) fcount = 1;
                  else fcount = count+1;
                  if(item.length() > 3)
                  bigram.put(item, fcount);
                }
                stream.end();
                stream.close();
                //trigram process
                TokenStream stream1 = triAnalyzer.tokenStream(null, new StringReader(line));
                CharTermAttribute cattr1 = stream1.addAttribute(CharTermAttribute.class);
                stream1.reset();
                while (stream1.incrementToken()) {
                  String item = cattr1.toString();
                  //item = item.replaceAll("$[\\s]","");
                  Integer count = trigram.get(item);
                  int fcount = 0;
                  if(count==null) fcount = 1;
                  else fcount = count+1;
                  if(item.length() > 5)
                  trigram.put(item, fcount);
                }
                stream1.end();
                stream1.close();
            }
            
        }
        //Tinh Xac suat cho cac unigram
        HashMap<String,Double> unigramProb = new HashMap<>();
        int totalUniFreq = 0;
        int uniSize = unigram.size();
        for(String item : unigram.keySet()) {
            item = item.toLowerCase();
            int freq = unigram.get(item);
            totalUniFreq += freq;
        }
        //Công thức xác suất dưới đây đã được sửa lại
        for(String item : unigram.keySet()) {
            item = item.toLowerCase();
            int freq = unigram.get(item);
            double prob = ((double)freq +1) / (totalUniFreq + uniSize);
            //unigram.size là số lượng từ vựng unigram khác nhau
            unigramProb.put(item, prob);
        }
        System.out.println("Tong tan suat cua unigram = " + totalUniFreq);
        //Tinh xac suat cho cac bigram
        HashMap<String,Double> bigramProb = new HashMap<>();
        HashMap<String, Integer> startUnigramOfBigram = new HashMap<>();// Luu tong tan suat cua A* bat dau boi unigram A
        //De phuc vu cong thuc xac suat co dieu kien
        int totalBiFreq = 0;//Tinh tong tan suat cua toan bo bigram A* cua unigram A
        //Luu A*
        for(String item : bigram.keySet()) {
            item = item.toLowerCase();
            int freq = bigram.get(item);
            totalBiFreq += freq;
            String[] arr = item.split("\\s");
            String key = arr[0].toLowerCase();//khong can thiet lam
            Integer startFreq = startUnigramOfBigram.get(key);
            if(startFreq==null) startUnigramOfBigram.put(key, freq);
            else startUnigramOfBigram.put(key, freq + startFreq);
        }
        //Ap dung cong thuc xac suat co dieu kien
        //Đã sửa lại công thức
        for(String item : bigram.keySet()) {
            int freq = bigram.get(item);
            String[] arr = item.split("\\s");
            String key = arr[0].toLowerCase();
            int startUniFreq = startUnigramOfBigram.get(key);
            double startUniProb;
            try {
                startUniProb = unigramProb.get(key);
            } catch(NullPointerException ex){
                startUniProb = 1d/(1+uniSize);
            }
            double prob = (((double)freq + 1)/(startUniFreq + uniSize))*startUniProb;
            //uniSize = V là kích thước từ điển unigram
            bigramProb.put(item, prob);
        }
        
        System.out.println("Tong tan suat cua bigram = " + totalBiFreq);
        //Tinh xac suat cho cac trigram
        HashMap<String,Double> trigramProb = new HashMap<>();
        HashMap<String, Integer> startBigramOfTrigram = new HashMap<>();// Luu tong tan suat cua AB* bat dau boi bigram AB
        int totalTriFreq = 0;
        for(String item : trigram.keySet()) {
            int freq = trigram.get(item);
            totalTriFreq += freq;
            String[] arr = item.split("\\s");
            String key = arr[0] + " " + arr[1];
            Integer startFreq = startBigramOfTrigram.get(key);
            if(startFreq==null) startBigramOfTrigram.put(key, freq);
            else startBigramOfTrigram.put(key, freq + startFreq);
        }
        //Ap dung cong thuc xac suat co dieu kien
        for(String item : trigram.keySet()) {
            double startBiProb;
            int freq = trigram.get(item);
            String[] arr = item.split("\\s");
            String key = arr[0] + " " + arr[1];
            //try {
                int startBiFreq = startBigramOfTrigram.get(key);
            try {
                startBiProb = bigramProb.get(key);
            } catch(NullPointerException ex) {
                startBiProb = 1d/(878592 + uniSize);
            }
                double prob = (((double)freq + 1)/(startBiFreq + uniSize))*startBiProb;
                trigramProb.put(item, prob);
            //} catch(NullPointerException ex) {
                
            //}
        }
        System.out.println("Tong tan suat cua trigram = " + totalTriFreq);
        //In ra file
        PrintWriter f0 = new PrintWriter(new FileWriter("D:/App/unigramProb.txt"));
        PrintWriter f1 = new PrintWriter(new FileWriter("D:/App/bigramProb.txt"));
        PrintWriter f2 = new PrintWriter(new FileWriter("D:/App/trigramProb.txt"));
        for(String item : unigramProb.keySet()) {
            double freq = unigramProb.get(item);
            f0.append(item + " = " + freq  + "\n");
        }

        f0.close();
        for(String item : bigramProb.keySet()) {
            double freq = bigramProb.get(item);
            f1.append(item + " = " + freq  + "\n");
        }
        f1.close();
        for(String item : trigramProb.keySet()) {
            double freq = trigramProb.get(item);
            f2.append(item + " = " + freq + "\n");
        }
        f2.close();
        PrintWriter f3 = new PrintWriter(new FileWriter("D:/App/stringProb.txt"));
        br = new BufferedReader(new FileReader("D:/phrases10"));
        HashMap<String,Integer> prefix3Gram = new HashMap<>();
        HashMap<String,Integer> phrases = new HashMap<>();
        while ((line = br.readLine()) != null) {
            line = line.replaceAll("\\s+", " ").trim();
            //Loai bo 1 so ky hieu
            line = line.replaceAll("<3","");
            line = line.replaceAll(":3","");
            line = line.replaceAll(":v","");
            line = line.replaceAll(":d","");
            line = line.replaceAll(":D","");
            line = line.replaceAll("p/s:", "");
            line = line.replaceAll(":\\)","");
            String[] arr = line.split("\\s");
            if(arr.length>2) {
                String prefix = arr[0] + " " + arr[1] + " " + arr[2];
                Integer prefixFreq = prefix3Gram.get(prefix);
                if(prefixFreq==null) prefix3Gram.put(prefix, 1);
                else prefix3Gram.put(prefix, 1 + prefixFreq);
            }
            Integer freq = phrases.get(line);
            if(freq == null) 
                phrases.put(line, 1);
            else
                phrases.put(line, freq + 1);
        }
        //br = new BufferedReader(new FileReader("D:/phrases10"));
        double totalProb = 0;
        int countItem = 0;
        for (String item : phrases.keySet()) {
            line = item;
            Integer lineFreq = phrases.get(item);
            if(lineFreq == null) lineFreq = 1;
            String[] arr = line.split("\\s");
            String prefix = line;
            double probOfLine = 1d*lineFreq/(uniSize + totalTriFreq/uniSize);
            int length = arr.length;
            
            if(length >= 3) { 
                prefix = arr[0] + " " + arr[1] + " " + arr[2];
                int prefixTotal = prefix3Gram.get(prefix);
                try {
                    double prefixProb = trigramProb.get(prefix);                   
                    probOfLine = prefixProb;
                    if(length > 3) {
                        for (int i=3;i<length;i++){
                            prefix = arr[i-2] + " " + arr[i-1] + " " + arr[i];
                            prefixTotal = prefix3Gram.get(prefix);
                            prefixProb = trigramProb.get(prefix);
                            probOfLine *= (1d/prefixTotal)*prefixProb;
                        }
                    }
                    //f3.append(line + " = " + probOfLine + "\n");
                } catch(NullPointerException ex) {
                    probOfLine = 1d*lineFreq/(prefixTotal+uniSize);
                }
            }
            
            f3.append(line + " = " + probOfLine + "\n");
            countItem += arr.length;
            totalProb -= (Math.log(probOfLine)/Math.log(2));
        }
        double somu = totalProb/countItem;
        double perplexity = Math.pow(2, somu);
        f3.close();
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(4);
        System.out.println(somu);
        System.out.printf("PERPLEXITY = " + df.format(perplexity));
    }
}
