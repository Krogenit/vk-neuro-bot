package ru.krogenit.vkbot.command;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.vk.api.sdk.objects.messages.Message;

import ru.krogenit.vkbot.user.VkUser;

public class WhenCommand extends BotCommand {
	
	SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy в hh:mm:ss");
	SimpleDateFormat formatter1 = new SimpleDateFormat("hh:mm:ss");

	public WhenCommand() {
		super(new String[] {"когда", "а когда", "када", "а када"}, false);
	}
	
	@Override
	public boolean processCommand(Message msg, String message, int chatId, int msgId, int userId, VkUser user) {
		long currentTime = System.currentTimeMillis();
		int random = rand.nextInt(25);
		if(random == 0) sender.sendMessage("завтра", chatId, msgId, userId, false, null, null);
		else {
			double value = rand.nextDouble() * (1000 * 60 * 60 * 24 * 30 * 12 * 10);
			long newValue = (long)(value) * 1000;
//			System.out.println(newValue);
			long newTime = currentTime + newValue;
//			System.out.println("cur: " + currentTime + " new: " + newTime);
			//long maxtime = 60 * 60 * 24 * 30 * 12 * 100 * 1000;
			//if(newtime > maxtime) newtime = maxtime;
			//long newTime = currentTime + newtime;
			Date d = new Date(newTime);
			String mesg = "";
			long today = 24 * 60 * 60 * 1000;
			String userPostMessage = "";
			try {
				userPostMessage = message.substring(usedCommand.length() + 1);
			}catch(Exception e) {
				
			}
			if(newTime - currentTime < today) {
				mesg = userPostMessage + " сегодня в " + formatter1.format(d);
			} else if(newTime - currentTime < today * 2) {
				mesg = userPostMessage + " завтра в " + formatter1.format(d);
			} else {
				mesg = userPostMessage + " " + formatter.format(d);
			}

			sender.sendMessage(mesg, chatId, msgId, userId, false, null, null);
		}
		
		return true;
	}
}
