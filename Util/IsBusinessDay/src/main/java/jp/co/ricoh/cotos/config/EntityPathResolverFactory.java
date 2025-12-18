package jp.co.ricoh.cotos.config;

import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

public class EntityPathResolverFactory {
	public static EntityPathResolver getInstance() {
		return SimpleEntityPathResolver.INSTANCE;
	}
} 