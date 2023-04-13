package ru.krogenit.vkbot.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ru.krogenit.vkbot.user.UserHandler;
import ru.krogenit.vkbot.user.VkUser;

public class GameUserHandler {

	List<GameProfile> users;
	Map<Integer, GameProfile> usersByIds;

	public GameUserHandler() {
		this.users = new ArrayList<GameProfile>();
		this.usersByIds = new HashMap<Integer, GameProfile>();
	}

	public void updateMinute() {
		for (GameProfile profile : users) {
			profile.updateMinute();
		}
	}

	class ProfileComparator implements Comparator<GameProfile> {
		public int compare(GameProfile a, GameProfile b) {
			return a.maker.getMemesComplete() > b.maker.getMemesComplete() ? -1 : 1;
		}
	}

	private ProfileComparator comparator = new ProfileComparator();

	public String getTopUsers(int userId) {
		Collections.sort(users, comparator);
		int pos = 0;
		GameProfile profile1 = null;

		for (int i = 0; i < users.size(); i++) {
			GameProfile profile = users.get(i);
			if (profile.getId() == userId) {
				pos = i+1;
				profile1 = profile;
				break;
			}
		}

		String s = "топ мемеров\n";
		int size = users.size() > 20 ? 20 : users.size();
		for (int i = 0; i < size; i++) {
			GameProfile profile = users.get(i);
			VkUser user = UserHandler.INSTACNE.getUser(profile.getId());
			s += (i+1) + ". @" + user.displayName + " (" + user.firstName + " " + user.lastName + ") " + profile.balance + " руб, " + profile.getMaker().getMemesComplete() + " мемов\n";
		}

		s += "\n";
		if(profile1 != null) {
			s += "ваша позиция " + pos + " \n" + profile1.balance + " руб, " + profile1.getMaker().getMemesComplete() + " мемов";
		} else {
			s += "ну а вы лох не играли ваще и не сделали ниодного мема";
		}

		return s;
	}

	public GameProfile getUser(int userId) {
		GameProfile profile = usersByIds.get(Integer.valueOf(userId));
		if (profile == null) return registerUser(userId);

		return profile;
	}

	public GameProfile registerUser(int userId) {
		GameProfile profile = new GameProfile(userId);
		users.add(profile);
		usersByIds.put(Integer.valueOf(userId), profile);

		return profile;
	}

	public void loadUsers() {
		users.clear();
		usersByIds.clear();
		try {
			File folder = new File("game");
			File file = new File(folder, "users.txt");
			if (!file.exists()) return;

			BufferedReader reader = new BufferedReader(new FileReader(file));
			int size = 0; try { size = Integer.parseInt(reader.readLine().split("=")[1]);}catch(Exception e) {}
			
			if(size == 0) {
				for(File user : folder.listFiles()) {
					String name = user.getName();
					if(!StringUtils.containsIgnoreCase(name, "maker") && !StringUtils.containsIgnoreCase(name, "skills") && !StringUtils.containsIgnoreCase(name, "users")) {
						name = name.replace(".txt", "");
						int id = Integer.parseInt(name);
						GameProfile profile = new GameProfile(id);
						profile.load(folder);
						users.add(profile);
						usersByIds.put(Integer.valueOf(id), profile);
					}
				}
			} else {
				for (int i = 0; i < size; i++) {
					try {
						int id = Integer.parseInt(reader.readLine().split("=")[1]);
						GameProfile profile = new GameProfile(id);
						profile.load(folder);
						users.add(profile);
						usersByIds.put(Integer.valueOf(id), profile);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}

			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveUsers() {
		try {
			File folder = new File("game");
			folder.mkdirs();
			File file = new File(folder, "users.txt");

			FileWriter writer = new FileWriter(file);
			writer.write("size=" + users.size() + "\n");
			for (GameProfile profile : users) {
				writer.write("id=" + profile.getId() + "\n");
				profile.save(folder);
			}

			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
