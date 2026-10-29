package dev.nasenov.miscale2pg.exception;

public class ZeppLifeException extends RuntimeException {

  public ZeppLifeException(String message) {
    super(message);
  }

  public ZeppLifeException(String message, Throwable cause) {
    super(message, cause);
  }
}
