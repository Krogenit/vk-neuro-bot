package ru.krogenit.vkbot.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import ru.krogenit.vkbot.game.skills.Skill;

public class GameSkills {
	
	NumberFormat formatter = new DecimalFormat("#0.000");    
	private GameProfile profile;
	
	private Skill memeMakerBaseSpeed;
	private Skill memeMakerFactorSpeed;
	private Skill memeMakerRandomSpeed;
	
	private Skill memeRewardBase;
	private Skill memeRewardFactor;
	private Skill memeRewardRandom;
	
	private Skill memeEnergyMax;
	private Skill memeEnergyRegen;
	private Skill memeEnergyRandomRegen;
	
	private Skill memeBonusRewardBase;
	private Skill memeBonusRewardFactor;
	private Skill memeBonusRewardRandom;
	
	private Skill memeStealChance;
	
	public GameSkills(GameProfile profile) {
		this.profile = profile;
		
		this.memeMakerFactorSpeed = new Skill(1.0, 1.0, 1);
		this.memeMakerBaseSpeed = new Skill(4.0, 4.0, 1);
		this.memeMakerRandomSpeed = new Skill(6.0, 6.0, 1);
		
		this.memeRewardFactor = new Skill(1.0, 1.0, 1);
		this.memeRewardBase = new Skill(1.0, 1.0, 1);
		this.memeRewardRandom = new Skill(2.0, 2.0, 1);
		
		this.memeEnergyMax = new Skill(10, 10, 1);
		this.memeEnergyRegen = new Skill(1.0, 1.0, 1);
		this.memeEnergyRandomRegen = new Skill(2.0, 2.0, 1);
		
		this.memeBonusRewardBase = new Skill(10.0, 10.0, 1);
		this.memeBonusRewardFactor = new Skill(1.0, 1.0, 1);
		this.memeBonusRewardRandom = new Skill(5.0, 5.0, 1);
		
		this.memeStealChance = new Skill(0.001, 0.001, 1);
	}

	public int getMemeMakerBaseSpeed() {
		return (int) memeMakerBaseSpeed.getValue();
	}

	public double getMemeMakerFactorSpeed() {
		return memeMakerFactorSpeed.getValue();
	}

	public int getMemeRewardBase() {
		return (int) memeRewardBase.getValue();
	}

	public double getMemeRewardFactor() {
		return memeRewardFactor.getValue();
	}

	public int getMemeMakerRandomSpeed() {
		return (int) memeMakerRandomSpeed.getValue();
	}

	public int getMemeRewardRandom() {
		return (int) memeRewardRandom.getValue();
	}

	public int getMemeEnergyMax() {
		return (int) memeEnergyMax.getValue();
	}

	public int getMemeEnergyRegen() {
		return (int) memeEnergyRegen.getValue();
	}

	public int getMemeBonusRewardBase() {
		return (int) memeBonusRewardBase.getValue();
	}

	public double getMemeBonusRewardFactor() {
		return memeBonusRewardFactor.getValue();
	}

	public int getMemeBonusRewardRandom() {
		return (int) memeBonusRewardRandom.getValue();
	}
	
	public double getMemeStealChance() {
		return memeStealChance.getValue();
	}
	
	public int getMemeEnergyRandomRegen() {
		return (int) memeEnergyRandomRegen.getValue();
	}

	public String getSkillsString() {
		long cost = memeMakerBaseSpeed.getLevel() * memeMakerBaseSpeed.getLevel() * 5;
		String s = "1.базовая скорость мемоделания\nуровень " + memeMakerBaseSpeed.getLevel() + " значение " + (int)memeMakerBaseSpeed.getValue() + " цена " + cost + "\n";
		cost = memeMakerFactorSpeed.getLevel() * memeMakerFactorSpeed.getLevel() * 5;
		s += "2.множитель скорости мемоделания\nуровень " + memeMakerFactorSpeed.getLevel() + " значение " + formatter.format(memeMakerFactorSpeed.getValue()) + " цена " + cost + "\n";
		cost = memeMakerRandomSpeed.getLevel() * memeMakerRandomSpeed.getLevel() * 5;
		s += "3.рандомное ускорение мемоделания\nуровень " + memeMakerRandomSpeed.getLevel() + " значение " + (int)memeMakerRandomSpeed.getValue() + " цена " + cost + "\n\n";
		
		cost = memeRewardBase.getLevel() * memeRewardBase.getLevel() * 10;
		s += "4.базовая награда за мем\nуровень " + memeRewardBase.getLevel() + " значение " + (int)memeRewardBase.getValue() + " цена " + cost + "\n";
		cost = memeRewardFactor.getLevel() * memeRewardFactor.getLevel() * 10;
		s += "5.множитель награды за мем\nуровень " + memeRewardFactor.getLevel() + " значение " + formatter.format(memeRewardFactor.getValue()) + " цена " + cost + "\n";
		cost = memeRewardRandom.getLevel() * memeRewardRandom.getLevel() * 10;
		s += "6.рандомная награда за мем\nуровень " + memeRewardRandom.getLevel() + " значение " + (int)memeRewardRandom.getValue() + " цена " + cost + "\n\n";
		
		cost = memeBonusRewardBase.getLevel() * memeBonusRewardBase.getLevel() * 5;
		s += "7.базовая бонусная награда за красивое число мемов\nуровень " + memeBonusRewardBase.getLevel() + " значение " + (int)memeBonusRewardBase.getValue() + " цена " + cost + "\n";
		cost = memeBonusRewardFactor.getLevel() * memeBonusRewardFactor.getLevel() * 5;
		s += "8.множитель бонусной награды за красивое число мемов\nуровень " + memeBonusRewardFactor.getLevel() + " значение " + formatter.format(memeBonusRewardFactor.getValue()) + " цена " + cost + "\n";
		cost = memeBonusRewardRandom.getLevel() * memeBonusRewardRandom.getLevel() * 5;
		s += "9.рандомная бонусная награда за красивое число мемов\nуровень " + memeBonusRewardRandom.getLevel() + " значение " + (int)memeBonusRewardRandom.getValue() + " цена " + cost + "\n\n";
		
		cost = memeEnergyMax.getLevel() * memeEnergyMax.getLevel() * 10;
		s += "10.максимальный запас сил на мемы\nуровень " + memeEnergyMax.getLevel() + " значение " + (int)memeEnergyMax.getValue() + " цена " + cost + "\n";
		cost = memeEnergyRegen.getLevel() * memeEnergyRegen.getLevel() * 10;
		s += "11.востановление сил\nуровень " + memeEnergyRegen.getLevel() + " значение " + (int)memeEnergyRegen.getValue() + " цена " + cost + "\n";
		cost = memeEnergyRandomRegen.getLevel() * memeEnergyRandomRegen.getLevel() * 10;
		s += "12.рандомное востановление сил\nуровень " + memeEnergyRandomRegen.getLevel() + " значение " + (int)memeEnergyRandomRegen.getValue() + " цена " + cost + "\n\n";
		
		s += "13.шанс сделать мем сразу\nуровень " + memeStealChance.getLevel() + " значение " + formatter.format(memeStealChance.getValue()) + " цена " + cost + "\n\n";
		
		return s;
	}

	public long tryUpgradeSkill(int num) {
		long balance = profile.getBalance();
		switch(num) {
		case 1:
			long cost = memeMakerBaseSpeed.getLevel() * memeMakerBaseSpeed.getLevel() * 5;
			if(balance >= cost) {
				profile.addBalance(-cost);
				memeMakerBaseSpeed.addValue(1);
				return 0;
			} else return cost;
		case 2:
			cost = memeMakerFactorSpeed.getLevel() * memeMakerFactorSpeed.getLevel() * 5;
			if(balance >= cost) {
				profile.addBalance(-cost);
				memeMakerFactorSpeed.addValue(0.1);
				return 0;
			} else return cost;
		case 3: 
			cost = memeMakerRandomSpeed.getLevel() * memeMakerRandomSpeed.getLevel() * 5;
			if(balance >= cost) {
				profile.addBalance(-cost);
				memeMakerRandomSpeed.addValue(1);
				return 0;
			} else return cost;
		case 4: 
			cost = memeRewardBase.getLevel() * memeRewardBase.getLevel() * 10;
			if(balance >= cost) {
				profile.addBalance(-cost);
				memeRewardBase.addValue(1);
				return 0;
			} else return cost;
		case 5: 
			cost = memeRewardFactor.getLevel() * memeRewardFactor.getLevel() * 10;
			if(balance >= cost) {
				profile.addBalance(-cost);
				memeRewardFactor.addValue(0.1);
				return 0;
			} else return cost;
		case 6: 
			cost = memeRewardRandom.getLevel() * memeRewardRandom.getLevel() * 10;
			if(balance >= cost) {
				profile.addBalance(-cost);
				memeRewardRandom.addValue(1);
				return 0;
			} else return cost;
		case 7: 
			cost = memeBonusRewardBase.getLevel() * memeBonusRewardBase.getLevel() * 5;
			if(balance >= cost) {
				profile.addBalance(-cost);
				memeBonusRewardBase.addValue(2);
				return 0;
			} else return cost;
		case 8: 
			cost = memeBonusRewardFactor.getLevel() * memeBonusRewardFactor.getLevel() * 5;
			if(balance >= cost) {
				profile.addBalance(-cost);
				memeBonusRewardFactor.addValue(0.2);
				return 0;
			} else return cost;
		case 9: 
			cost = memeBonusRewardRandom.getLevel() * memeBonusRewardRandom.getLevel() * 5;
			if(balance >= cost) {
				profile.addBalance(-cost);
				memeBonusRewardRandom.addValue(4);
				return 0;
			} else return cost;
		case 10: 
			cost = memeEnergyMax.getLevel() * memeEnergyMax.getLevel() * 10;
			if(balance >= cost) {
				profile.addBalance(-cost);
				memeEnergyMax.addValue(5);
				return 0;
			} else return cost;
		case 11: 
			cost = memeEnergyRegen.getLevel() * memeEnergyRegen.getLevel() * 10;
			if(balance >= cost) {
				profile.addBalance(-cost);
				memeEnergyRegen.addValue(1);
				return 0;
			} else return cost;
		case 12: 
			cost = memeEnergyRandomRegen.getLevel() * memeEnergyRandomRegen.getLevel() * 10;
			if(balance >= cost) {
				profile.addBalance(-cost);
				memeEnergyRandomRegen.addValue(1);
				return 0;
			} else return cost;
		case 13: 
			cost = memeStealChance.getLevel() * memeStealChance.getLevel() * 10;
			if(balance >= cost) {
				profile.addBalance(-cost);
				memeStealChance.addValue(0.001);
				return 0;
			} else return cost;
		}
		
		return -1;
	}

	public void save(File folder) throws IOException {
		File file = new File(folder, profile.getId() + "skills.txt");
		FileWriter writer = new FileWriter(file);
		writer.write("memeMakerBaseSpeed=" + (int)memeMakerBaseSpeed.getValue() + "=" + memeMakerBaseSpeed.getLevel() + "\n");
		writer.write("memeMakerFactorSpeed=" + memeMakerFactorSpeed.getValue() + "=" + memeMakerFactorSpeed.getLevel() + "\n");
		writer.write("memeMakerRandomSpeed=" + (int)memeMakerRandomSpeed.getValue() + "=" + memeMakerRandomSpeed.getLevel() + "\n");
		
		writer.write("memeRewardBase=" + (int)memeRewardBase.getValue() + "=" + memeRewardBase.getLevel() + "\n");
		writer.write("memeRewardFactor=" + memeRewardFactor.getValue() + "=" + memeRewardFactor.getLevel() + "\n");
		writer.write("memeRewardRandom=" + (int)memeRewardRandom.getValue() + "=" + memeRewardRandom.getLevel() + "\n");
		
		writer.write("memeEnergyMax=" + (int)memeEnergyMax.getValue() + "=" + memeEnergyMax.getLevel() + "\n");
		writer.write("memeEnergyRegen=" + (int)memeEnergyRegen.getValue() + "=" + memeEnergyRegen.getLevel() + "\n");
		
		writer.write("memeBonusRewardBase=" + (int)memeBonusRewardBase.getValue() + "=" + memeBonusRewardBase.getLevel() + "\n");
		writer.write("memeBonusRewardFactor=" + memeBonusRewardFactor.getValue() + "=" + memeBonusRewardFactor.getLevel() + "\n");
		writer.write("memeBonusRewardRandom=" + (int)memeBonusRewardRandom.getValue() + "=" + memeBonusRewardRandom.getLevel() + "\n");
		
		writer.write("memeStealChance=" + memeStealChance.getValue() + "=" + memeStealChance.getLevel() + "\n");
		writer.write("memeEnergyRandomRegen=" + (int)memeEnergyRandomRegen.getValue() + "=" + memeEnergyRandomRegen.getLevel() + "\n");
		
		writer.close();
	}

	public void load(File folder) throws IOException {
		File file = new File(folder, profile.getId() + "skills.txt");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String[] s = reader.readLine().split("=");
		this.memeMakerBaseSpeed = new Skill(4, Integer.parseInt(s[1]), Integer.parseInt(s[2]));
		s = reader.readLine().split("=");
		this.memeMakerFactorSpeed = new Skill(1, Double.parseDouble(s[1]), Integer.parseInt(s[2]));
		s = reader.readLine().split("=");
		this.memeMakerRandomSpeed = new Skill(6, Integer.parseInt(s[1]), Integer.parseInt(s[2]));
		
		s = reader.readLine().split("=");
		this.memeRewardBase = new Skill(1, Integer.parseInt(s[1]), Integer.parseInt(s[2]));
		s = reader.readLine().split("=");
		this.memeRewardFactor = new Skill(1, Double.parseDouble(s[1]), Integer.parseInt(s[2]));
		s = reader.readLine().split("=");
		this.memeRewardRandom = new Skill(2, Integer.parseInt(s[1]), Integer.parseInt(s[2]));
		
		s = reader.readLine().split("=");
		this.memeEnergyMax = new Skill(10, Integer.parseInt(s[1]), Integer.parseInt(s[2]));
		s = reader.readLine().split("=");
		this.memeEnergyRegen = new Skill(1, Integer.parseInt(s[1]), Integer.parseInt(s[2]));
		
		s = reader.readLine().split("=");
		this.memeBonusRewardBase = new Skill(10, Integer.parseInt(s[1]), Integer.parseInt(s[2]));
		s = reader.readLine().split("=");
		this.memeBonusRewardFactor = new Skill(1, Double.parseDouble(s[1]), Integer.parseInt(s[2]));
		s = reader.readLine().split("=");
		this.memeBonusRewardRandom = new Skill(5, Integer.parseInt(s[1]), Integer.parseInt(s[2]));
		
		s = reader.readLine().split("=");
		this.memeStealChance = new Skill(0.001, Double.parseDouble(s[1]), Integer.parseInt(s[2]));
		s = reader.readLine().split("=");
		this.memeEnergyRandomRegen = new Skill(2, Integer.parseInt(s[1]), Integer.parseInt(s[2]));
		reader.close();
	}
}
