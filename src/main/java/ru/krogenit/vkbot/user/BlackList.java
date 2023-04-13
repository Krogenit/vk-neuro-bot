package ru.krogenit.vkbot.user;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlackList {
	public static List<Integer> blockedUsers = new ArrayList<Integer>();
	public static void load() {
		try {
			File f = new File("messages", "blacklist.txt");
			if(f.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String data = null;
				while((data = br.readLine()) != null) {
					blockedUsers.add(Integer.parseInt(data));
				}
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void addUser(int userId) {
//		blockedUsers.remove(18);
		blockedUsers.add(userId);
		
		File f = new File("messages", "blacklist.txt");
		try {
			FileWriter wr = new FileWriter(f);
			for(Integer i : blockedUsers) {
				wr.write(i.intValue() + "\n");
			}
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
