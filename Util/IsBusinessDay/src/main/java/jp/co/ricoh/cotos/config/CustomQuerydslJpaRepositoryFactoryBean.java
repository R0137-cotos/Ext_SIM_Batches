package jp.co.ricoh.cotos.config;

import java.io.Serializable;

import jakarta.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class CustomQuerydslJpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
		extends JpaRepositoryFactoryBean<T, S, ID> {

	private EntityPathResolver entityPathResolver;

	public CustomQuerydslJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	public void setEntityPathResolver(EntityPathResolver entityPathResolver) {
		this.entityPathResolver = entityPathResolver;
	}

	@Override
	protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
		JpaRepositoryFactory factory = new JpaRepositoryFactory(entityManager);
		factory.setEntityPathResolver(this.entityPathResolver);
		return factory;
	}
} 