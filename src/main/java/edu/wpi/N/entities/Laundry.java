package edu.wpi.N.entities;

public class Laundry extends Employee {
  public Laundry(int id, String name) {
    super(id, name);
  }

  @Override
  public String getServiceType() {
    return "Laundry";
  }
}
