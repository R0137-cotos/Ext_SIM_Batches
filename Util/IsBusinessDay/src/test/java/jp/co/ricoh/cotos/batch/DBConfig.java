package jp.co.ricoh.cotos.batch;

import java.util.Arrays;

import jakarta.persistence.EntityManager;

import jp.co.ricoh.cotos.EntityManagerProvider;
import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import lombok.Data;

@Data
public class DBConfig {

	private EntityManager em;
	private DBUtil dbUtil;

	public DBConfig() {
		this.em = EntityManagerProvider.getEntityManager();
		this.dbUtil = new DBUtil(em);
	}

	public void clearData() {
		em.getTransaction().begin();
		try {
			Arrays.asList(dbUtil.loadSQLFromClasspath("clearData.sql").split(";")).stream().forEach(sql -> em.createNativeQuery(sql).executeUpdate());
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
		}
	}

	public void initTargetTestData(String path) {
		em.getTransaction().begin();
		try {
			Arrays.asList(dbUtil.loadSQLFromClasspath(path).split(";")).stream().forEach(sql -> em.createNativeQuery(sql).executeUpdate());
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
		}
	}

	public void initTargetTestClobData(String path) {
		em.getTransaction().begin();
		try {
			Arrays.asList(dbUtil.loadSQLFromClasspath(path).split("/")).stream().forEach(sql -> em.createNativeQuery(sql).executeUpdate());
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
		}
	}

	public void initTargetTestDataClear(String path) {
		em.getTransaction().begin();
		try {
			Arrays.asList(dbUtil.loadSQLFromClasspath(path).split(";")).stream().forEach(sql -> em.createNativeQuery(sql).executeUpdate());
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
		}
	}
}
