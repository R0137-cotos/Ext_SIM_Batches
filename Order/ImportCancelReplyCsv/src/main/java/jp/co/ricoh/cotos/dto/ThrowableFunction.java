package jp.co.ricoh.cotos.dto;

public interface ThrowableFunction<T, R>  {
	R apply(T t) throws Exception;
}
