package demo.model;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Calendar;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.stanford.nlp.time.SUTime.Time;
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

public class SUTimeDemo2 {
	
	static AnnotationPipeline pipeline = null;
	
	public static void setup() {
		try {
			String defs_sutime = "D:/workplcae3/stanford-corenlp-full-2018-02-27/sutime/defs.sutime.txt";
	        String holiday_sutime = "D:/workplcae3/stanford-corenlp-full-2018-02-27/sutime/english.holidays.sutime.txt";
	        String _sutime = "D:/workplcae3/stanford-corenlp-full-2018-02-27/sutime/english.sutime.txt";
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
						String startOffset = tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class).toString();
						String endOffset = tokens.get(tokens.size()-1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class).toString();
						Temporal temporal = cm.get(TimeExpression.Annotation.class).getTemporal();
						/*System.out.println("Token text:" + cm.toString());
						System.out.println("Temporal Value:" + temporal.toString());
						System.out.println("Timex:" + temporal.getTimexValue());
						System.out.println("Timex type:" + temporal.getTimexType().name());
						System.out.println("Start offset:" + startOffset);
						System.out.println("End Offset:" + endOffset);*/
						String type = temporal.getTimexType().name();
						String value = temporal.toString();
						List<String> timeRange = Normalize(type, value);
						for(String time:timeRange)
						{
							System.out.println(time);
						}
						return timeRange;
					}catch (Exception e) {
						e.printStackTrace();
						// TODO: handle exception
					}
				}
				
			}
			else {
				System.out.println("Annotation Pipeline object is NULL.");
			}
		
		}catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		return null;
	}
	
	public static List<String> Normalize(String type, String value) {
		List<String> returnTime = new ArrayList<>();
		if (type == "DATE")
		{	
			Pattern pattern1 = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
			Matcher matcher1 = pattern1.matcher(value);

			while (matcher1.find()) 
			{
				String tempTime = matcher1.group();
				String startTime = matcher1.group() + "T00:00:00";
				String endTime = matcher1.group() + "T23:59:59";
				returnTime.add(startTime);
				returnTime.add(endTime);
				//System.out.println(tempTime);
				return returnTime;
			}
			return returnTime;
			
		}
		else if (type == "TIME")
		{
			Pattern pattern1 = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
			Pattern pattern2 = Pattern.compile("T[a-zA-Z0-9:]+");
			Matcher matcher1 = pattern1.matcher(value);
			Matcher matcher2 = pattern2.matcher(value);
			
			while (matcher1.find() && matcher2.find()) 
			{
				
				if (matcher2.group().compareTo("TMO") == 0)
				{
					String tempTime = matcher1.group();
					String startTime = matcher1.group() + "T06:00:00";
					String endTime = matcher1.group() + "T11:59:59";
					returnTime.add(startTime);
					returnTime.add(endTime);
					//System.out.println(tempTime);
					return returnTime;
				}
				else if (matcher2.group().compareTo("TAF") == 0)
				{
					String tempTime = matcher1.group();
					String startTime = matcher1.group() + "T13:00:00";
					String endTime = matcher1.group() + "T18:59:59";
					returnTime.add(startTime);
					returnTime.add(endTime);
					//System.out.println(tempTime);
					return returnTime;
				}
				else if (matcher2.group().compareTo("TEV") == 0)
				{
					String tempTime = matcher1.group();
					String startTime = matcher1.group() + "T19:00:00";
					String endTime = matcher1.group() + "T23:59:59";
					returnTime.add(startTime);
					returnTime.add(endTime);
					//System.out.println(tempTime);
					return returnTime;
				}
				else 
				{
					String tempTime = matcher1.group();
					if (matcher2.group().length() >= 6)
					{
						String startTime = matcher1.group() + matcher2.group().substring(0, 6) + ":00";
						String endTime = matcher1.group() + matcher2.group().substring(0, 6) + ":59";
						returnTime.add(startTime);
						returnTime.add(endTime);
					}
					else
					{
						String startTime = matcher1.group() + matcher2.group();
						String endTime = matcher1.group() + matcher2.group();
						returnTime.add(startTime);
						returnTime.add(endTime);
					}
					
					//System.out.println(tempTime);
					return returnTime;
				}
			}
			return returnTime;
				
			
		}
		else if (type == "DURATION")
		{
			String tempTime = value.substring(value.length()-1, value.length());
			Pattern pattern1 = Pattern.compile("([T0-9:-]+),([T0-9:-]+)");
			Matcher matcher1 = pattern1.matcher(value);
			
			while (matcher1.find()) 
			{
				String currentTime = SUTime.getCurrentTime().toString().substring(0, 10);
				String temp1 = matcher1.group(1);
				String temp2 = matcher1.group(2);
				if (temp1.length() <= 6) //2:00 to 6:00   T02:00
				{
					String returnTime1 = currentTime.concat(temp1+":00");
					String returnTime2 = currentTime.concat(temp2+":59");
					returnTime.add(returnTime1);
					returnTime.add(returnTime2);
					//System.out.println(returnTime1);
					return returnTime;
				}
				else if (temp1.length() <= 9) //2:00:00
				{
					String returnTime1 = currentTime.concat(temp1);
					String returnTime2 = currentTime.concat(temp2);
					returnTime.add(returnTime1);
					returnTime.add(returnTime2);
					//System.out.println(returnTime1);
					return returnTime;
				}
				else if (temp1.length() <= 10)//2018-10-10
				{
					String returnTime1 = temp1.concat("T00:00:00");
					String returnTime2 = temp2.concat("T23:59:59");
					//System.out.println(returnTime1);
					return returnTime;
				}
				else 
				{
					returnTime.add(currentTime);
					returnTime.add(currentTime);
					return returnTime;
				}
			}
			
			Pattern pattern2 = Pattern.compile("P[0-9A-Za-z]+");
			Matcher matcher2 = pattern2.matcher(value);
			while (matcher2.find()) //for 2 hours/days.
			{
				String temp = matcher2.group();
				String currentTime = SUTime.getCurrentTime().toString().substring(0, 12);
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				if (temp.length() >= 4)
				{
					String string = temp.substring(temp.length()-1, temp.length());
					String beAddedTime = temp.substring(temp.length()-2,temp.length()-1);
					int beAddedTimeValue = Integer.parseInt(beAddedTime);
					Calendar calendar = Calendar.getInstance();
					if (string.compareTo("M") == 0)
						calendar.add(calendar.MINUTE, beAddedTimeValue);
					else if (string.compareTo("H") == 0)
						calendar.add(calendar.HOUR_OF_DAY, beAddedTimeValue);
					else if (string.compareTo("Y") == 0)
						calendar.add(calendar.YEAR, beAddedTimeValue);
					else if (string.compareTo("D") == 0)
						calendar.add(calendar.DATE, beAddedTimeValue);
					else if (string.compareTo("W") == 0)
						calendar.add(calendar.WEEK_OF_YEAR, beAddedTimeValue);
					//System.out.println(dateFormat.format(calendar.getTime()).replaceAll(" ", "T"));
					String startTime = currentTime;
					String endTime = dateFormat.format(calendar.getTime()).replaceAll(" ", "T");
					returnTime.add(startTime);
					returnTime.add(endTime);
					return returnTime;
					
				}
				else if (temp.length() == 3)
				{
					String string = temp.substring(temp.length()-1, temp.length());
					String beAddedTime = temp.substring(temp.length()-2,temp.length()-1);
					int beAddedTimeValue = Integer.parseInt(beAddedTime);
					Calendar calendar = Calendar.getInstance();
					if (string.compareTo("Y") == 0)
						calendar.add(calendar.YEAR, beAddedTimeValue);
					else if (string.compareTo("D") == 0)
						calendar.add(calendar.DATE, beAddedTimeValue);
					else if (string.compareTo("W") == 0)
						calendar.add(calendar.WEEK_OF_YEAR, beAddedTimeValue);
					//System.out.println(dateFormat.format(calendar.getTime()).replaceAll(" ", "T"));
					String startTime = currentTime;
					String endTime = dateFormat.format(calendar.getTime()).replaceAll(" ", "T");
					returnTime.add(startTime);
					returnTime.add(endTime);
					return returnTime;
				}
				
			}
		}
		String currentTime = SUTime.getCurrentTime().toString().substring(0, 12);
		returnTime.add(currentTime);
		returnTime.add(currentTime);
		return returnTime;
	}
	
	public static void main(String[] args)
	{
		setup();
		String currentTime = SUTime.getCurrentTime().toString();
		String formatTime = currentTime.substring(0, 10);
		for (String string:args)
		{
			annotateText(string,formatTime);
		}
		
	}
}
