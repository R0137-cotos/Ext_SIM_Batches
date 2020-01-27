package jp.co.ricoh.cotos.batch.entity;

public interface ThrowableFunction<T, R>  {
	R apply(T t) throws Exception;
}
