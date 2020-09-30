package jp.co.ricoh.cotos;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import jp.co.ricoh.cotos.commonlib.security.CotosAuthenticationDetails;
import jp.co.ricoh.cotos.commonlib.util.BatchMomInfoProperties;
import jp.co.ricoh.cotos.config.LoadConfigulation;
import jp.co.ricoh.cotos.logic.JobComponent;
import jp.co.ricoh.cotos.security.CreateJwt;

public class BatchApplication {

	/**
	 * メイン処理
	 * @param args
	 */
	public static void main(String[] args) {

		BatchMomInfoProperties batchProperty = LoadConfigulation.getBatchMomInfoProperties();

		// 認証処理
		JobComponent jobComponent = new JobComponent();
		CreateJwt createJwt = new CreateJwt();
		String jwt = createJwt.execute();
		CotosAuthenticationDetails principal = new CotosAuthenticationDetails(batchProperty.getMomEmpId(), "sid", null, null, jwt, true, true, null);
		Authentication auth = new PreAuthenticatedAuthenticationToken(principal, null, null);
		SecurityContextHolder.getContext().setAuthentication(auth);

		// ジョブの実行
		jobComponent.run(args);
	}
}
