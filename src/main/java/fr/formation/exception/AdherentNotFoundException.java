package fr.formation.exception;

public class AdherentNotFoundException extends RuntimeException {
  public AdherentNotFoundException(String message) {
    super(message);
  }
}