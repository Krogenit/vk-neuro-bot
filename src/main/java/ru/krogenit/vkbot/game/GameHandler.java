package ru.krogenit.vkbot.game;

import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.users.UserXtrCounters;

import ru.krogenit.vkbot.CommandHandler;
import ru.krogenit.vkbot.Main;
import ru.krogenit.vkbot.MessageHandler;
import ru.krogenit.vkbot.user.UserHandler;
import ru.krogenit.vkbot.user.VkUser;

public class GameHandler {
	
	public static GameHandler INSTANCE;
	private GameUserHandler userHandler;
	private long prevTime;
	private Random rand = CommandHandler.INSTANCE.rand;
	
	public GameHandler() {
		this.userHandler = new GameUserHandler();
		this.prevTime = System.currentTimeMillis();
		
		INSTANCE = this;
	}
	
	public void update(long time) {
		if(time - prevTime >= 60 * 2000) {
			userHandler.updateMinute();
			prevTime = time;
		}	
	}

	public boolean checkCommands(Message msg, String message, VkUser user, int chatId, int msgId, int userId) throws Exception {
//		userHandler.users.clear();
//		userHandler.usersByIds.clear();

		if(StringUtils.startsWithIgnoreCase(message, "мемить") && user.canCmd(false)) {
			GameProfile profile = userHandler.getUser(userId);
			MemeMaker maker = profile.getMaker();
			int energy = maker.getEnergy();
			if(energy > 0) {
				long oldMemesCount = maker.getMemesComplete();
				int prog = maker.addRandomProgress();
				long newMemesCount = maker.getMemesComplete();
				long memesCompleteThisTime = newMemesCount - oldMemesCount;
				if(prog == 0) {
					int r = rand.nextInt(3);
					if(r== 0)CommandHandler.INSTANCE.sender.sendMessage("начал лепить дерьмо, прогресс " + profile.getMaker().getProgress() + "%", chatId, msgId, userId, false);
					if(r== 1)CommandHandler.INSTANCE.sender.sendMessage("собираю мем, прогресс " + profile.getMaker().getProgress() + "%", chatId, msgId, userId, false);
					if(r== 2)CommandHandler.INSTANCE.sender.sendMessage("готовица очередняра, прогресс " + profile.getMaker().getProgress() + "%", chatId, msgId, userId, false);
					return true;
				} else if(prog == 200) {
					int reward = profile.getMaker().resetProgress();
					long balance = profile.addBalance(reward);
					CommandHandler.INSTANCE.attachRandomMem(chatId, userId);
					int r = rand.nextInt(3);
					if(r== 0)CommandHandler.INSTANCE.sender.sendMessage("мем сделан сходу, в карман " + reward + " руб\nбаланс " + balance, chatId, msgId, userId, false);
					else if(r== 1)CommandHandler.INSTANCE.sender.sendMessage("спиздил мем из соседнего пабоса, принесло те " + reward + " рубасоф\nбаланс " + balance, chatId, msgId, userId, false);
					else if(r== 2)CommandHandler.INSTANCE.sender.sendMessage("соворовал мемас, за щекой плюс " + reward + " руплей\nбаланс " + balance, chatId, msgId, userId, false);
					
					int bonus = maker.checkBonus();
					if(bonus != 0) {
						balance = profile.addBalance(bonus);
						CommandHandler.INSTANCE.sender.sendMessage("тута бонус подъехал " + bonus + " шекелей\nбаланс " + balance, chatId, msgId, userId, false);
					}
					return true;
				} else if(prog >= 100) {
					int reward = profile.getMaker().resetProgress();
					long balance = profile.addBalance(reward);
					CommandHandler.INSTANCE.attachRandomMem(chatId, userId);
					int r = rand.nextInt(3);
					if(memesCompleteThisTime > 1) {
						if(r== 0)CommandHandler.INSTANCE.sender.sendMessage("вот такая хуета получилась, сделано мемов " + memesCompleteThisTime + " в карман " + reward + " руб\nбаланс " + balance, chatId, msgId, userId, false);
						else if(r== 1)CommandHandler.INSTANCE.sender.sendMessage("высер очередной готов, высрано мемчиков " + memesCompleteThisTime + " принесло те " + reward + " рубасоф\nбаланс " + balance, chatId, msgId, userId, false);
						else if(r== 2)CommandHandler.INSTANCE.sender.sendMessage("ебанул " + memesCompleteThisTime + " мемасов, за щекой плюс " + reward + " руплей\nбаланс " + balance, chatId, msgId, userId, false);
					
						long memesCompleteBackup = maker.getMemesComplete();
						
						while(oldMemesCount < memesCompleteBackup) {
							oldMemesCount++;
							maker.setMemesComplete(oldMemesCount);
							int bonus = maker.checkBonus();
							if(bonus != 0) {
								balance = profile.addBalance(bonus);
								CommandHandler.INSTANCE.sender.sendMessage("тута бонус подъехал " + bonus + " шекелей за красивую " + maker.getMemesComplete() + " мемчиков\nбаланс " + balance, chatId, msgId, userId, false);
							}
						}
						maker.setMemesComplete(memesCompleteBackup);
					} else {
						if(r== 0)CommandHandler.INSTANCE.sender.sendMessage("вот такая хуета получилась, в карман " + reward + " руб\nбаланс " + balance, chatId, msgId, userId, false);
						else if(r== 1)CommandHandler.INSTANCE.sender.sendMessage("высер очередной готов, принесло те " + reward + " рубасоф\nбаланс " + balance, chatId, msgId, userId, false);
						else if(r== 2)CommandHandler.INSTANCE.sender.sendMessage("ебанул мемас, за щекой плюс " + reward + " руплей\nбаланс " + balance, chatId, msgId, userId, false);
					
						int bonus = maker.checkBonus();
						if(bonus != 0) {
							balance = profile.addBalance(bonus);
							CommandHandler.INSTANCE.sender.sendMessage("тута бонус подъехал " + bonus + " шекелей за красивую " + maker.getMemesComplete() + " мемчиков\nбаланс " + balance, chatId, msgId, userId, false);
						}
					}
					

					return true;
				} else {
					int r = rand.nextInt(6);
					if(r== 0)CommandHandler.INSTANCE.sender.sendMessage("прадалжаю ебашить мемес, прогресс " + profile.getMaker().getProgress() + "%", chatId, msgId, userId, false);
					else if(r== 1)CommandHandler.INSTANCE.sender.sendMessage("высираю дерьмо, прогресс " + profile.getMaker().getProgress() + "%", chatId, msgId, userId, false);
					else if(r== 2)CommandHandler.INSTANCE.sender.sendMessage("сру очередняру, прогресс " + profile.getMaker().getProgress() + "%", chatId, msgId, userId, false);
					else if(r== 3)CommandHandler.INSTANCE.sender.sendMessage("патею над мемчиком, прогресс " + profile.getMaker().getProgress() + "%", chatId, msgId, userId, false);
					else if(r== 4)CommandHandler.INSTANCE.sender.sendMessage("капаю алмазы, прогресс " + profile.getMaker().getProgress() + "%", chatId, msgId, userId, false);
					else if(r== 5)CommandHandler.INSTANCE.sender.sendMessage("мозгоеблирую над ржакой, прогресс " + profile.getMaker().getProgress() + "%", chatId, msgId, userId, false);
					return true;
				}
			} else {
				int r = rand.nextInt(4);
				if(r== 0)CommandHandler.INSTANCE.sender.sendMessage("ты сдох, атдахни", chatId, msgId, userId, false);
				else if(r== 1)CommandHandler.INSTANCE.sender.sendMessage("у тя ноль энергии пасиди, раз в пару минут регеница", chatId, msgId, userId, false);
				else if(r== 2)CommandHandler.INSTANCE.sender.sendMessage("вдахнавение кончилась((99(", chatId, msgId, userId, false);
				else if(r== 3)CommandHandler.INSTANCE.sender.sendMessage("пащади, у тя нет сил, ес чо энергия раз в пару минут регеница", chatId, msgId, userId, false);
				return true;
			}
		} else if(StringUtils.startsWithIgnoreCase(message, "навыки") && user.canCmd(false)) {
			GameProfile profile = userHandler.getUser(userId);
			GameSkills skills = profile.skills;
			String[] data = message.split(" ");
			if(data.length > 1) {
				if(StringUtils.startsWithIgnoreCase(data[1], "улучшить")) {
					try {
						int num = Integer.parseInt(data[2]);
						long needMoney = skills.tryUpgradeSkill(num);
						if(needMoney == 0) {
							CommandHandler.INSTANCE.sender.sendMessage("навык улучшен", chatId, msgId, userId, false);
							return true;
						} else if(needMoney == -1) {
							CommandHandler.INSTANCE.sender.sendMessage("навык не найден", chatId, msgId, userId, false);
							return true;
						} else {
							CommandHandler.INSTANCE.sender.sendMessage("необходимо " + needMoney + " руб для улучшения", chatId, msgId, userId, false);
							return true;
						}
					} catch(Exception e){
						CommandHandler.INSTANCE.sender.sendMessage("чтобы улучшить навык - навыки улучшить <номер>", chatId, msgId, userId, false);
						return true;
					}
				} else {
					CommandHandler.INSTANCE.sender.sendMessage("чтобы улучшить навык - навыки улучшить <номер>", chatId, msgId, userId, false);
					return true;
				}
			} else {
				String s = skills.getSkillsString();
				CommandHandler.INSTANCE.sender.sendMessage("ты ваще бесполезный вот твои навыки мытия сортиров: \n" + s + "\nчтобы улучшить навык - навыки улучшить <номер>", chatId, msgId, userId, false);
				return true;
			}
		} else if(StringUtils.startsWithIgnoreCase(message, "профиль") && user.canCmd(false)) {
			GameProfile profile = userHandler.getUser(userId);
			MemeMaker maker = profile.maker;
			GameSkills skills = profile.skills;
			long balance = profile.getBalance();
			String s = "тупа ты короч\n";
			s += "баланс " + balance + " руб\n\n";
			s += "энергии " + maker.getEnergy() + "/" + skills.getMemeEnergyMax() + " \n\n";
			s += "мемов сделал " + maker.getMemesComplete() + "\n";
			CommandHandler.INSTANCE.sender.sendMessage(s, chatId, msgId, userId, false);
			return true;
		} else if(StringUtils.startsWithIgnoreCase(message, "баланс") && user.canCmd(false)) {
			GameProfile profile = userHandler.getUser(userId);
			long balance = profile.getBalance();
			int r = rand.nextInt(3);
			if(r== 0)CommandHandler.INSTANCE.sender.sendMessage("заебал, баланс " + balance + " руп", chatId, msgId, userId, false);
			else if(r== 1)CommandHandler.INSTANCE.sender.sendMessage("ты бомж сраный, баланс " + balance + " руб", chatId, msgId, userId, false);
			else if(r== 2)CommandHandler.INSTANCE.sender.sendMessage("иди блядь работай, баланс " + balance + " руп", chatId, msgId, userId, false);
			return true;
		} else if(StringUtils.startsWithIgnoreCase(message, "топ") && user.canCmd(false)) {
			String s = userHandler.getTopUsers(userId);
			CommandHandler.INSTANCE.sender.sendMessage(s, chatId, msgId, userId, false);
			return true;
		} else if(StringUtils.startsWithIgnoreCase(message, "передать") && user.canCmd(false)) {
			GameProfile profile = userHandler.getUser(userId);
			String[] data = message.split(" ");
			if(data.length > 3) {
				int id = 0;
				String domain = "";
				
				try {
					data[3] = data[3].replace("id", "");
					id = Integer.parseInt(data[3]);
				} catch (Exception e) {
					domain = data[3];
				}
				
				if (id == 0) {
					List<UserXtrCounters> usersCounters = MessageHandler.INSTANCE.vk.users().get(MessageHandler.INSTANCE.actor).userIds(domain).execute();
					if (usersCounters.size() == 0) {
						CommandHandler.INSTANCE.sender.sendMessage("юзер не найден", chatId, msgId, userId, false);
						return true;
					}
					UserXtrCounters c = usersCounters.get(0);
					id = c.getId().intValue();
				}
				
				VkUser user1 = UserHandler.INSTACNE.getUser(id);
				if(user1 != null) {
					GameProfile profile1 = GameHandler.INSTANCE.userHandler.getUser(id);
					if(StringUtils.startsWithIgnoreCase(data[1], "энергию")) {
						int energyToMove = Integer.parseInt(data[2]);
						int curEnergy = profile.maker.getEnergy();
						if(curEnergy >= energyToMove) {
							profile.maker.addEnergy(-energyToMove);
							profile1.maker.addEnergy(energyToMove);
							if(profile1.maker.getEnergy() > profile1.skills.getMemeEnergyMax())
								profile1.maker.addEnergy(-(profile1.maker.getEnergy() - profile1.skills.getMemeEnergyMax()));
							CommandHandler.INSTANCE.sender.sendMessage("успешно закинул маны нубу", chatId, msgId, userId, false);
						} else {
							CommandHandler.INSTANCE.sender.sendMessage("недостаточно чебуреков", chatId, msgId, userId, false);
						}
						return true;
					} else if(StringUtils.startsWithIgnoreCase(data[1], "деньги")) {
						long moneyToMove = Long.parseLong(data[2]);
						long curBalance = profile.getBalance();
						if(curBalance >= moneyToMove) {
							profile.addBalance(-moneyToMove);
							profile1.addBalance(moneyToMove);
							CommandHandler.INSTANCE.sender.sendMessage("успешно закинул бабосов нубу", chatId, msgId, userId, false);
						} else {
							CommandHandler.INSTANCE.sender.sendMessage("недостаточно рубчинских", chatId, msgId, userId, false);
						}
						return true;
					}
				} else {
					CommandHandler.INSTANCE.sender.sendMessage("юзер не найден", chatId, msgId, userId, false);
					return true;
				}
				

			} else {
				CommandHandler.INSTANCE.sender.sendMessage("используй передать <энергию,деньги> <количество> <айди юзера>", chatId, msgId, userId, false);
				return true;
			}
		} else if(StringUtils.startsWithIgnoreCase(message, "мемный бизнес") && user.canCmd(false)) {
			String s = "инфа по мемному бизнесу\n";
			s+="мемить - начать делать мем\n";
			s+="навыки - ваши навыки\n";
			s+="навыки улучшить <номер> - улучшить навык\n";
			s+="профиль - тупы вы бамж\n";
			s+="баланс - ваш пустой кашель\n";
			s+="передать <энергию,деньги> <количество> <айди юзера> - тут понятно, если не даун\n";
			s+="топ - топ мемеров(аутистов)\n";
			CommandHandler.INSTANCE.sender.sendMessage(s, chatId, msgId, userId, false);
		} else if(StringUtils.startsWithIgnoreCase(message, "memsave") && userId == Main.OWNER_ID) {
			save();
			CommandHandler.INSTANCE.sender.sendMessage("saved", chatId, msgId, userId, false);
		} else if(StringUtils.startsWithIgnoreCase(message, "memload") && userId == Main.OWNER_ID) {
			load();
			CommandHandler.INSTANCE.sender.sendMessage("loaded", chatId, msgId, userId, false);
		}
//		else if(StringUtils.startsWithIgnoreCase(message, "статистика")) {
//			GameProfile profile = userHandler.getUser(userId);
//			String s = 
//			String[] data = message.split(" ");
//			if(data.length > 0) {
//				
//			} else {
//				CommandHandler.INSTANCE.sender.sendMessage("юзай \"статистика <раздел>\"\nразделы: мемоделонье", chatId, msgId, userId, false);
//			}
//		}
		
		return false;
	}
	
	public void save() {
		Thread t = new Thread() {
			@Override
			public void run() {
				synchronized (userHandler) {
					userHandler.saveUsers();	
				}
			}
		};
		t.start();
	}

	public void load() {
		userHandler.loadUsers();
	}
}
