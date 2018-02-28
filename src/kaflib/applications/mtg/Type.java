package kaflib.applications.mtg;

import java.util.HashSet;
import java.util.Set;

public enum Type {
	ARTIFACT("Artifact"),
	BASIC_LAND("Basic Land"),
	CREATURE("Creature"),
	ENCHANTMENT("Enchantment"),
	INSTANT("Instant"),
	LAND("Land"),
	PLANESWALKER("Planeswalker"),
	SORCERY("Sorcery");

	private final String text;
	
	public String getText() {
		return text;
	}
	
	private Type(final String type) {
		text = type;
	}
	
	public static Set<Type> getTypes(final String text) {
		Set<Type> types = new HashSet<Type>();
		
		for (Type type : Type.values()) {
			if (text.contains(type.getText())) {
				types.add(type);
			}
		}
		if (text.contains("Interrupt")) {
			types.add(Type.INSTANT);
		}
		return types;
	}
}
