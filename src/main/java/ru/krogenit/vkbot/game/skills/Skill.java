package ru.krogenit.vkbot.game.skills;

public class Skill {
	
	private double value, baseValue;
	private int level;
	
	public Skill(double baseValue, double value, int level) {
		this.value = value;
		this.baseValue = baseValue;
		this.level = level;
	}

	public double getValue() {
		return value;
	}

	public double getBaseValue() {
		return baseValue;
	}

	public int getLevel() {
		return level;
	}
	
	public void addValue(double value) {
		this.value += value;
		this.level++;
	}
	
	public void multiply(double value) {
		this.value *= value;
		this.level++;
	}
}
