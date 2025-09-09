package jp.co.ricoh.cotos.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class SnakeCasePhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

	@Override
	public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
		if (name == null) return null;
		String newName = name.getText()
				.replaceAll("([a-z])([A-Z])", "$1_$2")
				.toLowerCase();
		return Identifier.toIdentifier(newName);
	}
} 