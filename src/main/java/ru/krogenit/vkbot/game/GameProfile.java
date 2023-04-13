package ru.krogenit.vkbot.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class GameProfile {
	
	int id;
	long balance;
	MemeMaker maker;
	GameSkills skills;
	boolean hasChanges;
	
	public GameProfile(int userId) {
		this.balance = 0;
		this.id = userId;
		this.skills = new GameSkills(this);
		this.maker = new MemeMaker(this);
		this.hasChanges = false;
	}
	
	public void updateMinute() {
		maker.updateMinute();
	}
	
	public long addBalance(long value) {
		this.balance += value;
		setHasChanges(true);
		return balance;
	}

	public int getId() {
		return id;
	}

	public long getBalance() {
		return balance;
	}

	public MemeMaker getMaker() {
		return maker;
	}
	
	public GameSkills getSkills() {
		return skills;
	}

	public void save(File folder) throws IOException {
		if(hasChanges) {
			File file = new File(folder, id + ".txt");
			FileWriter writer = new FileWriter(file);
			writer.write("balance=" + balance + "\n");
			
			writer.close();
			
			maker.save(folder);
			skills.save(folder);
			hasChanges = false;
		}
	}
	
	public void load(File folder) throws IOException {
		File file = new File(folder, id + ".txt");
		if(file.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			this.balance = Long.parseLong(reader.readLine().split("=")[1]);
			
			reader.close();
			
			maker.load(folder);
			skills.load(folder);
		}
	}

	public boolean isHasChanges() {
		return hasChanges;
	}

	public void setHasChanges(boolean hasChanges) {
		this.hasChanges = hasChanges;
	}
	
	
	
}
