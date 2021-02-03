package com.qianxinyao.analysis.jieba.keyword;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.huaban.analysis.jieba.JiebaSegmenter;

/**
 * @author Tom Qian
 * @email tomqianmaple@outlook.com
 * @github https://github.com/bluemapleman
 * @date Oct 20, 2018
 * tfidf算法原理参考：http://www.cnblogs.com/ywl925/p/3275878.html
 * 部分实现思路参考jieba分词：https://github.com/fxsjy/jieba
 */
public class TFIDFAnalyzer
{
	
	static HashMap<String,Double> idfMap;
	static HashSet<String> stopWordsSet;
	static double idfMedian;
	
	/**
	 * tfidf分析方法
	 * @param content 需要分析的文本/文档内容
	 * @param topN 需要返回的tfidf值最高的N个关键词，若超过content本身含有的词语上限数目，则默认返回全部
	 * @return
	 */
	public List<Keyword> analyze(String content,int topN){
		List<Keyword> keywordList=new ArrayList<>();
		
		if(stopWordsSet==null) {
			stopWordsSet=new HashSet<>();
			loadStopWords(stopWordsSet, this.getClass().getResourceAsStream("/stop_words.txt"));
		}
		if(idfMap==null) {
			idfMap=new HashMap<>();
			loadIDFMap(idfMap, this.getClass().getResourceAsStream("/idf_dict.txt"));
		}
		
		Map<String, Double> tfMap=getTF(content);
		for(String word:tfMap.keySet()) {
			// 若该词不在idf文档中，则使用平均的idf值(可能定期需要对新出现的网络词语进行纳入)
			if(idfMap.containsKey(word)) {
				keywordList.add(new Keyword(word,idfMap.get(word)*tfMap.get(word)));
			}else
				keywordList.add(new Keyword(word,idfMedian*tfMap.get(word)));
		}
		
		Collections.sort(keywordList);
		
		if(keywordList.size()>topN) {
			int num=keywordList.size()-topN;
			for(int i=0;i<num;i++) {
				keywordList.remove(topN);
			}
		}
		return keywordList;
	}
	
	/**
	 * tf值计算公式
	 * tf=N(i,j)/(sum(N(k,j) for all k))
	 * N(i,j)表示词语Ni在该文档d（content）中出现的频率，sum(N(k,j))代表所有词语在文档d中出现的频率之和
	 * @param content
	 * @return
	 */
	private Map<String, Double> getTF(String content) {
		Map<String,Double> tfMap=new HashMap<>();
		if(content==null || content.equals(""))
			return tfMap; 
		
		JiebaSegmenter segmenter = new JiebaSegmenter();
		List<String> segments=segmenter.sentenceProcess(content);
		Map<String,Integer> freqMap=new HashMap<>();
		
		int wordSum=0;
		for(String segment:segments) {
			//停用词不予考虑，单字词不予考虑
			if(!stopWordsSet.contains(segment) && segment.length()>1) {
				wordSum++;
				if(freqMap.containsKey(segment)) {
					freqMap.put(segment,freqMap.get(segment)+1);
				}else {
					freqMap.put(segment, 1);
				}
			}
		}
		
		// 计算double型的tf值
		for(String word:freqMap.keySet()) {
			tfMap.put(word,freqMap.get(word)*0.1/wordSum);
		}
		
		return tfMap; 
	}
	
	/**
	 * 默认jieba分词的停词表
	 * url:https://github.com/yanyiwu/nodejieba/blob/master/dict/stop_words.utf8
	 * @param set
	 * @param filePath
	 */
	private void loadStopWords(Set<String> set, InputStream in){
		BufferedReader bufr;
		try
		{
			bufr = new BufferedReader(new InputStreamReader(in));
			String line=null;
			while((line=bufr.readLine())!=null) {
				set.add(line.trim());
			}
			try
			{
				bufr.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * idf值本来需要语料库来自己按照公式进行计算，不过jieba分词已经提供了一份很好的idf字典，所以默认直接使用jieba分词的idf字典
	 * url:https://raw.githubusercontent.com/yanyiwu/nodejieba/master/dict/idf.utf8
	 * @param set
	 * @param filePath
	 */
	private void loadIDFMap(Map<String,Double> map, InputStream in ){
		BufferedReader bufr;
		try
		{
			bufr = new BufferedReader(new InputStreamReader(in));
			String line=null;
			while((line=bufr.readLine())!=null) {
				String[] kv=line.trim().split(" ");
				map.put(kv[0],Double.parseDouble(kv[1]));
			}
			try
			{
				bufr.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			// 计算idf值的中位数
			List<Double> idfList=new ArrayList<>(map.values());
			Collections.sort(idfList);
			idfMedian=idfList.get(idfList.size()/2);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		List<String> keywordList = new ArrayList<>();
		keywordList.add("英语六年级下册");
		keywordList.add("大气环境");
		keywordList.add("三国演义 人民文学");
		keywordList.add("雪国之劫");
		keywordList.add("法律适用书籍");
		keywordList.add("道德与法治三年级");
		keywordList.add("破案科学：侦查逻辑与经验");
		keywordList.add("简爱");
		keywordList.add("能力培养与测试:物理");
		keywordList.add("写给孩子们的好童诗");
		keywordList.add("韩国史");
		keywordList.add("卫生健康常用法律法规汇编");
		keywordList.add("历史的填空人民文学");
		keywordList.add("抖音营叶澜：《教育概论》，人民教育出版社，2006年版销");
		keywordList.add("现代局部战争装备运用与保障战例研");
		keywordList.add("工业机器人性能测试技术浙江大学出版社");
		keywordList.add("一级建造师考试市政辅导用书");
		keywordList.add("中华人民共和国民法典");
		keywordList.add("新思路辅导与训练 数学六年级");
		keywordList.add("狐狸和仙鹤");
		keywordList.add("汪汪队立大功中英双语有声故事书(10册)");
		keywordList.add("湖北省事业单位公共基础知识");
		keywordList.add("混凝土质量控制标准");
		keywordList.add("将无同");
		keywordList.add("这题超纲了");
		keywordList.add("破云");
		keywordList.add("帝国主义要把我们的地瓜分掉");
		keywordList.add("魏东海");
		keywordList.add("朝鲜战争");
		keywordList.add("基础教程");
		keywordList.add("世说新语朱碧莲");
		keywordList.add("曹操传");
		keywordList.add("曹操的诗");
		for (String keyWords : keywordList) {
			System.out.println("keyword：" + keyWords);
			int topN=5;
			TFIDFAnalyzer tfidfAnalyzer=new TFIDFAnalyzer();
			List<Keyword> list=tfidfAnalyzer.analyze(keyWords,topN);
			for(Keyword word:list)
				System.out.print(word.getName()+":"+word.getTfidfvalue()+",");
			System.out.println();
			System.out.println(String.join(",",list.stream().map(Keyword::getName).collect(Collectors.toList())));
		}

	}
}

