package ru.krogenit.vkbot.command;

import com.vk.api.sdk.objects.messages.Message;

import ru.krogenit.vkbot.user.VkUser;

public class RandomRule extends BotCommand {

	public RandomRule() {
		super("закон", false);
	}
	
	@Override
	public boolean processCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		String out = "";
		int random = rand.nextInt(34);
		if (random == 0) out = "по закону сурана \n у чорного умерла мама";
		else if (random == 1) out = "по закону егараса \n не бывает кык без саса";
		else if (random == 2) out = "по закону прифа \n перехватили гифа";
		else if (random == 3) out = "по закону бабки \n потерялись тапки";
		else if (random == 4) out = "по закону каменюки \n пострадали ромакюки";
		else if (random == 5) out = "по закону ортама \n мышку забрала мама";
		else if (random == 6) out = "по закону тамары \n чорный съебал на канары";
		else if (random == 7) out = "по закону нидолета \n егарасова помета \n до светейшего сурана \n где игоря в школу подняли рано \n сиськи бабкины провисли \n а пизда ваще прокисла";
		else if (random == 8) out = "по закону пряномазой \n мыльного прибило вазой";
		else if (random == 9) out = "ПО ЗАКОНУ ГОВНО ШИПА \n ЕГАРАСОВСКОГО ДЖИПА \n ФАС ХУЯС НИДОЛЕТИМ \n ВЕДЬ НИДУМОЕТ НАШ КИМ";
		else if (random == 10) out = "по закону курсовой \n ритан угукает с совой";
		else if (random == 11) out = "по закону бузулука \n скелет из майна стрелял из лука";
		else if (random == 12) out = "по закону Тани \n чорный потратил много мани";
		else if (random == 13) out = "ПО ЗАКОНУ ХРОМОСОМ \n ВОДЯРУ ДЕДА ВЫЖРАЛ СОМ";
		else if (random == 15) out = "по закону есть всегда \n в бете скульптора конда!";
		else if (random == 16) out = "по закону банана \n долитаем до сурана";
		else if (random == 17) out = "по закону бананизма \n познаем дар бочинизма";
		else if (random == 18) out = "по закону шаманизма \n для здоровья организма \n бабка будет кушать шаву \n бочанизм даруя даве \n ну а тапки \n мы найдем по закону бабки";
		else if (random == 19) out = "бочка майн и калитан \n укрепляют путь на сран";
		else if (random == 20) out = "по закону риты \n все ежи слиты";
		else if (random == 14) out = "по закону тян-винишка \n у егара встала шишка";
		else if (random == 21) out = "по закону пвп \n мы купили авп";
		else if (random == 22) out = "по закону гронни \n выебали пони";
		else if (random == 23) out = "по закону уха \n нилетит андрюха";
		else if (random == 24) out = "по закону юююзи \n затролилися гууууси";
		else if (random == 25) out = "по закону уууутки \n крякоют проституууутки";
		else if (random == 26) out = "по закону пмс \n тамару слили с бсс";
		else if (random == 27) out = "по закону ома для участка цепи \n жид мозги мне не еби";
		else if (random == 28) out = "по закону тамары \n просканили базы инары";
		else if (random == 29) out = "по закону о бочах \n чорный учеца в сочах";
		else if (random == 30) out = "по закону ветровОЙ \n гобя высрал свое \"ой\"";
		else if (random == 31) out = "по закону\nот суранского престола\nсорок семь и сорок пять\nмыльный мазого опять\n" + "тролит лафканьем с совой\nЧОРНЫЙ, РОТИК-ТО ПРИКРОЙ!";
		else if (random == 32) out = "по закноу о тамаре\n распакуем мы винду в винраре";
		else if(random == 33) out = "по закону военкома\r\n" + 
				"коль живой ты и не в коме\r\n" + 
				"поебать здоров иль нет\r\n" + 
				"но нацепиш ты берет";

		sender.sendMessage(out, chatId, msgId, userId, false);
		return true;
	}

}
