package ru.krogenit.vkbot.command;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.trainedmodels.TrainedModels;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor;

import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.messages.MessageAttachment;
import com.vk.api.sdk.objects.messages.MessageAttachmentType;
import com.vk.api.sdk.objects.photos.Photo;

import ru.krogenit.vkbot.user.VkUser;

public class ImageAnalize extends BotCommand {
	
	ComputationGraph vgg16;

	public ImageAnalize() {
		super(new String[] {"анализ"}, true);
		System.out.println("Build image analizer...");
		try {
			vgg16 = ModelSerializer.restoreComputationGraph(new File("neural_saves", "vgg16_dl4j_inference.zip"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done.");
	}
	
	@Override
	public boolean processCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		analizeImage(msg, chatId, msgId, userId);
		return true;
	}
	
	public void analizeImage(Message msg, int chatId, int msgId, int userId) {
		List<MessageAttachment> atts = msg.getAttachments();
		if (atts.size() == 0 || atts.get(0).getType() != MessageAttachmentType.PHOTO) {
			sender.sendMessage("саси, хули ты фотку не прекрепил", chatId, msgId, userId, false);
			return;
		}

		MessageAttachment att = atts.get(0);
		Photo photo = att.getPhoto();

		try {
			URL url = new URL(photo.getPhoto604());
			Image image1 = ImageIO.read(url);
			ImageIO.write((RenderedImage) image1, "JPG", new File("test", "test.jpg"));

			// Convert file to INDArray
			NativeImageLoader loader = new NativeImageLoader(224, 224, 3);
			INDArray image = loader.asMatrix(new File("test", "test.jpg"));

			// Mean subtraction pre-processing step for VGG
			DataNormalization scaler = new VGG16ImagePreProcessor();
			scaler.transform(image);

			// Inference returns array of INDArray, index[0] has the predictions
			INDArray[] output = null;
			if(vgg16 != null) {
				output = vgg16.output(false, image);
			} else {
				sender.sendMessage("модуль времено не работает", chatId, msgId, userId, false);
				return;
			}

			String out = TrainedModels.VGG16.decodePredictions(output[0]);
			out = out.replace("Predictions for batch  :", "наверн на пикче есть чото из этого: ");
			out = out.replace("Band_Aid", "лейкопластырь");
			out = out.replace("diaper", "подгузник");
			out = out.replace("nipple", "ниппель");
			out = out.replace("sunscreen", "солнцезащитный крем");
			out = out.replace("hog", "свинья");
			out = out.replace("modem", "модем");
			out = out.replace("binder", "связующее вещество");
			out = out.replace("carton", "картон");
			out = out.replace("wallet", "бумажник");
			out = out.replace("desk", "стол письменный");
			out = out.replace("feather_boa", "боа из перьев");
			out = out.replace("red_wine", "красное вино");
			out = out.replace("tray", "лоток");
			out = out.replace("velvet", "бархат");
			out = out.replace("packet", "пакет");
			out = out.replace("wool", "шерсть");
			out = out.replace("mask", "маска");
			out = out.replace("ski_mask", "лыжная маска");
			out = out.replace("wig", "парик");
			out = out.replace("bathing_cap", "колпачок для купания");
			out = out.replace("beer_glass", "пивное стекло");
			out = out.replace("cab", "такси");
			out = out.replace("bubble", "пузырь");
			out = out.replace("stage", "сцена, каскад");
			out = out.replace("water_bottle", "фляга, бутылка с водой");
			out = out.replace("military_uniform", "военная форма");
			out = out.replace("torch", "факел");
			out = out.replace("academic_gown", "академическое платье");
			out = out.replace("rugby_ball", "мяч для регби");
			out = out.replace("suspension_bridge", "подвесной мост");
			out = out.replace("traffic_light", "светофор");
			out = out.replace("wreck", "развалина, крушение, авария, обломки");
			out = out.replace("pole", "столб");
			out = out.replace("swing", "свинг, качели");
			out = out.replace("tripod", "штатив");
			out = out.replace("crane", "кран");
			out = out.replace("rifle", "винтовка");
			out = out.replace("microphone", "микрофон");
			out = out.replace("cuirass", "панцирь");
			out = out.replace("breastplate", "нагрудник");
			out = out.replace("bow", "лук");
			out = out.replace("parachute", "парашют");
			out = out.replace("gasmask", "противогаз");
			out = out.replace("rule", "правило");
			out = out.replace("pencil_box", "пенал");
			out = out.replace("menu", "меню");
			out = out.replace("fountain_pen", "перьевая ручка");
			out = out.replace("envelope", "конверт");
			out = out.replace("toilet_tissue", "туалетная бумага");
			out = out.replace("slide_rule", "логарифмическая линейка");
			out = out.replace("matchstick", "деревянная часть спички");
			out = out.replace("candle", "свеча");
			out = out.replace("spotlight", "прожектор");
			out = out.replace("tick", "галочка, тик, клещ");
			out = out.replace("maraca", "маракас");
			out = out.replace("hook", "крюк");
			out = out.replace("ladle", "ковш");
			out = out.replace("fig", "инжир");
			out = out.replace("fountain", "фонтан");
			out = out.replace("car_mirror", "автомобильное зеркало");
			out = out.replace("golf_ball", "мяч для гольфа");
			out = out.replace("hip", "бедро, вальма");
			out = out.replace("balloon", "воздушный шар");
			out = out.replace("lampshade", "абажур");
			out = out.replace("wild_boar", "дикий кабан");
			out = out.replace("wombat", "вомбат");
			out = out.replace("bison", "бизон");
			out = out.replace("warthog", "бородавочник");
			out = out.replace("web_site", "веб сайт");
			out = out.replace("radio", "радио");
			out = out.replace("notebook", "блокнот");
			out = out.replace("monitor", "монитор");
			out = out.replace("screen", "экран");
			out = out.replace("space_shuttle", "космический шаттл");

			// out = out.replace("Band_Aid", "лейкопластырь");
			// out = out.replace("Band_Aid", "лейкопластырь");
			sender.sendMessage(out, chatId, msgId, userId, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
