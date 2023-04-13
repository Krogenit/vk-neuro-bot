package ru.krogenit.vkbot.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import ru.krogenit.vkbot.CommandHandler;

public class MemeMaker {
	private int progress;
	private long memesComplete;
	private Random rand = CommandHandler.INSTANCE.rand;
	private GameProfile profile;
	private GameSkills skills;
	private int energy;
	
	public MemeMaker(GameProfile profile, int memesComplete) {
		this.memesComplete = memesComplete;
		this.progress = 0;
		this.profile = profile;
		this.skills = profile.getSkills();
		this.energy = skills.getMemeEnergyMax();
	}
	
	public MemeMaker(GameProfile profile) {
		this.memesComplete = 0;
		this.progress = 0;
		this.profile = profile;
		this.skills = profile.getSkills();
		this.energy = skills.getMemeEnergyMax();
	}
	
	public void updateMinute() {
		this.energy += skills.getMemeEnergyRegen() + rand.nextInt(skills.getMemeEnergyRandomRegen());
		if(energy > skills.getMemeEnergyMax()) {
			energy = skills.getMemeEnergyMax();
		} else {
			profile.setHasChanges(true);
		}
	}
	
	public int addRandomProgress() {
		energy--;
		this.progress += (int)((rand.nextInt(skills.getMemeMakerRandomSpeed()) + skills.getMemeMakerBaseSpeed()) * skills.getMemeMakerFactorSpeed());
		
		if(rand.nextDouble() <= skills.getMemeStealChance()) {
			if(progress < 200) progress = 200;
			else progress+=100;
		}
		
		profile.setHasChanges(true);
		
		return this.progress;
	}
	
	public int addProgerss(int value) {
		energy--;
		this.progress += value;
		profile.setHasChanges(true);
		return this.progress;
	}
	
	public int resetProgress() {
		int reward = 0;
		while(progress >= 100) {
			progress-= 100;
			this.memesComplete++;
			reward += (skills.getMemeRewardBase() + rand.nextInt(skills.getMemeRewardRandom())) * skills.getMemeRewardFactor();
		}
		
		this.progress = 0;
		profile.setHasChanges(true);
		
		return reward;
	}
	
	public int getProgress() {
		return progress;
	}
	
	public void setMemesComplete(long value) {
		this.memesComplete = value;
		profile.setHasChanges(true);
	}

	public long getMemesComplete() {
		return memesComplete;
	}

	public int getEnergy() {
		return energy;
	}
	
	public void addEnergy(int value) {
		energy += value;
		profile.setHasChanges(true);
	}

	public int checkBonus() {
		int reward = 0;
		
		int firstValue = 100;
		int secondValue = 10;
		int factor = 1;
		boolean change = false;
		while(memesComplete >= firstValue) {
			if(!change) {
				firstValue *= 5;
				secondValue *= 5;
				factor+=5;
			} else {
				firstValue *= 2;
				secondValue *= 2;
				factor+=2;
			}
			change = !change;
		}
		
		if(memesComplete % secondValue == 0) reward += ((skills.getMemeBonusRewardBase() + rand.nextInt(skills.getMemeBonusRewardRandom())) * skills.getMemeBonusRewardFactor()) * factor;
		
//		if(memesComplete < 100 && memesComplete % 10 == 0) reward += ((skills.getMemeBonusRewardBase() + rand.nextInt(skills.getMemeBonusRewardRandom())) * skills.getMemeBonusRewardFactor());
//		else if(memesComplete < 500 && memesComplete % 50 == 0) reward += ((skills.getMemeBonusRewardBase() + rand.nextInt(skills.getMemeBonusRewardRandom())) * skills.getMemeBonusRewardFactor());
//		else if(memesComplete < 1000 && memesComplete % 100 == 0) reward += ((skills.getMemeBonusRewardBase() + rand.nextInt(skills.getMemeBonusRewardRandom())) * skills.getMemeBonusRewardFactor());
//		else if(memesComplete < 5000 && memesComplete % 500 == 0) reward += ((skills.getMemeBonusRewardBase() + rand.nextInt(skills.getMemeBonusRewardRandom())) * skills.getMemeBonusRewardFactor());
//		else if(memesComplete < 10000 && memesComplete % 1000 == 0) reward += ((skills.getMemeBonusRewardBase() + rand.nextInt(skills.getMemeBonusRewardRandom())) * skills.getMemeBonusRewardFactor());
		return reward;
	}

	public void save(File folder) throws IOException {
		File file = new File(folder, profile.getId() + "maker.txt");
		FileWriter writer = new FileWriter(file);
		writer.write("progress=" + progress + "\n");
		writer.write("memesComplete=" + memesComplete + "\n");
		writer.write("energy=" + energy + "\n");
		writer.close();
	}

	public void load(File folder) throws IOException {
		File file = new File(folder, profile.getId() + "maker.txt");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		this.progress = Integer.parseInt(reader.readLine().split("=")[1]);
		this.memesComplete = Long.parseLong(reader.readLine().split("=")[1]);
		this.energy = Integer.parseInt(reader.readLine().split("=")[1]);
		reader.close();
	}
}
