package jp.co.ricoh.cotos;

import javax.persistence.EntityManager;

import org.springframework.context.ApplicationContext;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

/**
 *
 * EntityManagerのインスタンスを生成、返却する
 *
 */
public class EntityManagerProvider {

	private static EntityManager entityManager;

	private EntityManagerProvider() {
	}

	/**
	 * EntityManagerを取得
	 * @return EntityManager
	 */
	public static EntityManager getEntityManager() {
		if (entityManager == null) {
			entityManager = createEntityManager();
		}
		return entityManager;
	}

	/**
	 * EntityManagerを生成
	 * @return EntityManager
	 */
	private static EntityManager createEntityManager() {
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		LocalContainerEntityManagerFactoryBean factory = context.getBean(LocalContainerEntityManagerFactoryBean.class);
		EntityManager entityManager = factory.getNativeEntityManagerFactory().createEntityManager();

		return entityManager;
	}
}
