package ru.krogenit.vkbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class WhiteList {
	public static boolean isWhitelisting;

	public static List<Integer> users = new ArrayList<Integer>();

	public static void addUser(int id) {
		users.add(id);
		save();
	}

	public static void removeUser(int id) {
		users.remove(Integer.valueOf(id));
		save();
	}

	public static boolean isWhitelisted(int id) {
		return users.contains(id);
	}

	public static void save() {
		try {
			FileWriter f = new FileWriter(new File("whitelist", "users.txt"));

			f.write(users.size() + "\n");

			for (Integer id : users) {
				f.write(id.intValue() + "\n");
			}

			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void load() {
		try {
			BufferedReader r = new BufferedReader(new FileReader(new File("whitelist", "users.txt")));

			int size = Integer.parseInt(r.readLine());

			for (int i = 0; i < size; i++) {
				int id = Integer.parseInt(r.readLine());
				users.add(id);
			}

			r.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
