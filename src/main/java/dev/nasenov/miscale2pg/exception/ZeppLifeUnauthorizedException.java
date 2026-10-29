package dev.nasenov.miscale2pg.exception;

public class ZeppLifeUnauthorizedException extends ZeppLifeException {

  public ZeppLifeUnauthorizedException() {
    super("Zepp Life rejected the app token, check the configured APP_ZEPP_LIFE_APP_TOKEN");
  }
}
