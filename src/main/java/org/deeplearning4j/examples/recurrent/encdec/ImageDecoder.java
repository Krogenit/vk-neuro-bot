package org.deeplearning4j.examples.recurrent.encdec;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.WordResult;

public class ImageDecoder
{
	public static void main(String[] args)
	{
		try
		{
			Configuration configuration = new Configuration();
			configuration.setAcousticModelPath("neural_saves/audio/cmusphinx-ru-5.2");
			configuration.setDictionaryPath("neural_saves/audio/cmusphinx-ru-5.2/ru.dic");
			configuration.setLanguageModelPath("neural_saves/audio/cmusphinx-ru-5.2/ru.lm");
			LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
			recognizer.startRecognition(true);
			while (true)
			{
				SpeechResult result;

				while ((result = recognizer.getResult()) != null)
				{
					for (WordResult r : result.getWords()) {
		        	    System.out.println(r);
		        	}
				}
			}
//			recognizer.stopRecognition();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
