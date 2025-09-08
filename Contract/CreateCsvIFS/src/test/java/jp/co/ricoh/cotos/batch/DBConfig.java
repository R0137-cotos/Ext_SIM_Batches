package jp.co.ricoh.cotos.batch;

import java.util.Arrays;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.ricoh.cotos.commonlib.db.DBUtil;
import lombok.Data;

@Component
@Data
public class DBConfig {

	@Autowired
	EntityManager em;
	@Autowired
	DBUtil dbUtil;

	@Transactional
	public void clearData() {
		Arrays.asList(dbUtil.loadSQLFromClasspath("clearData.sql").split(";")).stream().forEach(sql -> em.createNativeQuery(sql).executeUpdate());
	}

	@Transactional
	public void initTargetTestData(String path) {
		Arrays.asList(dbUtil.loadSQLFromClasspath(path).split(";")).stream().forEach(sql -> em.createNativeQuery(sql).executeUpdate());
	}
	
	@Transactional
	public void initTargetTestClobData(String path) {
		Arrays.asList(dbUtil.loadSQLFromClasspath(path).split("/")).stream().forEach(sql -> em.createNativeQuery(sql).executeUpdate());
	}
}
