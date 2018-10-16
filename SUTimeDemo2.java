package demo.model;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Calendar;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import edu.stanford.nlp.time.SUTime.PartialTime;
import edu.stanford.nlp.time.SUTime.Time.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.time.SUTime;
import edu.stanford.nlp.time.SUTime.Temporal;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotations.TimexAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;
import nu.xom.jaxen.function.SubstringAfterFunction;
import edu.stanford.nlp.ling.CoreAnnotations.NormalizedNamedEntityTagAnnotation;
import edu.stanford.nlp.optimization.QNMinimizer.eScaling;
import edu.stanford.nlp.time.SUTime.IsoDate;
public class SUTimeDemo2 {
	
	static AnnotationPipeline pipeline = null;
	
	public static void setup() {
		try {
			String defs_sutime = "/root/NLP/stanford-corenlp-full-2018-02-27/sutime/defs.sutime.txt";
	        String holiday_sutime = "/root/NLP/stanford-corenlp-full-2018-02-27/sutime/english.holidays.sutime.txt";
	        String _sutime = "/root/NLP/stanford-corenlp-full-2018-02-27/sutime/english.sutime.txt";
	        pipeline = new AnnotationPipeline();
	        Properties properties = new Properties();
	        String sutimeRules = defs_sutime + "," + holiday_sutime + "," + _sutime;
	        properties.setProperty("sutime.rules", sutimeRules);
	        properties.setProperty("sutimes.binders", "0");
	        properties.setProperty("sutime.markTimeRanges", "true");
	        properties.setProperty("sutime.includeRange", "true");
	        pipeline.addAnnotator(new TokenizerAnnotator(false));
	        pipeline.addAnnotator(new TimeAnnotator("sutime", properties));
	        
	        
		}catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}	
	}
	
	public static List<String> annotateText(String text, String referenceDate) {
		try {
			if (referenceDate == null || referenceDate.isEmpty()) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				referenceDate = dateFormat.format(new Date());
			}
			else {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				try{
					dateFormat.parse(referenceDate);
				}catch (Exception e) {
					referenceDate = dateFormat.format(new Date());
					// TODO: handle exception
				}
			}
			if (pipeline!=null)
			{
				Annotation annotation = new Annotation(text);
				
				annotation.set(CoreAnnotations.DocDateAnnotation.class, referenceDate);
				pipeline.annotate(annotation);
				List<CoreMap>timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);
				for (CoreMap cm:timexAnnsAll)
				{
					try
					{
						List<CoreLabel>tokens = cm.get(CoreAnnotations.TokensAnnotation.class);
						String beginOffset = tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class).toString();
						String endOffset = tokens.get(tokens.size()-1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class).toString();
						Temporal temporal = cm.get(TimeExpression.Annotation.class).getTemporal();
						/*System.out.println("Token text:" + cm.toString());
						System.out.println("Temporal Value:" + temporal.toString());
						System.out.println("Timex:" + temporal.getTimexValue());
						System.out.println("Timex type:" + temporal.getTimexType().name());
						System.out.println("begin offset:" + beginOffset);
						System.out.println("End Offset:" + endOffset);*/
						String type = temporal.getTimexType().name();
						String value = temporal.toString();
						//System.out.println(temporal.getRange().begin());
						//System.out.println(temporal.getRange().end());
						//System.out.println("!");
						List<String> timeRange = null;
						if (type == "DATE" || type == "DURATION")
						{
							timeRange = NormalizeDurationAndDate(temporal);
						}
						
						else if (type == "TIME")
						{
							timeRange = NormalizeTime(value);
						}
						timeRange.add(cm.toString());
						
						return timeRange;
						
					}catch (Exception e) {
						e.printStackTrace();
						// TODO: handle exception
					}
				}
				
			}
			else {
				//System.out.println("Annotation Pipeline object is NULL.");
			}
		
		}catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		return null;
	}
	
	public static List<String> NormalizeDurationAndDate(Temporal temporal) //默认beginTime,endTime 格式一样。
	{
		List<String> returnTime = new ArrayList<>();
		String beginTime = temporal.getRange().begin().toString();
		String endTime = temporal.getRange().end().toString();
		
		Pattern pattern1 = Pattern.compile("\\d{4}-[A-Z]+-[A-Z]+");
		Matcher matcher1 = pattern1.matcher(beginTime);
		Matcher matcher2 = pattern1.matcher(endTime);
		while(matcher1.find())
		{
			beginTime = matcher1.group().substring(0, 4);
			while(matcher2.find())
			{
				//System.out.println(matcher2.group());
				endTime = matcher2.group().substring(0, 4);
			}
		}
		if (beginTime.length() <= 4)
		{
			beginTime = beginTime + "-01-01T00:00:00";
			endTime = endTime + "-12-31T23:59:59";
		}
		else if (beginTime.length() <= 7) 
		{
			beginTime = beginTime + "-01T00:00:00";
			
			Calendar calendar = Calendar.getInstance();
			calendar.set(calendar.YEAR, Integer.parseInt(endTime.substring(0,4)));
			calendar.set(calendar.MONTH, Integer.parseInt(endTime.substring(5,7))-1);
	        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	        calendar.set(Calendar.DAY_OF_MONTH, lastDay);
	        
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        String lastDayOfMonth = sdf.format(calendar.getTime());
			endTime = endTime + "-" + lastDayOfMonth.substring(8, 10) + "T23:59:59";
		}
		else if (beginTime.length() <= 10)
		{
			beginTime = beginTime + "T00:00:00";
			endTime = endTime + "T23:59:59";
		}
		else if (beginTime.length() <= 13) 
		{
			beginTime = beginTime + ":00:00";
			endTime = endTime + ":00:00";
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		beginTime = beginTime.replace("T", " ");
		endTime = endTime.replace("T", " ");
		try {
			Date beginDate = dateFormat.parse(beginTime);
			Date endDate = dateFormat.parse(endTime);
			//System.out.println(beginDate.getTime());
			//System.out.println(endDate.getTime());
			Long longBeginDate = beginDate.getTime();
			Long longEndDate = endDate.getTime();
			
			returnTime.add(longBeginDate.toString());
			returnTime.add(longEndDate.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println(beginTime);
		//System.out.println(endTime);
		
		return returnTime;
	}
	
	public static List<String> NormalizeTime(String value) {
		List<String> returnTime = new ArrayList<>();
		Pattern pattern1 = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
		Pattern pattern2 = Pattern.compile("T[a-zA-Z0-9:]+");
		Matcher matcher1 = pattern1.matcher(value);
		Matcher matcher2 = pattern2.matcher(value);
		String beginTime = null;
		String endTime = null;
		while (matcher1.find() && matcher2.find()) 
		{
			
			if (matcher2.group().compareTo("TMO") == 0)
			{
				String tempTime = matcher1.group();
				beginTime = matcher1.group() + "T08:00:00";
				endTime = matcher1.group() + "T11:59:59";
				//System.out.println(tempTime);
				
			}
			else if (matcher2.group().compareTo("TAF") == 0)
			{
				String tempTime = matcher1.group();
				beginTime = matcher1.group() + "T13:00:00";
				endTime = matcher1.group() + "T18:59:59";
				//System.out.println(tempTime);
			}
			else if (matcher2.group().compareTo("TEV") == 0)
			{
				String tempTime = matcher1.group();
				beginTime = matcher1.group() + "T19:00:00";
				endTime = matcher1.group() + "T23:59:59";
				//System.out.println(tempTime);
				
			}
			else 
			{
				String tempTime = matcher1.group();
				if (matcher2.group().length() >= 6)
				{
					beginTime = matcher1.group() + matcher2.group().substring(0, 6) + ":00";
					endTime = matcher1.group() + matcher2.group().substring(0, 6) + ":59";
				}
				else
				{
					beginTime = matcher1.group() + matcher2.group();
					endTime = matcher1.group() + matcher2.group();
				}
				//System.out.println(tempTime);
			}
		}
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			beginTime = beginTime.replaceAll("T", " ");
			endTime = endTime.replaceAll("T", " ");
			Date beginDate = dateFormat.parse(beginTime);
			Date endDate = dateFormat.parse(endTime);
			//System.out.println(beginTime);
			//System.out.println(endTime);
			Long longBeginDate = beginDate.getTime();
			Long longEndDate = endDate.getTime();
			returnTime.add(longBeginDate.toString());
			returnTime.add(longEndDate.toString());
			
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	
	return returnTime;	
	}
	public static List<String> Parse(String string) {
		setup();
		String currentTime = SUTime.getCurrentTime().toString();
		String formatTime = currentTime.substring(0, 10);
		List<String> message = null;
		message = annotateText(string,formatTime);
		
		return message;
	}
	
	public static void main(String[] args)
	{
		for (String string:args)
		{
			Parse(string);
		}
		
	}
}
